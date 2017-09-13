package com.example.glados.rideabikeuol.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glados.rideabikeuol.app.Entities.IP;
import com.example.glados.rideabikeuol.app.R;

import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Navigation extends Activity {

    ImageView logo;
    TextView route;
    AutoCompleteTextView start, destination;
    ListView routeListView;
    Button button, reviewButton;
    String _start, _destination;
    String output = "", mLine = "";
    String routeStr;
    HashMap<String, String> names = new HashMap<String, String>();
    String[] nameArray;
    ArrayList<String> routeList = new ArrayList<String>();
    String path;
    @Override
    //starts when activity is started
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initializing views and setting layouts
        setContentView(R.layout.activity_navigation);
        logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);
        //getting the names to show in suggestions
        getNameList();
        populateArray();

        //creating the adapter for suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, nameArray);

        start = (AutoCompleteTextView) findViewById(R.id.target);
        start.setAdapter(adapter);

        destination = (AutoCompleteTextView) findViewById(R.id.destination);
        destination.setAdapter(adapter);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(navigationListener);
        reviewButton = (Button) findViewById(R.id.reviewButton);
        reviewButton.setVisibility(View.INVISIBLE);
        reviewButton.setOnClickListener(reviewListener);


        routeListView = (ListView) findViewById(R.id.routeListView);

    }
    //starting the route thread on click, also checks if both fields are filled
    View.OnClickListener navigationListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            _start = start.getText().toString();
            _destination = destination.getText().toString();

            if (!(_start.equals("")) && !(_destination.equals(""))) {
                NavigationThread navThread = new NavigationThread(names.get(_start), names.get(_destination));
                navThread.execute();
            }else {
                Toast.makeText(getApplicationContext(), "Missing start or destination!", Toast.LENGTH_SHORT).show();
                System.out.println(routeList.toString());
            }
        }
    };

    View.OnClickListener reviewListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            Intent switchToReview = new Intent(Navigation.this, Review.class);
            switchToReview.putExtra("start", names.get(_start));
            switchToReview.putExtra("destination", names.get(_destination));
            Navigation.this.startActivity(switchToReview);
        }
    };

    public class NavigationThread extends AsyncTask<Void, Void, Void> {

        String dstAddress = IP.ip;
        int dstPort = 4444;
        String line = "";
        ProgressDialog pDialog;
        String threadStart, threadDestination;
        Boolean reply;

        public NavigationThread(String t, String d) {
            threadStart = t;
            threadDestination = d;
        }

        @Override
        //shows please wait dialogue
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Navigation.this);
            pDialog.setMessage("Calculating Route\nPlease wait...");
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
                obj.put("start", threadStart);
                obj.put("destination", threadDestination);

                line = in.readLine();
                if (line.equals("Status 225")) {
                    out.println("ROUTEPLAN");
                }

                line = in.readLine();
                if(line.equals("Status 100")) {
                    out.println(obj.toJSONString());
                }

                line = in.readLine();
                if (line.equals("Status 204")){
                    out.println("CLOSE");
                    reply = true;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Could not find route!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    out.println("CLOSE");
                    reply = true;
                }
                routeStr = line;

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
                        Toast.makeText(getApplicationContext(), "Route found!", Toast.LENGTH_SHORT).show();
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

            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(routeStr);

                String pS = jsonObject.get("path").toString();
                pS = pS.substring(1, pS.length()-1);
                String[] s = pS.split(", ");

                String temp = "";
                int count = 1;
                for (String ss : s) {
                    if(!ss.equals(temp)) {
                        routeList.add("" + count + ".  "+ ss);
                        temp = ss;
                        count++;
                    }
                }
                StableArrayAdapter adapter = new StableArrayAdapter(Navigation.this,
                        android.R.layout.simple_list_item_1, routeList);
                routeListView.setAdapter(adapter);
                reviewButton.setVisibility(View.VISIBLE);
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    //reading the names from names.txt file
    public String readNames() {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("names.txt"), "UTF-8"));

            // do reading, usually loop until end of file reading
            while ((mLine = reader.readLine()) != null) {
                output = mLine;
            }
        } catch (IOException e) {}
        return output;
    }
       //getting through the jsonstring to get individual items
    public void getNameList() {
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory(){
            public List creatArrayContainer() {
                return new LinkedList();
            }

            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

        };

        try {

            Map json = (Map) parser.parse(readNames(), containerFactory);
            Iterator iter = json.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                names.put((String)entry.getKey(), (String)entry.getValue());
            }
        }catch(org.json.simple.parser.ParseException pe){
            System.out.println(pe);
        }
    }
    //fill the nameArray with names to use for suggestions
    public void populateArray() {
        nameArray = new String[names.size()];
        int count = 0;

        try {
            Iterator iter = names.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                names.put((String)entry.getKey(), (String)entry.getValue());
                nameArray[count] = (String)entry.getKey();
                count++;
            }
        }catch (Exception e) {}
    }
}
//StableArrayAdapter courtesy of http://www.vogella.com/tutorials/AndroidListView/article.html
class StableArrayAdapter extends ArrayAdapter<String> {

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public StableArrayAdapter(Context context, int textViewResourceId,
                              List<String> objects) {
        super(context, textViewResourceId, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
        String item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}

