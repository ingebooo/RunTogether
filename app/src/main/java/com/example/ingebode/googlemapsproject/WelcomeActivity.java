package com.example.ingebode.googlemapsproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ingebode.R;
import com.example.ingebode.googlemapsproject.models.UserRouteRelation;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by ingeborgoftedal on 28/09/16.
 */
public class WelcomeActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String user_id;
    private String route_name;
    private String route_id, point_collection_id;
    double lat1, long1, lat2, long2;
    String username;
    String competitor_username;
    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;

    Button new_route, choose_route, log_out;
    TextView welcome;
    private GoogleApiClient mGoogleApiClient;
    private static final String START_ACTIVITY_PATH = "/start-activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        Firebase.setAndroidContext(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");

        new_route = (Button)findViewById(R.id.new_route_btn);
        new_route.setTypeface(myFontLight);
        choose_route = (Button)findViewById(R.id.choose_route_btn);
        choose_route.setTypeface(myFontLight);
        log_out = (Button)findViewById(R.id.log_out_btn);
        log_out.setTypeface(myFontLight);

        welcome = (TextView)findViewById(R.id.welcome);
        welcome.setTypeface(myFontBold);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        user_id = intent.getStringExtra("USER_ID");

/*

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("point_collection_id", "-KU1bzOZqhnZsZwwBTHM");

        Firebase routeref = new Firebase(Config.ROUTE_URL).child("-KU1bzOJGnwVLDn1Up50");
        routeref.updateChildren(map);*/

        //createUserRouteRelation("-KTOuUgCYzXwPu_bYCa3", "106412529072612026794", "Hanna Kahrs","-KTOuUfz3c6jQgV4PNdH");
        //createUserRouteRelation("-KTjyoL9HZ_Q82wZJN0U", "106412529072612026794", "Ida Anderskog", "-KTjyoKwOTRE2hmayDGu");




    }

    private void createUserRouteRelation(String rid, String uid, String uname, String pointid) {

        Firebase reffer = new Firebase(Config.FIREBASE_URL);
        Firebase newReffer = reffer.child("routeRelations");

        Firebase newRefferRouteID = newReffer.child(rid);

        UserRouteRelation relation = new UserRouteRelation(rid, uid, uname, pointid);

        newRefferRouteID.push().setValue(relation);

        Toast.makeText(getApplicationContext(),
                "User route relation created...", Toast.LENGTH_SHORT)
                .show();
    }

    public void createNewRoute(View v){
            passIntentNewRoute();
    }

    public void chooseRoute(View v){

        new StartWearableActivityTask().execute();
        passIntent();
    }
    public void passIntent(){
        Intent intent = new Intent(this, RouteChoiceActivity.class);
        intent.putExtra("USER_ID", user_id);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }
    public void passIntentNewRoute(){
        Intent intent = new Intent(this, NewRouteActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("USER_ID", user_id);
        startActivity(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            Log.v("WelcomeActivity", "StartWearableActivityTask");
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }

    private void sendStartActivityMessage(String node) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e("WelcomeActivity", "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        } else {
                            Log.v("WelcomeActivity", "send start activity task");
                        }
                    }
                }
        );
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

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
}
