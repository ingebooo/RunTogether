package com.example.ingebode.googlemapsproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ingebode.R;
import com.example.ingebode.googlemapsproject.models.Finish;
import com.example.ingebode.googlemapsproject.models.History;
import com.example.ingebode.googlemapsproject.models.Route;
import com.example.ingebode.googlemapsproject.models.UserRouteRelation;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by ingeborgoftedal on 19/07/16.
 */
public class StartWearActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    GoogleApiClient googleClient;
    ArrayList<DataMap> pointData = new ArrayList<DataMap>();
    Button send_btn;
    Button start_wear;
    Button fillArray;

    CSVFile csvFile;
    File file;

    int timesBehind = 0;
    int timesInfront = 0;
    int counter = 0;
    Handler handler = new Handler();
    Timer timer;

    MarkerOptions markerOptions3;
    double current_lat, current_long;

    MarkerOptions markerOptions4;
    Marker marker3;
    Marker marker4;

    float topSpeed = 0;
    float avg_speed = 0;
    float old_avg = 0;
    float forget = 0;

    boolean isFinish = false;

    private LocationRequest locationRequest;
    private Location mLastLocation;
    boolean running = false;

    int point_number= 0;

    private List<Route.Point> newPoints = new ArrayList<>();

    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;
    Date now;

    //Firebase
    Firebase routeRef;
    Firebase newRouteRef;
    Firebase newPointCollection;
    Firebase pointRef;
    Firebase pointsRef = new Firebase(Config.POINTS_URL);
    Map<String, Object> runnedByMap = new HashMap<String, Object>();



    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    String WEARABLE_DATA_PATH = "/wearable_data";
    String MOBILE_DATA_PATH = "/mobile_data";

    String user_id, competitor_id, route_id;
    String route_name, competitor_username, username;
    boolean createNewRoute = false;
    int feedback;
    private String point_collection_id, new_point_collection_id;
    double lat1, long1, lat2, long2;

    private static GoogleMap mMap;
    Button stop_btn;


    private ArrayList<Route.Point> listPoints;

    TextView warningTextView;
    private TimerTask timerTask;
    private int timesBeside;

    TextView speed;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listPoints = new ArrayList<Route.Point>();
        setContentView(R.layout.activity_maps);
        warningTextView = (TextView)findViewById(R.id.warning);

        myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");

        send_btn = (Button)findViewById(R.id.send_route_btn);
        send_btn.setTypeface(myFontLight);

        speed = (TextView)findViewById(R.id.speed_textview);
        speed.setText("");

        stop_btn = (Button)findViewById(R.id.stop_btn);
        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Stopping", Toast.LENGTH_SHORT)
                        .show();
                running = false;
                timer.cancel();
                writeToHistory();
                Intent intent2 = new Intent(getApplicationContext(), Finish.class);
                intent2.putExtra("LAT1", lat1);
                intent2.putExtra("LONG1", long1);
                intent2.putExtra("LAT2", lat2);
                intent2.putExtra("LONG2", long2);
                intent2.putExtra("USER_ID", user_id);
                intent2.putExtra("COMPETITOR_ID", competitor_id);
                intent2.putExtra("ROUTE_ID", route_id);
                intent2.putExtra("CREATENEWROUTE", createNewRoute);
                intent2.putExtra("ROUTE_NAME", route_name);
                intent2.putExtra("FEEDBACK", feedback);
                intent2.putExtra("USERNAME", username);
                startActivity(intent2);

            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Start btn pushed", Toast.LENGTH_SHORT)
                        .show();
                running = true;
                warningTextView.setText("Now running...");

                now = new Date();

                newPointCollection = pointsRef.push();
                new_point_collection_id = newPointCollection.getKey();

                Toast.makeText(getApplicationContext(),
                        "Gooooo...", Toast.LENGTH_SHORT)
                        .show();

                if(timer == null){
                    startTimer();
                    Toast.makeText(getApplicationContext(),
                            "Timer started!!", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Timer not started...", Toast.LENGTH_SHORT)
                            .show();
                }
                stop_btn.setEnabled(true);
                stop_btn.setAlpha(1);
                send_btn.setEnabled(false);
                send_btn.setAlpha(.5f);

            }
        });

        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getDataFromIntent();
        final int point_number = 0;



        csvFile = new CSVFile((getApplicationContext()));

        pointsRef = new Firebase(Config.POINTS_URL).child(point_collection_id);
        pointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Route.Point point = snap.getValue(Route.Point.class);
                    DataMap map = new DataMap();

                    map.putString("user_id", point.getUser_id());
                    map.putString("route_id", point.getRoute_id());
                    map.putDouble("latitude", point.getLatitude());
                    map.putDouble("longitude", point.getLongitude());
                    map.putInt("point_number", point.getPoint_number());
                    map.putInt("counter", (int) snapshot.getChildrenCount());
                    map.putString("competitor_name", competitor_username);
                    map.putInt("feedback", feedback);
                    map.putString("username", username);
                    //Log.v("time", new Date().getTime() + "");
                    //ADD RANDOM ELLER TIMESTAMP!

                    //public void writePoints(String filename,String user_id,String route_id,String latitude,String longitude, String point_number){


                    file = csvFile.writePoints("points_number " + point_number + ".txt", ": user_id" + point.getUser_id(), " ,route_id: " + point.getRoute_id(), ", latitude: " + point.getLatitude(), ", longitude: " + point.getLongitude(), ", counter: " + snapshot.getChildrenCount());

                    pointData.add(map);
                    listPoints.add(point);


                }

                /*
                FileInputStream fileInputStream = null;

                byte[] bFile = new byte[(int) file.length()];
                try {
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(bFile);
                    fileInputStream.close();
                } catch (Exception e) {
                }
                Asset asset = Asset.createFromBytes(bFile);*/

                /*
                Log.v("pointdata.size", pointData.size()  + "");
                Asset asset = Asset.createFromBytes(file.toURI());*/

                new SendToDataLayerThread(WEARABLE_DATA_PATH, pointData).start();
                new SendReadyToRunTask().execute();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    public void sendTextFile(ArrayList list){

    }
    public void getDataFromIntent() {
        Intent intent = this.getIntent();
        user_id = intent.getStringExtra("USER_ID");
        lat1=intent.getDoubleExtra("LAT1", 0);
        long1=intent.getDoubleExtra("LONG1", 0);
        lat2=intent.getDoubleExtra("LAT2", 0);
        long2=intent.getDoubleExtra("LONG2", 0);
        competitor_id = intent.getStringExtra("COMPETITOR_ID");
        route_id = intent.getStringExtra("ROUTE_ID");
        createNewRoute = intent.getExtras().getBoolean("CREATENEWROUTE");
        route_name = intent.getStringExtra("ROUTE_NAME");
        feedback = intent.getExtras().getInt("FEEDBACK");
        point_collection_id = intent.getStringExtra("POINT_COLLECTION_ID");
        username = intent.getStringExtra("USERNAME");
        competitor_username = intent.getStringExtra("COMPETITOR_USERNAME");

    }


    private void sendReadyToRunMessage(String node) {
        Wearable.MessageApi.sendMessage(
                googleClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.v("fail", "faild to send msg" + "");
                        }
                    }
                }
        );
    }

    // Send a data object when the data layer connection is successful.    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    private void createUserRouteRelation() {

        Firebase reffer = new Firebase(Config.FIREBASE_URL);
        Firebase newReffer = reffer.child("routeRelations");

        Firebase newRefferRouteID = newReffer.child(route_id);

        UserRouteRelation relation = new UserRouteRelation(route_id, user_id, username, new_point_collection_id, "", timesBehind, timesInfront, timesBeside);

        newRefferRouteID.push().setValue(relation);

        Toast.makeText(getApplicationContext(),
                "User route relation created...", Toast.LENGTH_SHORT)
                .show();
    }

    class SendToDataLayerThread extends Thread {
        String path;
        ArrayList<DataMap> dataMapArrayList;
        SendToDataLayerThread(String p, ArrayList<DataMap> dataList) {
            path = p;
            dataMapArrayList = dataList;
            Log.v("SendWearLayer", "from mobile" + dataList.size());
        }

        public void run() {
            PutDataMapRequest dataMap = PutDataMapRequest
                    .create(path);

            dataMap.getDataMap().putDataMapArrayList("pointsList", dataMapArrayList);
            dataMap.getDataMap().putLong("time", new Date().getTime());
            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();

            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v("myTag", "DataMap: " + dataMapArrayList.size() + " sent successfully to data layer ");
            } else {
                // Log an error
                Log.v("myTag", "ERROR: failed to send DataMapArrayList to data layer");
            }
        }
    }

    private class SendReadyToRunTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendReadyToRunMessage(node);
            }
            return null;
        }
    }
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }
    protected void onStart() {
        Log.v("onStart", "Onstart");
        googleClient.connect();
        super.onStart();
        //checkProximity();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        } else {

        }
        if(LocationServices.FusedLocationApi.getLastLocation(googleClient) != null){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleClient);
            Log.v("mLastLocation", mLastLocation.toString());
            addMarkers();
            //startTimer();
            mMap.setMyLocationEnabled(true);
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(googleClient, locationRequest, this)
                .setResultCallback(new ResultCallback() {

                    @Override
                    public void onResult(Result status) {
                        if (status.getStatus().isSuccess()) {
                            Log.d("MapsActivity", "Successfully requested location updates");


                        } else {
                            Log.e("MapsActivity",
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatus()
                                            + ", message: "
                                            + status.getStatus());
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("onConnectionSuspended", String.valueOf(i));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (marker3 == null) {
            marker3 = mMap.addMarker(markerOptions3);
        }
        current_lat = location.getLatitude();
        current_long = location.getLongitude();
        marker3.setPosition(new LatLng(current_lat, current_long));

        mLastLocation = location;


        speed.setText(Math.random() * 10 + "Top speed: " + topSpeed*3600/1000 + "km/" + "Avg: speed " + location.getSpeed());

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(current_lat, current_long)).zoom(17).build();

        checkProximity();

        if(running == true){
            if(topSpeed == 0){
                topSpeed = location.getSpeed();
            } else if (location.getSpeed() > topSpeed){
                topSpeed = location.getSpeed();
            }
        }

    }

    public void startTimer(){
        timer = new Timer();
        checkCounter();

        timer.schedule(timerTask, 3000, 3000);
    }
    public void checkCounter() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {


                        //float new_avg = 0;
                        //float new_avg = 0;

                        if (running == true) {
                            Toast.makeText(getApplicationContext(), "Point added to list", Toast.LENGTH_SHORT).show();
                            Route.Point point = new Route.Point(user_id, route_id, current_lat, current_long, 0, point_number++);

                            Log.v("onLocCha: mobile", "lat" + current_lat);
                            Log.v("onLocCha: mobile", "long" + current_long);
                            Log.v("onLocChanged", "point_number" + point_number);
                            newPoints.add(point);

                            old_avg = avg_speed;
                            avg_speed = (newPoints.size() * old_avg - forget * mLastLocation.getSpeed());
                            forget = mLastLocation.getSpeed();

                            if (listPoints.size() > point_number) {
                                double friendLat = listPoints.get(point_number).getLatitude();
                                double friendLong = listPoints.get(point_number).getLongitude();
                                if (isAhead(friendLat, friendLong)) {
                                    timesInfront++;
                                } else {
                                    timesBehind++;
                                }
                            }
                        }
                    }
                });
            }
        };
    }
    public void onDestroy() {
        googleClient.disconnect();
        super.onDestroy();
    }

    private boolean isAhead(double latitude, double longitude) {
        double a = calcDistance(lat1, long1, current_lat, current_long);
        double b = calcDistance(lat1, long1, latitude, longitude);
        if (a > b) return true;
        else return false;
    }
    private String getDate() {
        //Get date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        final String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

    private void writeToHistory() {
        Date endTime = new Date();
        long diff = endTime.getTime() - now.getTime();
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        sdt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = sdt.format(new Date(diff));

        double distance;
        //Calculate distance(in km) from latitude & longitude

        distance = Math.floor(calcDistance(lat1, long1, lat2, long2) * 100) / 100;

        //Calculate Average Speed (in km/hr) calculated with point period of 10 secs
        //double avg_speed = Math.floor((360 * distance) / newPoints.size()) * 100 / 100;


        History history = new History(user_id, route_id, distance, avg_speed, topSpeed *3600/1000, time);

        Firebase historyRef = new Firebase(Config.HISTORY_URL);
        historyRef.push().setValue(history);

        createUserRouteRelation();

        if(googleClient.isConnected()) {
            googleClient.disconnect();
        }
    }
    private double calcDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double earthRadius = 6373;
        double dLat = Math.toRadians(latitude2 - latitude1);
        double dLong = Math.toRadians(longitude2 - longitude1);
        double sindLat = Math.sin(dLat / 2);
        double sindLong = Math.sin(dLong / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLong, 2) * Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        return dist;
    }
    public void checkProximity() {
        //check if user is close to Start
        warningTextView.setTypeface(myFontBold);

        if (((Math.abs(current_lat - lat1) > 0.0001) || (Math.abs(current_long - long1) > 0.0001)) && (running == false) && (createNewRoute == false)) {
            warningTextView.setText("Please move closer to start!");
            //send_btn.setEnabled(false);
            //send_btn.setAlpha(.5f);
            warningTextView.setTextColor(Color.RED);


        } else if ((Math.abs(current_lat - lat1) < 0.0001) && (Math.abs(current_long - long1) < 0.0001) && (running == false) && (createNewRoute == false)) {
            warningTextView.setText("Ready to run");
            send_btn.setEnabled(true);
            send_btn.setAlpha(1);
            //warningTextView.setTextColor(Color.tu);

            //check if user is close to finish
        } else if ((Math.abs(current_lat - lat2) < 0.0001) && (Math.abs(current_long - long2) < 0.0001) && (running == true) && (createNewRoute == false)) {
            warningTextView.setText("Route Completed");
            warningTextView.setTextColor(Color.GREEN);
            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            running = false;
            writeToHistory();
            Intent intent2 = new Intent(getApplicationContext(), Finish.class);
            intent2.putExtra("LAT1", lat1);
            intent2.putExtra("LONG1", long1);
            intent2.putExtra("LAT2", lat2);
            intent2.putExtra("LONG2", long2);
            intent2.putExtra("USER_ID", user_id);
            intent2.putExtra("COMPETITOR_ID", competitor_id);
            intent2.putExtra("ROUTE_ID", route_id);
            intent2.putExtra("CREATENEWROUTE", createNewRoute);
            intent2.putExtra("ROUTE_NAME", route_name);
            intent2.putExtra("FEEDBACK", feedback);
            intent2.putExtra("USERNAME", username);
            startActivity(intent2);


        }
    }
    public void addMarkers(){
        markerOptions3 = new MarkerOptions().position(new LatLng(current_lat, current_long)).title("You are here");
        Log.v("you are here", mLastLocation.getLatitude() + "lats og long " + mLastLocation.getLongitude());

        markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

        if (createNewRoute == false) {
            MarkerOptions markerOptions1 = new MarkerOptions().position(new LatLng(lat1, long1)).title("Start");
            MarkerOptions markerOptions2 = new MarkerOptions().position(new LatLng(lat2, long2)).title("Finish");
            markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(markerOptions1);
            mMap.addMarker(markerOptions2);
            //moving camera to current position
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat1, long1)).zoom(17).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else if (createNewRoute == true) {
            //moving camera to  start position(there is no start)

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())), 17));
        }
        if(mMap == null) {
            Toast.makeText(this.getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}