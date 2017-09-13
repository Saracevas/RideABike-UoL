package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.glados.rideabikeuol.app.Entities.IP;
import com.example.glados.rideabikeuol.app.R;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Register extends Activity {

    ImageView logo;
    Button register;
    EditText _firstname, _surname, _username, _password, _password2, _email;
    String firstname, surname, username, password, password2, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initializing variables and setting their layouts
        setContentView(R.layout.activity_register);
        logo = (ImageView)findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        _firstname = (EditText)findViewById(R.id.firstname);
        _surname = (EditText)findViewById(R.id.surname);
        _username = (EditText)findViewById(R.id.username);
        _password = (EditText)findViewById(R.id.password);
        _password2 = (EditText)findViewById(R.id.password2);
        _email = (EditText)findViewById(R.id.email);

        register = (Button)findViewById(R.id.register);

        register.setOnClickListener(buttonRegisterClick);
    }

    //when button is pressed, check is made to ensure that all fields are filled, afterwards
    //background thread is started to establish connection with server
    View.OnClickListener buttonRegisterClick = new View.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            firstname = _firstname.getText().toString();
            surname = _surname.getText().toString();
            username = _username.getText().toString();
            password = _password.getText().toString();
            password2 = _password2.getText().toString();
            email = _email.getText().toString();
            if (!(surname.equals("")) &&!(firstname.equals("")) && !(username.equals("")) &&
                    !(password.equals("")) && !(password2.equals("")) && !(email.equals(""))) {
                if (password.equals(password2)) {
                    Register.RegisterThread register = new Register.RegisterThread(firstname, surname, username, password, email);
                    register.execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Password do not match!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            }
        }};

    public class RegisterThread extends AsyncTask<Void, Void, Void> {

        String dstAddress = IP.ip;
        int dstPort = 4444;
        String fname = "";
        String lname = "";
        String uname = "";
        String pword = "";
        String email = "";
        Boolean reply = false;
        String line = "";

        RegisterThread(String fn, String sn, String un, String pw, String em){
            fname = fn;
            lname = sn;
            fname = fn;
            uname = un;
            pword = pw;
            email = em;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket;

            try {
                socket = new Socket(dstAddress, dstPort);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //creating JSON object to be sent to the server
                JSONObject obj = new JSONObject();
                obj.put("first_name", fname);
                obj.put("last_name", lname);
                obj.put("username", uname);
                obj.put("password", pword);
                obj.put("email", email);

                line = in.readLine();
                if (line.equals("Status 225")) {
                    out.println("REGISTER");
                }
                line = in.readLine();
                if(line.equals("Status 100")) {
                    out.println(obj.toJSONString());
                }

                line = in.readLine();
                if (line.equals("Status 204")){
                   out.println("CLOSE");
                   reply = true;
                }else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Username already taken!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            } catch (UnknownHostException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Could not connect to the server!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Could not connect to the server!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (reply) {
                Intent switchToProfile = new Intent(Register.this, Login.class);
                Register.this.startActivity(switchToProfile);
                finish();
            }
            return null;
        }
    }

}
