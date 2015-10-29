/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */

package ca.concordia.sensortag;

import ti.android.util.*;
import android.util.*;

/**
 * An implementation of {@link SensorTagListener} that logs every new sensor measurement to the
 * Android Logcat (a system-wide log you can access via the LogCat view in Eclipse, or using
 * {@code adb logcat} in a shell). This is provided as a debugging convenience class&mdash;if you
 * extend this class (and make sure to call the superclass method, e.g. call
 * {@code super.onUpdateAccelerometer(acc)} inside your
 * {@code void onUpdateAccelerometer(Point3D acc)}), then every data point received by the sensor
 * will be sent to the log.
 * 
 * Example: if you only need to do something with the accelerometer and none of the other sensors
 * (maybe they're all disabled) you can write:
 * 
 * <pre>
 * {@code
 * public class AccelerometerListener extends SensorTagLoggerListener {
 * 	   \@Override
 *     void onUpdateAccelerometer(SensorTagManager mgr, Point3D acc) {
 *         // call the superclass's onUpdateAccelerometer
 *         // i.e. {@link SensorTagLoggerListener#onUpdateAccelerometer(Point3D)}
 *         super.onUpdateAccelerometer(acc);
 *         
 *         // ... your code goes here to do something with a new acceleration value ...
 *     }
 * }
 * }
 * </pre>
 * 
 * Usage with {@link SensorTagManager} is as given in the documentation for
 * {@link SensorTagListener}.
 */
public class SensorTagLoggerListener extends SensorTagBaseListener {
	static final String TAG = "SensorTag";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.concordia.sensortag.SensorTagBaseListener#onUpdateAccelerometer(SensorTagManager, ti.android.util.Point3D)
	 */
	@Override
	public void onUpdateAccelerometer(SensorTagManager mgr, Point3D acc) {
		super.onUpdateAccelerometer(mgr, acc);
		Log.d(TAG, "Accelerometer: " + acc.toString() + "g");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onUpdateAmbientTemperature(SensorTagManager, double)
	 */
	@Override
	public void onUpdateAmbientTemperature(SensorTagManager mgr, double temp) {
		super.onUpdateAmbientTemperature(mgr, temp);
		Log.d(TAG, "Ambient temperature: " + Double.toString(temp) + "°C");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onUpdateBarometer(SensorTagManager, double)
	 */
	@Override
	public void onUpdateBarometer(SensorTagManager mgr, double pressure, double height) {
		super.onUpdateBarometer(mgr, pressure, height);
		Log.d(TAG,
				"Barometer: " + Double.toString(pressure) + "kPa (height: "
						+ Double.toString(height) + "m)");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onUpdateGyroscope(SensorTagManager, ti.android.util.Point3D)
	 */
	@Override
	public void onUpdateGyroscope(SensorTagManager mgr, Point3D ang) {
		super.onUpdateGyroscope(mgr, ang);
		Log.d(TAG, "Gyroscope: " + ang.toString() + "deg/s");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onUpdateHumidity(SensorTagManager, double)
	 */
	@Override
	public void onUpdateHumidity(SensorTagManager mgr, double rh) {
		super.onUpdateHumidity(mgr, rh);
		Log.d(TAG, "Humidity: " + Double.toString(rh) + "%");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onUpdateInfraredTemperature(SensorTagManager, double)
	 */
	@Override
	public void onUpdateInfraredTemperature(SensorTagManager mgr, double temp) {
		super.onUpdateInfraredTemperature(mgr, temp);
		Log.d(TAG, "IR temperature: " + Double.toString(temp) + "°C");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onUpdateKeys(SensorTagManager, boolean, boolean)
	 */
	@Override
	public void onUpdateKeys(SensorTagManager mgr, boolean left, boolean right) {
		super.onUpdateKeys(mgr, left, right);
		Log.d(TAG,
				"Keys: left[" + Boolean.toString(left) + "] " + "right[" + Boolean.toString(right)
						+ "] ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ca.concordia.sensortag.SensorTagBaseListener#onUpdateMagnetometer(SensorTagManager, ti.android.util.Point3D)
	 */
	@Override
	public void onUpdateMagnetometer(SensorTagManager mgr, Point3D b) {
		super.onUpdateMagnetometer(mgr, b);
		Log.d(TAG, "Magnetometer: " + b.toString() + "uT");
	}
}
