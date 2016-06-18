package com.example.ingebode.googlemapsproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.example.ingebode.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.ingebode.googlemapsproject.models.Route;
import com.example.ingebode.googlemapsproject.models.User;
import com.example.ingebode.googlemapsproject.models.UserRouteRelation;
import com.firebase.client.*;
import com.firebase.ui.FirebaseListAdapter;

public class CompetitorChoice extends Activity {
    public static double lat1,long1,lat2,long2;
    public static String route_id,user_id,competitor_id, point_collection_id;
    public static boolean createNewRoute;
    public static int feedback=1;
    public static String route_name,competitor_username;

    FirebaseListAdapter mAdapter;
    ArrayList<String> list;

    Firebase userRef = new Firebase(Config.USER_URL);

    //List<String> listUserIds;

    FirebaseArray listUsersId;
    FirebaseArray listUserNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competitor_choice);


        //Google Analytics
       /* Tracker t = ((fitnessApp) getApplication()).getTracker(fitnessApp.TrackerName.APP_TRACKER);
        t.setScreenName("CompetitorChoice");
        t.send(new HitBuilders.ScreenViewBuilder().build()); */

        final ListView listview = (ListView) findViewById(R.id.UserlistView1);

        Intent intent = getIntent();
        lat1=intent.getExtras().getDouble("LAT1");
        long1=intent.getExtras().getDouble("LONG1");
        lat2=intent.getExtras().getDouble("LAT2");
        long2=intent.getExtras().getDouble("LONG2");
        user_id=intent.getStringExtra("USER_ID");
        route_id=intent.getStringExtra("ROUTE_ID");
        createNewRoute=intent.getExtras().getBoolean("CREATENEWROUTE");
        route_name=intent.getStringExtra("ROUTE_NAME");
        point_collection_id = intent.getStringExtra("POINT_COLLECTION_ID");

        Firebase ref = new Firebase(Config.FIREBASE_URL).child("RouteRelations").child(route_id);

        mAdapter = new FirebaseListAdapter<UserRouteRelation>(this, UserRouteRelation.class, android.R.layout.activity_list_item, ref) {
            @Override
            protected void populateView(View view, UserRouteRelation ur, int i) {
                ((TextView)view.findViewById(android.R.id.text1)).setText(ur.getUsername());
            }
        };
        listview.setAdapter(mAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserRouteRelation relation = (UserRouteRelation) mAdapter.getItem(position);
                if (relation != null) {
                    competitor_id = relation.getUser_id();
                    competitor_username = relation.getUsername();
                    passIntent();
                }
            }
        });

    }

public void passIntent(){
        Intent intent2 = new Intent(this, FeedbackChoice.class);
        intent2.putExtra("LAT1", lat1);
        intent2.putExtra("LONG1", long1);
        intent2.putExtra("LAT2", lat2);
        intent2.putExtra("LONG2", long2);
        intent2.putExtra("USER_ID", user_id);
        intent2.putExtra("COMPETITOR_ID", competitor_id);
        intent2.putExtra("ROUTE_ID", route_id);
        intent2.putExtra("CREATENEWROUTE",createNewRoute);
        intent2.putExtra("FEEDBACK", feedback);
        intent2.putExtra("ROUTE_NAME", route_name);
        intent2.putExtra("COMPETITOR_USERNAME", competitor_username);
        intent2.putExtra("POINT_COLLECTION_ID", point_collection_id);
        startActivity(intent2);
    }

    @Override
    protected void onStart(){
        super.onStart();
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        //GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
