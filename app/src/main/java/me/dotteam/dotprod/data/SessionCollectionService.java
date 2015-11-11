package me.dotteam.dotprod.data;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

import java.util.Date;

import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.R;
import me.dotteam.dotprod.hw.SensorListenerInterface;

/**
 * Lightweight class used for Sensor Data Collection as a service.
 * Is the bridge between the {@link HikeDataDirector} and the HikeHardwareManager
 */
public class SessionCollectionService extends Service implements SensorListenerInterface,LocationListener{

    private final String LONG_NAME=".Hike Statistics Service";
    private final String TAG="SCS";

    private NotificationManager mNotifier;

    private SessionEnvData recordedData;
    private LocationPoints recordedCoordinates;
    private Hike currentHike;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //No special behaviour here.
        return null;
    }

    /**
     * Signal to indicate data collection must begin
     */
    @Override
    public void onCreate(){
        //Initialize objects
        recordedData = new SessionEnvData(); //TODO: Change after debugging
        recordedCoordinates = new LocationPoints();
        currentHike = new Hike();
        currentHike.start();

        mNotifier = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        firstNotify();
        Log.d("Collect","SERVICE STARTED!!!");

        //register yourself
        HikeHardwareManager.getInstance(this).addListener(this);
        //HikeLocationEntity.getInstance().addListener(this);
    }

    private void firstNotify(){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.dothikemin)
                        .setContentTitle(LONG_NAME)
                        .setContentText(new Hike(0,100,100*60*1000).elapsedTime())
                        .setOngoing(true);

        mNotifier.notify(this.getApplicationInfo().uid,
                mBuilder.build());
    }

    /**
     * Signal to indicate data collection must stop and result must be packaged.
     */
    @Override
    public void onDestroy(){
        //End the hike, package the data
        currentHike.end();
        SessionData thisSession = new SessionData(currentHike,recordedData,recordedCoordinates);

        //Now hand this data off to someone.
        HikeDataDirector.getInstance(this).receiveDataFromService(this,thisSession);

        Log.d("Collect", "Service ends...");
        mNotifier.cancelAll();

        //de-register yourself
        HikeHardwareManager.getInstance(this).removeListener(this);
        //HikeLocationEntity.getInstance().addListener(this);
    }

    /**
     * Method to implement listener behaviour and record hardware updates
     * @param hikesensors Environmental sensor being updated
     * @param value Double precision value presented
     */
    @Override
    public void update(HikeSensors hikesensors, double value) {
        Log.d("SCS",String.format("Got update %s: %s",hikesensors.toString(),value));
        switch (hikesensors){
            case TEMPERATURE:{
                recordedData.updateTemp(value);
                break;
            }
            case HUMIDITY:{
                recordedData.updateHumidity(value);
                break;
            }
            case PRESSURE:{
                recordedData.updatePressure(value);
                break;
            }
        }
    }

    /**
     * Method to get updates on location and record them.
     * @param location The object describing longitude and latitude
     */
    @Override
    public void onLocationChanged(Location location) {
        //Store the new point in our recorded coordinates list
        this.recordedCoordinates.addPoint(new Coordinates(
                location.getLongitude(),
                location.getLatitude(),
                location.getAltitude()
        ));
    }
}