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

public class SensorsActivity extends AppCompatActivity {

    private Button mButtonDone;
    private CompassAnimator mCompassAnimator;
    private CompassView mCompassView;
    private HikeHardwareManager
     mHHM;

    private class CompassAnimator extends Thread{

        float currentDegrees = 0.0f;
        float finalDegrees = 0.0f;
        float dampingPercentage = 0.05f;
        boolean runningThread =false;

        @Override
        public void run(){
            runningThread =true;
            while(runningThread){
                if (currentDegrees!=finalDegrees){
                    currentDegrees = lerp(currentDegrees,finalDegrees,dampingPercentage);
                    updateUI(currentDegrees);
                }
                else{
                    runningThread = false;
                }

                try{
                    sleep(34); //30 FPS, no compass needs to be at 60...
                }
                catch (InterruptedException e){
                    runningThread = false;
                }

            }
        }

        private float lerp(float start, float end, float percentage){
            return start+( percentage*(end-start)  );
        }

        public void updateCompass(double value){
            //Tell the animator thread to begin
            if(mCompassAnimator!=null){
                mCompassAnimator.setNewValue((float) value);
            }
        }

        private void updateUI(final float value){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCompassView.setDegrees(value);
                }
            });
        }

        public void setNewValue(float newValue){
            finalDegrees = newValue % 360;
            if(!isAlive() && !runningThread)
                this.start();
        }

        public void setAndStop(float newValue){
            updateUI(newValue);
            runningThread = false;
        }

        public void stopAnimation(){
            setAndStop(finalDegrees);
        }
    }


    private void setMemberIDs() {
        mButtonDone = (Button) findViewById(R.id.buttonDone);

        mCompassView = (CompassView) findViewById(R.id.compass);
        mCompassView.setRangeDegrees(180);
        mCompassView.setBackgroundColor(Color.BLACK);
        mCompassView.setLineColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setMarkerColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setTextColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setShowMarker(true);
        mCompassView.setTextSize(40);
        mCompassView.setDegrees(0);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);
        setMemberIDs();

        // Instantiate HikeHardwareManager
        mHHM = HikeHardwareManager.getInstance(this);

        // Start SensorTag connection and pedometer
        mHHM.startSensors(this);

        mCompassAnimator = new CompassAnimator();

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
    protected void onStop() {
        super.onStop();

        // Remove Listener from HHM
        mHHM.stopCompass();
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
