package com.example.ingebode.googlemapsproject;

import android.util.Log;

import com.example.ingebode.googlemapsproject.models.Route;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ingebode on 30/03/16.
 */
public class FirebaseHandler {

    Firebase root = new Firebase(Config.FIREBASE_URL);
    Firebase routeRoot = root.child("routes");

    List<Route> readRoutes(){
        final List<Route>resultList = new ArrayList<>();
        routeRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("There are: ", dataSnapshot.getChildrenCount() + "routes");
                for(DataSnapshot postSnapShot: dataSnapshot.getChildren()){
                    Route route = postSnapShot.getValue(Route.class);
                    resultList.add(route);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("Failed ", "to read");
            }
        });

        return resultList;
    }
    public void cleanFirebase(String path){

    }
    //Getting all userids from users that ran a specific route
    List<Integer> readUsers(int routeid){
        List<Integer> resultList = new ArrayList<Integer>();

        return resultList;
    }

}
