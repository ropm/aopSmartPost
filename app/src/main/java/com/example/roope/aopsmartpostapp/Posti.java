package com.example.roope.aopsmartpostapp;

import com.google.gson.annotations.SerializedName;

/**
 * Luokka SmartPost kyselyn tuloksille. Constructoria ei tarvitse, koska Retrofit hoitaa homman
 */
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

    /**
     * Hakee SmartPostin osoitteen
     * @return address - String
     */
    public String getAddress() {
        return address;
    }

    /**
     * Hakee SmartPostin postinumeron
     * @return zip - String
     */
    public String getZip() {
        return zip;
    }

    /**
     * Hakee SmartPostin julkisen nimen
     * @return publicName - String
     */
    public String getPublicName() {
        return publicName;
    }

    /**
     * Hakee SmartPostin pituusasteen
     * @return mapLong - double
     */
    public double getMapLong() {
        return mapLong;
    }

    /**
     * Hakee SmartPostin leveysasteen
     * @return mapLati - double
     */
    public double getMapLati() {
        return mapLati;
    }
}
