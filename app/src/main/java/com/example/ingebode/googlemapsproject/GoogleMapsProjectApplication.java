package com.example.ingebode.googlemapsproject;

import com.firebase.client.Firebase;

/**
 * Created by ingebode on 22/05/16.
 *
 * Includes one-time initialization of Firebase related code
 */

public class GoogleMapsProjectApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /* Initialize Firebase */
        Firebase.setAndroidContext(this);
        /* Enable disk persistence  */
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
