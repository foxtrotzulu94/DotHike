package me.dotteam.dotprod;

/**
 * Created by as on 2015-10-23.
 */

public interface SensorListenerInterface {

    public enum HikeSensors{
        TEMPERATURE, HUMIDITY, PRESSURE, PEDOMETER, MAGNETOMETER
    }

    void update(HikeSensors hikesensors, float value);

}
