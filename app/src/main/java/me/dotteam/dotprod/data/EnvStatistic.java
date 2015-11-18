package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * Data structure to represent a single Environmental Statistic
 */
public class EnvStatistic {

    /**
     * Observed Minimum
     */
    protected double min=Float.POSITIVE_INFINITY;

    /**
     * Observed Average. Generally, the latest obtained value from external sensors
     */
    protected double avg=Float.NaN;

    /**
     * Observed Maximum
     */
    protected double max=0;

    /**
     * Default constructor
     */
    public EnvStatistic() {
    }

    public double getAvg() {
        return avg;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    /**
     * Method to update the environmental statistic with a new value
     * @param newSample latest observed value of the Environmental measure.
     */
    public void insertSample(double newSample){
        //NOTE: Might want to replace this in the future...
        avg=newSample;

        if(newSample<min){
            min=newSample;
        }
        if(newSample>max){
            max=newSample;

        }
    }


    /**
     * Method to obtain the object of the Environmental Statistic to be used for storage
     * @return ContentValues object with correct key-value pairs
     */
    public ContentValues toStorage(int ID) {
        ContentValues retVal = new ContentValues();
        retVal.put(DBAssistant.HIKE_ID,ID);
        retVal.put(DBAssistant.MIN_COL,this.min);
        retVal.put(DBAssistant.AVG_COL,this.avg);
        retVal.put(DBAssistant.MAX_COL,this.max);
        return retVal;
    }

    public boolean isValid(){
        return max!=0 && avg!=Float.NaN && min != Float.POSITIVE_INFINITY;
    }

    public String toString(){
        return String.format("High: %.3f, Avg: %.3f, Low: %.3f",max,avg,min);
    }

}