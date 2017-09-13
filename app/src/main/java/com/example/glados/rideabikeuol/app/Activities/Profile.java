package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.glados.rideabikeuol.app.Entities.User;
import com.example.glados.rideabikeuol.app.R;

public class Profile extends Activity {

    ImageView logo;
    TextView firstname, lastname, gender, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        firstname = (TextView) findViewById(R.id.firstname);
        firstname.setText(User.getName());

        lastname = (TextView) findViewById(R.id.surname);
        lastname.setText(User.getSurname());

        email = (TextView) findViewById(R.id.email);
        email.setText(User.getEmail());
    }
}
