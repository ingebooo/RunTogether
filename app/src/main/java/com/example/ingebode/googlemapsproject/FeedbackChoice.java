package com.example.ingebode.googlemapsproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import com.example.ingebode.R;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class FeedbackChoice extends Activity {
    public static double lat1,long1,lat2,long2;
    public static String route_id,user_id,competitor_id, point_collection_id, competitor_username;
    public static boolean createNewRoute;
    public static int feedback=1;
    public static String route_name,feedback_type="no feedback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_choice);

        //Google Analytics
       /* Tracker t = ((fitnessApp) getApplication()).getTracker(fitnessApp.TrackerName.APP_TRACKER);
        t.setScreenName("FeedbackChoice");
        t.send(new HitBuilders.ScreenViewBuilder().build()); */

        Intent intent = getIntent();
        lat1=intent.getDoubleExtra("LAT1", 0);
        long1=intent.getDoubleExtra("LONG1", 0);
        lat2=intent.getDoubleExtra("LAT2", 0);
        long2=intent.getDoubleExtra("LONG2", 0);
        user_id=intent.getStringExtra("USER_ID");
        competitor_id=intent.getStringExtra("COMPETITOR_ID");
        route_id=intent.getStringExtra("ROUTE_ID");
        createNewRoute=intent.getExtras().getBoolean("CREATENEWROUTE");
        route_name=intent.getStringExtra("ROUTE_NAME");
        point_collection_id = intent.getStringExtra("POINT_COLLECTION_ID");
        competitor_username = intent.getStringExtra("COMPETITOR_USERNAME");
    }



    public void onRadioButtonClicked(View v) {
        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();
        // Check which radio button was clicked
        switch(v.getId()) {
            case R.id.radio1:
                if (checked)
                    feedback=1;
                feedback_type="no feedback";
                break;
            case R.id.radio2:
                if (checked)
                    feedback=2;
                feedback_type="vibration";
                break;
            case R.id.radio3:
                if (checked)
                    feedback=3;
                feedback_type="sound";
                break;
            case R.id.radio4:
                if (checked)
                    feedback=4;
                feedback_type="visual";
                break;
        }
    }

    //if user presses the continue button
    public void feedbackChoice(View v){
        passIntent();
    }

    public void passIntent(){

        //Google Analytics
     /*   Tracker t = ((fitnessApp) getApplication()).getTracker(fitnessApp.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder().setCategory("Feedback Choice").setAction("Feedback").setLabel(feedback_type).build());
*/
        Intent intent2 = new Intent(this, TabsActivity.class);
        intent2.putExtra("LAT1", lat1);
        intent2.putExtra("LONG1", long1);
        intent2.putExtra("LAT2", lat2);
        intent2.putExtra("LONG2", long2);
        intent2.putExtra("USER_ID", user_id);
        intent2.putExtra("COMPETITOR_ID", competitor_id);
        intent2.putExtra("ROUTE_ID", route_id);
        intent2.putExtra("CREATENEWROUTE",createNewRoute);
        intent2.putExtra("ROUTE_NAME", route_name);
        intent2.putExtra("FEEDBACK", feedback);
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
