package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * Data structure to represent the set of Environmental Data Statistics
 */
public class EnvData {
    /**
     * Temperature Statistics Object
     */
    protected EnvStatistic temperature;

    /**
     * Humidity Statistics Object
     */
    protected EnvStatistic humidity;

    /**
     * Pressure Statistics Object
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

    public EnvData(EnvStatistic temperature,EnvStatistic humidity,EnvStatistic pressure){
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
    }

    public void updateTemp(double newSample){
        temperature.insertSample(newSample);
    }

    public void updateHumidity(double newSample){
        humidity.insertSample(newSample);
    }

    public void updatePressure(double newSample){
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
     * Method to obtain the Temperature Statistics ContentValues to be used for storage
     * @return ContentValues object with correct key-value pairs
     */
    public ContentValues getSerializedTemp(int ID) {
        return temperature.toStorage(ID);
    }

    /**
     * Method to obtain the Humidity Statistics ContentValues to be used for storage
     * @return ContentValues object with correct key-value pairs
     */
    public ContentValues getSerializedHumidity(int ID) {
        return humidity.toStorage(ID);
    }

    /**
     * Method to obtain the Pressure Statistics ContentValues to be used for storage
     * @return ContentValues object with correct key-value pairs
     */
    public ContentValues getSerializedPressure(int ID) {
        return pressure.toStorage(ID);
    }

    public boolean isValid(){
        return temperature.isValid() && humidity.isValid() && pressure.isValid();
    }

    public String toString(){
        StringBuilder stringstream = new StringBuilder();
        if(temperature!=null){
            stringstream.append(String.format("Temp: %s\n",temperature.toString()));
        }
        else{ stringstream.append("No Temperature Data\n"); }

        if(humidity!=null){
            stringstream.append(String.format("Humidity: %s\n",humidity.toString()));
        }
        else{ stringstream.append("No Ambient Humidity Data\n"); }

        if(pressure!=null){
            stringstream.append(String.format("Pressure: %s\n",pressure.toString()));
        }
        else{ stringstream.append("No Pressure Data\n"); }


        return stringstream.toString();
    }

}