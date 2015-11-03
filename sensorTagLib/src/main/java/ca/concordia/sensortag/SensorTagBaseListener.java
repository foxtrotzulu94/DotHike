/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */

package ca.concordia.sensortag;

import android.util.*;
import ti.android.util.*;

/**
 * A default implementation of the SensorTagListener. All of the sensor update methods are
 * implemented to do nothing with the received measurements (i.e. they are no-ops);
 * {@link #onError()} and {@link #onStatus()} log status information to the Android logcat.
 * 
 * You can use this class as a base class for your own SensorTagListener implementations by
 * overriding the methods corresponding to the sensors of interest. Any other sensor measurements
 * received will call the no-op base class method and be ignored. This is a convenience class: the
 * purpose of using this class as a base class, instead of directly implementing
 * {@link SensorTagListener} is that you don't need to define methods for sensors you don't care
 * about.
 * 
 * Example: if you only need to do something with the accelerometer and none of the other
 * sensors (maybe they're all disabled) you can write:
 * 
 * <pre>
 * {@code
 * public class AccelerometerListener extends SensorTagBaseListener {
 * 	   \@Override
 *     void onUpdateAccelerometer(SensorTagManager mgr, Point3D acceleration) {
 *         // your code goes here to do something with a new acceleration value
 *     }
 * }
 * }
 * </pre>
 */
public class SensorTagBaseListener implements SensorTagListener {
	static final String TAG = "SensorTag";

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onError(SensorTagManager, SensorTagManager.ErrorType, String)
	 */
	@Override
	public void onError(SensorTagManager mgr, SensorTagManager.ErrorType type, String msg) {
		Log.e(TAG, msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onStatus(SensorTagManager, SensorTagManager.StatusType, String)
	 */
	@Override
	public void onStatus(SensorTagManager mgr, SensorTagManager.StatusType type, String msg) {
		Log.i(TAG, msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateAccelerometer(SensorTagManager, ti.android.util.Point3D)
	 */
	@Override
	public void onUpdateAccelerometer(SensorTagManager mgr, Point3D acc) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateAmbientTemperature(SensorTagManager, double)
	 */
	@Override
	public void onUpdateAmbientTemperature(SensorTagManager mgr, double temp) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateBarometer(SensorTagManager, double)
	 */
	@Override
	public void onUpdateBarometer(SensorTagManager mgr, double pressure, double height) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateGyroscope(SensorTagManager, ti.android.util.Point3D)
	 */
	@Override
	public void onUpdateGyroscope(SensorTagManager mgr, Point3D ang) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateHumidity(SensorTagManager, double)
	 */
	@Override
	public void onUpdateHumidity(SensorTagManager mgr, double rh) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateInfraredTemperature(SensorTagManager, double)
	 */
	@Override
	public void onUpdateInfraredTemperature(SensorTagManager mgr, double temp) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateKeys(SensorTagManager, boolean, boolean)
	 */
	@Override
	public void onUpdateKeys(SensorTagManager mgr, boolean left, boolean right) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagListener#onUpdateMagnetometer(SensorTagManager, ti.android.util.Point3D)
	 */
	@Override
	public void onUpdateMagnetometer(SensorTagManager mgr, Point3D b) {
	}
}
