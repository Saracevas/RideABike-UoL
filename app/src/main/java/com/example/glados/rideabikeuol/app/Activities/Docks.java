package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Docks extends Activity {

    private static final String TAG_ID = "id";
    private static final String TAG_STATUS = "status";

    ImageView logo;
    ListView listView;
    private int stationID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //creating and initializing views
        Bundle extras = getIntent().getExtras();
        String idString = (String)extras.get("id");
        stationID = Integer.parseInt(idString) + 1;
        setContentView(R.layout.activity_docks);
        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        listView = (ListView) findViewById(R.id.list);
        //executing the thread so that the information can be fetched
        DocksThread docksThread = new DocksThread();
        docksThread.execute();
    }

    public class DocksThread extends AsyncTask<Void, Void, Void> {

        String dstAddress = IP.ip;
        int dstPort = 4444;
        String line = "";
        ProgressDialog pDialog;
        List<HashMap<String, String>> docksList = null;

        DocksThread(){
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Docks.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket;

            try {
                //establishing connection to the server and starting readers and writers
                socket = new Socket(dstAddress, dstPort);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //creating json object to be sent to the server
                JSONObject sendID = new JSONObject();
                sendID.put("id", stationID);

                line = in.readLine();
                if (line.equals("Status 225")) {
                    out.println("DOCKSBYSTATION");
                }

                line = in.readLine();
                if(line.equals("Status 100")) {
                    out.println(sendID.toJSONString());
                }
                line = in.readLine();
                out.println("CLOSE");

                //Creating JSONObject using the string received from the server
                try {
                    Object jsonObj = JSONValue.parse(line);
                    //Getting JSON Array node
                    JSONArray array = (JSONArray)jsonObj;
                    docksList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0; i < array.size(); i++) {
                        JSONObject obj = (JSONObject)array.get(i);

                        String status = (String)obj.get("status");
                        String id = (String)obj.get("id");

                        //tmp hashmap for single dock
                        HashMap<String, String> dock = new HashMap<String, String>();

                        dock.put(TAG_ID, id);
                        dock.put(TAG_STATUS, status);

                        //adding a dock to contact list
                        docksList.add(dock);
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
            String[] from = new String[] {TAG_ID, TAG_STATUS};
            int[] to = new int[] {R.id.dockID, R.id.status};

            SimpleAdapter adapter = new SimpleAdapter(Docks.this, docksList, R.layout.docks_item, from, to);
            listView.setAdapter(adapter);
        }
    }
}
