package com.example.roope.aopsmartpostapp;

import com.google.gson.annotations.SerializedName;

public class Posti {
    @SerializedName("Address")
    private String address;
    @SerializedName("PostCode")
    private String zip;
    @SerializedName("PublicName")
    private String publicName;
    @SerializedName("MapLongitude")
    private double mapLong;
    @SerializedName("MapLatitude")
    private double mapLati;

    public String getAddress() {
        return address;
    }

    public String getZip() {
        return zip;
    }

    public String getPublicName() {
        return publicName;
    }

    public double getMapLong() {
        return mapLong;
    }

    public double getMapLati() {
        return mapLati;
    }
}
