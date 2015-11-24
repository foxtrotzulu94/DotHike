package me.dotteam.dotprod.data;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;

import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.R;
import me.dotteam.dotprod.hw.SensorListenerInterface;
import me.dotteam.dotprod.loc.HikeLocationEntity;
import me.dotteam.dotprod.loc.HikeLocationListener;

/**
 * Lightweight class used for Sensor Data Collection as a service.
 * Is the bridge between the {@link HikeDataDirector} and the HikeHardwareManager
 */
public class SessionCollectionService extends Service implements SensorListenerInterface, HikeLocationListener {

    private final String LONG_NAME=".Hike Statistics Service";
    private final String TAG="SCS";

    private TimeUpdate serviceThread;

    private NotificationManager mNotifier;
    NotificationCompat.Builder mBuilder;
    private int notificationID;

    private EnvData recordedData;
    private LocationPoints recordedCoordinates;
    private Hike currentHike;

    class TimeUpdate extends Thread{

        private Context mainContext;
        private boolean run;

        public TimeUpdate(Context currContext){
            mainContext = currContext;
            run = true;
        }

        @Override
        public void run(){
            while(run){
                try{
                    mBuilder.setContentText(currentHike.elapsedTime());
                    mNotifier.notify(notificationID,mBuilder.build());
                    sleep(1000);
                }
                catch (Exception e){
                    Log.e(TAG,"Service Thread was killed while Sleeping!");
                }
            }

        }

        public void end(){
            run=false;
        }
    }


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
        recordedData = new EnvData();
        recordedCoordinates = new LocationPoints();
        currentHike = new Hike();
        currentHike.start();

        mNotifier = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationID = this.getApplicationInfo().uid;
        firstNotify();
        serviceThread = new TimeUpdate(this);
        serviceThread.start();
        Log.d("Collect","SERVICE STARTED!!!");

        //register yourself
        HikeHardwareManager.getInstance(this).addListener(this);
        HikeLocationEntity.getInstance(this).addListener(this);
    }

    private void firstNotify(){
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.hikerservice)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.dothikemin))
                        .setContentTitle(LONG_NAME)
                        .setOngoing(true);

        mNotifier.notify(notificationID,mBuilder.build());
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
        serviceThread.end();
        Log.d("Collect", "Service ends...");
        mNotifier.cancelAll();

        //de-register yourself
        HikeHardwareManager.getInstance(this).removeListener(this);
        HikeLocationEntity.getInstance(this).removeListener(this);
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
    public void onLocationChanged(Location location, float distance) {
        // TODO: Save distance traveled
        //Store the new point in our recorded coordinates list
        Log.d(TAG, "onLocationChanged Added "+location.toString());
        this.recordedCoordinates.addPoint(new Coordinates(
                location.getLongitude(),
                location.getLatitude(),
                location.getAltitude()
        ));
    }
}