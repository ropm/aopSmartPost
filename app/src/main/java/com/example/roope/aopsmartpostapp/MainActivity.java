package com.example.roope.aopsmartpostapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    EditText txtEdit;
    Button btnSearch, btnLocate, btnShowOnMap;
    TextView txtResOne, txtSeekBar;
    SeekBar seekBar;
    String stringErrorZip, stringErrorGeneral, stringLocationPermissionTitle, stringLocationPermissionMessage, stringNotFound, yes, no;

    private FusedLocationProviderClient mFusedLocationClient;

    private double userLon;
    private double userLat;
    private double lon;
    private double lat;
    private List lonLatList;

    private int top;
    private JsonApi jsonApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSearch = findViewById(R.id.buttonSearch);
        btnLocate = findViewById(R.id.buttonLoc);
        btnShowOnMap = findViewById(R.id.buttonShowMap);
        txtResOne = findViewById(R.id.txtResOne);
        seekBar = findViewById(R.id.seekBarPosti);
        txtSeekBar = findViewById(R.id.txtSeekBar);
        stringErrorZip = getResources().getString(R.string.errorStringZip);
        stringErrorGeneral = getResources().getString(R.string.errorStringGeneral);
        stringLocationPermissionTitle = getResources().getString(R.string.locationPermissionString);
        stringLocationPermissionMessage = getResources().getString(R.string.locationPermissionMessageString);
        stringNotFound = getResources().getString(R.string.locationNotFoundString);
        yes = getResources().getString(R.string.yesString);
        no = getResources().getString(R.string.noString);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ohjelmat.posti.fi/pup/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //brings life to the methods without the body in the interface
        jsonApi = retrofit.create(JsonApi.class);



        getLocation();

        //TODO: tulosta arvo txtView, get value siitä?
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                top = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //nappi hakee syötteen perusteella
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                txtEdit = findViewById(R.id.editSearch);
                txtResOne.setText("");

                String searchTerm = txtEdit.getText().toString();
                String cleanedTerm = CheckInput(searchTerm);
                //virhekäsittely zip koodille
                if(cleanedTerm.equals("error")){
                    txtResOne.setText(stringErrorZip);
                    Toast.makeText(MainActivity.this, stringErrorZip, Toast.LENGTH_SHORT).show();
                }else{
                    getByZip(cleanedTerm);
                }
            }
        });
        //nappi hakee sijainnin perusteella
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtResOne.setText("");


                //TODO: get the lon lat from location of device
                getByXY(userLon, userLat, top);

            }
        });
        //nappi avaa google mapsin sijaintiin
        btnShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapByXY(lon, lat);

            }
        });
    }

    private void getLocation(){

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(stringLocationPermissionTitle)
                        .setMessage(stringLocationPermissionMessage)
                        .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton(no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted, access location
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                userLat = location.getLatitude();
                                userLon = location.getLongitude();
                            }else{
                                userLat = 62.501090;
                                userLon = 29.363530;
                                Toast.makeText(MainActivity.this, stringNotFound, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION){
            //if permission granted...
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{

            }
        }
    }

    private void getByXY(final double longitude, final double latitude, int limit){
        Call<List<Posti>> call = jsonApi.getPostisByXY(longitude, latitude, limit);

        call.enqueue(new Callback<List<Posti>>() {
            @Override
            public void onResponse(Call<List<Posti>> call, Response<List<Posti>> response) {
                //can be 404 also, will be null if not successful

                if(!response.isSuccessful()){
                    System.out.println("Error happened in the query: " + response.code());
                    txtResOne.setText(response.code());
                    return;
                }
                lonLatList.clear();
                List<Posti> postis = response.body();
                for (Posti posti : postis){
                    String result = "";
                    result += posti.getPublicName() + ", " + posti.getAddress() + ", " + posti.getZip() + "\n";

                    txtResOne.append(result);

                    lon = posti.getMapLong();
                    lat = posti.getMapLati();
                    Location loc = new Location("");
                    loc.setLatitude(lat);
                    loc.setLongitude(lon);
                    lonLatList.add(loc);
                }
            }

            @Override
            public void onFailure(Call<List<Posti>> call, Throwable t) {
                System.out.println("Error happened: " + t.getMessage());
                txtResOne.setText(t.getMessage());

            }
        });
    }


    private void getByZip(String query){
        Call<List<Posti>> call = jsonApi.getPostis(query);

        call.enqueue(new Callback<List<Posti>>() {
            @Override
            public void onResponse(Call<List<Posti>> call, Response<List<Posti>> response) {
                //can be 404 also, will be null if not successful

                if(!response.isSuccessful()){
                    System.out.println("Error happened in the query: " + response.code());
                    txtResOne.setText(response.code());
                    return;
                }

                lonLatList.clear();
                List<Posti> postis = response.body();
                for (Posti posti : postis){
                    lon = posti.getMapLong();
                    lat = posti.getMapLati();
                    Location loc = new Location("");
                    loc.setLatitude(lat);
                    loc.setLongitude(lon);
                    lonLatList.add(loc);

                }
                getByXY(lon, lat, top);
            }

            @Override
            public void onFailure(Call<List<Posti>> call, Throwable t) {
                System.out.println("Error happened: " + t.getMessage());
                txtResOne.setText(t.getMessage());

            }
        });
    }


    public String CheckInput(String input) {
        String parsedZip;
        if(input.matches("(\\d){5}")){
            parsedZip = input;
            return parsedZip;
        }else {
            parsedZip = "error";
            //TODO: error handling
            return parsedZip;
        }
    }

    public void openMapByXY(final double longitude, final double latitude) {
        //TODO: make arrows by smartposts
        try {
            Uri gmmIntentUri = Uri.parse("geo:" + longitude + "," + latitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        } catch (Exception e){
            System.out.println("Error showing map");
            Toast.makeText(MainActivity.this, stringErrorGeneral, Toast.LENGTH_SHORT).show();
        }
    }
}
