/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Example application: Data/Event Sampler
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */
package ca.concordia.sensortag.datasample;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import ti.android.ble.sensortag.Sensor;
import ca.concordia.sensortag.SensorTagListener;
import ca.concordia.sensortag.SensorTagManager;
import ca.concordia.sensortag.SensorTagManager.ErrorType;
import ca.concordia.sensortag.SensorTagManager.StatusType;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Recording service. This service runs in the background in order to capture data from the
 * SensorTag and record the time at which events happened. It is responsible for establishing a
 * connection to the SensorTag, receiving data from it, capturing and storing events. This service
 * is intended to run in the background once started by the RecordActivity, even if the
 * RecordActivity is no longer on-screen. It is only capable of recording one set of data at a time.
 * 
 * To use this Service, an Activity must:
 * 
 * 0. Define a ServiceConnection (which is called when you bind or unbind to the Service) and
 *    instantiate it. This is an Android listener that is called when the service is bound
 *    (onServiceConnected) or unbound (onServiceDisconnected). See step 1 and 2.
 *    
 * 0. Define a RecordServiceListener and instantiate it. This is a listener interface defined by
 *    RecordService, and allows the RecordService to push status updates to the Activity (for
 *    example, when a recording stops due to a time limit or sample limit set up for the
 *    recording session). See step 3.
 * 
 * 1. Bind to the Service from the activity by calling
 * {@link android.app.Activity#bindService(Intent, android.content.ServiceConnection, int)}.
 * This gives you a {@link RecordService.Binder} object; this class has functions that let you
 * communicate between any bound Activity and the Service, and constitutes this Service's internal
 * API. (This is one of several methods for Activities to communicate with Services and only works
 * when the Service and Activity are part of the same app).
 * 
 * <pre>
 * Intent recordIntent = new Intent(this, RecordService.class);
 * bindService(recordIntent, mSvcConnection, Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT);
 * </pre>
 * 
 * You must have previously defined and instantiated a ServiceConnection (here it's mSvcConnection).
 * 
 * If the service was already running before being bound, and it had previously been configured with
 * a SensorTag device, you can use it immediately. Skip to Step 3.
 * 
 * If the service was NOT already running, then binding also starts the service. The service, in
 * this case, does not have a SensorTag device configured, so you need to do so in Step 2.
 * 
 * 2. To configure the Service (only needs to be done if the Service has not been configured since
 *    it was last started), call {@link Binder#startService(BluetoothDevice)} and pass it the
 *    SensorTag Bluetooth device it should connect to. This internally calls the startService()
 *    Android method, which will make the service a background service (in contrast to a bound
 *    service, which would be destroyed as soon as you unbind from it or the Activity is closed).
 *    This, in turn, calls {@link RecordService#onStartCommand(Intent, int, int)} to configure it.
 *    
 *    <pre>
 *    mRecSvc.startService(mBtDevice); // mBtDevice is the BluetoothDevice object for the SensorTag
 *    </pre>
 * 
 * 3. Register your RecordServiceListener with the RecordService by calling
 *    {@link Binder#addListener(RecordServiceListener)}.
 *    
 *    <pre>
 *    mRecSvc.addListener(mRecSvcListener); // where mRecSvcListener is your RecordServiceListener instance
 *    </pre>
 * 
 * 4. Call any of the the RecordService.Binder methods to issue commands to the Service.
 * 
 *    <pre>
 *    // example
 *    mRecSvc.setRecordDuration(10000); // milliseconds
 *    mRecSvc.record();
 *    </pre>
 * 
 * If an error occurs at any point, status will be set to ERROR, ERROR_SENSORTAG_NULL or
 * ERROR_SENSORTAG. It will fail any command sent to it (e.g. record or pause). In this situation,
 * the Activity must unbind from the service, stop it and restart it. The startService method may
 * also fail in this way---therefore, once the Activity has bound to the service, the first thing it
 * should check is the status of the service.
 * 
 * This is a "Local Service" --- it's a service intended to be called from an Activity belonging to
 * the same app as the Service. Because it uses the Binder's methods to communicate between Activity
 * and Service, and that another app's Activity would have no way of importing the
 * RecordService.Binder type, another app could never use this Service. (It also would need to have
 * some way of starting it---the service needs to be made public in the AndroidManifest.xml file).
 * 
 */
public class RecordService extends Service {
	static public final String TAG = "RecordSvc"; // Tag for Android's logcat
	static public final String EXTRA_DEVICE = "ca.concordia.datasample.EXTRA_RECORD_DEVICE";
	static public final String PREFERENCE_NAME = "ca.concordia.datasample.Recording";
	
	static public final int UPDATE_PERIOD_ACC_MS = 100;
	static public final int NOTIF_UPDATE_RATE_MS = 1000;
	static public final int SEC_TO_MSEC = 1000;
	
	static public final long RECORD_DURATION_INFINITE = 0;
	static public final int RECORD_SAMPLES_INFINITE = 0;
	private static final int DATA_SAVE_DELTA_THRESHOLD = 5;

	// SensorTag communication objects
	private BluetoothDevice mBtDevice;
	private SensorTagManager mStManager;
	private SensorTagListener mStListener;

	// Status of the current recording: this enum is used to track the service's state
	public enum Status {
		NOT_STARTED, STANDBY, RECORD, PAUSE, FINISHED, ERROR, ERROR_SENSORTAG_NULL, ERROR_SENSORTAG;
	};

	// State
	private boolean mIsStarted = false;
	private Status mStatus = Status.NOT_STARTED; // Also saved in RecordingData
	private SensorTagManager.ErrorType mErrorSensorTag = null;
	private String mStatusMessage = null;

	private String mAppName = null;
	private SharedPreferences mPrefs = null;
	private RecordingData mData = null;
	
	private NotificationManager mNotificationManager;
	private Runnable mNotificationUpdateRunner;
	private Handler mNotificationUpdateHandler;
	
	// We use CopyOnWriteArraySet instead of a regular Set/List (e.g. ArrayList) in order to avoid
	// thread concurrency issues: if one thread is reading the Set to notify listeners and another
	// thread calls addListeners() to add a new listener, the add operation will COPY the original
	// list, modify it and replace the old one with the new one. Existing iterators will continue
	// to point to the old one, which will later be garbage collected when it is no longer in use.
	private CopyOnWriteArraySet<RecordServiceListener>  mListeners;

	/**
	 * Called when the service is first created (regardless of whether it is bound or started).
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Initialise status members
		mIsStarted = false;
		mStatus = Status.NOT_STARTED; // should not be a setStatus() call: default initialisation
		mErrorSensorTag = null;
		mAppName = getString(R.string.app_name);

		// Obtain the SharedPreferences for storing recording data and the RecordingData structure.
		mPrefs = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		mData = new RecordingData(mPrefs);
		
		// Create the handler and runner that are responsible for updating the notification in the
		// notif bar (it is set up in onStartCommand later)
		mNotificationUpdateRunner = new UpdateNotificationRunner();
		mNotificationUpdateHandler = new Handler();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// Listeners container
		mListeners = new CopyOnWriteArraySet<RecordServiceListener>();
	}

	/**
	 * Called when the Service is started via startService() (not when it is bound). Sets up the
	 * SensorTag connection and reads the existing recorded data and settings from a previous
	 * session, if any.
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		/*
		 * If we're currently recording and someone calls startService again, do not reinitialise
		 * the Service, just abort. If we are not recording, then we can consider this new call as
		 * an "update" call (to re-configure the service with a new BluetoothDevice, for example):
		 * in that case, do reinitialise the service.
		 */
		if (mIsStarted && (getStatus() == Status.RECORD || getStatus() == Status.PAUSE)) {
			return START_STICKY;
		}

		if(intent != null) {
			BluetoothDevice dev = (BluetoothDevice) intent.getParcelableExtra(EXTRA_DEVICE);
			if(dev != null) {
				// Only update mBtDevice if a new device was passed
				// This avoids an error if we are returning to the RecordActivity while the service
				// had already started and/or was recording in the background...
				mBtDevice = dev;
			}
		}

		// If we didn't get a SensorTag device, we can't do anything! Warn the user, log, and exit.
		// Note that this doesn't run if we do the "early exit" (if the Service was already started
		// and received another "start" command)
		if (mBtDevice == null) {
			Log.e(TAG, "No BluetoothDevice extra [" + DeviceSelectActivity.EXTRA_DEVICE
					+ "] provided in Intent.");
			Toast.makeText(this, "No SensorTag device selected for Record Service",
					Toast.LENGTH_SHORT).show();
			setStatus(Status.ERROR_SENSORTAG_NULL, "No SensorTag device passed to Service");
			return START_STICKY;
		}
		
		// Initialise resources
		boolean sensorTagOk = initSensorTag(mBtDevice);
		if(!sensorTagOk) {
			// If SensorTag fails, exit early; not that mStatus is set by initSensorTag
			mStatusMessage = "An error occurred while initialising the SensorTag.";
			return START_STICKY;
		}
		initRecordingData();
		
		// Show a persistent notification in the Android notification tray.
		updateNotification();
		
		mIsStarted = true;
		return START_STICKY;
	}
	
	/**
	 * Initialise the SensorTagManager, Bluetooth connection to the SensorTag, and all needed
	 * sensors.
	 * 
	 * @param sensorTag The BluetoothDevice object corresponding to the SensorTag to connect to.
	 * @return true on success, false on failure. On failure, also sets mErrorSensorTag and mStatus.
	 */
	private boolean initSensorTag(BluetoothDevice sensorTag) {

		// Set up the SensorTag, Listener, etc.
		mStManager = new SensorTagManager(this, sensorTag);
		mStListener = new JoggingListener(this);
		mStManager.addListener(mStListener);
		mStManager.initServices();
		if (!mStManager.isServicesReady()) {
			// if initServices failed or took too long, log an error (in LogCat) and exit
			Log.e(TAG, "Discover failed - exiting");
			if (mErrorSensorTag == null) mErrorSensorTag = ErrorType.SERVICE_DISCOVERY_FAILED;
			setStatus(Status.ERROR_SENSORTAG);
			return false;
		}

		/*
		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 * CHANGE ME: SENSOR INITIALISATION - This enables the sensors you want to get data from.
		 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 */

		// Here we enable the accelerometer. The accelerometer gives a reading of the acceleration
		// vector (cartesian coordinates), in Gs (1g = 9.81 m/s^2), at a fixed sample interval.
		//
		// On the SensorTag, the sensors' sample interval can be configured. However, some sensors
		// DO NOT support this, but it depends on the version of the firmware installed on the
		// SensorTag itself. isPeriodSupported() checks the capabilities of this version of the
		// SensorTag (which was obtained in initServices() above), and returns true if you can
		// configure the sample interval (a.k.a. update period) of the specified sensor.
		//
		// If we can do it, great! We enable the accelerometer with period = UPDATE_PERIOD_ACC_MS.
		//
		// If not, can we deal with it gracefully in our application? We can try enabling it with
		// the default period value (1 second, again depends on SensorTag firmware version). In
		// either case we store the set period value so that the rest of the application knows how
		// long between each acceleration reading.
		boolean res;
		if (mStManager.isPeriodSupported(Sensor.ACCELEROMETER)) {
			res = mStManager.enableSensor(Sensor.ACCELEROMETER, UPDATE_PERIOD_ACC_MS);
		}
		else {
			res = mStManager.enableSensor(Sensor.ACCELEROMETER);
		}
		if(!res) {
			if (mErrorSensorTag == null) mErrorSensorTag = ErrorType.SENSOR_CONFIG_FAILED;
			setStatus(Status.ERROR_SENSORTAG);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Initialise the RecordingData structure and data storage backend, and then load any data and
	 * settings already stored from a previous session. If the previous session indicates that the
	 * recording was already in progress, this will also set the recording status to Paused.
	 */
	private void initRecordingData() {
		// Load preferences from the SharedPreferences
		// If a previous recording session was interrupted, we can resume it
		mData.loadPreferences();
		switch(mData.getStatus()) {
		case FINISHED:
			setStatus(Status.FINISHED);
			break;
		case PAUSED:
			setStatus(Status.PAUSE);
			break;
		case RECORDING:
			// If the Service was interrupted and is restarting, we'll leave it paused
			// and let the user restart the recording
			mData.setStatus(RecordingData.Status.PAUSED);
			mData.savePreferences();
			setStatus(Status.PAUSE);
			break;
		case NOT_STARTED:
		default:
			setStatus(Status.STANDBY);
			break;
		}
	}
	
	/**
	 * Create a persistent notification, to show in the Android notification drawer, containing
	 * current information about recording status and recorded data.
	 * 
	 * Used internally by updateNotification() to generate <em>and show</em> the notification.
	 * 
	 * @return The Notification object.
	 */
	private Notification makeNotification() {
		
		// Generate the text elements to show on the Notification
		String title = getString(R.string.notif_service) + " ";
		switch(mData.getStatus()) {
		case FINISHED:
			title += getString(R.string.notif_service_finished);
			break;
		case PAUSED:
			title += getString(R.string.notif_service_pause);
			break;
		case RECORDING:
			title += getString(R.string.notif_service_record);
			break;
		default:
		case NOT_STARTED:
			title += getString(R.string.notif_service_stopped);
			break;
		}

		String elapsed = formatTime(mData.getElapsedTime());
		String body = getString(R.string.notif_service_label_samples) + " "
				+ mData.getSamplesStored() + " - "
				+ getString(R.string.notif_service_label_elapsed) + " " + elapsed;

		// Defines an Intent that can be used when the Notification is clicked.
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				RecordActivity.class), 0);
		
		Notification notif = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher) // Note: this is necessary for it to work!
				.setContentTitle(title)
				.setContentText(body)
				.setContentIntent(contentIntent)
				.build();
		return notif;
	}
	
	/**
	 * Internal utility method. Convert a time internal, in milliseconds, to h:mm:ss format.
	 * Truncates to 1-second resolution.
	 * 
	 * @param time_ms A time interval in milliseconds.
	 * @return A string representing the time interval in h:mm:ss format.
	 */
	private String formatTime(long time_ms) {
		final long HRS_TO_SEC = 3600;
		final long HRS_TO_MIN = 60;
		final long MIN_TO_SEC = 60;
		
		long time_s = time_ms / SEC_TO_MSEC;
		int hours = (int)(time_s / HRS_TO_SEC);
		
		long time_s_mod_hour = time_s - (hours * HRS_TO_SEC);
		int minutes = (int)(time_s_mod_hour / MIN_TO_SEC);
		
		long time_s_mod_min = time_s_mod_hour - (minutes * MIN_TO_SEC);
		int seconds = (int)(time_s_mod_min);
		
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	/**
	 * Generate and show a Notification in the Android notification drawer, with current status
	 * information (paused, recording, etc.) and information about the recording (number of samples,
	 * time elapsed).
	 */
	private void updateNotification() {
		mNotificationManager.notify(R.string.id_notif_svc_record, makeNotification());
	}
	
	/**
	 * Start a timer that will update the Notification every NOTIF_UPDATE_RATE_MS milliseconds.
	 * @see #updateNotification()
	 * @see #stopNotificationUpdateTimer()
	 */
	private void startNotificationUpdateTimer() {
		stopNotificationUpdateTimer();
		if(mNotificationUpdateHandler != null && mNotificationUpdateRunner != null)
			mNotificationUpdateHandler.postDelayed(mNotificationUpdateRunner, NOTIF_UPDATE_RATE_MS);
		else
			Log.e(TAG, "startNotificationUpdateTimer(): unexpected null");
	}
	
	/**
	 * Stop the timer that updates the Notification periodically.
	 * @see #startNotificationUpdateTimer()
	 */
	private void stopNotificationUpdateTimer() {
		if(mNotificationUpdateHandler != null && mNotificationUpdateRunner != null)
			mNotificationUpdateHandler.removeCallbacks(mNotificationUpdateRunner);
		else
			Log.e(TAG, "stopNotificationUpdateTimer(): unexpected null");
	}
	
	/**
	 * Runnable that contains the implementation of the notification update timer (see
	 * {@link RecordService#startNotificationUpdateTimer()} and
	 * {@link RecordService#stopNotificationUpdateTimer()}).
	 */
	private class UpdateNotificationRunner implements Runnable {

		@Override
		public void run() {
			updateNotification();
			
			if(mNotificationUpdateHandler != null)
				mNotificationUpdateHandler.postDelayed(this, NOTIF_UPDATE_RATE_MS);
		}
		
	}

	/**
	 * Called upon an Activity binding to the Service. Returns a Binder object, which allows local
	 * communication between the Activity and Service (of the same app only).
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 * @see ca.concordia.sensortag.datasample.RecordService.Binder
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}
	/**
	 * Called upon all clients having disconnected from the service. If not recording, stop the
	 * service; if recording, the service will continue running.
	 * @see Service#onUnbind(Intent)
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		if(isStarted() && getStatus() != Status.RECORD) {
			stopSelf();
		}
		return false;
	}
	
	/**
	 * A listener interface that allows an Activity or other Android component which is bound to the
	 * RecordService to listen to status changes from the RecordService. This can be useful, for
	 * example, to know when a recording stops (time elapsed or maximum samples reached) in order
	 * to update a GUI, for example.
	 */
	public static interface RecordServiceListener {
		/**
		 * Called whenever the status of the recording changes (e.g. Paused, Stopped, Recording,
		 * etc.).
		 * 
		 * @param s The new status.
		 */
		void onStatusChanged(Status s);
	}

	/**
	 * Binder allowing an Activity to communicate with the RecordService (within the same app only).
	 * Call any of the methods contained in the Binder to communicate with the RecordService (send
	 * commands or obtain information from the service).
	 */
	public class Binder extends android.os.Binder {
		RecordService mService = RecordService.this;
		
		/** Set a listener to call when the status of the service changes. */
		public void addListener(RecordServiceListener l) {
			Log.d(TAG, "Binder.addListener(" + l + ")");
			mService.addListener(l);
		}
		
		/** Remove a listener. */
		public void removeListener(RecordServiceListener l) {
			Log.d(TAG, "Binder.removeListener(" + l + ")");
			mService.removeListener(l);
		}

		/**
		 * Start the service in a persistent way, and initialise a connection to a SensorTag.
		 * This method must be called to initialise the service properly before calling other
		 * methods.
		 * 
		 * If this method is called when the service had already been started in this way:
		 * 	  a) If the service is NOT recording or paused, then reinitialise the service with the
		 *       passed SensorTag device. This can be used to change SensorTag; however, it is
		 *       recommended to instead stop the recording, stop the service, and restart the
		 *       service instead.
		 *    b) If the service IS recording or paused, the startService() call is ignored.
		 * 
		 * @param device The BluetoothDevice corresponding to the SensorTag to connect to.
		 */
		public void startService(BluetoothDevice device) {
			Log.i(TAG, "Starting RecordService...");

			// Start the RecordService as an always-running service (start command)
			Intent recordIntent = new Intent(mService, RecordService.class);
			recordIntent.putExtra(RecordService.EXTRA_DEVICE, device);
			mService.startService(recordIntent);
		}

		/**
		 * Manually stop the service. For the service to shut down completely, you must call this
		 * and then unbind from the service, otherwise it MAY remain in the background.
		 */
		public void stopService() {
			Log.i(TAG, "Requested stop RecordService...");
			mService.stopSelf();
		}
		
		/**
		 * Check if the Service has been started using startService().
		 * @return True if started, false otherwise.
		 */
		public boolean isStarted() {
			return mService.isStarted();
		}
		

		/** Get the current RecordService status (paused, play, etc.). */
		public Status getStatus() {
			Log.d(TAG, "Binder.getStatus()");
			return mService.getStatus();
		}

		/**
		 * Get the last error message (if any). In particular, if getStatus() returns a failure,
		 * the error message will be available here. This can occur immediately after startService()
		 * if an error occurs, for example initialising the SensorTag or storage.
		 */
		public String getLastError() {
			Log.d(TAG, "Binder.getLastError()");
			return mService.getLastError();
		}

		/**
		 * Get the recorded data.
		 * 
		 * @return Unmodifiable map of timestamps (milliseconds)
		 */
		public List<Long> getData() {
			Log.d(TAG, "Binder.getData()");
			return mService.getData().getData();
		}

		/**
		 * @return Current elapsed recording time, in milliseconds.
		 */
		public long getElapsedTime() {
			Log.d(TAG, "Binder.getElapsedTime()");
			return mService.getData().getElapsedTime();
		}

		/**
		 * @return Current recording duration setting, in milliseconds. Zero for infinite duration.
		 */
		public long getRecordDuration() {
			Log.d(TAG, "Binder.getRecordDuration()");
			return mService.getData().getRecordDuration();
		}

		/**
		 * Set the recording duration. Note that this will take immediate effect, if a recording is
		 * currently running.
		 * 
		 * @param duration
		 *            Recording duration value, in milliseconds. Zero for infinite duration.
		 */
		public void setRecordDuration(long duration) {
			Log.d(TAG, "Binder.setRecordDuration(" + Long.toString(duration) + "ms)");
			mService.getData().setRecordDuration(duration);
		}

		/**
		 * @return Maximum number of samples to record. Zero for infinite.
		 */
		public int getRecordMaxSamples() {
			Log.d(TAG, "Binder.getRecordMaxSamples()");
			return mService.getData().getRecordMaxSamples();
		}

		/**
		 * Set the maximum number of data samples to take. Note that this will take immediate
		 * effect, if a recording is currently running.
		 * 
		 * @param samples
		 *            Maximum number of samples to record. Zero for infinite.
		 */
		public void setRecordMaxSamples(int samples) {
			Log.d(TAG, "Binder.setRecordMaxSamples(" + Long.toString(samples) + ")");
			mService.getData().setRecordMaxSamples(samples);
		}

		/**
		 * Start a recording of a data set from the SensorTag.
		 * 
		 * @return True on success, false on failure (e.g. recording already running or Service
		 *         error occurred).
		 */
		public boolean record() {
			Log.d(TAG, "Binder.record()");
			return mService.record();
		}

		/**
		 * Pause the recording temporarily.
		 * 
		 * @return True on success, false on failure (e.g. recording not started or already paused,
		 *         or Service error occurred).
		 */
		public boolean pause() {
			Log.d(TAG, "Binder.pause()");
			return mService.pause();
		}

		/**
		 * Discard all data currently recorded.
		 * 
		 * @return True on success, false on failure (e.g. recording is running or Service error
		 *         occurred).
		 */
		public boolean reset() {
			Log.d(TAG, "Binder.reset()");
			return mService.reset();
		}

	}

	/**
	 * Called when the RecordService is destroyed. Save current recording session so far, clean up
	 * SensorTag connection and remove the ongoing notification in the Android notification area.
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.v(TAG, "RecordService.onDestroy()");
		
		mIsStarted = false;
		
		if(mData != null) mData.savePreferences();
		mData = null;
		
		if(mStManager != null) mStManager.close();
		mStManager = null;
		
		if(mNotificationUpdateHandler != null)
			mNotificationUpdateHandler.removeCallbacksAndMessages(null);
		mNotificationUpdateHandler = null;
		
		stopForeground(true);
		mNotificationManager.cancel(R.string.id_notif_svc_record);
	}

	/**
	 * Register a RecordServiceListener with the RecordService. See {@link RecordServiceListener}.
	 * @param l The Listener object to register.
	 */
	public void addListener(RecordServiceListener l) {
		mListeners.add(l);
	}

	/**
	 * Deregister a RecordServiceListener with the RecordService. See {@link RecordServiceListener}.
	 * @param l The Listener object to remove.
	 */
	public void removeListener(RecordServiceListener l) {
		mListeners.remove(l);
	}

	/**
	 * Notify all registered listeners of the current recording status. This should be called
	 * internally whenever the status changes.
	 */
	protected void notifyListeners() {
		for(RecordServiceListener l : mListeners) {
			l.onStatusChanged(getStatus());
		}
	}
	
	/**
	 * Return whether or not the service was properly started and configured. To correctly start the
	 * service, a client must bind to the service and then call
	 * {@link Binder#startService(BluetoothDevice)}.
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return mIsStarted;
	}

	/**
	 * Return the current recording status (paused, recording, stopped, etc.).
	 */
	public Status getStatus() {
		return mStatus;
	}
	
	/**
	 * Set the current status, and notify all listeners of this status change. If the status to set
	 * is an error status, a detailed error message should be set by calling
	 * {@link #setStatus(Status, String)} instead.
	 * @param status The status to set.
	 */
	protected void setStatus(Status status) {
		mStatus = status;
		notifyListeners();
	}
	
	/**
	 * Set the current status and an error message, and notify all listeners of this status change.
	 * This method should only be called when the status to set is an error status.
	 * 
	 * @param status The status to set. Should generally be an error status.
	 * @param msg The error message to set.
	 */
	protected void setStatus(Status status, String msg) {
		mStatusMessage = msg;
		setStatus(status);
	}
	
	/**
	 * Returns the last error message generated by the Service. Should be null unless the current
	 * status is an error status.
	 */
	public String getLastError() {
		return mStatusMessage;
	}
	
	/**
	 * Returns the RecordingData object that is responsible for all recording settings and data.
	 * Note that the object returned is a live object that can modify the current recording data.
	 * It is strongly recommended that you do <em>not</em> attempt to modify the data but treat
	 * it as read-only, and let the RecordService handle writing data. Otherwise, unexpected results
	 * may occur due to concurrency or simply data being in an unexpected state for the
	 * RecordService.
	 */
	public RecordingData getData() {
		return mData;
	}
	
	/**
	 * Start or resume recording data with the current settings. Can only start recording from
	 * STANDBY and PAUSE state.
	 * @return true on success, false on failure.
	 */
	public boolean record() {
		if(!isStarted()) {
			Log.e(TAG, "record(): Service not configured. Call startService() from the Binder.");
			return false;
		}
		
		if(getStatus() != Status.STANDBY && getStatus() != Status.PAUSE) {
			Log.e(TAG, "record(): cannot record from current state: " + getStatus().name());
			return false;
		}
		
		Log.i(TAG, "Starting recording...");
		Toast.makeText(this, mAppName + ": Starting recording...", Toast.LENGTH_SHORT).show();
		
		// Start this Service as a "foreground" service, that won't get killed by Android
		// before it's closed manually (except extreme circumstances)
		startForeground(R.string.id_notif_svc_record, makeNotification());
		startNotificationUpdateTimer();
		updateNotification();
		
		mData.setStatus(RecordingData.Status.RECORDING);
		mData.savePreferences();
		mStManager.enableUpdates();
		setStatus(Status.RECORD);
		return true;
	}

	/**
	 * Pause (temporarily stop) the current recording session. This method can only be called while
	 * in the RECORD state. The recording can be restarted with {@link #record()}.
	 * @return true on success, false on failure.
	 */
	public boolean pause() {
		if(!isStarted()) {
			Log.e(TAG, "pause(): Service not configured. Call startService() from the Binder.");
			return false;
		}
		
		if (getStatus() != Status.RECORD) {
			Log.e(TAG, "pause(): cannot pause from current state: " + getStatus().name());
			return false;
		}

		Log.i(TAG, "Paused recording.");
		Toast.makeText(this, mAppName + ": Paused recording.", Toast.LENGTH_SHORT).show();

		mData.setStatus(RecordingData.Status.PAUSED);
		mData.savePreferences();
		mStManager.disableUpdates();
		setStatus(Status.PAUSE);
		
		// We can resume from paused even if the service closes here...
		stopForeground(true);
		stopNotificationUpdateTimer();
		updateNotification();
		
		return true;
	}
	
	/**
	 * Stop the current recording session. The session cannot be restarted. This method can only be
	 * called when in the RECORD state.
	 * 
	 * To temporarily stop the current recording session, use {@link #pause()} instead.
	 * @return true on success, false on failure.
	 */
	public boolean stop() {
		if(!isStarted()) {
			Log.e(TAG, "stop(): Service not configured. Call startService() from the Binder.");
			return false;
		}
		
		if (getStatus() != Status.RECORD) {
			Log.e(TAG, "stop(): cannot stop from current state: " + getStatus().name());
			return false;
		}

		Log.i(TAG, "Stopped recording.");
		Toast.makeText(this, mAppName + ": Recording has finished.", Toast.LENGTH_LONG).show();

		mData.setStatus(RecordingData.Status.FINISHED);
		mData.savePreferences();
		mStManager.disableUpdates();
		setStatus(Status.FINISHED);
		
		stopForeground(true);
		stopNotificationUpdateTimer();
		updateNotification();
		
		return true;
	}

	/**
	 * Delete the current recording session, and all data associated with it. Prepares a new
	 * recording session in STANDBY state, with the same max samples and recording duration
	 * settings as the previous session.
	 * 
	 * This method will fail if the Service is in an error state.
	 * 
	 * @return True on success, false on failure.
	 */
	public boolean reset() {
		if(!isStarted()) {
			Log.e(TAG, "reset(): Service not configured. Call startService() from the Binder.");
			return false;
		}
		
		if(getStatus() == Status.ERROR || getStatus() == Status.ERROR_SENSORTAG ||
				getStatus() == Status.ERROR_SENSORTAG_NULL) {
			Log.e(TAG, "reset(): cannot reset from current state: " + getStatus().name());
			return false;
		}

		Log.i(TAG, "Clearing all data.");
		Toast.makeText(this, mAppName + ": Clearing all data.", Toast.LENGTH_SHORT).show();
		
		mData.setStatus(RecordingData.Status.NOT_STARTED);
		mData.setNewRecording(mData.getRecordDuration(), mData.getRecordMaxSamples());
		mData.savePreferences();
		mStManager.disableUpdates();
		setStatus(Status.STANDBY);
		
		stopForeground(true);
		stopNotificationUpdateTimer();
		updateNotification();
		
		return true;
	}
	
	/**
	 * Saves data to disk if sufficient data has changed. This method is to be used for frequent
	 * incoming data, to avoid excessives writes to disk. In order to save data immediately,
	 * regardless of how much data has changed, use mData.savePreferences() directly instead.
	 */
	private void saveDataOccasional() {
		if(mData.getDelta() >= DATA_SAVE_DELTA_THRESHOLD) {
			mData.savePreferences();
		}
	}

	/**
	 * Called (by JoggingListener) whenever a new event is detected.
	 * 
	 * If the current status is RECORD, adds the event to the stored data. If we have reached the
	 * maximum samples recorded, stops the recording.
	 */
	public void onEventRecorded() {
		if(!isStarted()) {
			Log.e(TAG, "onEventRecorded(): Service not configured. Call startService() from the Binder.");
			return;
		}
		
		if(getStatus() != Status.RECORD) {
			Log.w(TAG, "onEventRecorded() received but current state not RECORD; ignoring.");
			return;
		}
		
		mData.addEvent(mData.getElapsedTime());
		
		if(mData.getRecordMaxSamples() != RECORD_SAMPLES_INFINITE &&
				mData.getSamplesStored() >= mData.getRecordMaxSamples()) {
			Log.i(TAG, "Max samples recorded. Stopping.");
			stop();
		}
		saveDataOccasional();
	}

	/**
	 * Called when time has elapsed during a recording and should be stored. This information is
	 * used to know the total time that has elapsed (for recording duration purposes), as well as
	 * know at what time an event is recorded. This method skips over periods of time when the
	 * recording is paused: the "elapsed" timeline only consists of the time intervals that the 
	 * Service is in RECORD state.
	 */
	public void onTimeElapsed(long timeDelta) {
		if(!isStarted()) {
			Log.e(TAG, "onTimeElapsed(): Service not configured. Call startService() from the Binder.");
			return;
		}
		
		if(getStatus() != Status.RECORD) {
			Log.w(TAG, "onTimeElapsed() received but current state not RECORD; ignoring.");
			return;
		}
		
		mData.addElapsedTime(timeDelta);
		if(mData.getRecordDuration() != RECORD_DURATION_INFINITE &&
				mData.getElapsedTime() >= mData.getRecordDuration()) {
			Log.i(TAG, "Recording duration elapsed. Stopping.");
			stop();
		}
		saveDataOccasional();
	}

	/**
	 * Called by JoggingListener when a SensorTag error occurs. Sets the appropriate Service error
	 * status and notifies the Service listeners. Shows a toast to the user.
	 * @param type
	 * @param text
	 */
	public void onError(ErrorType type, String text) {

		Log.e(TAG, "Error:" + type.name() + "; message: " + text);
		if (text != null) Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		
		mErrorSensorTag = type;
		mStatusMessage = text;
		mStManager.disableUpdates();
		setStatus(Status.ERROR_SENSORTAG);
	}

	/**
	 * Called by JoggingListener when a SensorTag non-error status update is received. Shows a toast
	 * with this information.
	 * @param type
	 * @param text
	 */
	public void onStatus(StatusType type, String text) {
		Log.i(TAG, "Status:" + type.name() + "; message: " + text);
		if (text != null) Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
}
