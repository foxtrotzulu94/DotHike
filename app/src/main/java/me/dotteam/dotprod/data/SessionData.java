package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * Data structure for representing a completed hiking session.
 */
public class SessionData {

    private Hike mHike;
    private StepCount mStepCount;
    private EnvData mCurrentStats;
    private LocationPoints mGeoPoints;

    /**
     * Indicates whether or not the SessionData was loaded from Persistent Storage
     */
    private boolean isFromDB;


    /**
     * Default constructor without step count
     */
    @Deprecated
    public SessionData(Hike hikeSession, EnvData envData, LocationPoints trackpoints) {
        mHike=hikeSession;
        mStepCount = new StepCount(0);
        mCurrentStats = envData;
        mGeoPoints=trackpoints;
    }

    /**
     * New Default Constructor
     */
    public SessionData(Hike hikeSession, StepCount steps, EnvData envData, LocationPoints trackpoints) {
        mHike=hikeSession;
        mStepCount = steps;
        mCurrentStats = envData;
        mGeoPoints=trackpoints;
    }

    /**
     * Protected call to {@link Hike}'s toStorage() function
     * @return ContentValues object with correct Key (String) to Value pairings
     */
    public ContentValues hikeToStorage(){
        return mHike.toStorage();
    }

    public ContentValues hikeNameToStorage(){
        return mHike.nameToStorage();
    }

    public long hikeStartTime(){
        return mHike.startTime();
    }

    public long hikeEndTime(){
        return mHike.endTime();
    }

    public int hikeID(){ return mHike.getUniqueID(); }

    public EnvData getCurrentStats() {
        return mCurrentStats;
    }

    public LocationPoints getGeoPoints() {
        return mGeoPoints;
    }

    public StepCount getStepCount(){
        return mStepCount;
    }

    public boolean isFromDB() {
        return isFromDB;
    }

    public boolean setHikeID(int ID){
        return mHike.setUniqueID(ID);
    }

    public boolean isValid(){
        return mHike.startTime()>0 && mCurrentStats.isValid();
    }

    public String toString(){
        return String.format("Session Data:\n%s\n%s\n%s\n%s",
                mHike.toString(),
                mStepCount.toString(),
                mCurrentStats.toString(),
                mGeoPoints.toString());
    }
}