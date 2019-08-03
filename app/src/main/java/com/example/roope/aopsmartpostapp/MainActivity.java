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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    EditText txtEdit;
    Button btnSearch, btnLocate, btnShowOnMap;
    TextView txtResOne, txtSeekBar;
    SeekBar seekBar;
    String stringErrorZip, stringErrorGeneral, stringLocationPermissionTitle, stringLocationPermissionMessage, stringNotFound, yes, no;
    String stringLocationPermissionOK, stringLocationPermissionNO, stringNoPostis;

    private FusedLocationProviderClient mFusedLocationClient;

    private double userLon;
    private double userLat;
    private double lon;
    private double lat;
    private List<Location> lonLatList = new ArrayList<>();

    private int top;
    private JsonApi jsonApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtEdit = findViewById(R.id.editSearch);
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
        stringLocationPermissionOK = getResources().getString(R.string.locationPermissionOKString);
        stringLocationPermissionNO = getResources().getString(R.string.locationPermissionNOString);
        stringNotFound = getResources().getString(R.string.locationNotFoundString);
        yes = getResources().getString(R.string.yesString);
        no = getResources().getString(R.string.noString);
        stringNoPostis = getResources().getString(R.string.errorNoPostis);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ohjelmat.posti.fi/pup/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //brings life to the methods without the body in the interface
        jsonApi = retrofit.create(JsonApi.class);

        getLocation();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                top = progress;
                txtSeekBar.setText(String.valueOf(progress));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.emptyMenu) {
            if (lonLatList != null && lonLatList.size() > 0) {
                lonLatList.clear();
            }
            seekBar.setProgress(0);
            txtSeekBar.setText("");
            txtResOne.setText("");
            txtEdit.setText("");
        }else{
            this.finishAndRemoveTask();
        }
        return true;
    }

    private void getLocation(){

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, stringLocationPermissionNO, Toast.LENGTH_LONG).show();
            // Permission is not granted
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
                                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

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
                                userLat = 0;
                                userLon = 0;
                                Toast.makeText(MainActivity.this, stringNotFound, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            //if permission granted...
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, stringLocationPermissionOK, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, stringLocationPermissionNO, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getByXY(final double longitude, final double latitude, int limit){
        Call<List<Posti>> call = jsonApi.getPostisByXY(longitude, latitude, limit);

        call.enqueue(new Callback<List<Posti>>() {
            @Override
            public void onResponse(Call<List<Posti>> call, Response<List<Posti>> response) {
                //can be 404 also, will be null if not successful
                if (!response.isSuccessful()) {
                    System.out.println("Error happened in the query: " + response.code());
                    txtResOne.setText(response.code());
                    return;
                }

                if (lonLatList != null && lonLatList.size() > 0) {
                    lonLatList.clear();
                }

                List<Posti> postis = response.body();
                if (postis == null) {
                    txtResOne.setText(stringErrorGeneral);
                }else{
                    for (Posti posti : postis) {
                        String result = "";
                        result += posti.getPublicName() + ", " + posti.getAddress() + ", " + posti.getZip() + "\n";
                        txtResOne.append("- " + result);
                        lon = posti.getMapLong();
                        lat = posti.getMapLati();
                        Location loc = new Location("");
                        loc.setLatitude(lat);
                        loc.setLongitude(lon);
                        lonLatList.add(loc);
                    }
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

                List<Posti> postis = response.body();
                if (postis == null) {
                    txtResOne.setText(stringErrorGeneral);
                }else {
                    for (Posti posti : postis) {
                        lon = posti.getMapLong();
                        lat = posti.getMapLati();
                    }
                    getByXY(lon, lat, top);
                }
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
            return parsedZip;
        }
    }

    public void openMapByXY(final double longitude, final double latitude) {
        try {
            StringBuilder sb = new StringBuilder();
            //String mapQuery = "geo:0,0?z=12&q=";
            String mapQuery = "https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude + "&waypoints=";
            sb.append(mapQuery);
            if (lonLatList.size() < 1){
                Toast.makeText(MainActivity.this, stringNoPostis, Toast.LENGTH_LONG).show();

            }else {
                for (Location loc : lonLatList) {
                    String query = loc.getLatitude() + "," + loc.getLongitude();
                    sb.append(query);
                    sb.append("|");
                }
                sb.deleteCharAt(sb.length() - 1);
                String fullQuery = sb.toString();
                String uri = fullQuery + "&travelmode=driving";
                Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(mapIntent);
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        } catch (Exception e){
            System.out.println("Error showing map");
            Toast.makeText(MainActivity.this, stringErrorGeneral, Toast.LENGTH_SHORT).show();
        }
    }
}
