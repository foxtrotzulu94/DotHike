package me.dotteam.dotprod;

import android.content.Intent;
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
    private FragmentManager fm = getSupportFragmentManager();

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
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
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

        // Shows Setting Dialogue
        mLinearLayoutSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DSettingsFragment dSettingsFragment = new DSettingsFragment();
                dSettingsFragment.show(fm, "Settings Dialog");
                //TODO: Remove later
                HikeDataDirector mHDD = HikeDataDirector.getInstance(HomeActivity.this);
                mHDD.testStorage();
                Toast.makeText(HomeActivity.this,"Testing Storage Setup", Toast.LENGTH_LONG).show();
            }
        });

        mLinearLayoutSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NOTE: Uncomment to cause an unconditional exception.
                //Integer integers[] = new Integer[Integer.MAX_VALUE];
            }
        });

        mLinearLayoutPastHikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentPastHike = new Intent(HomeActivity.this, PastHikesActivity.class);
                startActivity(intentPastHike);
                System.gc();
            }
        });

    }

}
