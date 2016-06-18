package com.example.ingebode.wearable;

import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.example.ingebode.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;
    private List<SimpleRow> mRows;

    public SampleGridPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mRows = new ArrayList<SimpleRow>();

        mRows.add(new SimpleRow(cardFragment(R.string.welcome_text, R.string.welcome_text)));
        mRows.add(new SimpleRow(cardFragment(R.string.about_title, R.string.about_text)));
        mRows.add(new SimpleRow(new CustomFragment()));
        mRows.add(new SimpleRow(new CustomMapFragment()));
    }
    private CardFragment cardFragment(int titleRes, int textRes) {
        Resources res = mContext.getResources();
        CardFragment fragment =
                CardFragment.create(res.getText(titleRes), res.getText(textRes));
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }
    @Override
    public android.app.Fragment getFragment(int row, int col) {
        SimpleRow adapterRow = mRows.get(row);
        return adapterRow.getColumn(col);
    }

    @Override
    public int getRowCount() {
        return mRows.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return mRows.get(rowNum).getColumnCount();
    }
}