package me.dotteam.dotprod.hw;

import android.util.Log;

import me.dotteam.dotprod.hw.SensorListenerInterface;

/**
 * Created by EricTremblay on 15-11-04.
 */
public class TestSensorListener implements SensorListenerInterface {
    private final String TAG = "TestSensorListener";

    @Override
    public void update(HikeSensors hikesensors, double value) {
        switch (hikesensors) {
            case TEMPERATURE: {
                Log.d(TAG, "Temperature: " + String.valueOf(value));
                break;
            }
            case HUMIDITY: {
                Log.d(TAG, "Humidity: " + String.valueOf(value));
                break;
            }
            case PRESSURE: {
                Log.d(TAG, "Pressure: " + String.valueOf(value));
                break;
            }
            case PEDOMETER: {
                Log.d(TAG, "Step Count: " + String.valueOf(value));
                break;
            }
            case MAGNETOMETER: {
                Log.d(TAG, "Magnetometer: " + String.valueOf(value));
                break;
            }
            default: {
                Log.d(TAG, "Default case called");
                break;
            }
        }
    }
}
