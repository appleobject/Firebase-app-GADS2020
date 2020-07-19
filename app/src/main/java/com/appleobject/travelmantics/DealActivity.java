package com.appleobject.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class DealActivity extends AppCompatActivity {
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    EditText mTextTitle;
    EditText mTextPrice;
    EditText mTextDesc;
    TravelDeal deal;
    ListActivity caller;
    Button btnUpload;
    UploadTask uploadTask;
    public static final int PICTURE_RESULT = 42; // the answer to everything
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        FirebaseUtil.getFbReference("traveldeals", caller);
         mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
         mDatabaseReference = FirebaseUtil.mDatabaseReference;

         initViews();
         intentFromAdapter();
         btnUpload.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                 intent.setType("image/jpeg");
                 intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                 startActivityForResult(Intent.createChooser(intent, "Insert picture"), PICTURE_RESULT);
             }
         });
    }

    private void intentFromAdapter() {
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        mTextTitle.setText(deal.getTitle());
        mTextDesc.setText(deal.getDescription());
        mTextPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
    }

    private void initViews() {
        mTextTitle = findViewById(R.id.txtTitle);
        mTextPrice = findViewById(R.id.txtPrice);
        mTextDesc = findViewById(R.id.txtDescription);
        btnUpload = findViewById(R.id.btnImage);
        mImageView = findViewById(R.id.images);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu,menu);
        if (FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditText(true);
        }else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditText(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_SHORT).show();
                clearFields();
                backToList();
                return true;
            case R.id.delete_menu:
                removeDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void clearFields() {
        mTextTitle.setText("");
        mTextPrice.setText("");
        mTextDesc.setText("");

        mTextTitle.requestFocus();
    }

    private void saveDeal() {
        deal.setTitle(mTextTitle.getText().toString());
        deal.setPrice(mTextPrice.getText().toString());
        deal.setDescription(mTextDesc.getText().toString());
        if (deal.getId() == null){
            mDatabaseReference.push().setValue(deal);
        }else{
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }

    private void removeDeal(){
        if (deal.getId() == null){
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_LONG).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if (deal.getImageName() != null && !deal.getImageName().isEmpty() == false){
            StorageReference pRef = FirebaseUtil.mStorageRef.child(deal.getImageName());
            pRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete", "Deleted Successfully...");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Error", e.getMessage()); 

                }
            });
        }
    }

    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void enableEditText(Boolean isEnabled){
        mTextTitle.setEnabled(isEnabled);
        mTextPrice.setEnabled(isEnabled);
        mTextDesc.setEnabled(isEnabled);
        btnUpload.setEnabled(isEnabled);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUrl = data.getData();
            final StorageReference storageReference = FirebaseUtil.mStorageRef.child(imageUrl.getLastPathSegment());
             uploadTask =  storageReference.putFile(imageUrl);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();
                        deal.setImageUrl(url);
                        Log.d("Url: ", url);
                        showImage(url);
                    } else {
                        // Handle failures
                        // ... Something went wrong
                    }
                }
            });
        }



    }

    private void showImage(String url){
        if (url != null && !url.isEmpty()){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(mImageView);


        }
    }

}