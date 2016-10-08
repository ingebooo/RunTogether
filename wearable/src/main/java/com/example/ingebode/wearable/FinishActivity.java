package com.example.ingebode.wearable;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
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
import java.util.Date;
import java.util.List;


public class FinishActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener,
        MessageApi.MessageListener {

    String MOBILE_DATA_PATH = "/mobile_data";
    String WEARABLE_DATA_PATH = "/wearable_data";

    private GoogleApiClient mGoogleApiClient;
    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;

    String cords = "";

    boolean isConnected = false;
    File file;


    TextView textview;
    TextView waiting_textView;
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
        waiting_textView = (TextView)findViewById(R.id.waiting_textView);
        button = (Button)findViewById(R.id.sync_button);
        textview.setTypeface(myFontMedium);


        waiting_textView.setTypeface(myFontMedium);
        button.setTypeface(myFontMedium);

        getDataFromIntent();

        if(!(isConnected)){
            button.setEnabled(false);
        } else {
            button.setEnabled(true);

            Log.v("FinishActivity", "cords: " + cords);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                writePointsToFile(cords);

                Log.v("cords:", cords + "");

                //writePointsToFile("DRITTTSEKKKKDkashdiasdkashdkjsahdkasjhdkasjhdksahdsakdjasidjnskdhd");
                sendFile();
            }
        });


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

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

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
            Log.v("file deleted", "onPostExecute" + "");
        }
        try {
            file.createNewFile();
            Log.v("file created", "onPostExecute" + "");
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
        readFile();

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

        request.setUrgent();
    }

    public void readFile(){

        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/MyAppFolder/");
        if (!dir.exists()) { dir.mkdirs(); } // Create folder if needed

        // Read data from the Asset and write it to a file on external storage
        final File file = new File(dir, "testing.txt");

        try (FileInputStream fis = new FileInputStream(file)) {

            String s  = "";
            int content;
            while ((content = fis.read()) != -1) {
                // convert to char and display it
                System.out.print("readFile finish " + (char) content);
                s += (char) content;
            }
            List<String> historyList = Arrays.asList(s.split(","));
            Log.v("historyList.get(0) ", historyList.get(0));
            Log.v("historyList.get(1) ", historyList.get(1));
            Log.v("historyList.get(2) ", historyList.get(2));
            Log.v("historyList.get(3) ", historyList.get(3));




        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(CONNECTED_PATH)) {

            Log.v("Mobile is connected", "ready to send data");

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
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        super.onPause();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}