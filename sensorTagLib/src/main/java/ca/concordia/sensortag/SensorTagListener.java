/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */

package ca.concordia.sensortag;

import ti.android.util.*;

/**
 * A Listener interface for the {@link SensorTagManager}. All SensorTag updates (sensor
 * measurements, SensorTag status updates, etc.) are received through a class implementing this
 * interface. To do so, an instance of this implementation must be passed to
 * {@link SensorTagManager#addListener()} and updates must be enabled using
 * {@link SensorTagManager#enableUpdates()}. Once this is done, whenever an update is available, the
 * SensorTagManager will call the appropriate SensorTagListener method in a background thread.
 * 
 * Each SensorTagManager will call SensorTagListener callback methods only in <em>one</em>
 * background thread. This is regardless of the number of SensorTagListener objects registered using
 * {@link SensorTagManager#addListener(SensorTagListener)}. For example, suppose that one
 * SensorTag has both the accelerometer and gyroscope enabled. The SensorTag sends an accelerometer
 * value immediately followed by a gyroscope value. SensorTagManager will call
 * {@link #onUpdateAccelerometer(SensorTagManager, Point3D)} for each SensorTagListener, one at a
 * time, and <em>will wait for each of them to finish</em> before executing the next one. Once all
 * of them have executed, it will execute {@link #onUpdateGyroscope(SensorTagManager, Point3D)} for
 * each SensorTagListener, again one at a time. If new measurements are received or other events
 * happen during this time, they will be queued up and only execute after the previous callback
 * calls have completed.  It is therefore highly recommended that each SensorTagListener callback
 * be quick to execute; if you need to do more processing, you can use mechanisms such as spawning
 * your own worker threads, posting to a {@link android.os.Handler} that has its own dedicated
 * thread, or using {@link android.app.Activity#runOnUiThread(Runnable)} if you need to run code
 * in the main thread specifically.
 * 
 * Example for a SensorTagListener that only needs to process Accelerometer data (note: this is
 * <b>not</b> the recommended approach; see {@link SensorTagBaseListener} below, and if you use the
 * SensorTagBaseListener approach instead, you can ignore the below example).
 * 
 * <pre>
 * {@code
 * public class AccelerometerListener implements SensorTagListener {
 * 	   \@Override
 *     void onUpdateAccelerometer(SensorTagManager mgr, Point3D acceleration) {
 *         // ... your code goes here to do something with a new acceleration value ...
 *     }
 *     
 * 	   \@Override
 *     void onUpdateGyroscope(SensorTagManager mgr, Point3D ang) {
 *         // empty function because we don't want to use the gyroscope's measurements
 *     }
 *     
 * 	   \@Override
 *     void onUpdateMagnetometer(SensorTagManager mgr, Point3D bField) {
 *         // empty function because we don't want to use the magnetometer's measurements
 *     }
 *     
 *     // etc. for every method defined in SensorTagListener
 * }
 * }
 * </pre>
 * 
 * After you have defined this class, you can instantiate it and pass it to a SensorTagManager
 * instance. (In an Activity, you probably want to do this in the {@code onCreate(Bundle)} method).
 * The code would look like this:
 * 
 * <pre>
 * {@code
 * SensorTagManager stManager;
 * SensorTagListener stListener;
 * 
 * stManager = new SensorTagManager(\/* ... arguments here ... *\/);
 * stListener = new AccelerometerListener();
 * stManager.addListener(stListener);
 * }
 * </pre>
 * 
 * Some basic SensorTagListener implementations are provided in this library, which you can use as a
 * base class for your own class:
 * 
 * <ol>
 * <li> {@link SensorTagBaseListener}: This Listener does nothing with sensor data. It basically
 * allows you to omit certain methods in your own implementation (instead of making it an empty
 * methods if you need it to do nothing) and is a convenience class. This listener also logs onError
 * and onStatus messages to the Android logcat.
 * 
 * For example, if you only need to do something with the accelerometer and none of the other
 * sensors (maybe they're all disabled) you can write:
 * 
 * <pre>
 * {@code
 * public class AccelerometerListener extends SensorTagBaseListener {
 * 	   \@Override
 *     void onUpdateAccelerometer(SensorTagManager mgr, Point3D acceleration) {
 *         // ... your code goes here to do something with a new acceleration value ...
 *     }
 * }
 * }
 * </pre>
 * 
 * </li>
 * <li> {@link SensorTagLoggerListener}: This Listener logs every new sensor measurement to the
 * Android Logcat (a system-wide log you can access via the LogCat view in Eclipse, or using
 * {@code adb logcat} in a shell). This is provided as a debugging convenience class&mdash;if you
 * extend this class (and make sure to call the superclass method, e.g. call
 * {@code super.onUpdateAccelerometer(acc)} inside your
 * {@code void onUpdateAccelerometer(Point3D acc)}), then every data point received by the sensor
 * will be sent to the log.</li>
 * </ol>
 * 
 * @see SensorTagBaseListener
 * @see SensorTagLoggerListener
 */
public interface SensorTagListener {
	/**
	 * Called to notify the listener that an error has occurred.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param type
	 *            The type of error.
	 * @param msg
	 *            A detailed user-friendly error message. This message is of the style
	 *            "Failed to configure the sensor", "Bluetooth service discovery failed", etc.; a
	 *            more user-friendly message that does not mention details may be desired in some
	 *            contexts.
	 */
	void onError(SensorTagManager mgr, SensorTagManager.ErrorType type, String msg);

	/**
	 * Called to notify the listener of a change in SensorTag status which is not an error.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param type
	 *            The type of status change.
	 * @param msg
	 *            A detailed user-friendly status message. This message is of the style
	 *            "Sensor X configured", "Bluetooth service discovery completed", etc.; this message
	 *            may be useful in logging or some user status information, but in many cases it
	 *            probably does not need to be shown to the user or a less detailed, friendlier
	 *            message may be shown instead.
	 */
	void onStatus(SensorTagManager mgr, SensorTagManager.StatusType type, String msg);

	/**
	 * Called whenever a new ambient temperature measurement is received from the IR temperature
	 * sensor. This is the temperature of the silicon die inside the temperature sensor chip
	 * (visible in the hole in the front/top of the SensorTag).
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param temp
	 *            Ambient temperature (temperature of the sensor chip) in degrees Celsius.
	 */
	void onUpdateAmbientTemperature(SensorTagManager mgr, double temp);

	/**
	 * Called whenever a new IR object temperature measurement is received from the IR temperature
	 * sensor. This is the the temperature sensed by the sensor visible in the hole in the front/top
	 * of the SensorTag; it uses infrared light to detect the temperature of an object in front of
	 * it. See the SensorTag documentation (or ELEC390 tutorials/materials) for exact specs and best
	 * position for an accurate reading.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param temp
	 *            Target object temperature in degrees Celsius.
	 */
	void onUpdateInfraredTemperature(SensorTagManager mgr, double temp);

	/**
	 * Called whenever a new atmospheric pressure measurement is received.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param pressure
	 *            The measured atmospheric pressure in kilopascals (kPa). Multiply by 10 to obtain
	 *            millibars (mBar).
	 * @param height
	 *            The height difference relative to the calibration height, in meters.
	 */
	void onUpdateBarometer(SensorTagManager mgr, double pressure, double height);

	/**
	 * Called whenever a new humidity measurement is received. Note that the air holes on the back
	 * of the SensorTag allow air to reach the hygrometer; make sure these holes are not blocked
	 * and/or sufficient circulation is available for sensing.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param rh
	 *            Relative humidity in percent (%).
	 */
	void onUpdateHumidity(SensorTagManager mgr, double rh);

	/**
	 * Called whenever a new accelerometer measurement is received.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param acc
	 *            Acceleration in rectangular coordinates in multiples of the Earth gravitational
	 *            constant (g, 1g = 9.81 m/s^2).
	 */
	void onUpdateAccelerometer(SensorTagManager mgr, Point3D acc);

	/**
	 * Called whenever a new gyroscope measurement is received.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param ang
	 *            Angular velocity about the x, y, z axes in degrees per second.
	 */
	void onUpdateGyroscope(SensorTagManager mgr, Point3D ang);

	/**
	 * Called whenever a new magnetometer measurement is received.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param b
	 *            Magnetic field strength (a.k.a. magnetic flux density, B field) in microtesla
	 *            (uT).
	 */
	void onUpdateMagnetometer(SensorTagManager mgr, Point3D b);

	/**
	 * Called whenever one of the keys is pressed or released.
	 * 
	 * @param mgr
	 *            The SensorTagManager instance that is calling the Listener. This allows you to
	 *            obtain information about the specific SensorTag or sensor that sent the message
	 *            (e.g. its currently configured measurement period) and change the configuration of
	 *            the SensorTag if needed (e.g. enable/disable sensors).
	 * @param left
	 *            Status of the left key (true when pressed).
	 * @param right
	 *            Status of the right key (true when pressed).
	 */
	void onUpdateKeys(SensorTagManager mgr, boolean left, boolean right);
}
