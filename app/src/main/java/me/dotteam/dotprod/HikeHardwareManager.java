package me.dotteam.dotprod;

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
public class HikeHardwareManager {
    private String TAG = "HHM";
    private SensorTagManager mSensorTagManager;
    private SensorTagManagerListener mSensorTagManagerListener;
    List<SensorListenerInterface> mSensorListenerList;


    public HikeHardwareManager(Context context, BluetoothDevice bluetoothDevice) {
        mSensorTagManager = new SensorTagManager(context, bluetoothDevice);
        mSensorTagManagerListener = new SensorTagManagerListener();
        mSensorListenerList = new ArrayList<SensorListenerInterface>();

        mSensorTagManager.addListener(mSensorTagManagerListener);

        mSensorTagManager.initServices();
        if (!mSensorTagManager.isServicesReady()) {
            // if initServices failed or took too long, log an error (in LogCat) and exit
            Log.e(TAG, "Discover failed - exiting");
            return;
        }

        mSensorTagManager.enableSensor(Sensor.IR_TEMPERATURE);
        mSensorTagManager.enableSensor(Sensor.HUMIDITY);
        mSensorTagManager.enableSensor(Sensor.BAROMETER);

        mSensorTagManager.enableUpdates();

    }


    void addListener(SensorListenerInterface sensorListenerInterface){
        Log.d(TAG, "Adding Listener");
        mSensorListenerList.add(sensorListenerInterface);
    }

    void enableSensorTag() {
        mSensorTagManager.enableUpdates();
    }

    void disableSensorTag() {
        mSensorTagManager.disableUpdates();
    }


    public class SensorTagManagerListener extends SensorTagLoggerListener implements SensorTagListener {

        /**
         * Called on receiving a new ambient temperature measurement from the SensorTagManager.
         * Displays the new value on the GUI.
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateAmbientTemperature(double)
         */

        @Override
        public void onUpdateAmbientTemperature(SensorTagManager mgr, double temp) {
            // ManagerListener inherits from SensorTagLoggerListener; we call the superclass
            // method (in SensorTagLoggerListener) in order for the superclass functionality to
            // run: in this case, it logs the value of the temperature measurement ("temp") to the
            // Android LogCat for debugging purposes.
            super.onUpdateAmbientTemperature(mgr, temp);

            Log.d(TAG, "onUpdateTemperature Called");
            for (int i = 0; i < mSensorListenerList.size(); i++) {
                mSensorListenerList.get(i).update(SensorListenerInterface.HikeSensors.TEMPERATURE, temp);
            }

        }

        /**
         * CHANGE ME: Accelerometer - This is where you can receive and process a new sensor
         * measurement. By default you will get a measurement every 1 or 2 seconds depending on
         * the sensor (sometimes you can change this: see later example apps).
         * <p/>
         * Before this sensor will work you MUST enableSensor() in onCreate!
         * <p/>
         * See SensorTagListener.java (in the SensorTagLib project) for more details about this
         * method.
         * <p/>
         * This method is optional: you don't need to put it here if you're not using this
         * sensor!
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateAccelerometer(ca.concordia.sensortag
         * .SensorTagManager, ti.android.util.Point3D)
         */
        @Override
        public void onUpdateAccelerometer(SensorTagManager mgr, Point3D acc) {
            // Call the superclass's method - leave this to keep logging the new value in LogCat
            super.onUpdateAccelerometer(mgr, acc);

			/*
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * CHANGE ME: See explanation in javadoc above
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 */
        }

        /**
         * CHANGE ME: Barometer - same as onUpdateAccelerometer above
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateBarometer(ca.concordia.sensortag
         * .SensorTagManager, double, double)
         */
        @Override
        public void onUpdateBarometer(SensorTagManager mgr, double pressure, double height) {
            super.onUpdateBarometer(mgr, pressure, height);

			/*
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * CHANGE ME: See explanation in javadoc above
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 */

            for (int i = 0; i < mSensorListenerList.size(); i++) {
                mSensorListenerList.get(i).update(SensorListenerInterface.HikeSensors.PRESSURE, pressure);
            }
        }

        /**
         * CHANGE ME: Barometer - same as onUpdateAccelerometer above
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateGyroscope(ca.concordia.sensortag
         * .SensorTagManager, ti.android.util.Point3D)
         */
        @Override
        public void onUpdateGyroscope(SensorTagManager mgr, Point3D ang) {
            super.onUpdateGyroscope(mgr, ang);

			/*
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * CHANGE ME: See explanation in javadoc above
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 */
        }

        /**
         * CHANGE ME: Barometer - same as onUpdateAccelerometer above
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
         * CHANGE ME: Barometer - same as onUpdateAccelerometer above
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateInfraredTemperature(ca.concordia
         * .sensortag.SensorTagManager, double)
         */
        @Override
        public void onUpdateInfraredTemperature(SensorTagManager mgr, double temp) {
            super.onUpdateInfraredTemperature(mgr, temp);

			/*
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * CHANGE ME: See explanation in javadoc above
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 */
        }

        /**
         * CHANGE ME: Barometer - same as onUpdateAccelerometer above
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateKeys(ca.concordia.sensortag.
         * SensorTagManager, boolean, boolean)
         */
        @Override
        public void onUpdateKeys(SensorTagManager mgr, boolean left, boolean right) {
            super.onUpdateKeys(mgr, left, right);

			/*
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * CHANGE ME: See explanation in javadoc above
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 */
        }

        /**
         * CHANGE ME: Barometer - same as onUpdateAccelerometer above
         *
         * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateMagnetometer(ca.concordia.sensortag
         * .SensorTagManager, ti.android.util.Point3D)
         */
        @Override
        public void onUpdateMagnetometer(SensorTagManager mgr, Point3D b) {
            super.onUpdateMagnetometer(mgr, b);

			/*
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * CHANGE ME: See explanation in javadoc above
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 */
        }

    }

}