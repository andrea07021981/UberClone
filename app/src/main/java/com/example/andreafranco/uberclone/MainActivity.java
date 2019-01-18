package com.example.andreafranco.uberclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IntDef;
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

import com.google.android.gms.drive.query.internal.LogicalFilter;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoginButton;
    private TextView mSignUpTextView;
    private EditText mUsernameEditText, mPasswordEditText;

    @Retention(RetentionPolicy.SOURCE)
    // Enumerate valid values for this interface
    @IntDef({DRIVER, RIDER})
    // Create an interface for validating int types
    public @interface UserType {}
    // Declare the constants
    public static final int DRIVER = 0;
    public static final int RIDER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
        mSignUpTextView = findViewById(R.id.signup_textview);
        mSignUpTextView.setOnClickListener(this);
        mUsernameEditText = findViewById(R.id.username_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);

        if (ParseUser.getCurrentUser() != null) {
            //Go to the other activity
            int userType = (Boolean) ParseUser.getCurrentUser().get("driver")? DRIVER : RIDER;
            moveToMap(userType);
        }
    }

    @Override
    public void onClick(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
                if (TextUtils.isEmpty(mUsernameEditText.getText()) ||
                        TextUtils.isEmpty(mPasswordEditText.getText())) {
                    Toast.makeText(this, "Username and password required", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setPositiveButton("Rider", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    doSignUp(RIDER);
                                }
                            })
                            .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .setNegativeButton("Driver", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    doSignUp(DRIVER);
                                }
                            })
                            .setTitle("User type")
                            .setMessage("Select which type of user");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                break;

            default:
                break;
        }
    }

    private void doLogin() {
        ParseUser.logInInBackground(
                mUsernameEditText.getText().toString(),
                mPasswordEditText.getText().toString(),
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            //Login Ok
                            moveToMap(user.getInt("driver"));
                        } else {
                            Toast.makeText(MainActivity.this, "Login error:", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void doSignUp(@UserType final int user) {
        ParseUser currentUser = new ParseUser();
        currentUser.setUsername(mUsernameEditText.getText().toString());
        currentUser.setPassword(mPasswordEditText.getText().toString());
        currentUser.put("driver", user == DRIVER);
        currentUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //Sign up completed
                    moveToMap(user);
                }
            }
        });
    }

    private void moveToMap(@UserType int userType) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("driver", userType);
        startActivity(intent);
    }
}
