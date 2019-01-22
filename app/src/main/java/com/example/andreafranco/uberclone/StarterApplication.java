package com.example.andreafranco.uberclone;

import android.app.Application;

public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /*// Enable Local Datastore.
        Parse.enableLocalDatastore(this);


        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("4c8c5bf55a94c6d5223423e1c108c61902735656")
                .clientKey("88fcf011cff161788d6624a2ab878dedb22e2a30")
                .server("http://18.218.127.124:80/parse/") //USER AND PSW ARE user - 4O6seviSzdSE
                .build()
        );


        *//**
         * THIS IS USED FOR APPS THAT DON'T REQUIRE A LOGIN PAGE. WITH IT EVERY APP CAN LOGIN AUTOMATICALLY
         *//*
        //ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);*/

    }
}
