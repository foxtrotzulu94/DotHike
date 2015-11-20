package me.dotteam.dotprod.hw;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.sensortag.SensorTagListener;
import ca.concordia.sensortag.SensorTagLoggerListener;
import ca.concordia.sensortag.SensorTagManager;
import me.dotteam.dotprod.R;
import ti.android.ble.sensortag.Sensor;
import ti.android.util.Point3D;

/**
 * Created by as on 2015-10-23.
 */
public class HikeHardwareManager implements SensorTagConnector.STConnectorListener {

    private String TAG = "HHM";

    private static HikeHardwareManager mInstance;
    private Context mContext;

    private SensorTagManager mSensorTagManager;
    private SensorTagManagerListener mSensorTagManagerListener;
    private SensorTagConnector mSTConnector;

    private List<SensorListenerInterface> mSensorListenerList;
    private boolean mSTConnected = false;
    private int samplingFrequency = 500; //In milliseconds. 1000ms = 1s. MAX:2550ms

    // Android Sensors
    SensorManager mSensorManager;
    android.hardware.Sensor mPedometer;
    android.hardware.Sensor mAccelerometer;
    android.hardware.Sensor mMagnetometer;
    PedometerEventListener mPedometerListener;
    CompassEventListener mCompassListener;


    public static HikeHardwareManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HikeHardwareManager(context);
        }
        return mInstance;
    }

    private HikeHardwareManager(Context context) {
        //Set Context
        mContext = context;

        //Setup SensorTag classes
        mSensorTagManagerListener = new SensorTagManagerListener();
        mSensorListenerList = new ArrayList<SensorListenerInterface>();

        //Setup Android Sensor classes
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mPedometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER);
        mAccelerometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);


        mPedometerListener = new PedometerEventListener();
        mCompassListener = new CompassEventListener();
    }

    public void startSensors(Context context) {
        mContext = context;
        startSensorTagConnector();
        startPedometer();
        startCompass();
    }

    private void startSensorTagConnector() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.STConnectDialogFragmentTitle);
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "AlertDialog Yes");
                mSTConnector = new SensorTagConnector(mContext);
                mSTConnector.addListener(mInstance);
            }
        });
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "AlertDialog No");
            }
        });
        dialog.create().show();
    }

    public void stopSensorTag(){
        if(mSensorTagManager!=null) {
            mSensorTagManager.disableUpdates();
            if(mSensorTagManager.isServicesReady())
                mSensorTagManager.close();
        }
        if (mSTConnector != null) {
            mSTConnector.stop();
            mSTConnector = null;
        }

        System.gc(); //Mark for Cleanup!
    }

    private void startPedometer() {
        if(mPedometer!=null){
            if(mPedometerListener==null){
                mPedometerListener = new PedometerEventListener();
            }
            mSensorManager.registerListener(mPedometerListener, mPedometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else{
            Log.e(TAG, "startPedometer Failed! Maybe this sensor is not on the device?");
        }
    }

    public void stopPedometer() {
        mSensorManager.unregisterListener(mPedometerListener);
    }

    public void resetPedometer() {
        mPedometerListener.mFirstStep = true;
    }

    public void startCompass(){
        if(mAccelerometer!=null && mMagnetometer!=null) {
            mSensorManager.registerListener(mCompassListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(mCompassListener, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
        }
        else{
            Log.e(TAG, "startCompass Failed! Accelerometer OR Magnetometer may be missing from the device");
        }
    }

    public void endCompass(){
        mSensorManager.unregisterListener(mCompassListener,mAccelerometer);
        mSensorManager.unregisterListener(mCompassListener,mMagnetometer);
    }




    public void addListener(SensorListenerInterface sensorListenerInterface){
        Log.d(TAG, "Adding Listener");
        mSensorListenerList.add(sensorListenerInterface);
    }

    public void removeListener(SensorListenerInterface sensorListenerInterface) {
        Log.d(TAG, "Removing Listener");
        mSensorListenerList.remove(sensorListenerInterface);
    }

    private void broadcastUpdate(SensorListenerInterface.HikeSensors sensor, double value){
        for (int i = 0; i < mSensorListenerList.size(); i++) {
            mSensorListenerList.get(i).update(sensor,value);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // STConnectorListener Methods Implemented

    @Override
    public void onSensorTagConnect(BluetoothDevice btdevice) {
        Toast.makeText(mContext,"SensorTag Discovered. Connecting...",Toast.LENGTH_SHORT).show();
        mSensorTagManager = new SensorTagManager(mContext, btdevice);
        mSensorTagManager.addListener(mSensorTagManagerListener);

        Thread t = new Thread() {
            @Override
            public void run() {
                mSensorTagManager.initServices();
                if (!mSensorTagManager.isServicesReady()) {
                    // if initServices failed or took too long, log an error (in LogCat) and exit
                    Log.e(TAG, "Discover failed - exiting");
                    return;
                }

                //TODO: wrap this in a function call in case we want to dynamically change the sampling frequency
                mSensorTagManager.enableSensor(Sensor.IR_TEMPERATURE,samplingFrequency);
                mSensorTagManager.enableSensor(Sensor.HUMIDITY,samplingFrequency);
                mSensorTagManager.enableSensor(Sensor.BAROMETER,samplingFrequency);
                mSensorTagManager.enableUpdates();

                mSTConnected = true;
            }
        };
        t.start();
    }

    @Override
    public void onSensorTagDisconnect() {
        Toast.makeText(mContext,"SensorTag Disconnected",Toast.LENGTH_SHORT).show();
        mSensorTagManager.disableUpdates();
        mSTConnected = false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Hardware Event Listener Classes

    public class PedometerEventListener implements SensorEventListener {
        private double mInitialStepCount = 0;
        private boolean mFirstStep = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            double value = event.values[0];
            if (mFirstStep) {
                mInitialStepCount = value - 1;
                mFirstStep = false;
            }
            double stepcount = value - mInitialStepCount;
            for (int i = 0; i < mSensorListenerList.size(); i++) {
                mSensorListenerList.get(i).update(SensorListenerInterface.HikeSensors.PEDOMETER, stepcount);
            }
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
            //Do nothing
        }
    }

    public class CompassEventListener implements SensorEventListener{

        private float[] accelerometerRead;
        private float[] magneticFieldRead;
        private float[] rotationMatrix = new float[9];
        private float[] principalAxes = new float[3]; //[0] -> Yaw, [1] -> Pitch, [2] -> Roll
        private double currentYaw = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor == mAccelerometer){
                accelerometerRead = event.values;
            }
            else if(event.sensor == mMagnetometer){
                magneticFieldRead = event.values;
            }

            //If we've read enough data, time to start updating the compass
            if(accelerometerRead!=null && magneticFieldRead !=null){
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerRead, magneticFieldRead);
                SensorManager.getOrientation(rotationMatrix,principalAxes);

                //Once the getOrientation call is done, we have the result of where magnetic north is
                // ...in radians (from -pi to pi). So we process that before storing
                currentYaw = (Math.toDegrees(principalAxes[0])+360) % 360;

                //And, having stored it, we update everyone
                broadcastUpdate(SensorListenerInterface.HikeSensors.COMPASS, currentYaw);
            }

        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

        }
    }


    public class SensorTagManagerListener extends SensorTagLoggerListener implements SensorTagListener {

        /**
         * Ambient Temperature
         *
         * Called on receiving a new ambient temperature measurement from the SensorTagManager.
         * Displays the new value on the GUI.
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateAmbientTemperature(double)
         */

        @Override
        public void onUpdateAmbientTemperature(SensorTagManager mgr, double temp) {
            super.onUpdateAmbientTemperature(mgr, temp);
            broadcastUpdate(SensorListenerInterface.HikeSensors.TEMPERATURE, temp);
        }

        /**
         *  Accelerometer
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateAccelerometer(ca.concordia.sensortag
         * .SensorTagManager, ti.android.util.Point3D)
         */
        @Override
        public void onUpdateAccelerometer(SensorTagManager mgr, Point3D acc) {
            super.onUpdateAccelerometer(mgr, acc);
        }

        /**
         * Barometer
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateBarometer(ca.concordia.sensortag
         * .SensorTagManager, double, double)
         */
        @Override
        public void onUpdateBarometer(SensorTagManager mgr, double pressure, double height) {
            super.onUpdateBarometer(mgr, pressure, height);
            broadcastUpdate(SensorListenerInterface.HikeSensors.PRESSURE, pressure);
        }

        /**
         * Gyroscope
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateGyroscope(ca.concordia.sensortag
         * .SensorTagManager, ti.android.util.Point3D)
         */
        @Override
        public void onUpdateGyroscope(SensorTagManager mgr, Point3D ang) {
            super.onUpdateGyroscope(mgr, ang);
        }

        /**
         * Humidity
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateHumidity(ca.concordia.sensortag
         * .SensorTagManager, double)
         */
        @Override
        public void onUpdateHumidity(SensorTagManager mgr, double rh) {
            super.onUpdateHumidity(mgr, rh);
            broadcastUpdate(SensorListenerInterface.HikeSensors.HUMIDITY, rh);
        }

        /**
         * Infrared Temperature
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateInfraredTemperature(ca.concordia
         * .sensortag.SensorTagManager, double)
         */
        @Override
        public void onUpdateInfraredTemperature(SensorTagManager mgr, double temp) {
            super.onUpdateInfraredTemperature(mgr, temp);
        }

        /**
         * Keys
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateKeys(ca.concordia.sensortag.
         * SensorTagManager, boolean, boolean)
         */
        @Override
        public void onUpdateKeys(SensorTagManager mgr, boolean left, boolean right) {
            super.onUpdateKeys(mgr, left, right);
        }

        /**
         * Magnetometer
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateMagnetometer(ca.concordia.sensortag
         * .SensorTagManager, ti.android.util.Point3D)
         */
        @Override
        public void onUpdateMagnetometer(SensorTagManager mgr, Point3D b) {
            super.onUpdateMagnetometer(mgr, b);
        }

    }

}