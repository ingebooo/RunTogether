package com.example.ingebode.wearable;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.example.ingebode.R;

public class StartActivity extends Activity {

    private TextView mTextView;
    private Typeface myFontMedium;
    private Typeface myFontLight;
    private Typeface myFontBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_start2);

        mTextView = (TextView)findViewById(R.id.text);

        myFontMedium = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Medium.otf");
        myFontLight = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Light.otf");
        myFontBold = Typeface.createFromAsset(getAssets(), "fonts/Gotham-Bold.otf");

        mTextView.setTypeface(myFontBold);


    }
}
