package com.example.andreafranco.uberclone.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andreafranco.uberclone.R;
import com.example.andreafranco.uberclone.models.LoggedUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int INTENT_CODE_SIGNUP = 1;
    private static final int RC_SIGN_IN = 2;

    private Button mLoginButton;
    private TextView mSignUpTextView;
    private EditText mUsernameEditText, mPasswordEditText;
    private AlertDialog mAlertDialog;

    // Declare the constants
    public static final int NONE = 0;
    public static final int RIDER = 1;
    public static final int DRIVER = 2;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mDataBase;
    private DatabaseReference mUsersDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
        mSignUpTextView = findViewById(R.id.signup_textview);
        mSignUpTextView.setOnClickListener(this);
        mUsernameEditText = findViewById(R.id.username_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(LayoutInflater.from(this).inflate(R.layout.progress_dialog, null));
        mAlertDialog = builder.create();

        //Database
        mDataBase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mDataBase.getReference().child("users");

        //Authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                //I use the attach listener either the user is already logged or when the user click on login
                //User is already logged in
                if (currentUser != null) {
                    mAlertDialog.show();
                    onSignedInInitialize(currentUser);
                } else {
                    //No logged user
                    onSignedOutCleanUp();
                    //Simple example of using FirebaseUI if we want to use google, facebook etc
                    //In this case we have a custom sign up UI
                    /*List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setLogo(R.drawable.uber)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);*/
                }

            }
        };
    }

    private void onSignedOutCleanUp() {
        detachDatabaseReadListener();
    }

    private void onSignedInInitialize(@NonNull FirebaseUser currentUser) {
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    LoggedUser user = dataSnapshot.getValue(LoggedUser.class);
                    if (dataSnapshot.getKey().equals(mFirebaseAuth.getUid())) {
                        moveToMap(user);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mUsersDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
        }
        detachDatabaseReadListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        if (inputMethodManager.isActive()) inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        int id = view.getId();
        switch (id) {

            case R.id.login_button:
                if (TextUtils.isEmpty(mUsernameEditText.getText()) ||
                    TextUtils.isEmpty(mPasswordEditText.getText())) {
                    Toast.makeText(this, "Username and password required", Toast.LENGTH_SHORT).show();
                } else {
                    doLogin();
                }
                break;

            case R.id.signup_textview:
                doSignUp();
                break;

            default:
                break;
        }
    }

    private void doLogin() {
        mAlertDialog.show();
        mFirebaseAuth.signInWithEmailAndPassword(mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Login Ok
                            attachDatabaseReadListener();
                            mUsersDatabaseReference
                                    .child(task.getResult().getUser().getUid());
                        } else {
                            Toast.makeText(MainActivity.this, "Login error:", Toast.LENGTH_SHORT).show();
                            if (mAlertDialog.isShowing()) {
                                mAlertDialog.dismiss();
                            }
                        }
                    }
                });
    }

    private void doSignUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivityForResult(intent, INTENT_CODE_SIGNUP);
        } else {
            // TODO Swap without transition
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == INTENT_CODE_SIGNUP && resultCode == RESULT_OK && data != null) {
            moveToMap((LoggedUser) data.getParcelableExtra("user"));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void moveToMap(LoggedUser user) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }
}
