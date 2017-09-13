package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import android.view.View;
import android.widget.EditText;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glados.rideabikeuol.app.Entities.IP;
import com.example.glados.rideabikeuol.app.R;
import com.example.glados.rideabikeuol.app.Entities.User;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Login extends Activity {

    JSONParser jsonParser;
    ImageView logo;
    EditText loginText, passwordText;
    Button buttonConnect;
    TextView registerButton;
    private String _username;
    private String _password;
    private Boolean successful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //defining and displaying the views
        logo = (ImageView)findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        loginText = (EditText)findViewById(R.id.login);
        passwordText = (EditText)findViewById(R.id.password);
        buttonConnect = (Button)findViewById(R.id.connect);
        registerButton = (TextView)findViewById(R.id.regbutton);
        //setting button listeners
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        registerButton.setOnClickListener(regButton);
        jsonParser = new JSONParser();
    }
    //listener for register button
    OnClickListener regButton = new OnClickListener(){
        public void onClick(View arg0) {
            Intent switchToRegister = new Intent(Login.this, Register.class);
            Login.this.startActivity(switchToRegister);
        }
    };
    //listener for login button, when the button is pressed, checks if both
    //username and passwords aren't empty, if not starts a new login thread which
    //checks if the user exists
    OnClickListener buttonConnectOnClickListener = new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    _username = loginText.getText().toString();
                    _password = passwordText.getText().toString();
                    if (!(_username.equals("")) && !(_password.equals(""))) {
                        LoginThread login = new LoginThread(_username, _password);
                        login.execute();
                    }else {
                        Toast.makeText(getApplicationContext(), "No username or password entered!", Toast.LENGTH_SHORT).show();
                    }
                }};


public class LoginThread extends AsyncTask<Void, Void, Void> {

    String dstAddress = IP.ip;
    int dstPort = 4444;
    String uname = "";
    String pword = "";
    String line = "";
    JSONObject obj;
    //default constructor for this thread
    LoginThread(String u, String p){
        uname = u;
        pword = p;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket;

        try {
            //creating sockets and input and out streams for socket communication
            socket = new Socket(dstAddress, dstPort);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //creating a json object
            obj = new JSONObject();
            obj.put("username", uname);
            obj.put("password", pword);
            //after connection, if the server communication has been successfully established
            //Status 225 is return meaning the server is ready to take commands
            //if it does return 225, we send the LOGIN command
            line = in.readLine();
            if (line.equals("Status 225")) {
                out.println("LOGIN");
            }
            //if the command executed successsfuly, we get status 100 meaning we can send the login credentials
            line = in.readLine();
            if(line.equals("Status 100")) {
                //sending the JSON object containing password and username
                out.println(obj.toJSONString());
            }
            //reading a reply after sending the JSON object, if credentials match, server returns 204
            //we can confirm that information was successful
            line = in.readLine();
            if (line.equals("Status 401")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Wrong Details!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                try {
                    obj = (JSONObject)jsonParser.parse(line);
                    successful = true;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            out.println("CLOSE");


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
        //while connection is still open, we get all the information about the user
        //and create a new user object, we attach the object to an intent of switching an activity
        //we then start a new activity and pass the defined intent
        if (successful) {
            int idus = Integer.parseInt((String)obj.get("id"));
            User newUser = new User(idus, (String)obj.get("first_name"), (String)obj.get("last_name"), (String)obj.get("username"), (String)obj.get("email"), (double)obj.get("credit"));
            Intent switchToProfile = new Intent(Login.this, Home.class);
            Login.this.startActivity(switchToProfile);
            successful = false;
        }
        return null;
    }
    }
}