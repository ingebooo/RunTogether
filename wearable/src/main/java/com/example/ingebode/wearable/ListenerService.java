package com.example.ingebode.wearable;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

/**
 * Created by ingeborgoftedal on 17/07/16.
 */
public class ListenerService extends WearableListenerService {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    ArrayList<DataMap> pointsList;
    GoogleApiClient mGoogleApiClient;
    boolean hasDataChanged = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("ListenerService", "onCreate" + "");
        if(pointsList != null){
            Log.v("ListenerService", "pointsList != null" + "");
            onPointsReceived(pointsList);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        hasDataChanged = true;

        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {}
                DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                pointsList = dataItem.getDataMap().getDataMapArrayList("pointsList");
                onPointsReceived(pointsList);
            }
        }
    }
    public void onPointsReceived(ArrayList<DataMap> list){

        ArrayList<Intent> intentList = new ArrayList<Intent>();
        for(DataMap map : list){
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

        Intent dataIntent = new Intent();
        dataIntent.setAction(Intent.ACTION_SEND);
        dataIntent.putExtra("feedback", list.get(0).getInt("feedback"));
        dataIntent.putExtra("competitor_username", list.get(0).getString("competitor_username"));
        dataIntent.putExtra("username", list.get(0).getString("username"));
        dataIntent.putParcelableArrayListExtra("pointsList", intentList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
    }

    /*@Override
    public void onMessageReceived(MessageEvent messageEvent) {
        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Log.v("Wear", "listenerservice ready to run" + "");
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }*/
}