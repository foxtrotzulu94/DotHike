/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */

package ca.concordia.sensortag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ti.android.ble.common.BluetoothLeService;
import ti.android.ble.sensortag.BarometerCalibrationCoefficients;
import ti.android.ble.sensortag.MagnetometerCalibrationCoefficients;
import ti.android.ble.sensortag.Sensor;
import ti.android.ble.sensortag.SensorTag;
import ti.android.ble.sensortag.SimpleKeysStatus;
import ti.android.util.Point3D;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Manages communication with a TI SensorTag device and all its sensors over a Bluetooth connection.
 * This is the main class needed to interact with the SensorTag sensors from an
 * application/activity.
 * 
 * You must have established a connection to the SensorTag via Bluetooth and to the GATT Server
 * before using this class (the {@link DeviceSelectActivity} allows the user to select the SensorTag
 * device, and then connects to it and passes the BluetoothDevice object to your application's
 * Activity, which can then instantiate a SensorTag). You should <em>only use one instance of
 * SensorTagManager</em> per connection to a SensorTag device.
 * 
 * This class allows you to configure sensors (enable/disable them and set how often to receive
 * measurements from each one). Note that a <em>maximum of four sensors can be enabled</em>; this is
 * a limitation of the Bluetooth 4.0 LE Notification feature that is used to receive measurements
 * whenever they are available. This limitation applies to the current Bluetooth connection;
 * therefore, <em>a sensor that is enabled and later disabled still counts towards the four-sensor
 * limit</em>; you can only "reset" the enabled sensors by disconnecting and reconnecting to the
 * Bluetooth device.
 * 
 * This class also deals with receiving measurement data from the sensors, processing them into a
 * useful form (e.g. splitting up different measurements and scaling to a useful unit), and then
 * forwarding them to listeners.
 * 
 * In order to receive measurements from the SensorTag, you need to provide a listener to the
 * SensorTagManager by calling {@link #addListener(SensorTagListener)} and passing your own
 * implementation of {@link SensorTagListener} (for instance, a subclass of
 * {@link SensorTagBaseListener}).
 * 
 * Here is an example of how to set up the SensorTagManager (for an Android Activity or Service,
 * this would probably be in the onCreate() method):
 * 
 * <pre>
 * {@code
 * 
 * // You need the BluetoothDevice corresponding to the SensorTag you want to connect to
 * BluetoothDevice mBtDevice = \/* ... Get the BluetoothDevice of the SensorTag somehow ... *\/;
 * 
 * // Now you can create the SensorTagManager; pass it the context (Activity or Service; we use
 * // "this" because we're assuming that this code is inside your Activity or Service, not some
 * // other class), and also pass it the BluetoothDevice you found above
 * mStManager = new SensorTagManager(this, mBtDevice);
 * 
 * // Next, we create an instance of the SensorTagListener, which will get called by the SensorTagManager
 * // whenever the SensorTagManager gets a new measurement or status update from the  SensorTag.
 * // Note that "CustomSensorTagListener" stands for a custom class that <em>you</em> defined which
 * // implements SensorTagListener or extends one of SensorTagBaseListener/SensorTagLoggerListener.
 * // See those classes for more information.
 * mStListener = new CustomSensorTagListener();
 * 
 * // Tell the SensorTagManager to use our new Listener.
 * mStManager.addListener(mStListener);
 * 
 * // Now we can tell the SensorTagManager to initialise the SensorTag services (i.e. prepare the
 * // connection and get a list of all the sensors available). Note that this method call can take
 * // several seconds because it waits for the operation to complete (this makes it simpler to use).
 * mStManager.initServices();
 * 
 * // Now we check if the initServices() call failed... (Optional, but it's always better to check
 * // for errors before proceeding!)
 * if (!mStManager.isServicesReady()) {
 *     // initServices failed or took too long; you can do some LogCat logging or show an error
 * }
 * 
 * // Here we only enable one sensor, the Accelerometer. This call can take several seconds as well.
 * mStManager.enableSensor(Sensor.ACCELEROMETER);
 * 
 * // Finally, tell the SensorTagManager to start calling our Listener when it receives measurements
 * mStManager.enableUpdates();
 * 
 *  // Note: It might be desirable to call enableUpdates() in onResume() instead, and also call
 *  // disableUpdates() in onPause(). This means that you tell SensorTagManager to stop sending
 *  // updates when the app is in the background (not on-screen), and to start sending updates
 *  // again when it's in the foreground---this saves CPU/battery when the user isn't using your
 *  // application (but it's open in the background, the way Android likes to do things).
 * }
 * </pre>
 * 
 * You probably want to clean up after yourself when the application closes. Inside your Activity or
 * Service's onDestroy:
 * 
 * <pre>
 * {@code
 * mStManager.close();
 * }
 * </pre>
 */
public class SensorTagManager {

	/**
	 * Error categories, as passed to a listener class via
	 * {@link SensorTagListener#onError(ErrorType, String)}.
	 */
	public enum ErrorType {
		SERVICE_DISCOVERY_FAILED, // Discovery of SensorTag services failed.
		GATT_UNKNOWN_MESSAGE, // Received an unknown type of GATT message from the SensorTag.
		GATT_REQUEST_FAILED, // An outgoing GATT request failed.
		SENSOR_CONFIG_FAILED, // Failed to configure one of the sensors.
		UNDEFINED // Other errors.
	}

	/**
	 * Status message categories, as passed to a listener class via
	 * {@link SensorTagListener#onStatus(StatusType, String)}.
	 */
	public enum StatusType {
		SERVICE_DISCOVERY_STARTED, SERVICE_DISCOVERY_COMPLETED, //
		CALIBRATED_MAGNETOMETER, CALIBRATED_HEIGHT_MEASUREMENT, UNDEFINED
	}

	/**
	 * Internal class for tracking each sensor's current configuration. Every time a sensor is
	 * configured via a GATT request to the SensorTag, it is also updated here.
	 */
	private class SensorConfig {
		/** True if the sensor is enabled. */
		public boolean enabled;
		/** The sensor's update period (update rate) in milliseconds. */
		public int period;

		SensorConfig(boolean enabled, int period) {
			super();
			this.enabled = enabled;
			this.period = period;
		}
	}

	// Constants
	public static final String TAG = "SensorTagManager";
	public static final String HANDLER_THREAD_NAME = "SensorTagManager-GattHandler";
	public static final int GATT_TIMEOUT_MS = 500;
	// Time after GATT request completes before issuing a new request
	public static final int GATT_GRACE_MS = 100;
	public static final int SERVICES_TIMEOUT_MS = 15000;
	public static final double BARO_PA_PER_METER = 12.0;

	// Configuration: Android objects
	private final Context mContext;

	// Configuration: Bluetooth and GATT connection objects
	private final BluetoothLeService mBleService;
	private final BluetoothGatt mBtGatt;

	// State: Service Discovery
	private List<BluetoothGattService> mServices;
	private boolean mServicesReady;
	private final Object mServiceLock;

	// Internal: Broadcast receivers and handlers (from the BleService)
	private final GattUpdateReceiver mGattUpdateReceiver;
	private final Handler mGattReceiverHandler;
	private final HandlerThread mGattReceiverThread;

	// State: GATT requests
	private boolean mGattRequestPending;
	private final Object mGattRequestLock;

	// State: sensor configuration and pending requests (via GATT)
	private final Map<Sensor, SensorConfig> mSensorConfigs;
	private boolean mHeightCalibrateRequest;
	private boolean mMagCalibrateRequest;

	// Listeners
	private final List<SensorTagListener> mListeners;
	private final Object mListenerLock;
	private boolean mIsUpdatesEnabled;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Android context, e.g. the Activity that owns this SensorTagManager.
	 * @param bluetoothDevice
	 *            The BluetoothDevice corresponding to the connected SensorTag device.
	 */
	public SensorTagManager(Context context, BluetoothDevice bluetoothDevice) {
		super();
		mContext = context;
		mBleService = BluetoothLeService.getInstance();
		mBtGatt = BluetoothLeService.getBtGatt();

		mServices = null;
		mServicesReady = false;

		// Set up the GattUpdateReceiver (declared below). This receiver handles all messages coming
		// from the BleService, which manages the Bluetooth connection and GATT communication under-
		// the-hood. It also manages some Bluetooth messages from Android.
		// These messages are received as Broadcasts. We want to handle them in a background thread,
		// not in the main thread, to allow asynchronous handling (a perk for the Listeners too!),
		// which requires a Handler with a separate HandlerThread.
		// a background thread not the main thread
		mGattUpdateReceiver = new GattUpdateReceiver();
		mGattReceiverThread = new HandlerThread(HANDLER_THREAD_NAME);
		mGattReceiverThread.start();
		mGattReceiverHandler = new Handler(mGattReceiverThread.getLooper());
		mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter(), null,
				mGattReceiverHandler);
		mGattRequestPending = false; // status flag: outgoing request is pending

		// Set up the sensor configuration structures: these are used to locally remember the
		// configurations requested by the user & sent to the SensorTag via Bluetooth
		mSensorConfigs = new HashMap<Sensor, SensorConfig>(Sensor.SENSOR_LIST.length);
		for (Sensor sensor : Sensor.SENSOR_LIST) {
			mSensorConfigs.put(sensor, new SensorConfig(false, sensor.getDefaultPeriod()));
		}
		// Flags for user requests to calibrate a sensor
		mMagCalibrateRequest = false;
		mHeightCalibrateRequest = false;

		// Set up variables related to storing listeners and configuration
		mListeners = new LinkedList<SensorTagListener>();
		mIsUpdatesEnabled = false;

		// Concurrency objects - internal
		mServiceLock = new Object();
		mGattRequestLock = new Object();
		mListenerLock = new Object();
	}

	/**
	 * Generates the IntentFilter for GATT broadcasts from the BluetoothLeService.
	 * 
	 * @return The intent filter for GATT broadcasts
	 */
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("ti.android.ble.common.ACTION_GATT_SERVICES_DISCOVERED");
		intentFilter.addAction("ti.android.ble.common.ACTION_DATA_NOTIFY");
		intentFilter.addAction("ti.android.ble.common.ACTION_DATA_WRITE");
		intentFilter.addAction("ti.android.ble.common.ACTION_DATA_READ");
		intentFilter.addAction("ti.android.ble.common.ACTION_DATA_SET_NOTIF");
		return intentFilter;
	}

	/*
	 * Initialisation and uninitialisation of the SensorTag device
	 */

	/**
	 * Initialise the SensorTag's services. This method starts service discovery (to get the list of
	 * all available services from the SensorTag) and waits up to 15 seconds for the process to
	 * complete.
	 * 
	 * This method will trigger a call to all added Listeners depending on the result of the
	 * initialisation: On success, a {@link SensorType#SERVICE_DISCOVERY_COMPLETED} is passed to
	 * {@link SensorTagListener#onStatus()}; on failure, a
	 * {@link ErrorType#SERVICE_DISCOVERY_FAILED} is passed to {@link SensorTagListener#onError()}.
	 * 
	 * This method should be called <em>once</em> upon creation of your activity (e.g.
	 * {@code onCreate()}).
	 * 
	 * This method is synchronous and waits for up to {@link #SERVICES_TIMEOUT_MS} to complete. It
	 * is highly recommended that you run this method in a separate thread and not in the main
	 * thread in order to avoid the application GUI becoming unresponsive during that time.
	 */
	public void initServices() {
		Log.d(TAG, "initServices()");
		if (isServicesReady()) {
			Log.w(TAG, "initServices() called when already configured");
			return;
		}

		discoverServices();
		try {
			synchronized (mServiceLock) {
				mServiceLock.wait(SERVICES_TIMEOUT_MS);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			if (!mServicesReady) {
				publishError(ErrorType.SERVICE_DISCOVERY_FAILED,
						"Service discovery failed or timed out");
			}
		}
	}

	/**
	 * Start service discovery and return immediately. Service discovery occurs asynchronously.
	 */
	private void discoverServices() {
		Log.d(TAG, "discoverServices()");
		mServicesReady = false;
		if (mBtGatt != null && mBtGatt.discoverServices()) {
			Log.i(TAG, "Service discovery started");
			publishStatus(StatusType.SERVICE_DISCOVERY_STARTED, "Service discovery started");
			return;
		}
		publishError(ErrorType.SERVICE_DISCOVERY_FAILED, "Service discovery start failed");
	}

	/**
	 * If service discovery has been performed and services were discovered, returns true; otherwise
	 * returns false.
	 * 
	 * @see #initServices()
	 */
	public boolean isServicesReady() {
		return (mServicesReady && mBleService.getNumServices() > 0);
	}

	/**
	 * Stop the SensorTagManager and clean up. This method must be called before the owning Activity
	 * is destroyed in order to clean up resources.
	 */
	@SuppressWarnings("deprecation")
	public void close() {
		Log.d(TAG, "close()");
		try {
			mContext.unregisterReceiver(mGattUpdateReceiver);
			mGattReceiverThread.quitSafely();
			mGattReceiverThread.join(1000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			if (mGattReceiverThread.isAlive()) { // interrupt() hasn't stopped the thread yet
				Log.w(TAG, "Failed to stop GATT receiver thread");
				mGattReceiverThread.stop(); // TODO: unsafe ... better way of handling situation?
			}
		}
	}

	/*
	 * Configuration of sensors
	 */
	/**
	 * Enable a sensor. The sensor will send a new measurement according to default measurement
	 * period, i.e. every {@link Sensor#getDefaultPeriod()} milliseconds (you can call this method
	 * and say send it to LogCat to find out what this period is).
	 * 
	 * This method sends a message via Bluetooth to the SensorTag's GATT server to enable the sensor
	 * and to send a notification whenever a measurement is taken, and then waits for
	 * acknowledgement of these messages.
	 * 
	 * This method is synchronous and waits for up to 2x {@link #GATT_TIMEOUT_MS} to complete (4x
	 * for the barometer). It is highly recommended that you run this method in a separate thread
	 * and not in the main thread in order to avoid the application GUI becoming unresponsive during
	 * that time.
	 * 
	 * Only 4 sensors can be enabled throughout the duration of a Bluetooth connection to the
	 * SensorTag. This is due to a limitation of the Bluetooth LE Notification feature which lets
	 * sensors push new measurements to the Android device, which cannot configure more than 4
	 * notifications throughout the life of the connection. Note that a sensor disabled using
	 * {@link disableSensor()} still counts against this 4-notification limit.
	 * 
	 * @param sensor
	 *            The sensor to enable.
	 * @return True on success, false on failure.
	 * 
	 * @see #enableSensor(Sensor, int)
	 */
	public boolean enableSensor(Sensor sensor) {
		Log.d(TAG, "enableSensor: " + sensor.name());
		SensorConfig sensorConfig = mSensorConfigs.get(sensor);
		if (sensorConfig != null && !sensorConfig.enabled) {

			boolean res;
			// Barometer needs to be calibrated - purely automated
			if (sensor == Sensor.BAROMETER) {
				calibrateBarometer();
			}

			// enable/power on the sensor (except simple keys service)
			if(sensor != Sensor.SIMPLE_KEYS) {
				res = configureSensor(sensor, true);
				if (!res) return false;
			}

			res = configureNotification(sensor, true);
			if (!res) return false;

			sensorConfig.enabled = true;
		}
		else {
			Log.w(TAG, "Attempted to enable sensor when already enabled");
		}
		return true;
	}

	/**
	 * Enable a sensor with a custom configuration. If the sensor is already enabled, set the
	 * configuration.
	 * 
	 * This method is the same as {@link #enableSensor(Sensor)}, with the addition of a configurable
	 * measurement update period (how often a new measurement is read from the sensor).
	 * 
	 * Note that this method will wait up to 3x {@link #GATT_TIMEOUT_MS} for the configuration GATT
	 * requests to complete (5x for the barometer); the same threading recommendations apply.
	 * 
	 * It is possible that setting the period fails with certain sensors. This depends on the
	 * version of firmware installed on the SensorTag device itself. For example, with Version 4 of
	 * the firmware, only the following sensors are supported: accelerometer, magnetometer and
	 * barometer. In this case, {@code false} is returned but the sensor will be configured as if
	 * you called {@link #enableSensor(Sensor)}.
	 * 
	 * You can test whether the installed version of the SensorTag firmware supports the period
	 * feature beforehand by calling {@link #isPeriodSupported(Sensor)}. You can also use this
	 * manual method:
	 * 
	 * <ol>
	 * <li>In {@link ti.android.ble.sensortag.SensorTag}, find the UUID of the PERI (period) feature
	 * for the sensor(s) you're interested in.</li>
	 * <li>Correctly initialise the SensorTagManager and call {@link #initServices()}.</li>
	 * <li>Monitor the logcat for "onGetCharacteristic()" lines for your specific Device. This is a
	 * list of Characteristics that are supported by the SensorTag device.
	 * <li>Find the UUID in each line and check if the UUIDs you're interested in (step 1) are
	 * present. If they are, the feature is supported; otherwise it is not supported in the
	 * installed firmware version.</li>
	 * </ol>
	 * 
	 * Note that this method will only give you information about each individual SensorTag and is
	 * not a general programmatic solution.
	 * 
	 * @param sensor
	 *            The sensor to enable.
	 * @param period
	 *            The measurement period (in milliseconds). This is the rate at which a measurement
	 *            will be taken from the sensor and sent to the application; the minimum is sensor-
	 *            dependent and given by {@link Sensor#getMinPeriod()}, while the maximum is 2550
	 *            ms. The period must be a multiple of 10ms. Values outside the range will be set to
	 *            the nearest valid value, and values that are not a multiple of 10ms will be
	 *            rounded to the nearest multiple of 10ms.
	 * @return True on success, false on failure.
	 * @see #enableSensor(Sensor)
	 */
	public boolean enableSensor(Sensor sensor, int period) {
		Log.d(TAG, "enableSensor: " + sensor.name() + " with update period " + period + "ms");

		boolean res;
		SensorConfig sensorConfig = this.mSensorConfigs.get(sensor);

		if (sensorConfig != null && !sensorConfig.enabled) {
			UUID configUuid = sensor.getConfig();

			// Handle the SimpleKeys case: no period
			if (configUuid == null) {
				Log.w(TAG, "Simple Keys does not have an update period.");
				res = enableSensor(sensor);
			}
			else {
				// Barometer needs to be calibrated - automatic
				if (configUuid != null && configUuid.equals(SensorTag.UUID_BAR_CONF)) {
					calibrateBarometer();
				}

				// Enable the sensor and notification
				res = configureSensor(sensor, true);
				if (!res) return false;

				byte periodValue = this.calculateSensorPeriodValue(period);
				res = configureSensorPeriod(sensor, periodValue);
				if (!res) return false;

				res = this.configureNotification(sensor, true);
				if (!res) return false;

				sensorConfig.period = periodValue * 10;
				sensorConfig.enabled = true;
				return true;
			}
		}
		else {
			Log.w(TAG, "Sensor already enabled");
			byte periodValue = this.calculateSensorPeriodValue(period);
			res = configureSensorPeriod(sensor, periodValue);
			if (!res) return false;

			sensorConfig.period = periodValue * 10;
			return true;
		}
		return res;
	}

	/**
	 * Calculate the byte value for the sensor period to send to the GATT PERI characteristic, from
	 * the desired period in milliseconds. This method will round the period to the nearest 10ms
	 * multiple and latches to the range 100ms to 2550ms.
	 * 
	 * @param period_ms
	 *            Desired period in milliseconds.
	 * @return Byte value of the period. The effective period is 10*return value.
	 */
	private byte calculateSensorPeriodValue(int period_ms) {
		if (period_ms < 100) {
			Log.w(TAG, "Period " + period_ms + "ms too low; setting to 100ms.");
			period_ms = 100;
		}
		else if (period_ms > 2550) {
			Log.w(TAG, "Period " + period_ms + "ms too high; setting to 2550ms.");
			period_ms = 2550;
		}
		int period_val = (period_ms + 5) / 10;
		int rounded_period_ms = period_val * 10;
		if (period_ms != rounded_period_ms) {
			Log.w(TAG, "Update period " + period_ms + "ms not a multiple of 10ms; rounding to "
					+ rounded_period_ms + "ms");
		}
		return (byte) (period_val & 255);
	}

	/**
	 * Internal method. Configures the sensor (enable/disable) via a message over Bluetooth to the
	 * SensorTag GATT server, and then waits for the request to be acknowledged (up to
	 * {@link #GATT_TIMEOUT_MS}).
	 * 
	 * @param sensor
	 *            The sensor to configure
	 * @param enable
	 *            Whether to enable (true) or disable (false) the sensor.
	 * @return True on success, false on failure.
	 */
	private boolean configureSensor(final Sensor sensor, final boolean enable) {
		UUID serviceUuid = sensor.getService();
		UUID configUuid = sensor.getConfig();
		boolean res;
		Log.d(TAG,
				"configureSensor: " + (enable ? "enable" : "disable") + " sensor[" + sensor.name()
						+ "] service[" + serviceUuid + "] conf[" + configUuid + "]");

		BluetoothGattService service =
				mBtGatt.getService(serviceUuid != null ? serviceUuid : new UUID(0L, 0L));
		if (service == null) {
			Log.e("SensorTagManager", "configureSensor: Null service");
			return false;
		}

		BluetoothGattCharacteristic config =
				service.getCharacteristic(configUuid != null ? configUuid : new UUID(0L, 0L));
		if (config == null) {
			Log.e("SensorTagManager", "configureSensor: Null characteristic");
			return false;
		}

		byte writeValue = (enable ? sensor.getEnableSensorCode() : Sensor.DISABLE_SENSOR_CODE);

		try {
			synchronized (mGattRequestLock) {
				mGattRequestPending = true;
				mBleService.writeCharacteristic(config, writeValue);
				mGattRequestLock.wait(GATT_TIMEOUT_MS);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			res = true;
			if (mGattRequestPending) {
				mGattRequestPending = false;
				publishError(ErrorType.SENSOR_CONFIG_FAILED,
						"GATT request timeout [configureSensor]");
				res = false; // don't want to return inside a finally block
			}
		}
		return res;
	}

	/**
	 * Internal method. Configures the sensor's measurement period via a message over Bluetooth to
	 * the SensorTag GATT server, and then waits for the request to be acknowledged (up to
	 * {@link #GATT_TIMEOUT_MS}).
	 * 
	 * @param sensor
	 *            The sensor to configure
	 * @param value
	 *            The period value as sent to the SensorTag GATT server, in tens of milliseconds
	 *            (x10ms).
	 * @return True on success, false on failure.
	 */
	private boolean configureSensorPeriod(Sensor sensor, byte value) {
		UUID serviceUuid = sensor.getService();
		UUID periodUuid = sensor.getPeriod();
		boolean res;
		Log.d(TAG, "Setting update period: sensor[" + sensor.name() + "] service[" + serviceUuid
				+ "] period[" + periodUuid + "] value[" + (((int) value & 0xFF) * 10) + "ms]");

		BluetoothGattService service =
				mBtGatt.getService(serviceUuid != null ? serviceUuid : new UUID(0L, 0L));
		if (service == null) {
			Log.e("SensorTagManager", "configureSensorPeriod: Null service");
			return false;
		}

		BluetoothGattCharacteristic period = service.getCharacteristic(periodUuid);
		if (period == null) {
			Log.e("SensorTagManager", "configureSensorPeriod: Null characteristic");
			return false;
		}

		try {
			synchronized (mGattRequestLock) {
				mGattRequestPending = true;
				mBleService.writeCharacteristic(period, value);
				mGattRequestLock.wait(GATT_TIMEOUT_MS);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			res = true;
			if (mGattRequestPending) {
				mGattRequestPending = false;
				publishError(ErrorType.SENSOR_CONFIG_FAILED,
						"GATT request timeout [configureSensorPeriod]");
				res = false; // don't want to return inside a finally block
			}
		}
		return res;
	}

	/**
	 * Check whether the connected SensorTag's firmware supports setting the measurement period (how
	 * often measurements are sent from a sensor, in milliseconds per measurement).
	 * 
	 * @param sensor
	 *            The sensor to check.
	 * @return If true, you can set the sensor period using {@link #enableSensor(Sensor, int)}. If
	 *         false, you cannot set the sensor period and should use {@link #enableSensor(Sensor)}
	 *         instead.
	 */
	public boolean isPeriodSupported(Sensor sensor) {
		UUID serviceUuid = sensor.getService();
		UUID periodUuid = sensor.getPeriod();
		BluetoothGattService service = mBtGatt.getService(serviceUuid);
		if (service == null) {
			Log.e("SensorTagManager", "i: Null service");
			return false;
		}
		BluetoothGattCharacteristic period = service.getCharacteristic(periodUuid);
		return (period != null);
	}

	/**
	 * Internal method. Enables/disables the notification for new measurements via a message over
	 * Bluetooth to the SensorTag GATT server, and then waits for the request to be acknowledged (up
	 * to {@link #GATT_TIMEOUT_MS}).
	 * 
	 * @param sensor
	 *            The sensor to configure
	 * @param enable
	 *            Whether to enable (true) or disable (false) the notifications.
	 * @return True on success, false on failure.
	 */
	private boolean configureNotification(Sensor sensor, boolean enable) {
		UUID serviceUuid = sensor.getService();
		UUID dataUuid = sensor.getData();
		boolean res;
		Log.d(TAG, "configureNotification: sensor[" + sensor.name() + "] service[" + serviceUuid
				+ "] data[" + dataUuid + "]");

		BluetoothGattService service = mBtGatt.getService(serviceUuid);
		if (service == null) {
			Log.e("SensorTagManager", "configureNotification: Null service");
			return false;
		}

		BluetoothGattCharacteristic data = service.getCharacteristic(dataUuid);
		if (data == null) {
			Log.e("SensorTagManager", "configureNotification: Null characteristic");
			return false;
		}

		try {
			synchronized (mGattRequestLock) {
				mGattRequestPending = true;
				mBleService.setCharacteristicNotification(data, enable);
				mGattRequestLock.wait(GATT_TIMEOUT_MS);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			res = true;
			if (mGattRequestPending) {
				mGattRequestPending = false;
				publishError(ErrorType.SENSOR_CONFIG_FAILED,
						"GATT request timeout [configureNotification]");
				res = false; // don't want to return inside a finally block
			}
		}
		return res;
	}

	/**
	 * Internal method. Send a request to calibrate the barometer and then reads the calibration
	 * data, both via messages over Bluetooth to the SensorTag GATT server. Waits for each request
	 * to be completed (up to {@link #GATT_TIMEOUT_MS} each - two requests).
	 * 
	 * @return True on success, false on failure.
	 */
	private boolean calibrateBarometer() {
		UUID serviceUuid = Sensor.BAROMETER.getService();
		UUID configUuid = Sensor.BAROMETER.getConfig();
		boolean res;

		Log.d(TAG, "calibrateBarometer: requested calibration; service[" + serviceUuid + "] conf["
				+ configUuid + "]");

		BluetoothGattService service = mBtGatt.getService(serviceUuid);
		if (service == null) {
			Log.e("SensorTagManager", "calibrateBarometer: Null service");
			return false;
		}

		BluetoothGattCharacteristic config = service.getCharacteristic(configUuid);
		if (config == null) {
			Log.e("SensorTagManager", "calibrateBarometer: Null characteristic");
			return false;
		}

		BluetoothGattCharacteristic baroCal = service.getCharacteristic(SensorTag.UUID_BAR_CALI);
		if (baroCal == null) {
			Log.e("SensorTagManager", "calibrateBarometer: Null barometer calibration");
			return false;
		}

		try {
			synchronized (mGattRequestLock) {
				mGattRequestPending = true;
				mBleService.writeCharacteristic(config, Sensor.CALIBRATE_SENSOR_CODE);
				mGattRequestLock.wait(GATT_TIMEOUT_MS);

				mGattRequestPending = true;
				mBleService.readCharacteristic(baroCal);
				mGattRequestLock.wait(GATT_TIMEOUT_MS);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			res = true;
			if (mGattRequestPending) {
				mGattRequestPending = false;
				publishError(ErrorType.SENSOR_CONFIG_FAILED,
						"GATT request timeout [calibrateBarometer]");
				res = false;
			}
		}
		return res;
	}

	/**
	 * Disable a sensor. Causes that sensor to stop taking measurements and stop sending
	 * notifications of measurements.
	 * 
	 * This method sends a message via Bluetooth to the SensorTag's GATT server to enable the sensor
	 * and to send a notification whenever a measurement is taken (according to the measurement
	 * period), and then waits for acknowledgement of the messages.
	 * 
	 * This method is synchronous and waits for up to 2x {@link #GATT_TIMEOUT_MS} to complete (4x
	 * for the barometer). It is highly recommended that you run this method in a separate thread
	 * and not in the main thread in order to avoid the application GUI becoming unresponsive during
	 * that time.
	 * 
	 * A sensor disabled using disableSensor() counts against the 4-notification limit. See
	 * {@link #enableSensor(Sensor)}.
	 * 
	 * @param sensor
	 *            The sensor to disable.
	 * @return True on success, false on failure.
	 * 
	 * @see #enableSensor(Sensor)
	 */
	public boolean disableSensor(Sensor sensor) {
		Log.d(TAG, "disableSensor: " + sensor.name());
		final SensorConfig sensorConfig = this.mSensorConfigs.get(sensor);
		if (sensorConfig != null && sensorConfig.enabled) {
			boolean res;
			res = configureSensor(sensor, false);
			if (!res) return false;

			res = configureNotification(sensor, false);
			if (!res) return false;

			sensorConfig.enabled = false;
		}
		else {
			Log.w(TAG, "Attempted to disable sensor when not enabled");
		}
		return true;
	}

	/**
	 * Request a barometer height calibration. This sets the next barometer measurement as the zero
	 * metre (0m) height value.
	 * 
	 * The calibration will apply <em>after</em> the next barometer measurement has been received.
	 */
	public void calibrateHeight() {
		Log.d(TAG, "calibrateHeight: requested calibration");
		this.mHeightCalibrateRequest = true;
	}

	/**
	 * Request a magnetometer calibration. This sets the next magnetometer measurement as the zero
	 * value.
	 * 
	 * The calibration will apply <em>after</em> the next magnetometer measurement has been
	 * received.
	 */
	public void calibrateMagnetometer() {
		Log.d(TAG, "calibrateMagnetometer: requested calibration");
		MagnetometerCalibrationCoefficients.INSTANCE.val.x = 0.0;
		MagnetometerCalibrationCoefficients.INSTANCE.val.y = 0.0;
		MagnetometerCalibrationCoefficients.INSTANCE.val.z = 0.0;
		this.mMagCalibrateRequest = true;
	}

	/**
	 * Check if a sensor is enabled.
	 * 
	 * @param sensor
	 *            The sensor to check.
	 * @return True if enabled, false if disabled or an internal configuration error occurred.
	 */
	public boolean isSensorEnabled(final Sensor sensor) {
		SensorConfig config = this.mSensorConfigs.get(sensor);
		return (config != null && config.enabled);
	}

	/**
	 * Get the currently configured sensor measurement period.
	 * 
	 * @param sensor
	 *            The sensor to check.
	 * @return The current sensor measurement period in milliseconds (ms).
	 */
	public int getSensorPeriod(final Sensor sensor) {
		final SensorConfig sensorConfig = this.mSensorConfigs.get(sensor);
		if (sensorConfig != null && sensorConfig.period > 0) {
			return sensorConfig.period;
		}
		// if it's not set, assume it's the default
		return 10 * (int) sensor.getDefaultPeriod();
	}

	/*
	 * SensorTag Listeners
	 */
	/**
	 * Enables passing updates to the registered {@link SensorTagListener} objects. This does not
	 * affect sensor enabled/disabled, but only whether measurements received from the sensor are
	 * sent to the Listeners for this Manager. This applies to all sensors.
	 * 
	 * It is recommended to call this method in the Activity/Service {@code onResume()} method.
	 * 
	 * Status (onStatus) and error (onError) messages related to the SensorTag Bluetooth connection
	 * or the SensorTagManager will be sent to the Listener regardless of this setting.
	 * 
	 * @see #disableUpdates()
	 * @see #addListener(SensorTagListener)
	 */
	public void enableUpdates() {
		Log.d(TAG, "enableUpdates()");
		this.mIsUpdatesEnabled = true;
	}

	/**
	 * Disables passing updates to the registered {@link SensorTagListener} objects. This does not
	 * affect sensor enabled/disabled status, but only whether measurements received from the sensor
	 * are sent to the Listeners for this Manager. This applies to all sensors.
	 * 
	 * It is recommended to call this method in the Activity/Service {@code onPause()} method.
	 * 
	 * Status (onStatus) and error (onError) messages related to the SensorTag Bluetooth connection
	 * or the SensorTagManager will be sent to the Listener regardless of this setting.
	 * 
	 * @see #enableUpdates()
	 */
	public void disableUpdates() {
		Log.d(TAG, "disableUpdates()");
		this.mIsUpdatesEnabled = false;
	}

	/**
	 * Check whether sensor measurement updates are being passed to registered
	 * {@link SensorTagListener} objects.
	 * 
	 * @see #disableUpdates()
	 * @see #enableUpdates()
	 * @see #addListener(SensorTagListener)
	 */
	public boolean isUpdatesEnabled() {
		return this.mIsUpdatesEnabled;
	}

	/**
	 * Registers a new listener with the SensorTagManager. Listeners will receive measurement
	 * updates from the sensors as well as status updates related to the SensorTag Bluetooth
	 * connection and the Manager, when updates are enabled (see {@link isUpdatesEnabled()}).
	 * 
	 * @param listener
	 *            The listener to add. If it is null or already in the list of registered Listeners,
	 *            no action will be taken.
	 */
	public void addListener(SensorTagListener listener) {
		if (listener == null) return;
		synchronized (mListenerLock) {
			if (!mListeners.contains(listener)) {
				mListeners.add(listener);
			}
		}
	}

	/**
	 * Removes a previously registered listener. That listener will no longer receive updates from
	 * the SensorTagManager and the sensors.
	 * 
	 * @param listener
	 *            The listener to remove. If it is null or does not exist in the list of registered
	 *            Listeners, no action will be taken.
	 */
	public void removeListener(SensorTagListener listener) {
		if (listener == null) return;
		synchronized (mListenerLock) {
			// remove all listener instances, if somehow there's more than one
			while (mListeners.remove(listener))
				;
		}
	}

	/**
	 * Publish a SensorTag/SensorTagManager status message to all registered listeners.
	 * 
	 * @param status
	 *            The type of status message. This can be used to determine the type of status
	 *            programmatically in order to decide the action to be taken, if any, or to show the
	 *            user a custom message.
	 * @param msg
	 *            A human-readable error message containing details about the status. This message
	 *            is not necessarily "user friendly" as it simply states the technical nature of the
	 *            status; it is up to the implementation to decide whether it is worth showing to
	 *            the user, with this message or a custom one.
	 */
	protected void publishStatus(StatusType status, final String msg) {
		ArrayList<SensorTagListener> callableListeners;
		synchronized (mListenerLock) {
			callableListeners = new ArrayList<SensorTagListener>(mListeners);
		}
		for (SensorTagListener callListener : callableListeners) {
			callListener.onStatus(this, status, msg);
		}
	}

	/**
	 * Publish a SensorTag/SensorTagManager error message to all registered listeners.
	 * 
	 * @param error
	 *            The type of error message. This can be used to determine the type of error
	 *            programmatically in order to decide the action to be taken, if any, or to show the
	 *            user a custom error message.
	 * @param msg
	 *            A human-readable error message containing details about the error. This message is
	 *            not necessarily "user friendly" as it states the technical cause of the error in
	 *            simple terms; it is up to the implementation to decide whether it is worth showing
	 *            to the user, with this message or a custom one.
	 */
	protected void publishError(ErrorType error, String msg) {
		ArrayList<SensorTagListener> callableListeners;
		synchronized (mListenerLock) {
			callableListeners = new ArrayList<SensorTagListener>(mListeners);
		}
		for (SensorTagListener callListener : callableListeners) {
			callListener.onError(this, error, msg);
		}
	}

	/**
	 * Publish a received sensor measurement.
	 * 
	 * @param dataUuidStr
	 *            The UUID of the sensor's Data characteristic.
	 * @param byteValue
	 *            The value received from the sensor.
	 */
	protected void publishMeasurement(String dataUuidStr, final byte[] byteValue) {
		final Sensor sensor = Sensor.getFromDataUuid(UUID.fromString(dataUuidStr));

		Point3D vectorValue = null;
		if (sensor != Sensor.SIMPLE_KEYS) {
			vectorValue = sensor.convert(byteValue);
			if (vectorValue == null) {
				Log.e(TAG, "Failed to read " + sensor.name() + " value.");
				Log.d(TAG, "uuid[" + dataUuidStr + "] data" + Arrays.toString(byteValue));
				return;
			}
		}

		ArrayList<SensorTagListener> callableListeners;
		synchronized (mListenerLock) {
			callableListeners = new ArrayList<SensorTagListener>(mListeners);
		}

		switch (sensor) {
		case IR_TEMPERATURE:
			for (SensorTagListener callListener : callableListeners) {
				callListener.onUpdateInfraredTemperature(this, vectorValue.y);
				callListener.onUpdateAmbientTemperature(this, vectorValue.x);
			}
			break;
		case ACCELEROMETER:
			for (SensorTagListener callListener : callableListeners) {
				callListener.onUpdateAccelerometer(this, vectorValue);
			}
			break;
		case HUMIDITY:
			for (SensorTagListener callListener : callableListeners) {
				callListener.onUpdateHumidity(this, vectorValue.x);
			}
			break;
		case MAGNETOMETER:
			for (SensorTagListener callListener : callableListeners) {
				callListener.onUpdateMagnetometer(this, vectorValue);
			}
			break;
		case GYROSCOPE:
			for (SensorTagListener callListener : callableListeners) {
				callListener.onUpdateGyroscope(this, vectorValue);
			}
			break;
		case BAROMETER: {
			double pressureKPa = vectorValue.x / 1000.0;
			double height = (vectorValue.x - BarometerCalibrationCoefficients.INSTANCE.heightCalibration)
					/ BARO_PA_PER_METER;
			for (SensorTagListener callListener : callableListeners) {
				callListener.onUpdateBarometer(this, pressureKPa, height);
			}
		}
			break;

		case SIMPLE_KEYS: {
			SimpleKeysStatus convertKeys = sensor.convertKeys(byteValue);
			boolean left = convertKeys.equals(SimpleKeysStatus.ON_ON)
					|| convertKeys.equals(SimpleKeysStatus.ON_OFF);
			boolean right = convertKeys.equals(SimpleKeysStatus.ON_ON)
					|| convertKeys.equals(SimpleKeysStatus.OFF_ON);
			for (SensorTagListener callListener : callableListeners) {
				callListener.onUpdateKeys(this, left, right);
			}
		}
			break;
		default: {
			Log.e(TAG, "Unknown sensor UUID: uuid[" + dataUuidStr + "]");
			break;
		}
		}
		return;
	}

	/*
	 * GATT broadcast handlers
	 */
	/**
	 * BroadcastReceiver for messages coming from the SensorTag GATT server (via the
	 * BluetoothLeService). This class handles all messages coming from the SensorTag device via
	 * Bluetooth (GATT).
	 */
	class GattUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int status = intent.getIntExtra("ti.android.ble.common.EXTRA_STATUS", 0);
			byte[] value = intent.getByteArrayExtra("ti.android.ble.common.EXTRA_DATA");
			String uuidStr = intent.getStringExtra("ti.android.ble.common.EXTRA_UUID");

			if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				onServicesDiscovered(status);
			}
			else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
				onCharacteristicChanged(uuidStr, value);
			}
			else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
				onCharacteristicWrite(uuidStr, status);
			}
			else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
				onCharacteristicsRead(uuidStr, value, status);
			}
			else if (BluetoothLeService.ACTION_DATA_SET_NOTIF.equals(action)) {
				onSetNotification(uuidStr, status);
			}
			else {
				Log.w("SensorTagManager", "GATT unknown action received: " + action);
				publishError(ErrorType.GATT_UNKNOWN_MESSAGE, "GATT unknown action received: "
						+ action);
			}

			if (status != 0) {
				Log.w("SensorTagManager", "Received GATT error code: " + status);
				publishError(ErrorType.GATT_REQUEST_FAILED, "GATT error code: " + status);
			}
		}
	}

	/**
	 * Handles a "Services Discovered" message from the SensorTag GATT server (via the
	 * BluetoothLeService). Depending on the result, sets the internal state of the SensorTagManager
	 * and sends error/status messages to listeners. Also notifies all threads waiting on service
	 * discovery to complete.
	 * 
	 * @param status
	 *            The GATT status value (BluetoothGatt.GATT_SUCCESS, etc.).
	 */
	private void onServicesDiscovered(int status) {
		synchronized (mServiceLock) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				mServicesReady = false;
				Log.e(TAG, "Service discovery failed.");
				publishError(ErrorType.SERVICE_DISCOVERY_FAILED, "Service discovery failed.");
				mServiceLock.notifyAll();
				return;
			}

			mServices = mBleService.getSupportedGattServices();
			if (mServices == null || mServices.size() <= 0) {
				mServicesReady = false;
				Log.e(TAG, "No services discovered.");
				publishError(ErrorType.SERVICE_DISCOVERY_FAILED, "No services discovered.");
				mServiceLock.notifyAll();
				return;
			}
			else {
				mServicesReady = true;
				Log.i(TAG, "Service discovery complete.");
				publishStatus(StatusType.SERVICE_DISCOVERY_COMPLETED, "Service discovery complete.");
				mServiceLock.notifyAll();
				return;
			}
		}
	}

	/**
	 * Handles a "Notification Enabled" message from the SensorTag GATT server (via the
	 * BluetoothLeService). Notifies all threads waiting on a GATT request to complete.
	 * 
	 * @param uuidDataStr
	 *            the string representing the Data UUID for the sensor.
	 * @param status
	 *            The GATT status value (BluetoothGatt.GATT_SUCCESS, etc.).
	 */
	private void onSetNotification(String uuidDataStr, int status) {
		Log.d(TAG, "Notification set: " + uuidDataStr);
		synchronized (mGattRequestLock) {
			mGattRequestPending = false;
			mGattRequestLock.notifyAll();
		}
	}

	/**
	 * Handles a "Characteristic Changed" (i.e. notification of a changed Bluetooth LE
	 * characteristic, such as a sensor value) message from the SensorTag GATT server (via the
	 * BluetoothLeService). Notifies all threads waiting on a GATT request to complete, and handles
	 * any pending calibration requests if necessary.
	 * 
	 * @param uuidDataStr
	 *            the string representing the Data UUID for the sensor.
	 * @param status
	 *            The GATT status value (BluetoothGatt.GATT_SUCCESS, etc.).
	 */
	private void onCharacteristicChanged(String uuidDataStr, byte[] value) {
		// First handle pending calibration requests
		if (mMagCalibrateRequest && uuidDataStr.equals(SensorTag.UUID_MAG_DATA.toString())) {
			Point3D magnetValue = Sensor.MAGNETOMETER.convert(value);
			MagnetometerCalibrationCoefficients.INSTANCE.val = magnetValue;
			mMagCalibrateRequest = false;

			Log.i(TAG, "Magnetometer calibrated: " + magnetValue);
			this.publishStatus(StatusType.CALIBRATED_MAGNETOMETER, "Magnetometer calibrated: "
					+ magnetValue);
		}

		if (mHeightCalibrateRequest && uuidDataStr.equals(SensorTag.UUID_BAR_DATA.toString())) {
			Point3D baroValue = Sensor.BAROMETER.convert(value);
			BarometerCalibrationCoefficients.INSTANCE.heightCalibration = baroValue.x;
			mHeightCalibrateRequest = false;

			Log.i(TAG,
					"Height measurement calibrated: zero meters set to "
							+ Double.toString(baroValue.x / 1000.0) + "kPa");
			publishStatus(
					StatusType.CALIBRATED_HEIGHT_MEASUREMENT,
					"Height measurement calibrated: zero meters set to "
							+ Double.toString(baroValue.x / 1000.0) + "kPa");
		}

		// publish to the listeners, if enabled
		if (mIsUpdatesEnabled) {
			publishMeasurement(uuidDataStr, value);
		}
	}

	/**
	 * Handles a "Characteristic Written" message (any configuration message like enabling sensors
	 * or setting period values) from the SensorTag GATT server (via the BluetoothLeService).
	 * Notifies all threads waiting on a GATT request to complete.
	 * 
	 * @param uuidDataStr
	 *            the string representing the written characteristic's UUID
	 * @param status
	 *            The GATT status value (BluetoothGatt.GATT_SUCCESS, etc.).
	 */
	private void onCharacteristicWrite(String uuidDataStr, int status) {
		Log.d(TAG, "Characteristic written: " + uuidDataStr);
		synchronized (mGattRequestLock) {
			// Before releasing the lock (and thus allowing new GATT requests to be issued),
			// we will wait a short grace period - prevents some observed issues with a subsequent
			// GATT request being ignored.
			try {
				Thread.sleep(GATT_GRACE_MS);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			this.mGattRequestPending = false;
			this.mGattRequestLock.notifyAll();
		}
	}

	/**
	 * Handles a "characteristic read" message (only used for the barometer calibration read) from
	 * the SensorTag GATT server (via the BluetoothLeService). Notifies all threads waiting on a
	 * GATT request to complete.
	 * 
	 * @param uuidStr
	 *            The string representing the UUID of the read characteristic.
	 * @param value
	 *            The value of the read characteristic.
	 * @param status
	 *            GATT The GATT status value (BluetoothGatt.GATT_SUCCESS, etc.).
	 */
	private void onCharacteristicsRead(String uuidStr, byte[] value, int status) {
		Log.d(TAG, "onCharacteristicsRead: uuid[" + uuidStr + "] value[" + Arrays.toString(value)
				+ "] status[" + status + "]");
		if (uuidStr.equals(SensorTag.UUID_BAR_CALI.toString())) {

			List<Integer> calib = new ArrayList<Integer>();
			for (int offset = 0; offset < 8; offset += 2) {
				Integer lowerByte = (int) value[offset] & 0xFF;
				Integer upperByte = (int) value[offset + 1] & 0xFF;
				calib.add((upperByte << 8) + lowerByte);
			}

			for (int offset = 8; offset < 16; offset += 2) {
				Integer lowerByte = (int) value[offset] & 0xFF;
				Integer upperByte = (int) value[offset + 1];
				calib.add((upperByte << 8) + lowerByte);
			}

			BarometerCalibrationCoefficients.INSTANCE.barometerCalibrationCoefficients = calib;
			Log.i(TAG, "Barometer calibrated");
			Log.v(TAG, "Barometer calibration: " + Arrays.toString(calib.toArray()));
		}

		synchronized (mGattRequestLock) {
			mGattRequestPending = false;
			mGattRequestLock.notifyAll();
		}
	}
}
