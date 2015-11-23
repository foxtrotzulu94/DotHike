package me.dotteam.dotprod.data;

import android.content.ContentValues;
import android.util.Log;

import java.util.concurrent.TimeUnit;


/**
 * Data structure to represent a Hiking Session
 */
public class Hike {

    /**
     *
     */
    protected String nickName="MyNewHike";

    /**
     * Reference to the ID in Persistent Storage
     */
    protected int uniqueID;

    /**
     * Start Time in Milliseconds since EPOCH
     */
    protected long startTime;

    /**
     * End Time in Milliseconds since EPOCH
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

    /**
     * Constructor whe building from Database
     * @param ID The DB given UniqueID
     * @param start Time in Milliseconds since Epoch
     * @param end Time in Milliseconds since Epoch
     */
    public Hike(int ID, long start, long end) {
        uniqueID=ID;
        startTime=start;
        endTime=end;
    }

    /**
     * Copy Constructor when adding additional information
     * @param ID The DB given UniqueID
     * @param copyHike A reference to the instanced being copied
     */
    public Hike(int ID, Hike copyHike){
        uniqueID=ID;
        if(!copyHike.isComplete()){
            Log.w("HIKE_OBJ","Incomplete Hike Model being copied! DB might enter inconsistent state");
        }
        startTime=copyHike.startTime;
        endTime=copyHike.endTime;
    }

    /**
     * Notify the starting of a Hike
     */
    public void start(){
        if(uniqueID<0 && startTime<0){
            startTime=System.currentTimeMillis();
        }
    }

    /**
     * Notify the end of a Hike
     */
    public void end(){
        if(uniqueID<0 && endTime<0){
            endTime=System.currentTimeMillis();
        }
    }

    /**
     * Check if the hike object is complete
     * @return True if Start and End have been called
     */
    public boolean isComplete(){
        return (startTime>0 && endTime>0);
    }

    /**
     * Method to obtain an object representing the Hike Session instance to be used for storage
     * @return ContentValues object with correct Key (String) to Value pairings
     */
    public ContentValues toStorage() {
        ContentValues retVal = new ContentValues();
        retVal.put(DBAssistant.HIKE_START,this.startTime);
        retVal.put(DBAssistant.HIKE_END,this.endTime);
        return retVal;
    }

    public ContentValues nameToStorage(){
        ContentValues retVal = new ContentValues(2);
        retVal.put(DBAssistant.HIKE_ID,this.uniqueID);
        retVal.put(DBAssistant.NICKNAME,nickName);
        return retVal;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public boolean setUniqueID(int ID){
        if(uniqueID==-1){
            uniqueID=ID;
            return true;
        }
        else{
            return false;
        }
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }

    public String elapsedTime(){
        long current=0;
        if(endTime==-1){
            current=System.currentTimeMillis() - startTime;

        }
        else{
            current = endTime - startTime;
        }
        return String.format("%02dh %02dm %02dsec",
                TimeUnit.MILLISECONDS.toHours(current),
                TimeUnit.MILLISECONDS.toMinutes(current%3600000),
                TimeUnit.MILLISECONDS.toSeconds(current%60000));
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String toString(){
        return String.format("ID: %s Elapsed: %s\n",this.uniqueID,elapsedTime());
    }
}