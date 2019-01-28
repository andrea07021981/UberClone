package com.example.andreafranco.uberclone.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.andreafranco.uberclone.fragments.MapFragment;
import com.example.andreafranco.uberclone.fragments.SingleFragmentActivity;
import com.example.andreafranco.uberclone.models.LoggedUser;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends SingleFragmentActivity implements MapFragment.OnFragmentInteractionListener {

    private static final int REQUEST_DESTINATION = 1;
    FirebaseAuth mAuth;

    @Override
    protected Fragment createFragment() {
        mAuth = FirebaseAuth.getInstance();
        //Check if rider or driver
        //TODO create differents fragments for user type
        Parcelable parcellableUser = getIntent().getParcelableExtra("user");
        if (parcellableUser instanceof LoggedUser) {
            LoggedUser user = (LoggedUser) parcellableUser;
            MapFragment fragment = MapFragment.newInstance(user);
            return fragment;
        }
        return null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onStartNewActivity(Intent intent) {
        startActivityForResult(intent, REQUEST_DESTINATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_DESTINATION && resultCode == RESULT_OK) {
            FragmentManager fn = getSupportFragmentManager();
            Fragment mapFragment = fn.findFragmentByTag(MapFragment.class.getSimpleName());
            if (mapFragment != null) {
                ((MapFragment) mapFragment).addRequestOnMap(data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Info message")
                .setMessage("Would you like to log out?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logOut();
                    }
                }).show();
    }

    private void logOut() {
        mAuth.signOut();
        finish();
    }
}
