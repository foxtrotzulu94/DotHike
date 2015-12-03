package me.dotteam.dotprod.hw;

/**
 * Listener for HikeHardwareManager.
 *
 * Created by as on 2015-10-23.
 */

public interface SensorListenerInterface {

    /**
     * Enum for available sensors
     */
    enum HikeSensors{
        TEMPERATURE,
        HUMIDITY,
        PRESSURE,
        PEDOMETER,
        COMPASS
    }

    /**
     * Method called when a sensor has a new value
     * @param hikesensors Sensor being updated
     * @param value Value of sensor being updated
     */
    void update(HikeSensors hikesensors, double value);

}
