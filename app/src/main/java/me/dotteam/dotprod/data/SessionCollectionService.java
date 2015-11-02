package me.dotteam.dotprod.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Lightweight class used for Sensor Data Collection as a service.
 * Is the bridge between the {@link HikeDataDirector} and the HikeHardwareManager
 */
public class SessionCollectionService extends Service{

    private EnvData recordedData;
    private LocationPoints recordedCoordinates;
    private long startTime;
    private long endTime;

    /**
     * Reference to the thread doing background work.
     * Started when the service is called and stopped when the service is no longer needed.
     */
    private Thread workerThread;


    /**
     * Default constructor
     */
    public SessionCollectionService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO implement here
        return null;
    }

    /**
     * Signal to indicate data collection must begin
     */
    public void beginCollection() {
        // TODO implement here
    }

    /**
     * Signal to indicate data collection must stop and result must be packaged.
     */
    public void endCollection() {
        // TODO implement here
    }

}