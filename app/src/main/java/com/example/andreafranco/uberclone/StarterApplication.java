package com.example.andreafranco.uberclone;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);


        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("b3486976d294dd43f7485f3b8dce2871476a8584")
                .clientKey("95d1aafc41371496e94e5a12b5ba11d13c7d0ee5")
                .server("http://3.16.186.142:80/parse/") //USER AND PSW ARE user - 0EWYk2udkzNn
                .build()
        );


        /**
         * THIS IS USED FOR APPS THAT DON'T REQUIRE A LOGIN PAGE. WITH IT EVERY APP CAN LOGIN AUTOMATICALLY
         */
        //ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
