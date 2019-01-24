package com.example.andreafranco.uberclone.models;

import android.net.Uri;

public class LoggedUser {

    private String name;
    private String surname;
    private int userType;
    private Uri profileUri;

    private LoggedUser(){

    }

    public LoggedUser(String name, String surname, int userType, Uri profileUri) {
        this.name = name;
        this.surname = surname;
        this.userType = userType;
        this.profileUri = profileUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public Uri getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(Uri profileUri) {
        this.profileUri = profileUri;
    }
}
