package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glados.rideabikeuol.app.Entities.IP;
import com.example.glados.rideabikeuol.app.R;
import com.example.glados.rideabikeuol.app.ServiceHandlers.GPSTracker;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Stations extends Activity {

    private static final String TAG_NAME = "name";
    private static final String TAG_MILES = "miles";

    private GPSTracker gps;

    ImageView logo;
    TextView gpsText;
    String test;
    ListView listView;

    private double lat, lon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);
        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        listView = (ListView) findViewById(R.id.list);

        gps = new GPSTracker(Stations.this);

        if (gps.canGetLocation()) {
            lat = gps.getLatitude();
            lon = gps.getLongitude();
        }
        Location locationA = new Location("current LOC");
        locationA.setLatitude(lat);
        locationA.setLongitude(lon);

        StationsThread stationsthread = new StationsThread(locationA);
        stationsthread.execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent switchToDocks = new Intent(Stations.this, Docks.class);
                switchToDocks.putExtra("id", ""+position+"");
                Stations.this.startActivity(switchToDocks);
            }
        });
    }

    public class StationsThread extends AsyncTask<Void, Void, Void> {

        String dstAddress = IP.ip;
        int dstPort = 4444;
        String line = "";
        Location currentLocation;
        ProgressDialog pDialog;
        List<HashMap<String, String>> stationsList = null;


        StationsThread(Location loc){
            currentLocation = loc;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Stations.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket;

            try {
                socket = new Socket(dstAddress, dstPort);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                line = in.readLine();
                if (line.equals("Status 225")) {
                    out.println("ALLSTATIONS");
                }

                line = in.readLine();
                if(line.equals("Status 100")) {
                    out.println("GET");
                }
                line = in.readLine();
                out.println("CLOSE");

                //Creating JSONObject using the string received from the server
                try {
                    Object jsonObj = JSONValue.parse(line);
                    //Getting JSON Array node
                    JSONArray array = (JSONArray)jsonObj;
                    stationsList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0; i < array.size(); i++) {
                        JSONObject obj = (JSONObject)array.get(i);

                        String name = (String)obj.get(TAG_NAME);
                        double lat = (double)obj.get("latitude");
                        double lon = (double)obj.get("longitude");

                        Location locationB = new Location("point B");
                        locationB.setLatitude(lat);
                        locationB.setLongitude(lon);

                        double distanceInKm = currentLocation.distanceTo(locationB) / 1000;
                        double distance = distanceInKm * 0.62137;

                        //tmp hashmap for single contact
                        HashMap<String, String> station = new HashMap<String, String>();

                        station.put(TAG_NAME, name);
                        station.put(TAG_MILES, Double.toString(round(distance))+" miles");

                        //adding contact to contact list
                        stationsList.add(station);
                    }
                }catch (Exception e) {}




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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            String[] from = new String[] {TAG_NAME, TAG_MILES};
            int[] to = new int[] { R.id.name, R.id.miles };

            SimpleAdapter adapter = new SimpleAdapter(Stations.this, stationsList, R.layout.stations_item, from, to);
            listView.setAdapter(adapter);
        }

    }

    public static double round(double d) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}