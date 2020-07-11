package com.appleobject.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

    }


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.getFbReference("traveldeals", this);
        RecyclerView recyclerView = findViewById(R.id.rc_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        final DealAdapter dealAdapter = new DealAdapter();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(dealAdapter);
        FirebaseUtil.attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.list_activity_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.insert_menu);
        if (FirebaseUtil.isAdmin == true){
            menuItem.setVisible(true);
        }
        else{
            menuItem.setVisible(false);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.insert_menu:
               Intent intent = new Intent(this, DealActivity.class);
               startActivity(intent);
               return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                Log.d("Logout : ", "User logged-out...");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showMenu(){
        invalidateOptionsMenu();
    }
}