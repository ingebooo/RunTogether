package com.example.ingebode.googlemapsproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.ingebode.R;
import com.example.ingebode.googlemapsproject.models.User;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ingebode on 28/04/16.
 */
public class LoginActivity extends FirebaseLoginBaseActivity {

    String name;
    Firebase firebaseRef;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_login);

        firebaseRef = new Firebase(Config.FIREBASE_URL);

        loginBtn = (Button)findViewById(R.id.login_button);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showFirebaseLoginPrompt();
            }
        });

    }
    @Override
    protected void onStart(){
        super.onStart();

        //setEnabledAuthProvider(AuthProviderType.FACEBOOK);
        setEnabledAuthProvider(AuthProviderType.GOOGLE);
    }
    // google client id : 307559296197-4i3oquerj1c5fjfdcf5mq6eda1s2cufj.apps.googleusercontent.com
    //google secret: FsB_aOevnQxJV05-KFkBNAg7

    @Override
    protected Firebase getFirebaseRef() {
        return firebaseRef;
    }

    @Override
    protected void onFirebaseLoginProviderError(FirebaseLoginError firebaseError) {

    }

    @Override
    protected void onFirebaseLoginUserError(FirebaseLoginError firebaseError) {

    }
    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        // TODO: Handle successful login


        //Map<String, String> map = new HashMap<String, String>();
        //map.put("provider", authData.getProvider());
        if(authData.getProviderData().containsKey("displayName")){
           // map.put("username", authData.getProviderData().get("displayName").toString());
        }


 //       Log.v("provider", "" + authData.getProvider());
//        Log.v("username", ""+ authData.getProviderData().get("username").toString());

        Intent intent = new Intent(this, RouteChoice.class);

        String name = authData.getProviderData().get("displayName").toString();
        String user_id = authData.getUid();

        User user = new User(name, user_id);
        firebaseRef.child("users").child(user_id).setValue(user);

        intent.putExtra("USER_ID", user_id);
        intent.putExtra("USERNAME", name);
        startActivity(intent);
    }

    @Override
    public void onFirebaseLoggedOut() {
        // TODO: Handle logout
    }
}