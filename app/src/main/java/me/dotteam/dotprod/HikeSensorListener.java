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
        String valueString = String.valueOf(value);
        Log.d(TAG, hikesensors.toString() + ": " + valueString);
        switch (hikesensors) {
            case TEMPERATURE: {
                owner.updateTemperature(valueString);
                break;
            }
            case HUMIDITY: {
                owner.updateHumidity(valueString);
                break;
            }
            case PRESSURE: {
                owner.updatePressure(valueString);
                break;
            }
            case PEDOMETER:
                owner.updateStepCount(valueString);
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