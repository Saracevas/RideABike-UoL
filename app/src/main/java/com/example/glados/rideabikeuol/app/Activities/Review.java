package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glados.rideabikeuol.app.Entities.IP;
import com.example.glados.rideabikeuol.app.Entities.User;
import com.example.glados.rideabikeuol.app.R;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Review extends Activity {

    String start, destination, _reviewText, ratingStr;
    ImageView logo;
    TextView title;
    EditText reviewText;
    Button submitReview;
    RatingBar ratingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        start = extras.getString("start");
        destination = extras.getString("destination");
        setContentView(R.layout.activity_review);

        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);

        title = (TextView) findViewById(R.id.reviewTitle);
        reviewText = (EditText) findViewById(R.id.review);

        submitReview = (Button) findViewById(R.id.button2);
        submitReview.setOnClickListener(reviewSubmit);
        addListenerOnRatingBar();
    }

    public void addListenerOnRatingBar() {

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);


        //if rating value is changed,
        //display the current rating value in the result (textview) automatically
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                ratingStr = String.valueOf(rating);
            }
        });
    }

    View.OnClickListener reviewSubmit = new View.OnClickListener() {
        public void onClick(View arg0) {
            _reviewText = reviewText.getText().toString();
            if (!(_reviewText.equals(""))) {
                ReviewThread reviewThread = new ReviewThread(_reviewText, start, destination, ratingStr);
                reviewThread.execute();
            }else {
                Toast.makeText(getApplicationContext(), "Please write your review first!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public class ReviewThread extends AsyncTask<Void, Void, Void> {

        String dstAddress = IP.ip;
        int dstPort = 4444;
        String line = "";
        ProgressDialog pDialog;
        String start = "";
        String destination = "";
        String reviewText, id, rating;
        Boolean reply;

        public ReviewThread(String t, String s, String d, String r) {
            reviewText = t;
            start = s;
            destination = d;
            rating = r;
        }

        @Override
        //shows please wait dialogue
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Review.this);
            pDialog.setMessage("Submitting review\nPlease wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        //established connection, and sends the data back and forth
        protected Void doInBackground(Void... arg0) {
            Socket socket;

            try {
                socket = new Socket(dstAddress, dstPort);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                JSONObject obj = new JSONObject();
                //obj.put("review", reviewText);
                obj.put("start", start);
                obj.put("destination", destination);

                line = in.readLine();
                if (line.equals("Status 225")) {
                    out.println("ROUTEGET");
                }


                line = in.readLine();
                if(line.equals("Status 100")) {
                    out.println(obj.toJSONString());
                }

                line = in.readLine();
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject)parser.parse(line);
                    System.out.println(jsonObject.toJSONString());
                    id = (String)jsonObject.get("id").toString();
                }catch (Exception e) {
                    e.printStackTrace(); }

                out.println("REVIEWADD");

                JSONObject sendReviewObj = new JSONObject();
                sendReviewObj.put("userid", User.getID());
                sendReviewObj.put("id", id);
                sendReviewObj.put("comment", reviewText);
                sendReviewObj.put("rating", rating);
                out.println(sendReviewObj.toJSONString());

                line = in.readLine();

                if (line.equals("Status 204")){
                    out.println("CLOSE");
                    reply = true;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Could not submit the review!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    out.println("CLOSE");
                    reply = true;
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show();
//                        balance.setText("£"+User.getBalance());
                    }
                });            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }
}
