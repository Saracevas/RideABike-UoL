package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.glados.rideabikeuol.app.R;
import com.example.glados.rideabikeuol.app.ServiceHandlers.JSONParser;


public class Home extends Activity {

    private ImageView logo;
    private ImageButton balance, profile, nav, stations;
    private TextView welcomeMessage, weather;
    private String finalUrl = "http://api.openweathermap.org/data/2.5/weather?q=Leicester&units=metric";
    private JSONParser obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getting the user from login activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //displaying weather
        weather = (TextView) findViewById(R.id.temp);
        setWeather(weather);
        //logo and welcome message
        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);

        welcomeMessage = (TextView) findViewById(R.id.welcome);

        //all the buttons and their listeners

        balance = (ImageButton) findViewById(R.id.balance);
        profile = (ImageButton) findViewById(R.id.profile);
        stations = (ImageButton) findViewById(R.id.station);
        nav = (ImageButton) findViewById(R.id.nav);

        balance.setOnClickListener(balanceListener);
        profile.setOnClickListener(profileListener);
        stations.setOnClickListener(stationsListener);
        nav.setOnClickListener(navListener);
    }
    //"log off" feature that prevent the user from stopping the activity by accident
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Home.super.onBackPressed();
                    }
                }).create().show();
    }
    // buttons listeners that start a new activty
    View.OnClickListener balanceListener = new View.OnClickListener(){
        public void onClick(View arg0) {
            Intent switchToRegister = new Intent(Home.this, Balance.class);
            Home.this.startActivity(switchToRegister);
        }
    };

    View.OnClickListener profileListener = new View.OnClickListener(){
        public void onClick(View arg0) {
            Intent switchToProfile = new Intent(Home.this, Profile.class);
            Home.this.startActivity(switchToProfile);
        }
    };

    View.OnClickListener stationsListener = new View.OnClickListener(){
        public void onClick(View arg0) {
            Intent switchToStations = new Intent(Home.this, Stations.class);
            Home.this.startActivity(switchToStations);
        }
    };

    View.OnClickListener navListener = new View.OnClickListener(){
        public void onClick(View arg0) {
            Intent switchToNav = new Intent(Home.this, Navigation.class);
            Home.this.startActivity(switchToNav);
        }
    };
    //setting the weather
    public void setWeather(View view){
        obj = new JSONParser(finalUrl);
        obj.fetchJSON();

        while(obj.parsingComplete);
        weather.setText("Temperature: "+obj.getTemperature()+"° | Humidity: "+obj.getHumidity()+"% | Pressure: "+obj.getPressure());
    }

}