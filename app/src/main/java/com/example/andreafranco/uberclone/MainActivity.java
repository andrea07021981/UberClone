package com.example.andreafranco.uberclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int INTENT_CODE_SIGNUP = 1;
    private Button mLoginButton;
    private TextView mSignUpTextView;
    private EditText mUsernameEditText, mPasswordEditText;

    @Retention(RetentionPolicy.SOURCE)
    // Enumerate valid values for this interface
    @IntDef({DRIVER, RIDER})
    // Create an interface for validating int types
    public @interface UserType {}
    // Declare the constants
    public static final int NONE = 0;
    public static final int RIDER = 1;
    public static final int DRIVER = 2;

    FirebaseAuth mAuth;
    FirebaseDatabase mDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ParseAnalytics.trackAppOpenedInBackground(getIntent());

        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
        mSignUpTextView = findViewById(R.id.signup_textview);
        mSignUpTextView.setOnClickListener(this);
        mUsernameEditText = findViewById(R.id.username_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);

        mDataBase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            //Retrieve user type and move to the other activity
            //TODO Move the type saved to the map activity amd save a user instance after the login in order to avoid multiple queries
            mDataBase.getReference("users")
                    .child(mAuth.getCurrentUser().getUid())
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (dataSnapshot.exists() && dataSnapshot.getKey().equals("usertype")) {
                                int usertype = Math.toIntExact((Long) dataSnapshot.getValue());
                                moveToMap(usertype);
                                int i = 1;
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
                    });
            /*int userType = (Boolean) mAuth.getCurrentUser().g.get("driver")? DRIVER : RIDER;
            moveToMap(userType);*/
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
        mAuth.signInWithEmailAndPassword(mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Login Ok
                            //moveToMap(user.getInt("driver"));
                        } else {
                            Toast.makeText(MainActivity.this, "Login error:", Toast.LENGTH_SHORT).show();
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
            // Swap without transition
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == INTENT_CODE_SIGNUP && resultCode == RESULT_OK && data != null) {
            moveToMap(data.getIntExtra("userType", NONE));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void moveToMap(@UserType int userType) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("driver", userType);
        startActivity(intent);
    }
}
