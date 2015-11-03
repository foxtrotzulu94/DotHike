package me.dotteam.dotprod;

import android.util.Log;

/**
 * Created by as on 2015-10-23.
 */
public class TestSensorListener implements SensorListenerInterface {
    String TAG = "SensorListener";

    @Override
    public void update(HikeSensors hikesensors, double value) {
        switch (hikesensors) {
            case TEMPERATURE: {
                Log.d(TAG, "Temperature: " + String.valueOf(value));
            }
            case HUMIDITY: {
                Log.d(TAG, "Humidity: " + String.valueOf(value));
            }
        }
    }
}