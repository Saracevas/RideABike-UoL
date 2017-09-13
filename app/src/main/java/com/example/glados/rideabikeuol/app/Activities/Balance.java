package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glados.rideabikeuol.app.Entities.IP;
import com.example.glados.rideabikeuol.app.Entities.User;
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

public class Balance extends Activity {

    ImageView logo;
    TextView balance, topUp;
    SeekBar topUpSelection;
    Button topUpButton;
    private int topUpAmount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        topUpButton = (Button) findViewById(R.id.topUpButton);

        balance = (TextView) findViewById(R.id.balance);
        balance.setText("£"+User.getBalance());

        topUpSelection = (SeekBar) findViewById(R.id.topUpSeekBar);
        topUp = (TextView) findViewById(R.id.topUp);
        topUp.setText("£" + topUpSelection.getProgress());

        topUpSelection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                topUpAmount = progress;
                topUp.setText("£" + topUpAmount);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        topUpButton = (Button) findViewById(R.id.topUpButton);
        topUpButton.setOnClickListener(topUpListener);
    }

    View.OnClickListener topUpListener = new View.OnClickListener(){
        public void onClick(View arg0) {
            if (topUpAmount > 0) {
                BalanceThread balance = new BalanceThread(topUpAmount);
                balance.execute();
            }else {
                Toast.makeText(getApplicationContext(), "No username or password entered!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public class BalanceThread extends AsyncTask<Void, Void, Void> {

        String dstAddress = IP.ip;
        int dstPort = 4444;
        String line = "";
        double credit;
        Boolean reply;

        BalanceThread(double c){
            credit = c;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket;

            try {
                socket = new Socket(dstAddress, dstPort);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //creating json object that is sent to the server
                JSONObject obj = new JSONObject();
                obj.put("id", User.getID());
                obj.put("increaseBy", credit);

                line = in.readLine();
                if (line.equals("Status 225")) {
                    out.println("CREDITINCREASE");
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
                            Toast.makeText(getApplicationContext(), "Top-Up was unsuccessful!", Toast.LENGTH_SHORT).show();
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
            //if done successfully, toast message is printed and user balance is increased
            //to display to the user in the application immediately
            if (reply) {
                User.increaseBalance(credit);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Top-Up SUCCESSFUL!", Toast.LENGTH_SHORT).show();
                        balance.setText("£"+User.getBalance());
                    }
                });
            }
            return null;
        }
    }

}
