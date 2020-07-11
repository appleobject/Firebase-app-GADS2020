package com.appleobject.travelmantics;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    public static ArrayList<TravelDeal> mDeals;
    @SuppressLint("StaticFieldLeak")
    private static FirebaseUtil mFirebaseUtil;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;
    public static FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123;
    @SuppressLint("StaticFieldLeak")
    private static ListActivity caller;
    public static Boolean isAdmin;

    private FirebaseUtil(){}

    public static void getFbReference(String ref, final ListActivity callerActivity){
        if (mFirebaseUtil == null){
            mFirebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null){
                        FirebaseUtil.signIn();
                    }
                    else{
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);
                    }
                    Toast.makeText(callerActivity.getBaseContext(), "Welcome Back...", Toast.LENGTH_LONG).show();
                }
            };
        }

        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    public static void attachListener(){
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    public static void detachListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    public static void signIn(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    private static void checkAdmin(String uid){
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrator")
                .child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               FirebaseUtil.isAdmin = true;
               caller.showMenu();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addChildEventListener(listener);
    }

    public static void connectStorage(){
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference().child("deals_projects");
    }


}
