package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * 
 */
public class SessionData {

    /**
     * 
     */
    private Hike mHike;

    /**
     * 
     */
    private EnvData mCurrentStats;

    /**
     * 
     */
    private LocationPoints mGeoPoints;

    /**
     * 
     */
    private boolean isHistoric;

    /**
     * 
     */
    private boolean isFromDB;


    /**
     * Default constructor
     */
    public SessionData(Hike hikeSession, EnvData envData, LocationPoints trackpoints) {
        mHike=hikeSession;
        mCurrentStats = envData;
        mGeoPoints=trackpoints;
    }

    public ContentValues hikeToStorage(){
        return mHike.toStorage();
    }

    public long hikeStartTime(){
        return mHike.startTime();
    }

    public long hikeEndTime(){
        return mHike.endTime();
    }

    public EnvData getCurrentStats() {
        return mCurrentStats;
    }

    public LocationPoints getGeoPoints() {
        return mGeoPoints;
    }

    public boolean isHistoric() {
        return isHistoric;
    }

    public boolean isFromDB() {
        return isFromDB;
    }
}