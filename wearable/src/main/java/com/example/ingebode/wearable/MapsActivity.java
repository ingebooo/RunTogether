package com.example.ingebode.wearable;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.ingebode.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity extends Activity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * Overlay that shows a short help text when first launched. It also provides an option to
     * exit the app.
     */
    private DismissOverlayView mDismissOverlay;
    private GoogleApiClient mGoogleApiClient;

    PolylineOptions rectOptions;
    Polyline line;

    double current_lat, current_long;
    double newRouteStartLat, newRouteStartLong, newRouteFinishLat, newRouteFinishLong;
    double lat1, long1, lat2, long2;
    boolean running = false;
    int counter = 0;
    double tempLat = 0;
    double tempLon = 0;


    boolean createNewRoute = false;
    private String point_collection_id;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    ArrayList<Intent> pointData;

    Date startTime;
    Date endTime;

    Handler handler = new Handler();
    MarkerOptions markerOptions3;
    MarkerOptions markerOptions4;
    Marker marker3;
    Marker marker4;
    private ArrayList<Intent> intentList;

    int point_number = 0;

    /**
     * The map. It is initialized when the map has been fully loaded and is ready to be used.
     *
     * @see #onMapReady(com.google.android.gms.maps.GoogleMap)
     */
    private GoogleMap mMap;
    private ArrayList<Point> listPoints;
    private Location mLastLocation;
    private Timer timer;
    private TimerTask timerTask;
    private int feedback = 4;
    private String username = "Ingeborg";
    private String competitor_username = "";
    int listSize = 0;
    private LocationRequest locationRequest;
    private PendingResult<LocationSettingsResult> result;


    ArrayList<Point> newPointList;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        listPoints = new ArrayList<Point>();
        newPointList = new ArrayList<Point>();

        pointData = new ArrayList<Intent>();

        createNewRoute = false;

        running = true;

        if (!hasGps()) {
            Log.d("tag", "This hardware doesn't have GPS.");
            // Fall back to functionality that does not use location or
            // warn the user that location function is not available.
        }

        // Set the layout. It only contains a MapFragment and a DismissOverlay.
        setContentView(R.layout.activity_maps);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setAmbientEnabled();

        // Retrieve the containers for the root of the layout and the map. Margins will need to be
        // set on them to account for the system window insets.
        final FrameLayout topFrameLayout = (FrameLayout) findViewById(R.id.root_container);
        final FrameLayout mapFrameLayout = (FrameLayout) findViewById(R.id.map_container);

        // Set the system view insets on the containers when they become available.
        topFrameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Call through to super implementation and apply insets
                insets = topFrameLayout.onApplyWindowInsets(insets);

                FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) mapFrameLayout.getLayoutParams();

                // Add Wearable insets to FrameLayout container holding map as margins
                params.setMargins(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
                mapFrameLayout.setLayoutParams(params);

                return insets;
            }
        });

        // Obtain the DismissOverlayView and display the introductory help text.
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.intro_text);
        mDismissOverlay.showIntroIfNecessary();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        // if (createNewRoute == false) {
        //get points
        //Draw polyline on map
        getDataFromIntent();

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                //startTimer();
                Toast.makeText(getApplicationContext(),
                        "GOOOO", Toast.LENGTH_SHORT)
                        .show();
            }

        }.start();

        //  }


        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void getDataFromIntent() {
        Intent intent = getIntent();
        intentList = intent.getParcelableArrayListExtra("pointsList");
        listSize = intentList.get(0).getIntExtra("counter", 0);
        rectOptions = new PolylineOptions();

        for (int i = 0; i < listSize; i++) {
            Point point = new Point(intentList.get(i).getStringExtra("user_id"), intentList.get(i).getStringExtra("route_id"), intentList.get(i).getDoubleExtra("latitude", 0), intentList.get(i).getDoubleExtra("longitude", 0), intentList.get(i).getIntExtra("point_number", 0));
            listPoints.add(point);

            rectOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
            rectOptions.color(Color.RED);
            rectOptions.width(5);
        }

        lat1 = listPoints.get(0).getLatitude();
        long1 = listPoints.get(0).getLongitude();

        lat2 = listPoints.get(listSize - 1).getLatitude();
        long2 = listPoints.get(listSize - 1).getLongitude();

        feedback = intent.getExtras().getInt("feedback");
        point_collection_id = intent.getStringExtra("POINT_COLLECTION_ID");
        username = intent.getStringExtra("username");
        competitor_username = "Ingeborg";
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("onConnectionFailed()", connectionResult.getErrorMessage());
    }

    protected void onStart() {
        Log.v("onStart", "Onstart");
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStart();
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
            addMarkers();
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
        if (marker3 == null) {
            marker3 = mMap.addMarker(markerOptions3);
        }

        if (marker4 == null) {
            marker4 = mMap.addMarker(markerOptions4);
        }

        current_lat = location.getLatitude();
        current_long = location.getLongitude();
        marker3.setPosition(new LatLng(current_lat, current_long));

        if(timer == null){
            startTimer();
        }

        if(running == true){
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(current_lat, current_long)).zoom(17).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        /*

            if(running == true){
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(current_lat, current_long)).zoom(17).build();
                //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                Point point = new Point("", "", current_lat, current_long, point_number);
                newPointList.add(point);
                point_number++;

                double friendLat = listPoints.get(point_number).getLatitude();
                double friendLong = listPoints.get(point_number).getLongitude();
                marker4.setPosition(new LatLng(friendLat, friendLong));
                Log.v("onLocChanged", "friendLat" + friendLat);
                Log.v("onLocChanged", "friendLong" + friendLong);
                Log.v("onLocChanged", "myLat" + current_lat);
                Log.v("onLocChanged", "myLong" + current_long);
                Log.v("onLocChanged", "point_number" + point_number);

                marker4.showInfoWindow();

            }*/
        checkProximity();
    }
    public void checkProximity() {
        //check if user is close to Start
       /* if (((Math.abs(current_lat - lat1) > 0.0001) || (Math.abs(current_long - long1) > 0.0001)) && (running == false) && (createNewRoute == false)) {

        } else if ((Math.abs(current_lat - lat1) < 0.0001) && (Math.abs(current_long - long1) < 0.0001) && (running == false) && (createNewRoute == false)) {
            Toast.makeText(this.getApplicationContext(),
                    "You can start running!, timer started", Toast.LENGTH_SHORT)
                    .show();
            running = true;
            startTimer();
            //Set timer
            //check if user is close to finish */
       // }
    if ((Math.abs(current_lat - lat2) < 0.0001) && (Math.abs(current_long - long2) < 0.0001) && (running == true) && (createNewRoute == false)) {
            Toast.makeText(this.getApplicationContext(),
                    "Route completed", Toast.LENGTH_SHORT)
                    .show();

            if (timer != null) {
                timer.cancel();
                timer = null;

                //Her må poengene sendes tilbake til mobil / finish activity
                running = false;
                stopLocationUpdates();
                mGoogleApiClient.disconnect();

                Intent intent = new Intent(this, FinishActivity.class);
                startActivity(intent);
            }

            //writeToHistory();
        } else if ((running == false) && (createNewRoute == true)) {
           /* warningTextView.setText("Press Start to begin");
            warningTextView.setTextColor(Color.GREEN);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            startButton.setAlpha(1);
            stopButton.setAlpha(1);*/
       } else {
       }
   }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // Display the dismiss overlay with a button to exit this activity.
        mDismissOverlay.show();
        stopLocationUpdates();
    }

    private boolean hasGps() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    /*
    @Override
    public void onEnterAmbient (Bundle ambientDetails){
        MapFragment mf = new MapFragment();
        mf.onEnterAmbient(ambientDetails);
    }
    @Override
    public void onExitAmbient(){
        MapFragment mf = new MapFragment();
        mf.onExitAmbient();
    }*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //GoogleMapOptions gm = new GoogleMapOptions();
       // gm.ambientEnabled(true);
        // Map is ready to be used.
        mMap = googleMap;
        // Set the long click listener as a way to exit the map.
        mMap.setOnMapLongClickListener(this);

    }
    public void addMarkers(){
        markerOptions3 = new MarkerOptions().position(new LatLng(current_lat, current_long)).title("You are here");
        BitmapDescriptor icon2 = BitmapDescriptorFactory.fromResource(R.drawable.ingeborg);

        //markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        markerOptions3.icon(icon2);

        if (createNewRoute == false) {
            MarkerOptions markerOptions1 = new MarkerOptions().position(new LatLng(lat1, long1)).title("Start");
            MarkerOptions markerOptions2 = new MarkerOptions().position(new LatLng(lat2, long2)).title("Finish");

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.magda);

            markerOptions4 = new MarkerOptions().position(new LatLng(listPoints.get(0).getLatitude(), listPoints.get(0).getLongitude())).title(competitor_username);
            //markerOptions4.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


            markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markerOptions4.icon(icon);
            mMap.addMarker(markerOptions1);
            mMap.addMarker(markerOptions2);
            //moving camera to current position
                if(running == false) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat1, long1)).zoom(17).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            mMap.addPolyline(rectOptions);
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
        running = true;
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, the TimerTask will run every 1 sec
        timer.schedule(timerTask, 3000, 3000); //

    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }



    public void initializeTimerTask() {
        Toast.makeText(this.getApplicationContext(),
                "Timer Task started", Toast.LENGTH_SHORT);

        Log.v("Timer Task started", "wopwop" + "");
        //mMap.addPolyline(rectOptions);
        timerTask = new TimerTask() {


            public void run() {
                //Using a handler to write route points and give feedback(visual,sound or vibration)

                handler.post(new Runnable() {
                    public void run() {

                        if(running == true) {
                            Point point = new Point("", "", current_lat, current_long, point_number);
                            newPointList.add(point);
                            point_number++;

                            if(listPoints.size() > point_number) {
                                Log.v("listpoint.size: ", listPoints.size() + "");
                                Log.v("point_number: ", point_number + "");

                                double friendLat = listPoints.get(point_number).getLatitude();
                                double friendLong = listPoints.get(point_number).getLongitude();
                                marker4.setPosition(new LatLng(friendLat, friendLong));
                                marker4.showInfoWindow();
                            }
                        }
                        /*


                        //show competitor route only if createNewRoute==false and feedback==4
                        if ((createNewRoute == false) && (feedback == 4)) {
                            if (marker4 == null) {
                                marker4 = mMap.addMarker(markerOptions4);
                            }

                            if (counter < listPoints.size()) {

                                double latitude = listPoints.get(counter).getLatitude();
                                Log.v("timertask: latitude: ", "ad" + latitude);
                                double longitude = listPoints.get(counter).getLongitude();
                                Log.v("timertask: lonitude: ", "ad" + longitude);
                                counter++;
                                Log.v("counter", counter + "");
                                marker4.setPosition(new LatLng(latitude, longitude));
                                marker4.showInfoWindow();

                            }
                        }*/
                    }
                });
            }
        };
    }
}