package me.dotteam.dotprod.data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Centralized, Singleton class for managing all Data collection, storage and review
 */
public class HikeDataDirector {

    private final static String TAG ="Hike_HDD";

    private static HikeDataDirector mInstance;
    private Context mCreateContext;
    private PersistentStorageEntity mPSE;
    private boolean mPauseCollection = false;

    /**
     * Reference to the SessionData object currently in memory
     * This may correspond to a recently generated entity OR a loaded object from Persistent Storage
     */
    private SessionData mSessionData;

    /**
     * Reference to a lightweight service for background data collection
     */
    private Intent mCollectionServiceIntent;

    /**
     * Indicates if the SessionData object is from a Persistent Storage entry
     */
    private boolean mDataIsHistoric=false;

    /**
     * Indicates if there is an active SessionsCollectionService running
     */
    private boolean mIsCollectingData=false;

    private HikeDataDirector(Context currentContext){
        mCreateContext = currentContext;
//        mPSE = new PersistentStorageEntity(); //Defer creation of a persistent storage manager.
    }

    private void checkOrSetPSE(){
        if(mPSE==null){
            mPSE = new PersistentStorageEntity(mCreateContext);
        }
    }

    /**
     * Singleton method to obtain or generate current instance.
     * @param currentContext Context from which the instance is being requested
     * @return Singleton Object instance of the HikeDataDirector
     */
    public static HikeDataDirector getInstance(Context currentContext) {
        if(mInstance==null && currentContext!=null){
            mInstance = new HikeDataDirector(currentContext);
        }
        return mInstance;
    }

    /**
     * Method to indicate that background collection must begin
     */
    public void beginCollectionService() {
        if(mCollectionServiceIntent==null && !mIsCollectingData){
            //Say we are collecting data and begin the service.
            mIsCollectingData=true;
            mCollectionServiceIntent = new Intent(mCreateContext,SessionCollectionService.class);
            mCreateContext.startService(mCollectionServiceIntent);
            Log.d(TAG, "Starting background collection service");
        }
        else{
            Log.w(TAG,"beginCollectionService was called, but a background service might be running already!");
        }
    }

    /**
     * Method to signal the end of background collection and the need to present a SessionData object
     */
    public void endCollectionService() {
        if(mCollectionServiceIntent!=null){
            mCreateContext.stopService(new Intent(mCreateContext,SessionCollectionService.class));
            mCollectionServiceIntent=null;
            Log.d(TAG, "Finishing the background collection service");
        }
        else{
            Log.w(TAG, "beginCollectionService was called, but a background service might be running already!");
        }
    }

    public void receiveDataFromService(Service reportingService, SessionData collectedData){
        Log.d(TAG,"Receiving data");
        if(reportingService!=null && collectedData!=null){
            //The data was received, so store it and say we are NOT collecting data anymore.
            //This allows other components to call beginCollectionService() again.
            mSessionData = collectedData;
            mIsCollectingData=false;
            Log.d(TAG,"Data received!");
            Log.e(TAG, "receiveDataFromService " + collectedData.toString());
        }
    }

    /**
     * Used to indicate that a generated SessionData object can be saved in Persistent Storage
     * @return boolean indicating the success or failure of the operation
     */
    public boolean storeCollectedStatistics() {
        checkOrSetPSE();
        return mPSE.saveSession(mSessionData);
    }

    public List<Hike> getAllStoredHikes(){
        checkOrSetPSE();
        return mPSE.getHikesList();
    }

    public boolean retrieveSessionFromHike(Hike selectedHike){
        checkOrSetPSE();
        mSessionData = mPSE.loadHikeData(selectedHike);
        if(mSessionData!=null) {
            mDataIsHistoric = true;
            return true;
        }
        else {
            return false;
        }
    }

    public boolean retrieveSessionFromHike(int hikeID){
        checkOrSetPSE();
        mSessionData = mPSE.loadHikeData(hikeID);
        if(mSessionData!=null) {
            mDataIsHistoric = true;
            return true;
        }
        else {
            return false;
        }
    }

    public Map<Integer,LocationPoints> bulkLoadCoordinates(){
        checkOrSetPSE();
        return mPSE.loadMaps();
    }

    public boolean canBulkLoadCoordinates(){
        checkOrSetPSE();
        return mPSE.hasCoordinatesCached();
    }

    public void setPauseStatus(boolean b){
        mPauseCollection = b;
    }

    public boolean isPaused(){
        return mPauseCollection;
    }

    //TODO: Eventually remove
    public void testStorage(){
        Thread backgroundCheck = new Thread(){
            @Override
            public void run(){
                Random randy = new Random();
                Hike mockHike = new Hike();
                EnvData mockStats = new EnvData();
                LocationPoints mockGeo = new LocationPoints();
                float lastHeight=0;

                //Start the hike
                mockHike.start();
                for (int i=0; i<100; ++i) {
                    mockStats.updateHumidity(randy.nextFloat());
                    mockStats.updateTemp(randy.nextFloat());
                    mockStats.updatePressure(randy.nextFloat());
                    lastHeight += randy.nextFloat();
                    mockGeo.addPoint(new Coordinates(randy.nextFloat(), randy.nextFloat(), lastHeight));
                }
                mockHike.end();
                //End the Hike

                //Dump the values in the database
                if(mPSE==null){
                    mPSE=new PersistentStorageEntity(mCreateContext);
//                    mPSE.reset();
                }
                if(mPSE.saveSession(new SessionData(mockHike, new StepCount(99), mockStats,mockGeo))){
                    Log.d("HDD", "Save Successful");

                    //If the save was successful, call them back for a load.
                    List<Hike> storedHikes = mPSE.getHikesList();
                    for (Hike loadedHike :storedHikes) {
                        if(loadedHike.startTime==mockHike.startTime){
                            Log.d("HDD", "Load Successful");
                        }
                    }
                }
                else{
                    Log.e("HDD", "SAVE WAS UNSUCCESSFUL IN TEST RUN!");
                }
            }
        };
        backgroundCheck.start();
    }

    public boolean deleteCurrentSessionData(){
        checkOrSetPSE();
        if(mSessionData!=null){
            boolean retVal =  mPSE.deleteSession(mSessionData);
            if(retVal)
                mSessionData=null;
            return retVal;
        }
        else{
            return false;
        }
    }

    public boolean deleteStoredHike(Hike aHike){
        checkOrSetPSE();
        return mPSE.deleteHike(aHike);
    }

    public void deleteAllData(){
        checkOrSetPSE();
        mPSE.reset();
    }

    public boolean isCollectingData() {
        return mIsCollectingData;
    }

    public boolean isDataHistoric() {
        return mDataIsHistoric;
    }

    public SessionData getSessionData(){
        return mSessionData;
    }
}