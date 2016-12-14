package com.example.ingebode.wearable;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ingeborgoftedal on 17/07/16.
 */
public class ListenerService extends WearableListenerService {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    private static final String WEARABLE_DATA_PATH2 = "/wearable_data";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    ArrayList<DataMap> pointsList;
    ArrayList<DataMap> pointsList2;
    GoogleApiClient mGoogleApiClient;
    private ArrayList<Point> filePoints;
    private String route_id;
    private String user_id;
    double counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("ListenerService", "onCreate" + "");



        if(pointsList != null){
            Log.v("ListenerService", "pointsList != null" + "");
            onPointsReceived(pointsList, filePoints);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        /*

        requestPermissions(getApplicationContext(),
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                100); */
    }



    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v("wearable","Listenerservive, onDataChanged" + "");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {
                    DataMapItem dataItem = DataMapItem.fromDataItem(event.getDataItem());
                    pointsList = dataItem.getDataMap().getDataMapArrayList("pointsList");
                    onPointsReceived(pointsList, null);
                }

            }
        }
    }

    public void addList(ArrayList<DataMap> pointsList, ArrayList<DataMap> pointsList2){
        for(DataMap map : pointsList2){
            pointsList.add(map);
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

            counter = Double.parseDouble(pointsList.get(0));
            route_id = pointsList.get(2);
            user_id = pointsList.get(1);


            for(int i = 3; i < pointsList.size(); i++){

                s2 = pointsList.get(i);
                pList = Arrays.asList(s2.split("!"));

                double lat = Double.parseDouble(pList.get(0));
                double lonng = Double.parseDouble(pList.get(1));

                Point point = new Point(lat, lonng);
                filePoints.add(point);
            }

            onPointsReceived(null, filePoints);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    public void onPointsReceived(ArrayList<DataMap> list, ArrayList<Point> list2){

        ArrayList<Intent> intentList = new ArrayList<Intent>();

        if(list != null) {

            for (DataMap map : list) {
                Intent intent = new Intent();

                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("user_id", map.getString("user_id"));
                intent.putExtra("route_id", map.getString("route_id"));
                intent.putExtra("latitude", map.getDouble("latitude"));
                intent.putExtra("longitude", map.getDouble("longitude"));
                intent.putExtra("counter", map.getInt("counter"));
                intent.putExtra("point_number", map.getInt("point_number"));
                intentList.add(intent);
            }
        } else if (list2 != null){
            for (Point point : list2){

                Intent intent = new Intent();

                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("user_id", user_id);
                intent.putExtra("route_id", route_id);
                intent.putExtra("latitude", point.getLatitude());
                intent.putExtra("longitude", point.getLongitude());
                intent.putExtra("counter", counter);
                intentList.add(intent);
            }
        }

        Intent dataIntent = new Intent();
        dataIntent.setAction(Intent.ACTION_SEND);
        dataIntent.putExtra("feedback", list.get(0).getInt("feedback"));
        dataIntent.putExtra("competitor_username", list.get(0).getString("competitor_username"));
        dataIntent.putExtra("username", list.get(0).getString("username"));
        dataIntent.putParcelableArrayListExtra("pointsList", intentList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Log.v("ListenerService", "start actitivty");
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
            //readyToRun = true;
        }

    }
}