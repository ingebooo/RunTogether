package com.example.ingebode.googlemapsproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.ingebode.R;
/**
 * Created by ingebode on 14/03/16.
 */
public class StatusFragment extends Fragment {

    private String title;
    private int page;

    public static double lat1,long1,lat2,long2;
    public static int user_id,competitor_id,route_id,feedback;
    public static boolean createNewRoute;



    // newInstance constructor for creating fragment with arguments
    public static StatusFragment newInstance(int page, String title) {
        StatusFragment statusFragment = new StatusFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        statusFragment.setArguments(args);
        return statusFragment;
    }


    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
   /*     TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        tvLabel.setText(page + " -- " + title); */
        TextView textView1 = (TextView) view.findViewById(R.id.usernameTextView);
        //textView1.setText(db.getUsername(user_id));

        textView1.setText("test");

        TextView textView2 = (TextView) view.findViewById(R.id.user_idTextView);
        textView2.setText(Integer.toString(user_id));

        TextView textView3 = (TextView) view.findViewById(R.id.route_idTextView);
        if (createNewRoute == true) {
            textView3.setText("New Route!");
        } else {
            //textView3.setText(db.getRouteName((route_id)));
            textView3.setText("test");
        }

        TextView textView4 = (TextView) view.findViewById(R.id.feedbackTextView);
        textView4.setText(Integer.toString(feedback));

        TextView textView5 = (TextView) view.findViewById(R.id.competitorTextView);
        if (createNewRoute == true) {
            textView5.setText("New Route!");
        } else {
            //textView5.setText(db.getUsername(competitor_id));
            textView5.setText("test");
        }

        String start=Double.toString(lat1)+','+Double.toString(long1);
        TextView textView6 = (TextView) view.findViewById(R.id.route_start);
        if (createNewRoute == true) {
            textView6.setText("New Route!");
        } else {
            textView6.setText(start);
        }

        String finish=Double.toString(lat2)+','+Double.toString(long2);
        TextView textView7 = (TextView) view.findViewById(R.id.route_finish);
        if (createNewRoute == true) {
            textView7.setText("New Route!");
        } else {
            textView7.setText(finish);
        }
        return view;
    }
    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

        Intent intent = getActivity().getIntent();
        lat1=intent.getDoubleExtra("LAT1", 0);
        long1=intent.getDoubleExtra("LONG1", 0);
        lat2=intent.getDoubleExtra("LAT2", 0);
        long2=intent.getDoubleExtra("LONG2", 0);
        user_id=intent.getExtras().getInt("USER_ID");
        competitor_id=intent.getExtras().getInt("COMPETITOR_ID");
        route_id=intent.getExtras().getInt("ROUTE_ID");
        createNewRoute=intent.getExtras().getBoolean("CREATENEWROUTE");
        feedback=intent.getExtras().getInt("FEEDBACK");


    }
}
