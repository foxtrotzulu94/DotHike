package me.dotteam.dotprod;

import android.util.Log;

import me.dotteam.dotprod.hw.SensorListenerInterface;

/**
 * Created by as on 2015-10-23.
 */
public class HikeSensorListener implements SensorListenerInterface {
    private HikeViewPagerActivity owner;

    String TAG = "HikeSensorListener";

    public HikeSensorListener(HikeViewPagerActivity owner) {
        this.owner = owner;
    }

    @Override
    public void update(HikeSensors hikesensors, double value) {
        String valueString = String.valueOf(value);
        switch (hikesensors) {
            case TEMPERATURE: {
                Log.d(TAG, "Temperature: " + valueString);
                owner.updateTemperature(valueString);
                break;
            }
            case HUMIDITY: {
                Log.d(TAG, "Humidity: " + valueString);
                owner.updateHumidity(valueString);
                break;
            }
            case PRESSURE: {
                Log.d(TAG, "Pressure: " + String.valueOf(value));
                owner.updatePressure(valueString);
                break;
            }
            case PEDOMETER:
                Log.d(TAG, "Step Count: " + String.valueOf(value));
                owner.updateStepCount(valueString);
                break;
            case MAGNETOMETER:
                break;
            default:{
                break;
            }
        }

    }
}