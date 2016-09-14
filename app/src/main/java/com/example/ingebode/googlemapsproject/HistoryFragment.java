package com.example.ingebode.googlemapsproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.ingebode.R;

import com.example.ingebode.googlemapsproject.models.History;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ingebode on 14/03/16.
 */
public class HistoryFragment extends Fragment {
    private String title;
    private int page;

    public static String user_id;

    // newInstance constructor for creating fragment with arguments
    public static HistoryFragment newInstance(int page, String title) {
        HistoryFragment historyFragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        historyFragment.setArguments(args);
        return historyFragment;
    }
    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        //TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        //tvLabel.setText(page + " -- " + title);
        final ListView listview1 = (ListView) view.findViewById(R.id.historyListView1);
        final ListView listview2 = (ListView) view.findViewById(R.id.historyListView2);
        final ListView listview3 = (ListView) view.findViewById(R.id.historyListView3);
        final ListView listview4 = (ListView) view.findViewById(R.id.historyListView4);


        //read from History Database Table
        //List<History> listHistory = db.getHistoryByUserId(user_id);
        List<History> listHistory = new ArrayList<>();
        //String user_id,String route_id,String date, double distance,double avg_speed

        //listHistory.add(new History("dsadas", "rerwe", "123", 2.3, 1.2));


        //Populating lists
        final ArrayList<String> list1 = new ArrayList<String>();
        final ArrayList<String> list2 = new ArrayList<String>();
        final ArrayList<String> list3 = new ArrayList<String>();
        final ArrayList<String> list4 = new ArrayList<String>();
        for (int i=0; i < listHistory.size(); ++i){
            //list1.add(db.getRouteName(listHistory.get(i).getRoute_id()));

            Date date = new Date();
            list1.add("penis");
            list2.add(listHistory.get(i).getTime());
            list3.add(String.valueOf(listHistory.get(i).getDistance()));
            list4.add(String.valueOf(listHistory.get(i).getAvg_speed()));
        }
        //Creating ArrayAdapters from lists
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(),R.layout.custom_layout, list1);
        listview1.setAdapter(adapter1);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(),R.layout.custom_layout, list2);
        listview2.setAdapter(adapter2);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getActivity(),R.layout.custom_layout, list3);
        listview3.setAdapter(adapter3);
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(getActivity(),R.layout.custom_layout, list4);
        listview4.setAdapter(adapter4);

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

        Intent intent = getActivity().getIntent();
        user_id = intent.getStringExtra("USER_ID");


    }
}
