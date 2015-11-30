package me.dotteam.dotprod.data;


import java.util.LinkedList;
import java.util.List;

/**
 * Data structure class to maintain an extended list of recorded samples in a session
 * Presently not in use.
 */
public class DataListStatistic extends DataStatistic {

    /**
     * 
     */
    protected List<Double> recordedValues;

    /**
     *
     */
    protected List<Long> timeStamps;

    /**
     * Default constructor
     */
    public DataListStatistic() {
        recordedValues = new LinkedList<>();
        timeStamps = new LinkedList<>();
    }

    /**
     * Method to update the environmental statistic with a new value
     * @param newSample latest observed value of the Environmental measure.
     */
    public void insertSample(double newSample){
        //Give a call to Super
        super.insertSample(newSample);
        recordedValues.add(newSample);
        //add a timestamp for this
        timeStamps.add(System.currentTimeMillis());
    }

    @Override
    public String toString() {
//        return String.format("%s\nCollected Samples: Temp %s Humidity %s Pressure %s\nTimestamps: %s",
//                super.toString(), recordedTemp.size(), recordedPressure.size(), recordedHumidity.size(),
//                timeStamps.size());        return String.format("%s\nCollected Samples: Temp %s Humidity %s Pressure %s\nTimestamps: %s",
//                super.toString(), recordedTemp.size(), recordedPressure.size(), recordedHumidity.size(),
//                timeStamps.size());
        return "";
    }

    public List<Long> getTimestamps(){
        return timeStamps;
    }
}