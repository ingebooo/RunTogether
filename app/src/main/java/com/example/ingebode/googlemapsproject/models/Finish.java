package com.example.ingebode.googlemapsproject.models;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ingebode.R;
import com.example.ingebode.googlemapsproject.RouteChoice;
import com.example.ingebode.googlemapsproject.Welcome_activity;
import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ingebode on 15/03/16.
 */
public class Finish extends Activity {
        String user_id,route_id, username, route_name;
        int user_repetition;
        boolean createNewRoute;
    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;
    Button exit, returnBtn;
    TextView goodJob;

    Firebase newRouteRef;
    String ref;
        ProgressDialog progressDialog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_finish);

            myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
            myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
            myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");

            returnBtn = (Button)findViewById(R.id.return_btn);
            returnBtn.setTypeface(myFontLight);

            goodJob = (TextView)findViewById(R.id.goodJob);
            goodJob.setTypeface(myFontMedium);

            getDataFromIntent();

            if(createNewRoute == true) {
                newRouteRef = new Firebase(ref);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("route_name", route_name);
                newRouteRef.updateChildren(map);
            }
        }

    public void getDataFromIntent(){
        Intent intent = getIntent();
        user_id = intent.getStringExtra("USER_ID");
        route_id = intent.getStringExtra("ROUTE_ID");
        createNewRoute=intent.getExtras().getBoolean("CREATENEWROUTE");
        user_repetition = intent.getExtras().getInt("REPETITION");
        username = intent.getStringExtra("USERNAME");
        ref = intent.getStringExtra("NEWROUTEREF");
        route_name = intent.getStringExtra("ROUTE_NAME");
    }


    //if user presses the Return to Login button
    public void returnToLogin(View v){
        Intent intent = new Intent(this, Welcome_activity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("USER_ID", user_id);
        startActivity(intent);
    }

    //if user presses the Exit App button
    public void exitApp2(View v){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //System.exit(0);
    }
}