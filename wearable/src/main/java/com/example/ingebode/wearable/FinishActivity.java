package com.example.ingebode.wearable;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ingebode.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class FinishActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    String MESSAGE_RECEIVED_PATH = "/synd_data_path";
    String WEARABLE_DATA_PATH = "/wearable_data";

    private GoogleApiClient mGoogleApiClient;
    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;

    String cords = "";

    boolean isConnected = false;
    File file;


    TextView textview;
    TextView synced_textView;
    Button button;
    ArrayList<DataMap> dataMapArrayList;

    private static final String CONNECTED_PATH = "/mobile-connected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

                      /* Request user permissions in runtime */
        ActivityCompat.requestPermissions(FinishActivity.this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                100);

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        synced_textView = (TextView)findViewById(R.id.sync_textView);
        synced_textView.setTypeface(myFontMedium);

        getDataFromIntent();

        button = (Button)findViewById(R.id.sync_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writePointsToFile(cords);

                Log.v("cords:", cords + "");

                sendFile();
            }
        });

        button.setTypeface(myFontMedium);

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
    public void getDataFromIntent(){
        Intent intent = getIntent();
        cords = intent.getStringExtra("cords");
        System.out.println("GetDataFromIntent, finnish " + cords);
    }

    public void createFile() throws IOException {
        Log.v("creaeFile", "file created");
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/MyAppFolder/");

        if (!dir.exists()) {
            dir.mkdirs();
        } // Create folder if needed

        file = new File(dir, "testing.txt");

        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePointsToFile(String data) {

        try {
            createFile();
            Date now = new Date();
            long nTime = now.getTime();
            FileOutputStream fOut = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fOut);
            ps.println(data);
            ps.close();
        } catch (Exception e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }
    public void sendFile() {
        // Read the text file into a byte array
        Log.v("sendFile", "file sent");

        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
        }

        // Create an Asset from the byte array, and send it via the DataApi
        Asset asset = Asset.createFromBytes(bFile);
        PutDataMapRequest dataMap = PutDataMapRequest.create("/txt");
        dataMap.getDataMap().putAsset("com.example.company.key.TXT", asset);
        PutDataRequest request = dataMap.asPutDataRequest();



        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);

        synced_textView.setText("Data synced with mobile");

        request.setUrgent();
    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(CONNECTED_PATH)) {

            isConnected = true;
            button.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        super.onPause();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}