package com.example.andreafranco.uberclone.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class LoggedUser implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LoggedUser createFromParcel(Parcel in) {
            return new LoggedUser(in);
        }

        public LoggedUser[] newArray(int size) {
            return new LoggedUser[size];
        }
    };

    private String name;
    private String surname;
    private int userType;
    private String profileUri;

    private LoggedUser(){

    }

    public LoggedUser(String name, String surname, int userType, String profileUri) {
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

    public String getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    // Parcelling part
    public LoggedUser(Parcel in){
        this.name = in.readString();
        this.surname = in.readString();
        this.userType = in.readInt();
        this.profileUri = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.surname);
        dest.writeInt(this.userType);
        dest.writeString(this.profileUri);
    }
}
