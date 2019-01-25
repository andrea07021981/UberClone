package com.example.andreafranco.uberclone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;

import com.example.andreafranco.uberclone.fragments.MapFragment;
import com.example.andreafranco.uberclone.models.LoggedUser;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends FragmentActivity implements MapFragment.OnFragmentInteractionListener {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();
        if (findViewById(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            //Check if rider or driver
            //TODO create differents fragments for user type
            Parcelable parcellableUser = getIntent().getParcelableExtra("user");
            if (parcellableUser != null && parcellableUser instanceof LoggedUser) {
                LoggedUser user = (LoggedUser) parcellableUser;
                MapFragment fragment = MapFragment.newInstance(user);
                fragment.setArguments(getIntent().getExtras());

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).commit();
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

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
        /*ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //Log out done
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }
}
