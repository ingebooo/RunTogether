package com.example.ingebode.googlemapsproject.models;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.ingebode.R;
import com.example.ingebode.googlemapsproject.RouteChoice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ingebode on 15/03/16.
 */
public class Finish extends Activity {
        String user_id,route_id, username;
        int user_repetition;
        boolean createNewRoute;
        ProgressDialog progressDialog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_finish);

            MediaPlayer mp = new MediaPlayer();
            AssetFileDescriptor descriptor;
            try {
                descriptor = getAssets().openFd("applause.wav");
                mp.setDataSource( descriptor.getFileDescriptor(),descriptor.getStartOffset(), descriptor.getLength() );
                descriptor.close();
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Can't play audio file!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            getDataFromIntent();
        }

    public void getDataFromIntent(){
        Intent intent = getIntent();
        user_id = intent.getStringExtra("USER_ID");
        route_id = intent.getStringExtra("ROUTE_ID");
        createNewRoute=intent.getExtras().getBoolean("CREATENEWROUTE");
        user_repetition = intent.getExtras().getInt("REPETITION");
        username = intent.getStringExtra("USERNAME");
    }


    //if user presses the Return to Login button
    public void returnToLogin(View v){
        Intent intent = new Intent(this, RouteChoice.class);
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