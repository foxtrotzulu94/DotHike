package me.dotteam.dotprod.data;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Random;


/**
 * 
 */
public class HikeDataDirector {
    /**
     * 
     */
    private Context mCreateContext;

    /**
     * 
     */
    private static HikeDataDirector mInstance;

    /**
     * 
     */
    private PersistentStorageEntity mPSE;

    /**
     * 
     */
    private SessionData mSessionData;

    /**
     * 
     */
    private SessionCollectionService mCollectionService;

    /**
     * 
     */
    private boolean mDataIsHistoric=false;

    /**
     * 
     */
    private boolean mIsCollectingData=false;

    private HikeDataDirector(Context currentContext){
        mCreateContext = currentContext;
//        mPSE = new PersistentStorageEntity();
    }



    /**
     * @param currentContext 
     * @return
     */
    public static HikeDataDirector getInstance(Context currentContext) {
        if(mInstance==null){
            mInstance = new HikeDataDirector(currentContext);
        }
        return mInstance;
    }

    /**
     * 
     */
    public void beginCollectionService() {
        // TODO implement here (Spawn a SessionCollectionService entity)
    }

    /**
     * 
     */
    public void endCollectionService() {
        // TODO implement here (Kill the SessionCollectionService entity)
    }

    /**
     * @return
     */
    public boolean storeCollectedStatistics() {
        // TODO implement here
        return false;
    }

    public void testStorage(){
        Thread backgroundCheck = new Thread(){
            @Override
            public void run(){
                Random randy = new Random();
                Hike mockHike = new Hike();
                EnvData mockStats = new EnvData();
                LocationPoints mockGeo = new LocationPoints();

                //Start the hike
                mockHike.start();
                for (int i=0; i<100; ++i) {
                    mockStats.updateHumidity(randy.nextFloat());
                    mockStats.updateTemp(randy.nextFloat());
                    mockStats.updatePressure(randy.nextFloat());
                    mockGeo.addPoint(new Coordinates(randy.nextFloat(), randy.nextFloat(), randy.nextFloat()));
                }
                mockHike.end();
                //End the Hike

                //Dump the values in the database
                if(mPSE==null){
                    mPSE=new PersistentStorageEntity(mCreateContext);
                    mPSE.reset();
                }
                if(mPSE.saveSession(new SessionData(mockHike, mockStats,mockGeo))){
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
                    Log.e("HDD","SAVE WAS UNSUCCESSFUL IN TEST RUN!");
                }
            }
        };
        backgroundCheck.start();
    }

}