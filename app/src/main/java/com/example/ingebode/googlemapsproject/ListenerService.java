package com.example.ingebode.googlemapsproject;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.ingebode.googlemapsproject.models.History;
import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ingeborgoftedal on 17/07/16.
 */
public class ListenerService extends WearableListenerService {

    private static final String MOBILE_DATA_PATH = "/mobile_data";
    GoogleApiClient mGoogleApiClient;

    File file;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("ListenerServiceMobile", "onCreate" + "");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        Log.v("onDataChanged mobile", "onCreate" + "");

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/txt")) {
                // Get the Asset object

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset asset = dataMapItem.getDataMap().getAsset("com.example.company.key.TXT");

                Log.v("onDataChanged","ListenerService" + "");

                new ConvertAssetAsyncTask().execute(asset);

            }
        }
    }

    private class ConvertAssetAsyncTask extends AsyncTask<Asset, Void, InputStream> {

        @Override
        protected InputStream doInBackground(Asset... params) {
            Log.v("doInBackGround","startWearActivity" + "");

            if (params.length > 0) {

                Asset asset = params[0];

                // Convert asset into a file descriptor and block until it's ready
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).await().getInputStream();
                mGoogleApiClient.disconnect();

                if (assetInputStream == null) { return null; }

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
                final File file = new File(dir, "testing.txt");

                if (file.exists()) {
                    file.delete();
                    Log.v("file deleted", "ListenerService" + "");
                }
                try {
                    file.createNewFile();
                    Log.v("file created", "ListenerService" + "");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {

                    FileOutputStream fOut = new FileOutputStream(file);
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                        fOut.write(data, 0, nRead);
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
        public void readFile(File file){
            System.out.println("reading file");
            String distanceString = "";
            String avg_speedString = "", top_speedString = "", timesInfront = "", timesBehind = "";
            String time = "";


            try (FileInputStream fis = new FileInputStream(file)) {
                System.out.println("Total file size to read (in bytes) : "
                        + fis.available());

                String s = "";

                int content;
                while ((content = fis.read()) != -1) {
                    s += (char)content;

                }

                List<String> historyList = Arrays.asList(s.split(","));

                for(int i = 0; i < historyList.size(); i++){
                    Log.v("historyList " + i," " + historyList.get(i));
                }

                distanceString = historyList.get(1);
                time = historyList.get(2);
                avg_speedString =  historyList.get(3);
                top_speedString =  historyList.get(4);
                timesBehind = historyList.get(5);
                timesInfront = historyList.get(6);


                History history = new History("", "", distanceString, avg_speedString, top_speedString, time, timesBehind, timesInfront);

                Firebase historyRef = new Firebase(Config.HISTORY_URL);
                historyRef.push().setValue(history);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }

            Intent intent2 = new Intent(getApplicationContext(), Finish.class);
            startActivity(intent2);

        }
    }}