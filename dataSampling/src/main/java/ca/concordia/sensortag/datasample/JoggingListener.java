/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Example application: Data/Event Sampler
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */
package ca.concordia.sensortag.datasample;

import ti.android.ble.sensortag.Sensor;
import ti.android.util.Point3D;
import android.util.Log;
import ca.concordia.sensortag.SensorTagListener;
import ca.concordia.sensortag.SensorTagLoggerListener;
import ca.concordia.sensortag.SensorTagManager;
import ca.concordia.sensortag.SensorTagManager.ErrorType;
import ca.concordia.sensortag.SensorTagManager.StatusType;

/**
 * SensorTagListener. Takes accelerometer input values, and detects steps taken by a user with the
 * SensorTag in their pocket or on their belt. Passes the time of these step events to the
 * RecordService.
 * 
 * The algorithm used to detect a shake is as follows:
 * 1) High pass filter: assuming that the SensorTag is not rotating rapidly, the gravity vector
 *    should be very slow compared to accelerations due to walking. This filter will remove slow
 *    components like the gravity vector.
 * 2) Threshold check: If the acceleration, after filtering, is above a certain value, a "shake" is
 *    is detected (therefore a step is detected).
 * 3) Cooldown: After an event (step/shake) is detected, no more events can be detected for
 *    ACC_EVENT_COOLDOWN_MS milliseconds. This prevents a sustained high acceleration from the same
 *    movement, or multiple shakes due to the same movement, from being detected as separate
 *    events.
 * 
 * @todo This algorithm with the constants given (cooldown, threshold) seem to capture 80% of
 * step events when walking or jogging, but it does occasionally catch multiple shakes right after
 * the cooldown and misses quite a few steps. There might be ways of improving this algorithm,
 * either by refining the constants or using an alternative algorithm.
 */
public class JoggingListener extends SensorTagLoggerListener implements SensorTagListener {

	final static String TAG = "JoggingListener";

	/**
	 * Reference to the GameActivity, to allow this class to make calls back to it.
	 */
	RecordService mContext;

	/**
	 * Amount of time to "cool down" an acceleration shake event: this prevents another event from
	 * being registered immediately after the first (e.g. due to a single shake causing
	 * acceleration-deceleration, or a jolt causing multiple up-and-down movements inadvertently).
	 */
	public final static int ACC_EVENT_COOLDOWN_MS = 250;

	/** * High pass filter time constant. See {@link #applyFilter(Point3D)}. */
	public final static int ACC_FILTER_TAU_MS = 100;

	/** Acceleration magnitude threshold. A "shake" is detected if the magnitude of the acceleration
	 * vector, after filtering, is above this value. */
	public final static double ACC_THRESHOLD = 0.4;

	/** Previous acceleration value. */
	private Point3D mLastAcc = null;
	/** Previous acceleration output value of the high-pass filter. */
	private Point3D mLastFiltAcc = null;
	/**
	 * When this value is less than ACC_EVENT_COOLDOWN_MS, we are in cooldown mode and do not detect
	 * acceleration shake events. This is set to 0 whenever an event is detected, and incremented by
	 * the sample period at every new sample received while in cooldown mode. When this value is
	 * equal to or greater than ACC_EVENT_COOLDOWN_MS, we are in normal detection mode.
	 */
	private int mCooldownCounterMs = ACC_EVENT_COOLDOWN_MS;

	/** Previous state of the SensorTag buttons. */
	boolean prevLeft = false, prevRight = false;

	JoggingListener(RecordService context) {
		mContext = context;
	}

	/**
	 * Called on receiving a new accelerometer reading. This method filters the accelerometer values
	 * and attempts to detect a "step" (i.e. a shake event from taking a step with the SensorTag
	 * in one's pocket).
	 * 
	 * @see ca.concordia.sensortag.SensorTagLoggerListener#onUpdateAmbientTemperature(ca.concordia.sensortag.SensorTagManager, double)
	 */
	@Override
	public void onUpdateAccelerometer(SensorTagManager mgr, Point3D acc) {
		super.onUpdateAccelerometer(mgr, acc);
		
		// Get the Accelerometer's update period
		int mPeriod = mgr.getSensorPeriod(Sensor.ACCELEROMETER);
		
		// Inform the Service that time has passed in the recording.
		mContext.onTimeElapsed(mPeriod);

		// If this is the first data point, assume the acceleration has been this value forever
		// for signal processing purposes.
		if (mLastAcc == null) {
			mLastAcc = acc;
			mLastFiltAcc = new Point3D(0, 0, 0);
		}

		// Apply the high-pass filter.
		mLastFiltAcc = applyFilter(mPeriod, acc, mLastAcc, mLastFiltAcc);
		mLastAcc = acc;

		// If the cooldown timer is already expired, we can try and detect a shake
		if (mCooldownCounterMs >= ACC_EVENT_COOLDOWN_MS) {
			// if the magnitude of the acceleration exceeds the shake threshold
			if (mLastFiltAcc.norm() > ACC_THRESHOLD) {
				Log.i(TAG, "Accelerometer shake detected");
				// reset/start the cooldown timer
				mCooldownCounterMs = 0;
				mContext.onEventRecorded();
			}
		}
		else {
			// if cooldown timer not expired, increment it by the accelerometer update period
			mCooldownCounterMs += mgr.getSensorPeriod(Sensor.ACCELEROMETER);
			Log.v(TAG, "Cooldown counter: " + mCooldownCounterMs + "ms");
		}
	}

	/**
	 * Applies a high-pass filter to a new sample value. Relies on the member constants
	 * ACC_FILTER_TAU_MS, ACC_FILTER_TAU_MS and the variable mUpdatePeriodRealMs.
	 * 
	 * @param newInput
	 *            The next input sample.
	 * @param prevInput
	 *            The previous input sample.
	 * @param prevOutput
	 *            The previous filter output sample.
	 * @return The next filter output sample.
	 */
	private Point3D applyFilter(int samplePeriodMs, Point3D newInput, Point3D prevInput,
			Point3D prevOutput) {
		/*
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
		double k = (double) ACC_FILTER_TAU_MS / (ACC_FILTER_TAU_MS + samplePeriodMs);

		// These variable names are used just to make the code closer to the description above
		Point3D yn, yn1, xn, xn1;
		yn1 = prevOutput;
		xn = newInput;
		xn1 = prevInput;

		// Apply the filter to each component of the 3D vector separately
		yn = new Point3D(
				k * (yn1.x + xn.x - xn1.x),
				k * (yn1.y + xn.y - xn1.y),
				k * (yn1.z + xn.z - xn1.z));

		Log.v(TAG, "ACC FILTER: " + xn + " -> " + yn);

		return yn;
	}

	/**
	 * Called on receiving a SensorTag-related error. Displays a Toast showing a message to the
	 * user.
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onError(
	 *      ca.concordia.sensortag.SensorTagManager,
	 *      ca.concordia.sensortag.SensorTagManager.ErrorType, java.lang.String)
	 */
	@Override
	public void onError(SensorTagManager mgr, ErrorType type, String msg) {
		super.onError(mgr, type, msg);
		String text = null;
		switch (type) {
		case GATT_REQUEST_FAILED:
			text = "Error: Request failed: " + msg;
			break;
		case GATT_UNKNOWN_MESSAGE:
			text = "Error: Unknown GATT message (Programmer error): " + msg;
			break;
		case SENSOR_CONFIG_FAILED:
			text = "Error: Failed to configure sensor: " + msg;
			break;
		case SERVICE_DISCOVERY_FAILED:
			text = "Error: Failed to discover sensors: " + msg;
			break;
		case UNDEFINED:
			text = "Error: Unknown error: " + msg;
			break;
		default:
			break;
		}
		mContext.onError(type, text);
	}

	/**
	 * Called on receiver a SensorTag-related status message. Displays a Toast showing a message to
	 * the user, if relevant.
	 * 
	 * @see ca.concordia.sensortag.SensorTagBaseListener#onStatus(ca.concordia.sensortag.SensorTagManager,
	 *      ca.concordia.sensortag.SensorTagManager.StatusType, java.lang.String)
	 */
	@Override
	public void onStatus(SensorTagManager mgr, StatusType type, String msg) {
		super.onStatus(mgr, type, msg);
		String text = null;
		switch (type) {
		case SERVICE_DISCOVERY_COMPLETED:
			// Not needed and some problems with too many toasts at the same time
			// text = "Sensors discovered";
			break;
		case SERVICE_DISCOVERY_STARTED:
			text = "Preparing SensorTag";
			break;
		case UNDEFINED:
			text = "Unknown status";
			break;
		default: // ignore other cases
			break;
		}
		mContext.onStatus(type, text);
	}
}
