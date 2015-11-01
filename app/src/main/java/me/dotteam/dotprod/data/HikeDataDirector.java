package me.dotteam.dotprod.data;
import android.content.Context;

import java.util.*;

/**
 * 
 */
public class HikeDataDirector {

    /**
     * Default constructor
     */
    public HikeDataDirector() {
    }

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
    private boolean mDataIsHistoric;

    /**
     * 
     */
    private boolean mIsCollectingData;







    /**
     * @param currentContext 
     * @return
     */
    public HikeDataDirector getInstance(Context currentContext) {
        // TODO implement here
        return null;
    }

    /**
     * 
     */
    public void beginCollectionService() {
        // TODO implement here
    }

    /**
     * 
     */
    public void endCollectionService() {
        // TODO implement here
    }

    /**
     * @return
     */
    public boolean storeCollectedStatistics() {
        // TODO implement here
        return false;
    }

}