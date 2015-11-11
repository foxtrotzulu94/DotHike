package me.dotteam.dotprod.hw;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ca.concordia.sensortag.SensorTagListener;
import ca.concordia.sensortag.SensorTagLoggerListener;
import ca.concordia.sensortag.SensorTagManager;
import ti.android.ble.sensortag.Sensor;
import ti.android.util.Point3D;

/**
 * Created by as on 2015-10-23.
 */
public class HikeHardwareManager implements SensorTagConnector.STConnectorListener {
    private String TAG = "HHM";
    private SensorTagManager mSensorTagManager;
    private SensorTagManagerListener mSensorTagManagerListener;
    private SensorTagConnector mSTConnector;
    private List<SensorListenerInterface> mSensorListenerList;
    private boolean mSTConnected = false;
    private Context mContext;
    private static HikeHardwareManager mInstance;
    private int samplingFrequency = 2000; //In milliseconds. 1000ms = 1s. MAX:2550ms


    public static HikeHardwareManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HikeHardwareManager(context);
        }
        return mInstance;
    }

    private HikeHardwareManager(Context context) {
        mContext = context;
        mSensorTagManagerListener = new SensorTagManagerListener();
        mSensorListenerList = new ArrayList<SensorListenerInterface>();
    }

    public void startSensorTagConnector() {
        mSTConnector = new SensorTagConnector(mContext);
        mSTConnector.addListener(this);
    }


    public void addListener(SensorListenerInterface sensorListenerInterface){
        Log.d(TAG, "Adding Listener");
        mSensorListenerList.add(sensorListenerInterface);
    }

    public void removeListener(SensorListenerInterface sensorListenerInterface) {
        Log.d(TAG, "Removing Listener");
        mSensorListenerList.remove(sensorListenerInterface);
    }

    @Override
    public void onSensorTagConnect(BluetoothDevice btdevice) {
        Log.d(TAG, "PARTY!! We have a bluetooth device!");
        mSensorTagManager = new SensorTagManager(mContext, btdevice);
        mSensorTagManager.addListener(mSensorTagManagerListener);

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

    @Override
    public void onSensorTagDisconnect() {
        mSensorTagManager.disableUpdates();
        mSTConnected = false;
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

            Log.d(TAG, "onUpdateTemperature Called");
            for (int i = 0; i < mSensorListenerList.size(); i++) {
                mSensorListenerList.get(i).update(SensorListenerInterface.HikeSensors.TEMPERATURE, temp);
            }

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

            for (int i = 0; i < mSensorListenerList.size(); i++) {
                mSensorListenerList.get(i).update(SensorListenerInterface.HikeSensors.PRESSURE, pressure);
            }
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

            for (int i = 0; i < mSensorListenerList.size(); i++) {
                mSensorListenerList.get(i).update(SensorListenerInterface.HikeSensors.HUMIDITY, rh);
            }
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