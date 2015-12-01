package me.dotteam.dotprod;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.redinput.compassview.CompassView;

import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.hw.SensorListenerInterface;

public class SensorsActivity extends AppCompatActivity implements SensorListenerInterface{

    private Button mButtonDone;
    private CompassAnimator mCompassAnimator;
    private CompassView mCompassView;
    private HikeHardwareManager mHHM;

    public void update(HikeSensors hikesensors, double value) {
        switch (hikesensors){

            case TEMPERATURE:
                break;
            case HUMIDITY:
                break;
            case PRESSURE:
                break;
            case PEDOMETER:
                break;
            case COMPASS:
                mCompassAnimator.setNewValue((float) value);
                break;
        }
    }

    private void setMemberIDs() {
        mButtonDone = (Button) findViewById(R.id.buttonDone);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        setMemberIDs();

        mCompassView = (CompassView) findViewById(R.id.compass);
        mCompassView.setRangeDegrees(180);
        mCompassView.setBackgroundColor(Color.BLACK);
        mCompassView.setLineColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setMarkerColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setTextColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setShowMarker(true);
        mCompassView.setTextSize(40);
        mCompassView.setDegrees(0);

        // Instantiate HikeHardwareManager
        mHHM = HikeHardwareManager.getInstance(this);

        // Start SensorTag connection and pedometer
        mHHM.startSensors(this);

        mCompassAnimator = new CompassAnimator(this, mCompassView);

        mButtonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentHome = new Intent(SensorsActivity.this, HomeActivity.class);
                intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentHome);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add Listener to HHM
        mHHM.startCompass();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHHM.addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove Listener from HHM
        mHHM.stopCompass();
        mHHM.removeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mHHM.stopSensors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensors, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
