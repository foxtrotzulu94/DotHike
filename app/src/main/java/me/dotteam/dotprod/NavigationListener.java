package me.dotteam.dotprod;

import me.dotteam.dotprod.hw.SensorListenerInterface;

/**
 * Created by EricTremblay on 15-11-13.
 */
public class NavigationListener implements SensorListenerInterface {

    NavigationActivity mActivity;

    public NavigationListener(NavigationActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public void update(HikeSensors hikesensors, double value) {
        switch (hikesensors) {
            case TEMPERATURE:
                break;
            case HUMIDITY:
                break;
            case PRESSURE:
                break;
            case PEDOMETER: {
                mActivity.updateStepCount(String.valueOf(value));
                break;
            }
            case MAGNETOMETER:
                break;
        }
    }
}
