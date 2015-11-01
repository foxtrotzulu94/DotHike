package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * 
 */
public class EnvStatistic {

    /**
     * 
     */
    protected float min=Float.POSITIVE_INFINITY;

    /**
     * 
     */
    protected float avg=Float.NaN;

    /**
     * 
     */
    protected float max=0;

    /**
     * Default constructor
     */
    public EnvStatistic() {
    }

    public float getAvg() {
        return avg;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    public void insertSample(float newSample){
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
     * @return
     */
    public ContentValues toStorage(int ID) {
        ContentValues retVal = new ContentValues();
        retVal.put(DBAssistant.HIKE_ID,ID);
        retVal.put(DBAssistant.MIN_COL,this.min);
        retVal.put(DBAssistant.AVG_COL,this.avg);
        retVal.put(DBAssistant.MAX_COL,this.max);
        return retVal;
    }

}