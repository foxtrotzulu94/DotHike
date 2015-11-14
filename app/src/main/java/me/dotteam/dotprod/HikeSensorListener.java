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
        switch (hikesensors) {
            case TEMPERATURE: {
                Log.d(TAG, "Temperature: " + String.valueOf(value));
                owner.updateTemperature(String.valueOf(value));
                break;
            }
            case HUMIDITY: {
                Log.d(TAG, "Humidity: " + String.valueOf(value));
                owner.updateHumidity(String.valueOf(value));
                break;
            }
            case PRESSURE: {
                Log.d(TAG, "Pressure: " + String.valueOf(value));
                owner.updatePressure(String.valueOf(value));
                break;
            }
            case PEDOMETER:
                break;
            case MAGNETOMETER:
                break;
            default:{
                break;
            }
        }

    }
}