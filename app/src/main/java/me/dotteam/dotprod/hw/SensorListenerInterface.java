package me.dotteam.dotprod.hw;

/**
 * Created by as on 2015-10-23.
 */

public interface SensorListenerInterface {

    enum HikeSensors{
        TEMPERATURE,
        HUMIDITY,
        PRESSURE,
        PEDOMETER,
        COMPASS
    }

    void update(HikeSensors hikesensors, double value);

}
