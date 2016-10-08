package com.example.ingebode.googlemapsproject;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.ingebode.R;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
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
 * Created by ingebode on 14/03/16.
 */
public class CustomMapFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback, MessageApi.MessageListener {

    private String title;
    private int page;

    Button startButton;
    Button stopButton;

    //models
    double current_lat, current_long;
    double newRouteStartLat, newRouteStartLong, newRouteFinishLat, newRouteFinishLong;
    String user_id, competitor_id, route_id;
    String route_name, competitor_username, username;
    String timeString = "";
    int point_number = 0;

    double lat1, long1, lat2, long2;

    private List<Route.Point> listPoints;
    private List<Route.Point> newPoints = new ArrayList<>();
    Timer timer;
    TimerTask timerTask;
    int counter = 0;


    Handler handler = new Handler();
    MarkerOptions markerOptions3;
    MarkerOptions markerOptions4;
    Marker marker3;
    Marker marker4;

    TextView warningTextView;
    boolean running;
    boolean createNewRoute = true;
    int feedback, user_repetition, co_user_repetition;
    Date now;

    private String point_collection_id;
    private static final String START_ACTIVITY_PATH = "/start-activity";
    String WEARABLE_DATA_PATH = "/wearable_data";

    //Firebase
    Firebase routeRef;
    Firebase newRouteRef;
    Firebase newPointCollection;
    Firebase pointRef;
    Firebase pointsRef = new Firebase(Config.POINTS_URL);
    Map<String, Object> runnedByMap = new HashMap<String, Object>();

    //Location
    private String mLastUpdateTime;
    PendingResult<LocationSettingsResult> result;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    PolylineOptions rectOptions;
    ArrayList<DataMap> pointData;

    float avg_speed = 0;
    float old_avg = 0;
    float forget = 0;


    //map
    private static GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    private float topSpeed;

    // newInstance constructor for creating fragment with arguments
    public static CustomMapFragment newInstance(int page, String title) {
        CustomMapFragment customMapFragment = new CustomMapFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        customMapFragment.setArguments(args);
        return customMapFragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

        running = false;

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Wearable.API)
                    .build();
        }

        getDataFromIntent();

        //read from Points database to find other users routes
        if (createNewRoute == false) {
            Toast.makeText(getActivity().getApplicationContext(), "createnew route = false", Toast.LENGTH_SHORT).show();

            listPoints = new ArrayList<>();
            pointData = new ArrayList<>();


            Firebase pointsRef = new Firebase(Config.POINTS_URL).child(point_collection_id);
            pointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // do some stuff once
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Route.Point point = snap.getValue(Route.Point.class);
                        Route.Point otherPoint = new Route.Point(point.getLatitude(), point.getLongitude());

                        DataMap map = new DataMap();

                        map.putString("user_id", point.getUser_id());
                        map.putString("route_id", point.getRoute_id());
                        map.putDouble("latitude", point.getLatitude());
                        map.putDouble("longitude", point.getLongitude());
                        pointData.add(map);

                        rectOptions = new PolylineOptions().add(new LatLng(point.getLatitude(), point.getLongitude()));
                        rectOptions.color(Color.BLUE);
                        rectOptions.width(5);
                        listPoints.add(otherPoint);
                    }

                    new SendReadyToRunTask();
                    new SendToDataLayerThread(WEARABLE_DATA_PATH, pointData).start();
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }

        //user_repetition = db.getRepetitions(Integer.toString(route_id), Integer.toString(user_id)) + 1;
        user_repetition = 0;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, null, false);
        //it should not be possible to stop the time if not the start button has been pushed
        stopButton = (Button) view.findViewById(R.id.buttonStop);

        //stopButton.setEnabled(false);

        warningTextView = (TextView) view.findViewById(R.id.warning);
        startButton = (Button) view.findViewById(R.id.buttonStart);
        //stopButton.setClickable(true);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity().getApplicationContext(), "Start recording route", Toast.LENGTH_SHORT).show();
                newPointCollection = pointsRef.push();
                point_collection_id = newPointCollection.getKey();


                if (createNewRoute == false) {
                    warningTextView.setText("Now running...");
                    warningTextView.setTextColor(Color.GREEN);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    startButton.setAlpha(.5f);
                    stopButton.setAlpha(.5f);
                    running = true;
                    if (timer == null) {
                        startTimer();
                    }
                } else if (createNewRoute == true) {
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
                    }
                }

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity().getApplicationContext(), "Stopped recording route", Toast.LENGTH_SHORT).show();

                stopLocationUpdates();
                Log.v("stopLocationUpdates", "location updates stopped");
                mGoogleApiClient.disconnect();
                Log.v("GoogleApi", "disconnected");

                lat2 = current_lat;
                long2 = current_long;
                running = false;
                //stop the timer, if it's not already null
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }

                if (createNewRoute == true) {


                    Date endTime = new Date();
                    long diff = endTime.getTime() - now.getTime();


                    SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
                    sdt.setTimeZone(TimeZone.getTimeZone("GMT"));


                    String dateString = sdt.format(new Date(diff));

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

                    map.put("time", dateString);


                    //trenger null sjekk
                    if(newRouteRef != null) {
                        newRouteRef.updateChildren(map);
                    }

                    writeToHistory();


                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    private void sendReadyToRunMessage(String node) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
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

    //Firebase methods for saving data to server
    public void addRoute(double startLat, double startLong, double finishLat, double finishLong, String route_name, String time) {
        Firebase ref = new Firebase(Config.FIREBASE_URL);

        routeRef = ref.child("routes");

        newRouteRef = routeRef.push();

        Route route = new Route("", point_collection_id, startLat, startLong, finishLat, finishLong, route_name, "", "", 0);

        newRouteRef.setValue(route);

        route_id = newRouteRef.getKey();
        route.setRoute_id(route_id);
        newRouteRef.child("route_id").setValue(route_id);


    }
    private void writeToHistory() {

        String date = getDate();

        Date endTime = new Date();
        long diff = endTime.getTime() - now.getTime();
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        sdt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = sdt.format(new Date(diff));

        double distance = 0;
        //Calculate distance(in km) from latitude & longitude
        distance = Math.floor(calcDistance(newRouteStartLat, newRouteStartLong, newRouteFinishLat, newRouteFinishLong) * 100) / 100;


        //Calculate Average Speed (in km/hr) calculated with point period of 10 secs
        //avg_speed = Math.floor((360 * distance) / newPoints.size()) * 100 / 100;

        //TODO: add time here

        /*History history = new History(user_id, route_id, distance, avg_speed,topSpeed * 3600/1000, time);

        Firebase historyRef = new Firebase(Config.HISTORY_URL);
        historyRef.push().setValue(history);*/

        createUserRouteRelation();

        //placing the whole list  with new points into firebase with new point collection ID
        newPointCollection.setValue(newPoints);

        //Google Analytics
      /*  Tracker t = ((fitnessApp) getApplication()).getTracker(fitnessApp.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder().setCategory("Achievement").setAction("Finish Route").setLabel("Finish").build()); */

        passIntent();

        if(mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    private void createUserRouteRelation() {


        Date endTime = new Date();
        long diff = endTime.getTime() - now.getTime();


        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
        sdt.setTimeZone(TimeZone.getTimeZone("GMT"));


        String dateString = sdt.format(new Date(diff));

        Firebase reffer = new Firebase(Config.FIREBASE_URL);
        Firebase newReffer = reffer.child("routeRelations");

        Firebase newRefferRouteID = newReffer.child(route_id);

        UserRouteRelation relation = new UserRouteRelation(route_id, user_id, username, point_collection_id, dateString, 0, 0,0);

        newRefferRouteID.push().setValue(relation);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                  LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // permission has been granted, continue
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            createLocationRequest();

            /*addMarkers();
            mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Start"));*/

            //CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).zoom(17).build();
        }
    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            Toast.makeText(getActivity().getApplicationContext(),
                    "PERMISSION GRANYED", Toast.LENGTH_SHORT)
                    .show();
            //createLocationRequest();

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v("LocationRequest", "onMapReady");

        mMap = googleMap;


        enableMyLocation();

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(getActivity().getApplicationContext(), "Location button presed!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // check if map is created successfully or not
        if (googleMap == null) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    public void addMarkers(){
        markerOptions3 = new MarkerOptions().position(new LatLng(current_lat, current_long)).title("You are here");
        Log.v("you are here", mLastLocation.getLatitude() + "lats og long " + mLastLocation.getLongitude());

        markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

        if (createNewRoute == false) {
            MarkerOptions markerOptions1 = new MarkerOptions().position(new LatLng(lat1, long1)).title("Start");
            MarkerOptions markerOptions2 = new MarkerOptions().position(new LatLng(lat2, long2)).title("Finish");
            markerOptions4 = new MarkerOptions().position(new LatLng(listPoints.get(0).getLatitude(), listPoints.get(0).getLongitude())).title(competitor_username);
            markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markerOptions4.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
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
            Toast.makeText(getActivity().getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void getDataFromIntent() {
        Intent intent = getActivity().getIntent();
        lat1 = intent.getDoubleExtra("LAT1", 0);
        long1 = intent.getDoubleExtra("LONG1", 0);
        lat2 = intent.getDoubleExtra("LAT2", 0);
        long2 = intent.getDoubleExtra("LONG2", 0);
        user_id = intent.getStringExtra("USER_ID");
        competitor_id = intent.getStringExtra("COMPETITOR_ID");
        route_id = intent.getStringExtra("ROUTE_ID");
        createNewRoute = intent.getExtras().getBoolean("CREATENEWROUTE");
        route_name = intent.getStringExtra("ROUTE_NAME");
        feedback = intent.getExtras().getInt("FEEDBACK");
        point_collection_id = intent.getStringExtra("POINT_COLLECTION_ID");
        username = intent.getStringExtra("USERNAME");
        competitor_username = intent.getStringExtra("COMPETITOR_USERNAME");
    }

    @Override
    public void onLocationChanged(Location location) {

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


        //mLastLocation = location;

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        /*
        if(running == true){
            Route.Point point  = new Route.Point(user_id, route_id, current_lat, current_long, user_repetition, point_number++);
            newPoints.add(point);
            //addPoint(current_lat, current_long);

            if(topSpeed == 0){
                topSpeed = location.getSpeed();
            } else if (location.getSpeed() > topSpeed){
                topSpeed = location.getSpeed();
            }
        }*/
        checkProximity();
    }
    //connect to google api client
    @Override
    public void onStart() {

        mGoogleApiClient.connect();
        super.onStart();
        checkProximity();
        Log.v("GoogleApi", "connected");
    }

    @Override
    public void onStop() {

        /*stopLocationUpdates();
        mGoogleApiClient.disconnect();
        Log.v("GoogleApi", "disconnected");*/
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            addMarkers();
            //when running a old route, this and the second gets called


            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.v("LocationRequest", "started");
        }
    }

    protected void createLocationRequest() {
        Log.v("LocationRequest", "created");
        mLocationRequest = new LocationRequest();
        /*mLocationRequest.setInterval(1000 * 10);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);*/

        /*
		 * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result1) {
                final Status status = result1.getStatus();
                //  final LocationSettingsStates = result1.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //init location request here
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // not satisfied location settings, show the user dialog
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(),
                                    LOCATION_PERMISSION_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity(), "OnConnectionSuspended.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "OnConnectionFailed.", Toast.LENGTH_LONG).show();

    }
    public void checkProximity() {
        //check if user is close to Start
        if (((Math.abs(current_lat - lat1) > 0.0001) || (Math.abs(current_long - long1) > 0.0001)) && (running == false) && (createNewRoute == false)) {
            warningTextView.setText("Please move closer to Start!");
            warningTextView.setTextColor(Color.RED);
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            startButton.setAlpha(.5f);
            stopButton.setAlpha(.5f);
        } else if ((Math.abs(current_lat - lat1) < 0.0001) && (Math.abs(current_long - long1) < 0.0001) && (running == false) && (createNewRoute == false)) {
            warningTextView.setText("You can start running!");
            warningTextView.setTextColor(Color.GREEN);
            startButton.setEnabled(true);
            stopButton.setEnabled(true);
            startButton.setAlpha(1);
            stopButton.setAlpha(1);
            //check if user is close to finish
        } else if ((Math.abs(current_lat - lat2) < 0.0001) && (Math.abs(current_long - long2) < 0.0001) && (running == true) && (createNewRoute == false)) {
            warningTextView.setText("Route Completed!");
            warningTextView.setTextColor(Color.GREEN);
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            startButton.setAlpha(.5f);
            stopButton.setAlpha(.5f);
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            running = false;
            writeToHistory();
        } else if ((running == false) && (createNewRoute == true)) {
            warningTextView.setText("Press Start to begin");
            warningTextView.setTextColor(Color.GREEN);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            startButton.setAlpha(1);
            stopButton.setAlpha(1);
        } else {
        }
    }


    public void passIntent() {
        Intent intent2 = new Intent(getContext(), Finish.class);
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
        intent2.putExtra("REPETITION", user_repetition);
        intent2.putExtra("USERNAME", username);
        startActivity(intent2);
    }

    private boolean isAhead(double latitude, double longitude) {
        double a = calcDistance(lat1, long1, current_lat, current_long);
        double b = calcDistance(lat1, long1, latitude, longitude);
        if (a > b) return true;
        else return false;
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

    private String getDate() {
        //Get date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        final String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }
    public void onDestroy() {
        if(running == true) {
            mGoogleApiClient.disconnect();
            stopLocationUpdates();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, the TimerTask will run every 10sec
        //adding points each second
            timer.schedule(timerTask, 3000, 3000); //

    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
        public void initializeTimerTask() {
            //mMap.addPolyline(rectOptions);
        timerTask = new TimerTask() {


            public void run() {
                //Using a handler to write route points and give feedback(visual,sound or vibration)

                handler.post(new Runnable() {
                    public void run() {
                        //show competitor route only if createNewRoute==false and feedback==4

                        if(running == true){
                            Route.Point point  = new Route.Point(user_id, route_id, current_lat, current_long, user_repetition, point_number++);
                            newPoints.add(point);
                            //addPoint(current_lat, current_long);
                            Toast.makeText(getActivity().getApplicationContext(), "Point added to list", Toast.LENGTH_SHORT).show();

                            old_avg = avg_speed;
                            avg_speed = (newPoints.size() * old_avg - forget * mLastLocation.getSpeed());
                            forget = mLastLocation.getSpeed();
                        }

                        /*
                        if ((createNewRoute == false) && (feedback == 4)) {
                            if (marker4 == null) {
                                marker4 = mMap.addMarker(markerOptions4);
                            }
                            if (counter < listPoints.size()) {
                                double latitude = listPoints.get(counter).getLatitude();
                                double longitude = listPoints.get(counter).getLongitude();
                                counter++;
                                marker4.setPosition(new LatLng(latitude, longitude));
                                marker4.showInfoWindow();
                            }
                        } else if ((createNewRoute == false) && (feedback == 2)) {
                            //vibration
                            if (counter < listPoints.size()) {
                                double latitude = listPoints.get(counter).getLatitude();
                                double longitude = listPoints.get(counter).getLongitude();
                                counter++;
                                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                if (isAhead(latitude, longitude)) {
                                    //3 long vibrations if user is ahead of competitor
                                    long pattern1[] = {0, 800, 0, 800, 0, 800};
                                    v.vibrate(pattern1, -1);
                                } else {
                                    //5 short vibrations if user is behind of competitor
                                    long pattern2[] = {0, 400, 0, 400, 0, 400, 0, 400, 0, 400};
                                    v.vibrate(pattern2, -1);
                                }
                            }
                        } else if ((createNewRoute == false) && (feedback == 3)) {
                            //sound
                            if (counter < listPoints.size()) {
                                double latitude = listPoints.get(counter).getLatitude();
                                double longitude = listPoints.get(counter).getLongitude();
                                counter++;
                                if (isAhead(latitude, longitude)) {
                                    MediaPlayer mp1 = new MediaPlayer();
                                    AssetFileDescriptor descriptor1;
                                    try {
                                        descriptor1 = getActivity().getAssets().openFd("ahead.wav");
                                        mp1.setDataSource(descriptor1.getFileDescriptor(), descriptor1.getStartOffset(), descriptor1.getLength());
                                        descriptor1.close();
                                        mp1.prepare();
                                        mp1.start();
                                    } catch (IOException e) {
                                        Toast.makeText(getActivity().getApplicationContext(), "Can't play audio file!", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }

                                } else {
                                    MediaPlayer mp2 = new MediaPlayer();
                                    AssetFileDescriptor descriptor2;
                                    try {
                                        descriptor2 = getActivity().getAssets().openFd("behind.wav");
                                        mp2.setDataSource(descriptor2.getFileDescriptor(), descriptor2.getStartOffset(), descriptor2.getLength());
                                        descriptor2.close();
                                        mp2.prepare();
                                        mp2.start();
                                    } catch (IOException e) {
                                        Toast.makeText(getActivity().getApplicationContext(), "Can't play audio file!", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }*/
                    }
                });
            }
        };
    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    public void onDisconnected() {
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
    private class SendReadyToRunTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Log.v("SendReadyToRunTask", "" + "");
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendReadyToRunMessage(node);
            }
            return null;
        }
    }
    class SendToDataLayerThread extends Thread {
        String path;
        ArrayList<DataMap> dataMapArrayList;

        SendToDataLayerThread(String p, ArrayList<DataMap> dataList) {
            path = p;
            dataMapArrayList = dataList;
        }

        public void run() {
            PutDataMapRequest dataMap = PutDataMapRequest
                    .create(path);

            dataMap.getDataMap().putDataMapArrayList("pointsList", dataMapArrayList);

            PutDataRequest request = dataMap.asPutDataRequest();

            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v("myTag", "DataMap: " + dataMapArrayList.size() + " sent successfully to data layer ");
            }
            else {
                // Log an error
                Log.v("myTag", "ERROR: failed to send DataMapArrayList to data layer");
            }
        }
    }
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }
}

