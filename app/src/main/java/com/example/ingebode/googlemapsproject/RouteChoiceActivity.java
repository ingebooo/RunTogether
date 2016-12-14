package com.example.ingebode.googlemapsproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ingebode.R;


import com.example.ingebode.googlemapsproject.models.Route;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

import java.util.ArrayList;

/**
 * Created by ingebode on 09/03/16.
 */
public class RouteChoiceActivity extends Activity{


    TextView usernameTextView;
    Firebase firebaseRef;

    FirebaseListAdapter mAdapter;
    ArrayList<String> list;
    ListView routeList;
    Route r = null;

    private String user_id;
    private String route_name;
    private String route_id, point_collection_id;
    double lat1, long1, lat2, long2;
    String username;
    String competitor_username;
    Typeface myFontMedium;
    Typeface myFontLight;
    Typeface myFontBold;

    boolean createNewRoute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");

        setContentView(R.layout.route_choice);

        TextView welcome = (TextView)findViewById(R.id.welcome);
        welcome.setTypeface(myFontBold);



        firebaseRef = new Firebase(Config.FIREBASE_URL);
        Firebase routeRef = firebaseRef.child("routes");

        list = new ArrayList<String>();

        routeList = (ListView)findViewById(R.id.RoutelistView1);
        //usernameTextView = (TextView)findViewById(R.id.name);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        user_id = intent.getStringExtra("USER_ID");

        mAdapter = new FirebaseListAdapter<Route>(this, Route.class, R.layout.custom_list, routeRef) {
            @Override
            protected void populateView(View view, Route r, int i) {

                TextView routeNameView = (TextView) view.findViewById(R.id.route_name);
                TextView created_by = (TextView) view.findViewById(R.id.created_by);
                TextView by = (TextView) view.findViewById(R.id.by);
                created_by.setText("user"+(i+1));
                created_by.setTypeface(myFontLight);
                by.setTypeface(myFontLight);

                routeNameView.setText("route"+(i+1));
                routeNameView.setTypeface(myFontMedium);

            }
        };

        routeList.setAdapter(mAdapter);

        routeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Route selectedRoute = (Route) mAdapter.getItem(position);
                if (selectedRoute != null) {
                    lat1 = selectedRoute.getStart_latitude();
                    long1 = selectedRoute.getStart_longitude();
                    lat2 = selectedRoute.getFinish_latitude();
                    long2 = selectedRoute.getFinish_longitude();
                    route_id = selectedRoute.getRoute_id();
                    route_name = selectedRoute.getRoute_name();
                    point_collection_id = selectedRoute.getPoint_collection_id();

                    Log.v("RouteChoiceActivity: ", "point collection id " + point_collection_id);
                    passIntent();
                }
            }
        });

    }
    //if user presses the create new route button
    public void createNewRoute(View v){

        createNewRoute = true;
        EditText editText = (EditText) findViewById(R.id.editTextRouteName);
        route_name = editText.getText().toString();
        editText.setTypeface(myFontLight);



        if (route_name.isEmpty()){
            Toast.makeText(getApplicationContext(), "Please insert a route name", Toast.LENGTH_SHORT).show();
        }
        else if (list.contains(route_name)){
            Toast.makeText(getApplicationContext(), "Sorry,route name already exists!", Toast.LENGTH_SHORT).show();
        }else {
            passIntentNewRoute();
        }
    }
    public void passIntent(){
        Intent intent = new Intent(this, CompetitorChoiceActivity.class);
        intent.putExtra("LAT1", lat1);
        intent.putExtra("LAT2", lat2);
        intent.putExtra("LONG1", long1);
        intent.putExtra("LONG2", long2);
        intent.putExtra("USER_ID", user_id);
        intent.putExtra("ROUTE_ID", route_id);
        intent.putExtra("ROUTE_NAME", route_name);
        intent.putExtra("CREATENEWROUTE", createNewRoute);
        intent.putExtra("USERNAME", username);
        intent.putExtra("POINT_COLLECTION_ID", point_collection_id);
        intent.putExtra("COMPETITOR_USERNAME", competitor_username);
        startActivity(intent);
    }
    public void passIntentNewRoute(){
        Intent intent = new Intent(this, NewRouteActivity.class);
        intent.putExtra("CREATENEWROUTE", createNewRoute);
        intent.putExtra("ROUTE_NAME", route_name);
        intent.putExtra("USERNAME", username);
        intent.putExtra("USER_ID", user_id);
        startActivity(intent);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

}

