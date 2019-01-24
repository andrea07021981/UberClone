package com.example.andreafranco.uberclone;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.andreafranco.uberclone.models.LoggedUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGES = 1;
    private static final int REQUEST_PERMISSION_CODE = 0;

    private ImageView mPictureImageView;
    private EditText mNameEditText, mSurnameEditText, mEmailEditText, mPasswordEditText, mConfirmPasswordEditText;
    private Spinner mUserTypeSpinner;
    private Uri mProfileUri;
    @SuppressWarnings("deprecation")
    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPictureImageView = findViewById(R.id.picture_imageview);
        mNameEditText = findViewById(R.id.name_edittext);
        mSurnameEditText = findViewById(R.id.surname_edittext);
        mEmailEditText = findViewById(R.id.email_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);
        mConfirmPasswordEditText = findViewById(R.id.confirm_password_editetext);
        mUserTypeSpinner = findViewById(R.id.usertype_spinner);
        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(LayoutInflater.from(this).inflate(R.layout.progress_dialog, null));
        mAlertDialog = builder.create();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    public void signUpClick(View view) {
        if (checkMandatoryField()) {
            mAlertDialog.show();
            mAuth.createUserWithEmailAndPassword(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.getResult() != null && task.isSuccessful()) {
                                //User created, we need to create a new record user
                                final FirebaseUser user = task.getResult().getUser();
                                final int userType = ((ArrayAdapter) mUserTypeSpinner.getAdapter()).getPosition(mUserTypeSpinner.getSelectedItem());

                                LoggedUser loggedUser = new LoggedUser(
                                        mNameEditText.getText().toString(),
                                        mSurnameEditText.getText().toString(),
                                        userType,
                                        mProfileUri);

                                //Save record on user table
                                mUsersDatabaseReference
                                        .child(user.getUid())
                                        .setValue(loggedUser)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(mNameEditText.getText().toString())
                                                            .setPhotoUri(mProfileUri)
                                                            .build();
                                                    user.updateProfile(changeRequest)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        //Go back
                                                                        Intent intent = new Intent();
                                                                        intent.putExtra("userType", userType);
                                                                        setResult(RESULT_OK, intent);
                                                                        finish();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                });
                            }
                        }
                    });
        }
    }

    private boolean checkMandatoryField() {
        boolean result = true;
        if (TextUtils.isEmpty(mNameEditText.getText())) {
            mNameEditText.setError("Name is mandatory!");
            result = false;
        }

        if (TextUtils.isEmpty(mSurnameEditText.getText())) {
            mSurnameEditText.setError("Surname is mandatory!");
            result = false;
        }

        if (TextUtils.isEmpty(mEmailEditText.getText())) {
            mEmailEditText.setError("Email is mandatory!");
            result = false;
        } else if (!isEmail(mEmailEditText)){
            mEmailEditText.setError("Email is wrong formatted!");
            result = false;
        }

        if (TextUtils.isEmpty(mPasswordEditText.getText())) {
            mPasswordEditText.setError("Password is mandatory!");
            result = false;
        }

        if (TextUtils.isEmpty(mConfirmPasswordEditText.getText())) {
            mConfirmPasswordEditText.setError("Confirm password!");
            result = false;
        } else if (!mConfirmPasswordEditText.getText().toString().equals(mPasswordEditText.getText().toString())) {
            mConfirmPasswordEditText.setError("Password is different!");
            result = false;
        }

        if (mUserTypeSpinner.getSelectedItemId() == 0L) {
            result = false;
            Toast.makeText(this, "Select a type", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void getPhoto() {
        Intent getPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(getPhotoIntent, REQUEST_IMAGES);
    }

    public void profileImageClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getPhoto();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoto();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGES && resultCode == RESULT_OK) {
            mProfileUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mProfileUri);
                mPictureImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
