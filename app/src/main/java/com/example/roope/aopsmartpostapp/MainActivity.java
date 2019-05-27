package com.example.roope.aopsmartpostapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    EditText txtEdit;
    Button btnSearch, btnLocate;
    TextView txtResOne, txtSeekBar;
    SeekBar seekBar;
    String stringErrorZip;
    String stringErrorGeneral;

    private double lon;
    private double lat;

    private int top;
    private JsonApi jsonApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSearch = findViewById(R.id.buttonSearch);
        txtResOne = findViewById(R.id.txtResOne);
        seekBar = findViewById(R.id.seekBarPosti);
        txtSeekBar = findViewById(R.id.txtSeekBar);
        stringErrorZip = getResources().getString(R.string.errorStringZip);
        stringErrorGeneral = getResources().getString(R.string.errorStringGeneral);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ohjelmat.posti.fi/pup/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //brings life to the methods without the body in the interface
        jsonApi = retrofit.create(JsonApi.class);


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
                }else{
                    getByZip(cleanedTerm);
                }
            }
        });

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtResOne.setText("");
                //TODO:
                double userLon = 0.0;
                double userLat = 0.0;
                getByXY(userLon, userLat, top);
            }
        });
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

                List<Posti> postis = response.body();
                for (Posti posti : postis){
                    String result = "";
                    result += posti.getPublicName() + ", " + posti.getAddress() + ", " + posti.getZip() + "\n";

                    txtResOne.append(result);
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
                for (Posti posti : postis){
                    lon = posti.getMapLong();
                    lat = posti.getMapLati();
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

    public void ShowMap() {
        //TODO: show map by location
    }
}
