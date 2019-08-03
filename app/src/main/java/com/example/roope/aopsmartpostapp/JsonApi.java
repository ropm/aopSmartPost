package com.example.roope.aopsmartpostapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit kirjaston vaatima interface. Ei tarvitse antaa metodille sisältöä interfacen sisällä, pelkkä alustaminen riittää.
 * Retrofit luo metodin sisällön automaattisesti, kun kysely tehdään.
 * <code>GET(pickuppoints)</code> - hakee postit postinumeron perusteella
 * <code>GET(pickuppoints?type=smartpost)</code> - hakee postit karttatietojen perusteella,
 * params: longitude(double), latitude(double), top(int)
 */
public interface JsonApi {

    //get list of postis, json. no need to give method a body in interface, only declare, retrofit autogenerates method body.
    @GET("pickuppoints")
    Call<List<Posti>> getPostis(@Query("zipcode") String zipcode);

    @GET("pickuppoints?type=smartpost")
    Call<List<Posti>> getPostisByXY(@Query("longitude") double longitude, @Query("latitude") double latitude, @Query("top") int top);
}
