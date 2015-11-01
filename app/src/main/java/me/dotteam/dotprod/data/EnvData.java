package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * 
 */
public class EnvData {
    /**
     * 
     */
    protected EnvStatistic temperature;

    /**
     * 
     */
    protected EnvStatistic humidity;

    /**
     * 
     */
    protected EnvStatistic pressure;

    /**
     * Default constructor
     */
    public EnvData() {

    }

    public void updateTemp(float newSample){
        temperature.insertSample(newSample);
    }

    public void updateHumidity(float newSample){
        humidity.insertSample(newSample);
    }

    public void updatePressure(float newSample){
        pressure.insertSample(newSample);
    }

    public EnvStatistic getTemperature() {
        return temperature;
    }

    public EnvStatistic getHumidity() {
        return humidity;
    }

    public EnvStatistic getPressure() {
        return pressure;
    }

    /**
     * @return
     */
    public ContentValues getSerializedTemp() {
        return temperature.toStorage();
    }

    /**
     * @return
     */
    public ContentValues getSerializedHumidity() {
        return humidity.toStorage();
    }

    /**
     * @return
     */
    public ContentValues getSerializedPressure() {
        return pressure.toStorage();
    }

}