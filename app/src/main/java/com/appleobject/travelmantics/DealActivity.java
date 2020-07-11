package com.appleobject.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DealActivity extends AppCompatActivity {
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    EditText mTextTitle;
    EditText mTextPrice;
    EditText mTextDesc;
    TravelDeal deal;
    ListActivity caller;
    Button btnUpload;
    public static final int REQUEST_CODE = 42;

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
                 intent.setType("images/jpeg");
                 intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                 startActivityForResult(Intent.createChooser(intent, "Insert picture"), REQUEST_CODE);
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
    }

    private void initViews() {
        mTextTitle = findViewById(R.id.txtTitle);
        mTextPrice = findViewById(R.id.txtPrice);
        mTextDesc = findViewById(R.id.txtDescription);
        btnUpload = findViewById(R.id.btnImage);
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
    }

    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void enableEditText(Boolean isEnabled){
        mTextTitle.setEnabled(isEnabled);
        mTextPrice.setEnabled(isEnabled);
        mTextDesc.setEnabled(isEnabled);

    }
}