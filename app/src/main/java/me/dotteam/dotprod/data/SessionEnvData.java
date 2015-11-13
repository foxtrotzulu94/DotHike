package me.dotteam.dotprod.data;


import java.util.LinkedList;
import java.util.List;

/**
 * Data structure class to maintain an extended list of recorded samples in a session
 * Presently not in use.
 */
public class SessionEnvData extends EnvData {

    /**
     * 
     */
    protected List<Double> recordedTemp;

    /**
     * 
     */
    protected List<Double> recordedPressure;

    /**
     * 
     */
    protected List<Double> recordedHumidity;

    protected List<Long> timeStamps;

    /**
     * Default constructor
     */
    public SessionEnvData() {
        recordedHumidity = new LinkedList<>();
        recordedPressure = new LinkedList<>();
        recordedTemp = new LinkedList<>();
        timeStamps = new LinkedList<>();
    }

    @Override
    public String toString() {
        return String.format("%s\nCollected Samples: Temp %s Humidity %s Pressure %s\nTimestamps: %s",
                super.toString(), recordedTemp.size(), recordedPressure.size(), recordedHumidity.size(),
                timeStamps.size());
    }

    @Override
    public void updatePressure(double newSample) {
        super.updatePressure(newSample);
        recordedPressure.add(newSample);
        timeStamps.add(System.currentTimeMillis());
    }

    @Override
    public void updateHumidity(double newSample) {
        super.updateHumidity(newSample);
        recordedHumidity.add(newSample);
        timeStamps.add(System.currentTimeMillis());
    }

    @Override
    public void updateTemp(double newSample) {
        super.updateTemp(newSample);
        recordedTemp.add(newSample);
        timeStamps.add(System.currentTimeMillis());
    }

    public List<Long> getTimestamps(){
        return timeStamps;
    }
}