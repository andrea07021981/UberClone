package com.example.andreafranco.uberclone.models;

import android.location.Location;

public class Request {

    private boolean requestActive;
    private String driverUuid;
    private String riderUuid;
    private double latitude;
    private double longitute;

    private Request(){

    }

    public Request(boolean requestActive, String driverUuid, String riderUuid, double latitude, double longitute) {
        this.driverUuid = driverUuid;
        this.requestActive = requestActive;
        this.riderUuid = riderUuid;
        this.latitude = latitude;
        this.longitute = longitute;
    }

    public String getDriverUuid() {
        return driverUuid;
    }

    public void setDriverUuid(String driverUuid) {
        this.driverUuid = driverUuid;
    }

    public boolean getRequestActive() {
        return requestActive;
    }

    public void setRequestActive(boolean requestActive) {
        this.requestActive = requestActive;
    }

    public String getRiderUuid() {
        return riderUuid;
    }

    public void setRiderUuid(String riderUuid) {
        this.riderUuid = riderUuid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitute() {
        return longitute;
    }

    public void setLongitute(double longitute) {
        this.longitute = longitute;
    }
}
