package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * Data structure for representing a completed hiking session.
 */
public class SessionData {

    private Hike mHike;
    private EnvData mCurrentStats;
    private LocationPoints mGeoPoints;

    //TODO: consider removing
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

    /**
     * Protected call to {@link Hike}'s toStorage() function
     * @return ContentValues object with correct Key (String) to Value pairings
     */
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

    public boolean setHikeID(int ID){
        return mHike.setUniqueID(ID);
    }
}