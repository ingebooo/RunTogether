package com.example.ingebode.googlemapsproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
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
public class NewRouteActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    GoogleApiClient mGoogleApiClient;
    ArrayList<DataMap> pointData = new ArrayList<DataMap>();
    Button startButton;
    Button start_wear;
    Button fillArray;

    Handler handler = new Handler();

    PolylineOptions rectOptions;

    Timer timer;

    MarkerOptions markerOptions3;
    double current_lat, current_long;

    String distanceString = "";

    Marker marker3;

    float distance = 0;

    double hannaStartLat, hannaStartLong, hannaFinishLat, hannaFinishLong;

    double newRouteStartLat, newRouteStartLong, newRouteFinishLat, newRouteFinishLong;


    float topSpeed;
    float avg_speed;
    float old_avg;
    float forget;

    private LocationRequest locationRequest;
    private Location mLastLocation;
    boolean running = false;

    int point_number= 0;

    String cords = "";

    private List<Route.Point> newPoints = new ArrayList<>();

    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;
    Date now;

    //Firebase
    Firebase routeRef;
    Firebase newRouteRef;
    Firebase newPointCollection;
    Firebase pointRef2 = new Firebase(Config.POINTS_URL);
    Firebase pointsRef = new Firebase(Config.POINTS_URL);

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
    String user_id, competitor_id, route_id;
    String route_name, username;
    boolean createNewRoute = true;
    int feedback;
    private String point_collection_id;
    double lat1, long1, lat2, long2;

    private static GoogleMap mMap;
    Button stopButton;

    TextView warningTextView;
    private TimerTask timerTask;
    private int timesBeside, timesInfront, timesBehind;

    TextView speed;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newroute_activity);

        // Build a new GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        running = false;

        newPoints = new ArrayList<Route.Point>();

        warningTextView = (TextView)findViewById(R.id.warning);

        myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");

        startButton = (Button)findViewById(R.id.start_btn);
        startButton.setTypeface(myFontLight);

        stopButton = (Button)findViewById(R.id.stop_btn);

        
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Stopped recording route", Toast.LENGTH_SHORT).show();

                //writePointsToFile(cords);

                stopLocationUpdates();
                mGoogleApiClient.disconnect();

                lat2 = current_lat;
                long2 = current_long;
                running = false;

                //stop the timer, if it's not already null
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                Date endTime = new Date();
                long diff = endTime.getTime() - now.getTime();


                SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
                sdt.setTimeZone(TimeZone.getTimeZone("GMT"));


                String dateString = sdt.format(new Date(diff));

                //double distance = 0;
                //Calculate distance(in km) from latitude & longitude


                distanceString = Math.floor(calcDistance(newRouteStartLat, newRouteStartLong, newRouteFinishLat, newRouteFinishLong) * 100) / 100 + "";

                warningTextView.setText("Route saved!");
                warningTextView.setTextColor(Color.GREEN);
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
                startButton.setAlpha(.5f);
                stopButton.setAlpha(.5f);
                newRouteFinishLat = current_lat;
                newRouteFinishLong = current_long;

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("finish_latitude", newRouteFinishLat);
                map.put("finish_longitude", newRouteFinishLong);
                map.put("distance", distance + "");


                map.put("time", dateString);


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NewRouteActivity.this);
                LayoutInflater inflater = (NewRouteActivity.this).getLayoutInflater();

                final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                final EditText input = (EditText) dialogView.findViewById(R.id.editTextRouteName);
                final Button okBtn = (Button)dialogView.findViewById(R.id.dialog_btn);
                final TextView routeText = (TextView)dialogView.findViewById(R.id.route_finish);
                final TextView routeText2 = (TextView)dialogView.findViewById(R.id.enter_route_name);



                routeText.setTypeface(myFontLight);
                routeText2.setTypeface(myFontLight);
                okBtn.setTypeface(myFontLight);
                dialogBuilder.setView(dialogView);

                route_name = input.getText().toString();
                if (newRouteRef != null) {
                    newRouteRef.updateChildren(map);
                }
                //map.put("route_name", route_name);




                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        route_name = input.getText().toString();


                        writeToHistory();
                        passIntent();

                    }
                });

                AlertDialog b = dialogBuilder.create();
                b.show();

            }
        });


        rectOptions = new PolylineOptions();


            rectOptions.add(new LatLng(63.42796855, 10.40603855));
            rectOptions.add(new LatLng(63.42788217, 10.40732601));
            rectOptions.add(new LatLng(63.42899558, 10.4074333));

            rectOptions.add(new LatLng(63.42928617, 10.40759325));

            rectOptions.add(new LatLng(63.43058668, 10.41123033));
            rectOptions.add(new LatLng(63.43139767, 10.41336536));
            rectOptions.add(new LatLng(63.43084582, 10.41453481));
            rectOptions.add(new LatLng(63.43183435, 10.41670203));
            rectOptions.add(new LatLng(63.43238138, 10.4155755));
            rectOptions.add(new LatLng(63.43485731, 10.42096138));
            rectOptions.add(new LatLng(63.43527474, 10.41827917));
            rectOptions.add(new LatLng(63.43210786, 10.41055441));
            rectOptions.add(new LatLng(63.43160881, 10.41158438));
            rectOptions.add(new LatLng(63.4296169, 10.4065531));






                        hannaStartLat = 63.42796855;
                        hannaStartLong = 10.40603855;


                        hannaFinishLat =63.4296169;
                        hannaFinishLong = 10.4065531;


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Start recording route", Toast.LENGTH_SHORT).show();
                newPointCollection = pointsRef.push();
                point_collection_id = newPointCollection.getKey();

                    warningTextView.setText("Start saved.Now running...");
                    warningTextView.setTextColor(Color.GREEN);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    startButton.setAlpha(.5f);
                    stopButton.setAlpha(1);
                    running = true;
                    newRouteStartLat = current_lat;
                    newRouteStartLong = current_long;

                    now = new Date();

                    addRoute(newRouteStartLat, newRouteStartLong, 0, 0, route_name, "");

                if (timer == null) {
                        startTimer();
                    Log.v("timer", "started" + "");
                    }
                }
        });
        
        getDataFromIntent();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    public void getDataFromIntent() {
        Intent intent = this.getIntent();
        user_id = intent.getStringExtra("USER_ID");
        username = intent.getStringExtra("USERNAME");
    }

    /*
    // Send a data object when the data layer connection is successful.    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }*/

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

        UserRouteRelation relation = new UserRouteRelation(route_id, user_id, username, point_collection_id, "", timesBehind, timesInfront, timesBeside);

        newRefferRouteID.push().setValue(relation);

        Toast.makeText(getApplicationContext(),
                "User route relation created...", Toast.LENGTH_SHORT)
                .show();
    }
    protected void onStart() {
        Log.v("onStart", "Onstart");
        mGoogleApiClient.connect();
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
        if(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.v("mLastLocation", mLastLocation.toString());
            addMarkers();
            //startTimer();
            mMap.setMyLocationEnabled(true);
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
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

        mLastLocation = location;

        if (marker3 == null) {
            marker3 = mMap.addMarker(markerOptions3);
        }

        current_lat = location.getLatitude();
        current_long = location.getLongitude();
        marker3.setPosition(new LatLng(current_lat, current_long));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(current_lat, current_long)).zoom(17).build();
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())), 17));



        if(topSpeed == 0){
            topSpeed = location.getSpeed();
        } else if (location.getSpeed() > topSpeed){
            topSpeed = location.getSpeed();
        }

    }
    //Firebase methods for saving data to server
    public void addRoute(double startLat, double startLong, double finishLat, double finishLong, String route_name, String time) {
        Firebase ref = new Firebase(Config.FIREBASE_URL);

        routeRef = ref.child("routes");

        newRouteRef = routeRef.push();

        Route route = new Route("", point_collection_id, startLat, startLong, finishLat, finishLong, route_name, "", username, 0);

        newRouteRef.setValue(route);

        route_id = newRouteRef.getKey();
        route.setRoute_id(route_id);
        newRouteRef.child("route_id").setValue(route_id);


    }

    public void createFile() throws IOException {
        Log.v("creaeFile", "file created");
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/MyAppFolder/");
        if (!dir.exists()) {
            dir.mkdirs();
        } // Create folder if needed

        file = new File(dir, "test.txt");

        if (file.exists()) {
            file.delete();
        } else {
            file.createNewFile();
        }
    }

    public void writePointsToFile(String data) {

        try {
            Date now = new Date();
            long nTime = now.getTime();
            FileOutputStream fOut = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fOut);
            ps.println("Time = " + Long.toString(nTime)); // A value that changes each time

            //ps.println(user_id.getBytes());
            ps.println(data);
            ps.close();
        } catch (Exception e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public void startTimer(){
        timer = new Timer();
        checkCounter();


        timer.schedule(timerTask, 3000, 3500);
    }
    public void checkCounter() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (running == true) {

                            Route.Point point = new Route.Point(user_id, route_id, current_lat, current_long, 0, point_number);
                            newPoints.add(point);

                            float[] results = new float[1];

                            Log.v("point number ", "" + point_number);


                            if(point_number > 0) {
                                Location.distanceBetween(newPoints.get(point_number - 1).getLatitude(), newPoints.get(point_number - 1).getLongitude(), point.getLatitude(), point.getLongitude(), results);
                                System.out.print("point_number - 1" + (point_number - 1));
                            }



                            point_number++;

                            distance += results[0];
                            Log.v("distance ", "" + distance);

                            Toast.makeText(getApplicationContext(), "Point added to list", Toast.LENGTH_SHORT).show();



                            /*
                            if(old_avg == 0){
                                old_avg = mLastLocation.getSpeed();
                            } else {
                                old_avg = avg_speed;
                            }
                            avg_speed = (newPoints.size() * old_avg - forget * mLastLocation.getSpeed());
                            forget = mLastLocation.getSpeed();


*/

                            if(newPoints.size() == 350){
                                Toast.makeText(getApplicationContext(), "Stopped recording route", Toast.LENGTH_SHORT).show();

                                //writePointsToFile(cords);

                                writeToHistory();
                                passIntent();

                                stopLocationUpdates();
                                mGoogleApiClient.disconnect();

                                lat2 = current_lat;
                                long2 = current_long;
                                running = false;


                                //stop the timer, if it's not already null
                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }
                                Date endTime = new Date();
                                long diff = endTime.getTime() - now.getTime();


                                SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
                                sdt.setTimeZone(TimeZone.getTimeZone("GMT"));


                                String dateString = sdt.format(new Date(diff));

                                //double distance = 0;
                                //Calculate distance(in km) from latitude & longitude
                                distanceString = ""+ Math.floor(calcDistance(newRouteStartLat, newRouteStartLong, newRouteFinishLat, newRouteFinishLong) * 100) / 100;

                                avg_speed = (float) (Math.floor((360 * distance) / newPoints.size()) * 35 / 35);

                                warningTextView.setText("Route saved!");
                                warningTextView.setTextColor(Color.GREEN);
                                startButton.setEnabled(false);
                                stopButton.setEnabled(false);
                                startButton.setAlpha(.5f);
                                stopButton.setAlpha(.5f);
                                newRouteFinishLat = current_lat;
                                newRouteFinishLong = current_long;

                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("finish_latitude", newRouteFinishLat);
                                map.put("finish_longitude", newRouteFinishLong);

                                map.put("distance", distance + " distance2: " + distanceString);
                                map.put("time", dateString);


                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NewRouteActivity.this);
                                LayoutInflater inflater = (NewRouteActivity.this).getLayoutInflater();

                                final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
                                final EditText input = (EditText) dialogView.findViewById(R.id.editTextRouteName);
                                final Button okBtn = (Button)dialogView.findViewById(R.id.dialog_btn);
                                final TextView routeText = (TextView)dialogView.findViewById(R.id.route_finish);
                                final TextView routeText2 = (TextView)dialogView.findViewById(R.id.enter_route_name);



                                routeText.setTypeface(myFontLight);
                                routeText2.setTypeface(myFontLight);
                                okBtn.setTypeface(myFontLight);
                                dialogBuilder.setView(dialogView);

                                route_name = input.getText().toString();
                                if (newRouteRef != null) {
                                    newRouteRef.updateChildren(map);
                                }
                                //map.put("route_name", route_name);




                                okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        route_name = input.getText().toString();


                                        writeToHistory();
                                        passIntent();

                                    }
                                });

                                AlertDialog b = dialogBuilder.create();
                                b.show();
                            }
                        }
                    }
                });
            }
        };
    }
    public void onDestroy() {
        mGoogleApiClient.disconnect();
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

        String date = getDate();

        Date endTime = new Date();
        long diff = endTime.getTime() - now.getTime();
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        sdt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = sdt.format(new Date(diff));

        //TODO: add time here

        History history = new History(user_id, route_id, distance + "", avg_speed + "",topSpeed+ "", time);

       // * 3600/1000

        Firebase historyRef = new Firebase(Config.HISTORY_URL);
        historyRef.push().setValue(history);

        createUserRouteRelation();

        //placing the whole list  with new points into firebase with new point collection ID
        newPointCollection.setValue(newPoints);

        //Google Analytics
      /*  Tracker t = ((fitnessApp) getApplication()).getTracker(fitnessApp.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder().setCategory("Achievement").setAction("Finish Route").setLabel("Finish").build()); */

        if(mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
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
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    public void passIntent() {
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
        intent2.putExtra("NEWROUTEREF", newRouteRef.toString());
        intent2.putExtra("USERNAME", username);
        startActivity(intent2);
    }
    public void addMarkers(){
        markerOptions3 = new MarkerOptions().position(new LatLng(current_lat, current_long)).title("You are here");
        Log.v("you are here", mLastLocation.getLatitude() + "lats og long " + mLastLocation.getLongitude());


        MarkerOptions markerOptions1 = new MarkerOptions().position(new LatLng(hannaStartLat, hannaStartLong)).title("Start");
        MarkerOptions markerOptions2 = new MarkerOptions().position(new LatLng(63.4296169, 10.4065531)).title("Finish");

        Log.v("markerOptions2)", markerOptions2.toString() + "");


        //markerOptions4.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


       // rectOptions.add(new LatLng(63.4296169, 10.4065531));

        markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mMap.addMarker(markerOptions1);
        mMap.addMarker(markerOptions2);

        markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())), 17));

        Log.v("rectOptions: ", rectOptions.getPoints().size() + "");
        mMap.addPolyline(rectOptions);

        if(mMap == null) {
            Toast.makeText(this.getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}