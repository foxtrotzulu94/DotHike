package me.dotteam.dotprod;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Random;

import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.hw.SensorListenerInterface;
import me.dotteam.dotprod.loc.HikeLocationEntity;

/**
 * Created by foxtrot on 01/12/15.
 */
public class DemoShow extends Thread{

    private static final String TAG = "DEMOMAN";

    private final double[] latitude = {43.657630,
            43.657710,
            43.657760,
            43.657930,
            43.658060,
            43.658210,
            43.658380,
            43.659150,
            43.659190,
            43.659500,
            43.659640,
            43.660310,
            43.660440,
            43.660530,
            43.660670,
            43.660930,
            43.661170,
            43.661340,
            43.661470,
            43.661530,
            43.661620,
            43.661680,
            43.661730,
            43.661750};

    private final double[] longitude = {-79.920150,
            -79.920240,
            -79.920410,
            -79.920580,
            -79.920730,
            -79.920950,
            -79.921210,
            -79.922790,
            -79.922980,
            -79.923690,
            -79.924080,
            -79.925620,
            -79.925930,
            -79.926140,
            -79.926490,
            -79.927170,
            -79.927970,
            -79.928680,
            -79.929400,
            -79.929770,
            -79.931190,
            -79.932320,
            -79.934180,
            -79.934570};

    private final int LOCATION_POINTS = 24;
    private final int MAX_TIME = 900000; //15 Minutes = 15 * 60 seconds
    private final int AVG_TIME_PER_POINT = 3750;

    //Environmental Conditions in Demo Place Georgetown, ON
    private double startTemp = 3; //Temperature in Degrees
    private double startHumidity = 95;//Humidity in percentage
    private double startPressure = 101.1;//Pressure in KiloPascals

    private Context mContext;

    //The guys running the show
    private HikeDataDirector mHDD;
    private HikeHardwareManager mHHM;
    private HikeLocationEntity mHLE;

    private int mSensorFrequency=500;

    private Method broadcastHW;
    private Method broadcastLoc;

    //Check what the directives are
    private SharedPreferences mPrefMan;
    private boolean driveSensors=true;
    private boolean driveLocation=true;
    private boolean driveStepCount=true;

    private Thread mSensorWorker;
    private Thread mStepWorker;
    private Thread mLocationWorker;

    private Random valueGenerator;

    public static boolean isRunning=false;

    public DemoShow(Context runningContext){
        mContext = runningContext;
        valueGenerator = new Random();
    }

    @Override
    public void run() {
        isRunning = true;

        //Get all the needed methods/objects
        mHDD = HikeDataDirector.getInstance(mContext);
        mHHM = HikeHardwareManager.getInstance(mContext);
        mHLE = HikeLocationEntity.getInstance(mContext);

        mPrefMan = PreferenceManager.getDefaultSharedPreferences(mContext);
        fetchDemoParams();

        while(mHHM.getListenerCount()<1 && !mHLE.isRequestingLocationUpdates()){
            //Just do some sleeping until its time to spring into action!
            try {
                sleep(10000);
            }
            catch (Exception e){
                Log.wtf(TAG,"Got Interrupted, What the heck man?");
            }
            //Leaving this loop means a hike has probably started
        }

        //Stop all Default Behaviour
        mHHM.stopSensors();
        mHHM.startCompass();
        mHLE.stopLocationUpdates();

        //Get the Methods
        getMethodsThroughReflection();

        if(driveLocation) {


            mLocationWorker = beginLocationDriver();

            if (driveSensors) {
                mSensorWorker = beginSensorDriver();
            }
            if(driveStepCount){
                mStepWorker = beginStepDriver();
            }
        }

        try {
            mLocationWorker.join();
            //After this, there are only 10 seconds remaining to the demo
            sleep(30000);
        }
        catch (Exception e){
            Log.wtf(TAG,"Got Interrupted, I'll clean up my mess now");
            mLocationWorker.interrupt();

            if(mSensorWorker!=null){ mSensorWorker.interrupt(); }
            if(mStepWorker!=null){ mStepWorker.interrupt(); }
        }

        isRunning = false;

    }

    private void fetchDemoParams(){
        if(mPrefMan.contains("demodrive_sensors")){
            driveSensors = mPrefMan.getBoolean("demodrive_sensors",driveSensors);
        }
        if(mPrefMan.contains("demodrive_location")){
            driveLocation = mPrefMan.getBoolean("demodrive_location",driveLocation);
        }
        if(mPrefMan.contains("demodrive_stepcount")){
            driveStepCount = mPrefMan.getBoolean("demodrive_stepcount",driveStepCount);
        }
        if(mPrefMan.contains("extsensor_period")){
            mSensorFrequency = mPrefMan.getInt("extsensor_period", mSensorFrequency);
        }
    }

    private void getMethodsThroughReflection(){
        try {
            broadcastLoc = mHLE.getClass().getMethod("onLocationChanged",Location.class);
//            broadcastHW = mHHM.getClass().getMethod("broadcastUpdate", new Class[]{SensorListenerInterface.HikeSensors.class, double.class});
            broadcastHW = mHHM.getClass().getDeclaredMethod("broadcastUpdate", new Class[]{SensorListenerInterface.HikeSensors.class, Double.TYPE});
//            broadcastLoc = mHLE.getClass().getMethod("broadcastUpdate", new Class[]{Location.class,float.class});

            broadcastHW.setAccessible(true);
            broadcastLoc.setAccessible(true);
        }
        catch (Exception e){
            Log.wtf(TAG,e);
        }
    }

    private Thread beginSensorDriver(){
        Thread worker = new Thread(){
            @Override
            public void run(){
                int sensorToUpdate;

                //Run for as long as we are updating the UI with Location points
                while(isRunning) {
                    try {
                        sensorToUpdate = valueGenerator.nextInt() %3;

                        switch (sensorToUpdate) {
                            case 0: {
                                broadcastHW.invoke(mHHM,
                                        SensorListenerInterface.HikeSensors.HUMIDITY,
                                        startHumidity + 3 * Math.sin(valueGenerator.nextDouble()));
                                break;
                            }
                            case 1: {
                                broadcastHW.invoke(mHHM,
                                        SensorListenerInterface.HikeSensors.PRESSURE,
                                        startPressure + valueGenerator.nextDouble());
                                break;
                            }
                            case 2: {
                                broadcastHW.invoke(mHHM,
                                        SensorListenerInterface.HikeSensors.TEMPERATURE,
                                        startTemp + 0.3 * Math.sin(System.currentTimeMillis()));
                                break;
                            }
                        }

                        //Very unlikely the same sensor will get called twice
                        sleep(mSensorFrequency/3);

                    }
                    catch (Exception e){
                        Log.wtf(TAG+"-SensorWorker",e);
                    }
                }

            }
        };
        worker.start();
        return worker;
    }

    private Thread beginLocationDriver(){
        Thread worker = new Thread(){
            @Override
            public void run(){
                try{
                    for (int i = 0; i < LOCATION_POINTS; i++) {
                        Location newLocation = new Location(LocationManager.GPS_PROVIDER); //DEMOMAN is the provider
                        newLocation.setAccuracy(7.0f);
                        newLocation.setLatitude(latitude[i]);
                        newLocation.setLongitude(longitude[i]);
                        newLocation.setTime(System.currentTimeMillis());


                        broadcastLoc.invoke(mHLE, newLocation);

                        //We might change this, though we really only need it 15 minutes.
                        sleep(AVG_TIME_PER_POINT);
                    }
                }
                catch (Exception e){
                    Log.wtf(TAG+"-LocationWorker",e);
                }
            }
        };
        worker.start();
        return worker;
    }

    private Thread beginStepDriver(){
        Thread worker = new Thread(){

            @Override
            public void run(){
                try {
                    double steps =0;
                    while(isRunning) {
                        broadcastHW.invoke(mHHM, SensorListenerInterface.HikeSensors.PEDOMETER, steps);
                        steps+=1;
                        sleep(777);
                    }
                }
                catch (Exception e){
                    Log.wtf(TAG+"-SensorWorker",e);
                }
            }
        };
        worker.start();
        return worker;
    }
}
