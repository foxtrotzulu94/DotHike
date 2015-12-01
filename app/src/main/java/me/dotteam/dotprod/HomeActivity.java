package me.dotteam.dotprod;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.HikeDataDirector;

public class HomeActivity extends AppCompatActivity{

    private LinearLayout mLinearLayoutStartHike;
    private LinearLayout mLinearLayoutPastHikes;
    private LinearLayout mLinearLayoutSensors;
    private LinearLayout mLinearLayoutSettings;

    public void setMemberIDs(){
        mLinearLayoutStartHike = (LinearLayout) findViewById(R.id.linearLayoutStartHike);
        mLinearLayoutPastHikes = (LinearLayout) findViewById(R.id.linearLayoutPastHikes);
        mLinearLayoutSensors = (LinearLayout) findViewById(R.id.linearLayoutSensors);
        mLinearLayoutSettings = (LinearLayout) findViewById(R.id.linearLayoutSettings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setMemberIDs();
        setOnClickListeners();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        //Optimize loading of the bitmap
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inScaled = true;
        decodeOptions.inPremultiplied =false;
        decodeOptions.inDither = false;
        LinearLayout homeRoot = (LinearLayout) findViewById(R.id.linlayout_homeroot);
        homeRoot.setBackground(new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(),R.drawable.background_image_white, decodeOptions)));

        //The icons were also optimized into .9.png thanks to
        //https://romannurik.github.io/AndroidAssetStudio/nine-patches.html

        //TODO: Recover and put on settings or something.
        //Show build information for debugging.
        if (BuildConfig.DEBUG) {
            TextView buildField = (TextView) findViewById(R.id.app_build);
            Date buildDate = new Date(BuildConfig.TIMESTAMP);
            Formatter formatter = new Formatter(new StringBuilder(), Locale.US);
            formatter.format("[ %1$s build ] \nBuild: %2$s\nCommitt: %3$s \n[from %4$s]",
                    BuildConfig.BUILD_TYPE,
                    buildDate.toString(),
                    BuildConfig.GIT_COMMIT_INFO,
                    BuildConfig.GIT_BRANCH);
            buildField.setText(formatter.toString());
        }
        Runtime.getRuntime().gc();
        System.gc();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Runtime.getRuntime().gc();
        System.gc();
        finish();
    }

    public void setOnClickListeners(){
        // Intent to HikeActivity
        mLinearLayoutStartHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMainHike = new Intent(HomeActivity.this, HikeViewPagerActivity.class);
                startActivity(intentMainHike);
            }
        });

        //Intent to Sensors
        mLinearLayoutSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSensors = new Intent(HomeActivity.this, SensorsActivity.class);
                startActivity(intentSensors);
            }
        });

        //Intent to Past Hikes
        mLinearLayoutPastHikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentPastHike = new Intent(HomeActivity.this, PastHikesActivity.class);
                startActivity(intentPastHike);
            }
        });

        // Shows Setting Dialogue
        mLinearLayoutSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,HikeSettingsActivity.class));
                Log.d("HikeHome",
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                                getString("example_text", ""));
            }
        });
    }
}
