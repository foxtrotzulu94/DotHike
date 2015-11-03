package me.dotteam.dotprod;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by as on 2015-10-23.
 */
public class EnvCondListener implements SensorListenerInterface {
    private EnvCondActivity owner;

    String TAG = "EnvCondListener";

    public EnvCondListener(EnvCondActivity owner) {
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
            default:{
                break;
            }
        }

    }
}