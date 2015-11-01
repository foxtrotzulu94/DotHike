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
        temperature = new EnvStatistic();
        humidity = new EnvStatistic();
        pressure = new EnvStatistic();
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
    public ContentValues getSerializedTemp(int ID) {
        return temperature.toStorage(ID);
    }

    /**
     * @return
     */
    public ContentValues getSerializedHumidity(int ID) {
        return humidity.toStorage(ID);
    }

    /**
     * @return
     */
    public ContentValues getSerializedPressure(int ID) {
        return pressure.toStorage(ID);
    }

}