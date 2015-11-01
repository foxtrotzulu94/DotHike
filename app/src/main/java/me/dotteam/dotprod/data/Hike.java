package me.dotteam.dotprod.data;

import android.content.ContentValues;
import android.util.Log;


/**
 * 
 */
public class Hike {

    /**
     * 
     */
    protected int uniqueID;

    /**
     * 
     */
    protected long startTime;

    /**
     * 
     */
    protected long endTime;


    /**
     * Default constructor
     */
    public Hike() {
        uniqueID=-1;
        startTime=-1;
        endTime=-1;
    }

    public Hike(int ID, long start, long end) {
        uniqueID=ID;
        startTime=start;
        endTime=end;
    }

    public Hike(int ID, Hike copyHike){
        uniqueID=ID;
        if(!copyHike.isComplete()){
            Log.w("HIKE_OBJ","Incomplete Hike Model being copied! DB might enter inconsistent state");
        }
        startTime=copyHike.startTime;
        endTime=copyHike.endTime;
    }

    public void start(){
        if(uniqueID<0 && startTime<0){
            startTime=System.currentTimeMillis();
        }
    }

    public void end(){
        if(uniqueID<0 && endTime<0){
            startTime=System.currentTimeMillis();
        }
    }

    public boolean isComplete(){
        return (startTime>0 && endTime>0);
    }

    /**
     * @return
     */
    public ContentValues toStorage() {
        ContentValues retVal = new ContentValues();
//        retVal.put("id",this.uniqueID);
        retVal.put(DBAssistant.HIKE_START,this.startTime);
        retVal.put(DBAssistant.HIKE_END,this.endTime);
        return retVal;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }
}