package com.example.ingebode.wearable;

import android.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ingebode on 24/05/16.
 */
public class SimpleRow {

    final List<Fragment> columns = new ArrayList<Fragment>();

    public SimpleRow(Fragment... fragments) {
        for (Fragment f : fragments) {
            add(f);
        }
    }

    public void add(Fragment f) {
        columns.add(f);
    }

    Fragment getColumn(int i) {
        return columns.get(i);
    }

    public int getColumnCount() {
        return columns.size();
    }
}