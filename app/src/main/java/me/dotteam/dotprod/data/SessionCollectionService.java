package me.dotteam.dotprod.data;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import me.dotteam.dotprod.HikeViewPagerActivity;
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

    private double stepCount=0.0; //This would later get transformed into an integer. But this avoids a costly casting when updating
    private EnvData recordedData;
    private LocationPoints recordedCoordinates;
    private Hike currentHike;
    private HikeDataDirector mHDD;

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
        mHDD = HikeDataDirector.getInstance(this);

        mNotifier = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationID = this.getApplicationInfo().uid;

        //Read the preferences to see if we build a notification
        boolean activeNotifications = true;
        SharedPreferences prefMan = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(prefMan.contains("notifications_show")){
            activeNotifications = prefMan.getBoolean("notifications_show",true);
        }

        if(activeNotifications) {
            firstNotify();
        }


        Log.d(TAG,"Statistical Collection has begun");
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
                        .setOngoing(true)
                        .setUsesChronometer(true);

        mBuilder.setContentIntent(
                PendingIntent.getActivity(
                        this,0,new Intent(this, HikeViewPagerActivity.class),0)
                );

        mNotifier.notify(notificationID,mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_NOT_STICKY;
    }

    /**
     * Signal to indicate data collection must stop and result must be packaged.
     */
    @Override
    public void onDestroy(){
        //End the hike, package the data
        currentHike.end();
        SessionData thisSession = new SessionData(currentHike, new StepCount(stepCount),recordedData,recordedCoordinates);

        //Now hand this data off to someone.
        HikeDataDirector.getInstance(this).receiveDataFromService(this,thisSession);
//        serviceThread.end();
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
        if(!mHDD.isPaused()) {
            Log.d("SCS", String.format("Got update %s: %s", hikesensors.toString(), value));
            switch (hikesensors) {
                case TEMPERATURE: {
                    recordedData.updateTemp(value);
                    break;
                }
                case HUMIDITY: {
                    recordedData.updateHumidity(value);
                    break;
                }
                case PRESSURE: {
                    recordedData.updatePressure(value);
                    break;
                }
                case PEDOMETER:{
                    stepCount = value;
                }
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