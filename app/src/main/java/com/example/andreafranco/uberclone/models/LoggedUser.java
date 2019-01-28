package com.example.andreafranco.uberclone.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LoggedUser implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    // Enumerate valid values for this interface
    @IntDef({DRIVER, RIDER})
    // Create an interface for validating int types
    public @interface UserType {}

    // Declare the constants
    public static final int NONE = 0;
    public static final int RIDER = 1;
    public static final int DRIVER = 2;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LoggedUser createFromParcel(Parcel in) {
            return new LoggedUser(in);
        }

        public LoggedUser[] newArray(int size) {
            return new LoggedUser[size];
        }
    };

    private String uuid;
    private String name;
    private String surname;
    private int userType;
    private String profileUri;

    private LoggedUser(){

    }

    public LoggedUser(String uuid, String name, String surname, @UserType int userType, String profileUri) {
        this.uuid = uuid;
        this.name = name;
        this.surname = surname;
        this.userType = userType;
        this.profileUri = profileUri;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public @UserType int getUserType() {
        return userType;
    }

    public void setUserType(@UserType int userType) {
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
        this.uuid = in.readString();
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
        dest.writeString(this.uuid);
        dest.writeString(this.name);
        dest.writeString(this.surname);
        dest.writeInt(this.userType);
        dest.writeString(this.profileUri);
    }
}
