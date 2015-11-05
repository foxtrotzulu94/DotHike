package me.dotteam.dotprod;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import me.dotteam.dotprod.hw.HikeHardwareManager;

public class EnvCondActivity extends AppCompatActivity {
    private String TAG = "EnvCondActivity";
    private EnvCondListener mSensorListener;


    private Button mButtonBackToMainHike;

    //TextViews of the titles of Displays
    private TextView mTextCurrentHumidity;
    private TextView mTextCurrentTemperature;
    private TextView mTextCurrentPressure;

    //TextViews of containing the Displays
    private TextView mTextDisplayHumidity;
    private TextView mTextDisplayTemperature;
    private TextView mTextDisplayPressure;

    private HikeHardwareManager mHHM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate Called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_env_cond);
        mButtonBackToMainHike = (Button) findViewById(R.id.buttonBack);
        mTextCurrentHumidity = (TextView) findViewById(R.id.textCurHum);
        mTextCurrentTemperature = (TextView) findViewById(R.id.textCurTemp);
        mTextCurrentPressure = (TextView) findViewById(R.id.textCurPress);
        mTextDisplayHumidity = (TextView) findViewById(R.id.textDispHum);
        mTextDisplayTemperature = (TextView) findViewById(R.id.textDispTemp);
        mTextDisplayPressure = (TextView) findViewById(R.id.textDispPress);

        mButtonBackToMainHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMainHike = new Intent(EnvCondActivity.this, HikeActivity.class);
                startActivity(intentMainHike);
            }
        });

        mHHM = HikeHardwareManager.getInstance(this);
        mSensorListener = new EnvCondListener(this);

        }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart called");
        super.onStart();
        mHHM.addListener(mSensorListener);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop Called");
        super.onStop();
        mHHM.removeListener(mSensorListener);
    }

    void updateTemperature(final String temp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextDisplayTemperature.setText(temp);
            }
        });
    }

    void updateHumidity(final String hum) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextDisplayHumidity.setText(hum);
            }
        });
    }

    void updatePressure(final String pressure) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextDisplayPressure.setText(pressure);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_env_cond, menu);
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
