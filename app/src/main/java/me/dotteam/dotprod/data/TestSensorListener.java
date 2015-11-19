package me.dotteam.dotprod.data;

import android.util.Log;

import me.dotteam.dotprod.hw.SensorListenerInterface;

/**
 *
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
            case PRESSURE:{
                Log.d(TAG, "Pressure: " + String.valueOf(value));
            }
            case COMPASS:{
                Log.d(TAG, "Magnetometer: " + String.valueOf(value));
            }
            default:{
                Log.d(TAG,String.format("Unrecognized value %s: %s",hikesensors.toString(),Double.toString(value)));
            }
        }
    }
}