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

    private boolean dedicatedPedometer = false;

    private int mSensorDelay = SensorManager.SENSOR_DELAY_NORMAL;

    // Android Sensors
    SensorManager mSensorManager;
    android.hardware.Sensor mPedometer;
    android.hardware.Sensor mAccelerometer;
    android.hardware.Sensor mMagnetometer;
    PedometerEventListener mPedometerListener;
    AccelerometerAsPedometerListener mFallbackPedometerListener;
    CompassEventListener mCompassListener;
    android.hardware.Sensor mFallbackTemperature;
    android.hardware.Sensor mFallbackHumidity;
    android.hardware.Sensor mFallbackPressure;
    FallbackTemperatureEventListener mFallbackTemperatureListener;
    FallbackHumidityEventListener mFallbackHumidityListener;
    FallbackPressureEventListener mFallbackPressureListener;

    boolean usingFallbackSensors = false;


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

        mFallbackTemperature = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE);
        mFallbackHumidity = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY);
        mFallbackPressure = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_PRESSURE);

        mPedometerListener = new PedometerEventListener();
        mCompassListener = new CompassEventListener();
        mFallbackPedometerListener = new AccelerometerAsPedometerListener();
    }

    public void startSensors(Context context) {
        mContext = context;
        startSensorTagConnector();
        startPedometer();
        startCompass();
    }

    public void stopSensors() {
        if (!usingFallbackSensors) {
            stopSensorTag();
        } else {
            stopFallbackSensors();
        }
        stopPedometer();
        stopCompass();
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
                startFallbackSensors();
                usingFallbackSensors = true;
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
            dedicatedPedometer = true;
            if(mPedometerListener==null){
                mPedometerListener = new PedometerEventListener();
            }
            mSensorManager.registerListener(mPedometerListener, mPedometer, mSensorDelay);
        }
        else{
            Log.w(TAG, "startPedometer Failed! Maybe this sensor is not on the device?");
            Log.w(TAG, "startPedometer Falling back to accelerometer");
            mSensorManager.registerListener(mFallbackPedometerListener, mAccelerometer, mSensorDelay);
        }
    }

    public void stopPedometer() {
        if(dedicatedPedometer) {
            mSensorManager.unregisterListener(mPedometerListener);
        }
        else{
            mSensorManager.unregisterListener(mFallbackPedometerListener);
        }
    }

    public void resetPedometer() {
        mPedometerListener.mFirstStep = true;
    }

    public void startCompass(){
        if(mAccelerometer!=null && mMagnetometer!=null) {
            mSensorManager.registerListener(mCompassListener, mAccelerometer, mSensorDelay);
            mSensorManager.registerListener(mCompassListener, mMagnetometer, mSensorDelay);
        }
        else{
            Log.e(TAG, "startCompass Failed! Accelerometer OR Magnetometer may be missing from the device");
        }
    }

    public void stopCompass(){
        mSensorManager.unregisterListener(mCompassListener, mAccelerometer);
        mSensorManager.unregisterListener(mCompassListener, mMagnetometer);
    }

    public void startFallbackSensors() {
        if (mFallbackTemperature == null) {
            Log.d(TAG, "No Temperature Sensor");
        } else {
            if (mFallbackTemperatureListener == null) {
                mFallbackTemperatureListener = new FallbackTemperatureEventListener();
            }
            mSensorManager.registerListener(mFallbackTemperatureListener, mFallbackTemperature, mSensorDelay);
        }

        if (mFallbackHumidity == null) {
            Log.d(TAG, "No Humidity Sensor");
        } else {
            if (mFallbackHumidityListener == null) {
                mFallbackHumidityListener = new FallbackHumidityEventListener();
            }
            mSensorManager.registerListener(mFallbackHumidityListener, mFallbackHumidity, mSensorDelay);
        }

        if (mFallbackPressure == null) {
            Log.d(TAG, "No Pressure Sensor");
        } else {
            if (mFallbackPressureListener == null) {
                mFallbackPressureListener = new FallbackPressureEventListener();
            }
            mSensorManager.registerListener(mFallbackPressureListener, mFallbackPressure, mSensorDelay);
        }
    }

    public void stopFallbackSensors() {
        mSensorManager.unregisterListener(mFallbackTemperatureListener, mFallbackTemperature);
        mSensorManager.unregisterListener(mFallbackHumidityListener, mFallbackHumidity);
        mSensorManager.unregisterListener(mFallbackPressureListener, mFallbackPressure);
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
            broadcastUpdate(SensorListenerInterface.HikeSensors.PEDOMETER, stepcount);
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
            //Do nothing
        }
    }

    public class FallbackTemperatureEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(TAG, "Fallback Temperature Called");
            double value = event.values[0];
            broadcastUpdate(SensorListenerInterface.HikeSensors.TEMPERATURE, value);
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
            // Do nothing
        }
    }

    public class FallbackHumidityEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            double value = event.values[0];
            broadcastUpdate(SensorListenerInterface.HikeSensors.HUMIDITY, value);
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

        }
    }

    public class FallbackPressureEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            double value = event.values[0];

            // Convert from hectoPascals to kiloPascals
            value = value * 0.1;

            broadcastUpdate(SensorListenerInterface.HikeSensors.PRESSURE, value);
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

        }
    }

    /**
     * Fail safe class to provide step count readings in the absence of a built-in pedometer
     * This class is heavily inspired by code written by M.A. Chan, B.Eng in the DataSampling app
     * used in Concordia's ELEC390/COEN390 course.
     * The Authors have maintained all original comments and notices, where applicable
     */
    public class AccelerometerAsPedometerListener implements SensorEventListener{

        /**
         * ELEC390 and COEN390: TI SensorTag Library for Android
         * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
         *     Modified by: Javier E. Fajardo <foxtrotzulu94@gmail.com>
         * Institution: Concordia University
         */

        public final static int ACC_EVENT_COOLDOWN_MS = 500;

        /** High pass filter time constant. */
        public final static int ACC_FILTER_TAU_MS = 50;

        /** Acceleration magnitude threshold. A "shake" is detected if the magnitude of the acceleration
         * vector, after filtering, is above this value. */
        public final static double ACC_THRESHOLD = 0.8;

        /** Previous acceleration value. */
        private float[] mLastAcc = null;

        /** Previous acceleration output value of the high-pass filter. */
        private float[] mLastFiltAcc = null;
        /**
         * When this value is less than ACC_EVENT_COOLDOWN_MS, we are in cooldown mode and do not detect
         * acceleration shake events. This is set to 0 whenever an event is detected, and incremented by
         * the sample period at every new sample received while in cooldown mode. When this value is
         * equal to or greater than ACC_EVENT_COOLDOWN_MS, we are in normal detection mode.
         */
        private int mCooldownCounterMs = ACC_EVENT_COOLDOWN_MS;
        private int SensorUpdatePeriod = 200;

        private double stepsTaken=0;

        private double normalizedValue=0.0f;

        private final static double EPSILON = 0.00001;


        /**
         * This method filters the accelerometer values according to M.A. Chan's Code
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (mLastAcc == null) {
                
                Log.d(TAG, "onSensorChanged First try");
                mLastAcc = new float[3];
                System.arraycopy(event.values, 0, mLastAcc, 0, event.values.length);
                mLastFiltAcc = new float[]{0,0,0};
            }

            // Apply the high-pass filter.
            mLastFiltAcc = applyFilter(SensorUpdatePeriod, event.values, mLastAcc, mLastFiltAcc);
            System.arraycopy(event.values, 0, mLastAcc, 0, event.values.length);

            // If the cooldown timer is already expired, we can try and detect a shake
            if (mCooldownCounterMs >= ACC_EVENT_COOLDOWN_MS) {

                // if the magnitude of the acceleration exceeds the shake threshold
                normalizedValue=(magnitude(mLastFiltAcc));

                if (normalizedValue > ACC_THRESHOLD) {
                    Log.d(TAG, "Accelerometer shake detected");
                    // reset/start the cooldown timer
                    mCooldownCounterMs=0;
                    stepsTaken+=1;
                    broadcastUpdate(SensorListenerInterface.HikeSensors.PEDOMETER, stepsTaken);
                }

            }
            else{
                mCooldownCounterMs +=SensorUpdatePeriod;
            }

        }
        private float[] applyFilter(long samplePeriodMs, float[] newInput, float[] prevInput,
                                    float[] prevOutput) {
		/**
		 * The accelerometer always detects a 1.0g gravity component, but we don't know what the
		 * SensorTag's orientation is so we don't necessarily know which direction the gravity
		 * component is.
		 *
		 * We can assume that it is slow moving (the user won't be rotating the SensorTag very
		 * quickly ... or if they do we can detect that as a "shake" anyway!). A high-pass filter
		 * will therefore remove the acceleration element and allow us to only capture faster
		 * events.
		 *
		 * The implementation here is a simple first-order high-pass filter:
		 *
		 * H(s) = (s RC) / (1 + s RC)
		 *
		 * where RC is the time constant the cutoff frequency is f_c = 1/(2*pi*RC).
		 *
		 * By applying the bilinear transformation we can get a discrete time implementation of this
		 * filter, expressed here in the time domain:
		 *
		 * y[n] := k * (y[n-1] + x[n] - x[n-1])
		 *
		 * where x[n] is the filter input signal, y[n] is the output signal, n is the sample index,
		 * and k is an arbitrary real constant which is related to the time constant. The system
		 * time constant tau is equal to:
		 *
		 * tau = T k / (1 - k)
		 *
		 * where T is the sample period of the signal in seconds.
		 *
		 * We implement this filter below individually to each of the acceleration components, using
		 * a history of one sample point (since the filter never needs to go more than one sample
		 * point behind).
		 *
		 * More information:
		 * https://en.wikipedia.org/wiki/High-pass_filter#Discrete-time_realization
		 */

            // Calculate the needed parameters
            float k = (float) ACC_FILTER_TAU_MS / (ACC_FILTER_TAU_MS + samplePeriodMs);

            // These variable names are used just to make the code closer to the description above
            float[] yn, yn1, xn, xn1;
            yn1 = prevOutput;
            xn = newInput;
            xn1 = prevInput;

            // Apply the filter to each component of the 3D vector separately
            yn = new float[]{
                    k * (yn1[0] + xn[0] - xn1[0]),
                    k * (yn1[1] + xn[1] - xn1[1]),
                    k * (yn1[2] + xn[2] - xn1[2])};

//            Log.v(TAG, "ACC FILTER: " + String.format("%.2f,%.2f,%.2f", xn[0], xn[1], xn[2]) + " -> " +
//                    String.format("%.2f,%.2f,%.2f", yn[0], yn[1], yn[2]));

            return yn;
        }

        @Override
        public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
            //Do nothing
        }

        private float magnitude(float[] vector){
            float retVal = 0;
            for (int i = 0; i < vector.length; i++) {
                retVal+= (vector[i]*vector[i]);
            }
            return (float) Math.sqrt(retVal);
        }

        private float[] normalize(float[] vector){
            float maxVal = 0;
            float[] retVal = new float[vector.length];
            for (int i = 0; i < vector.length; i++) {
                if(Math.abs(vector[i])>maxVal){
                    maxVal=Math.abs(vector[i]);
                }
            }
            for (int i = 0; i < vector.length; i++) {
                retVal[i] = (vector[i]/maxVal);
            }
            return retVal;
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