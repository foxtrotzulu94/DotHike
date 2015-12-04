package me.dotteam.dotprod;

import android.util.Log;

import me.dotteam.dotprod.hw.SensorListenerInterface;

/**
 * Created by as on 2015-10-23.
 */
//TODO: This should probably be an inner class of the HikeViewPager
public class HikeSensorListener implements SensorListenerInterface {

    private HikeViewPagerActivity owner;

    String TAG = "HikeSensorListener";

    public HikeSensorListener(HikeViewPagerActivity owner) {
        this.owner = owner;
    }

    @Override
    public void update(HikeSensors hikesensors, double value) {
        Log.d(TAG, hikesensors.toString() + ": " + String.valueOf(value));
        switch (hikesensors) {
            case TEMPERATURE: {
                owner.updateTemperature(value);
                break;
            }
            case HUMIDITY: {
                owner.updateHumidity(value);
                break;
            }
            case PRESSURE: {
                owner.updatePressure(value);
                break;
            }
            case PEDOMETER:
                owner.updateStepCount(value);
                break;
            case COMPASS:
                owner.updateCompass(value);
                break;
            default:{
                break;
            }
        }

    }
}
