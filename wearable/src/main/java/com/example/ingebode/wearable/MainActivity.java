package com.example.ingebode.wearable;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ingebode.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener {

    private ArrayList<Intent> intentList;
    private static final String START_ACTIVITY_PATH = "/start-activity";

    private GoogleApiClient mGoogleApiClient;
    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    ArrayList<DataMap> pointsList;

    ArrayList<Point> filePoints;

    String competitor_username;
    int feedback;

    Button startBtn;
    TextView text;

    boolean readyToRun = false;
    private int counter = 0;
    private Typeface myFontMedium;
    private Typeface myFontLight;
    private Typeface myFontBold;
    private String route_id;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_start);
        startBtn = (Button)findViewById(R.id.start_button);
        text = (TextView)findViewById(R.id.textView2);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                100);

        myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");
        text.setTypeface(myFontBold);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            getData();
        }

        if(readyToRun==false){
            text.setText("Waiting for data");
            startBtn.setEnabled(false);
            startBtn.setAlpha(.5f);
        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    passIntent();
            }
        });
        startBtn.setTypeface(myFontLight);
    }
    private void getData(){
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    public void finishRun(){
        text.setText("Route is completed");
        text.setTypeface(myFontMedium);
        startBtn.setText("Sync with mobile");
        startBtn.setBackgroundColor(Color.parseColor("#7EECEC"));
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v("wearable","MainActivity, onDataChanged" + "");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/txt")) {

                // Get the Asset object

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset asset = dataMapItem.getDataMap().getAsset("com.example.company.key.TXT");

                Log.v("onDataChanged", "startWearActivity" + "");

                new ConvertAssetAsyncTask().execute(asset);

            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {}
                DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                pointsList = dataItem.getDataMap().getDataMapArrayList("pointsList");
                onPointsReceived(pointsList);
            }
        }
    }


    private class ConvertAssetAsyncTask extends AsyncTask<Asset, Void, InputStream> {

        @Override
        protected InputStream doInBackground(Asset... params) {
            Log.v("doInBackGround", "startWearActivity" + "");

            if (params.length > 0) {

                Asset asset = params[0];

                // Convert asset into a file descriptor and block until it's ready
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).await().getInputStream();
                mGoogleApiClient.disconnect();

                if (assetInputStream == null) {
                    return null;
                }

                if (assetInputStream == null) {
                    return null;
                }
                return assetInputStream;

            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(InputStream assetInputStream) {

            if (assetInputStream != null) {
                Log.v("onPostExecute", "startWearActivity" + "");
                File sdcard = Environment.getExternalStorageDirectory();
                File dir = new File(sdcard.getAbsolutePath() + "/MyAppFolder/");
                if (!dir.exists()) {
                    dir.mkdirs();
                } // Create folder if needed

                // Read data from the Asset and write it to a file on external storage
                final File file = new File(dir, "longRoute.txt");

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

                try {

                    FileOutputStream fOut = new FileOutputStream(file);
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                        fOut.write(data, 0, nRead);
                        //osw.write(assetInputStream.read());
                    }

                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Rescan folder to make it appear
                try {
                    String[] paths = new String[1];
                    paths[0] = file.getAbsolutePath();
                    MediaScannerConnection.scanFile(getApplicationContext(), paths, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                readFile(file);

            }
        }

    }
    public void readFile(File file){
        System.out.println("reading file");
        String distanceString = "";
        String avg_speedString = "", top_speedString = "";
        String time = "";
        filePoints = new ArrayList<>();



        try (FileInputStream fis = new FileInputStream(file)) {
            System.out.println("Total file size to read (in bytes) : "
                    + fis.available());

            String s = "";

            int content;
            while ((content = fis.read()) != -1) {
                s += (char)content;

                Log.v("s: " , s + "");
            }
            String s2 = "";

            List<String> pointsList = Arrays.asList(s.split(","));
            List<String> pList;

            counter = (int) Double.parseDouble(pointsList.get(0));
            route_id = pointsList.get(2);
            user_id = pointsList.get(1);


            for(int i = 3; i < pointsList.size(); i++){

                s2 = pointsList.get(i);
                pList = Arrays.asList(s2.split("!"));

                int k = 0;
                double lat = Double.parseDouble(pList.get(k));
                double lonng = Double.parseDouble(pList.get(pList.size() - 1));
                k++;

                Point point = new Point(lat, lonng);
                filePoints.add(point);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void passIntent(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putParcelableArrayListExtra("pointsList", intentList);
        intent.putExtra("counter", counter);
        intent.putExtra("feedback", feedback);
        intent.putExtra("competitor_username", competitor_username);

        Log.v("MainActivity wear", "competitor_username" + competitor_username);
        startActivity(intent);
    }
    public void onPointsReceived(ArrayList<DataMap> list){
        ArrayList<Point> pointList = new ArrayList<Point>();
        int i =0;
        for(DataMap map : list){
            Point p = new Point(map.getString("user_id"), map.getString("route_id"), map.getDouble("latitude"), map.getDouble("longitude"),map.getInt("point_number"));
            pointList.add(p);
        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            intentList = intent.getParcelableArrayListExtra("pointsList");
            counter = intent.getIntExtra("counter", 0);
            competitor_username = intent.getStringExtra("competitor_username");
            feedback = intent.getExtras().getInt("feedback");
            readyToRun = true;

            text.setText("Ready to run");
            startBtn.setEnabled(true);
            startBtn.setAlpha(1);
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
    public void onMessageReceived(MessageEvent messageEvent) {

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            readyToRun = true;
        }

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
