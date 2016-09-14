package com.example.ingebode.wearable;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ingebode.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Date;


public class FinishActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    String MOBILE_DATA_PATH = "/mobile_data";
    private GoogleApiClient mGoogleApiClient;
    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;


    TextView textview;
    ArrayList<Intent> pointData;
    String username, competitor_username, time, point_collection_id;
    ArrayList<DataMap> dataMapArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        textview = (TextView)findViewById(R.id.textView3);
        textview.setTypeface(myFontMedium);
        //getDataFromIntent();


    }
    protected void onStart() {
        Log.v("onStart", "Onstart");
        mGoogleApiClient.connect();
        super.onStart();
    }
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }
    /*public void getDataFromIntent(){
        Intent intent = new Intent();
        pointData = intent.getParcelableArrayListExtra("pointData");
        //intent.putExtra("isFinish", true);
        username = intent.getStringExtra("username");
        competitor_username = intent.getStringExtra("competitor_username");
        point_collection_id = intent.getStringExtra("POINT_COLLECTION_ID");
        time = intent.getStringExtra("time");

    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class SendToDataLayerThread extends Thread {
        String path;
        ArrayList<DataMap> dataMapArrayList;
        SendToDataLayerThread(String p, ArrayList<DataMap> dataList) {
            path = p;
            dataMapArrayList = dataList;
            Log.v("SendToDataLayer", "point.size" + dataList.size());
        }
        public void run() {
            PutDataMapRequest dataMap = PutDataMapRequest
                    .create(path);

            dataMap.getDataMap().putDataMapArrayList("pointsList", dataMapArrayList);
            dataMap.getDataMap().putLong("time", new Date().getTime());
            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();

            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v("MapsActivity", "DataMap: " + dataMapArrayList.size() + " sent successfully to data layer ");

            } else {
                // Log an error
                Log.v("myTag", "ERROR: failed to send DataMapArrayList to data layer");
            }
        }
    }
}