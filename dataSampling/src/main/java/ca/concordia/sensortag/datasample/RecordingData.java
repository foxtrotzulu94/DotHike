package ca.concordia.sensortag.datasample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Data container for recorded data, and all metadata related to the recording (such as the current
 * status, how long to record data for, how many samples to take at most, etc.).
 * 
 * This container is also responsible for writing data to the SharedPreferences in order to save
 * them and make them persistent.  SharedPreferences are Android constructs of a simple key->value
 * pair type of structure, both being strings. They are available to apps usually for user settings
 * and configurations; in this case, for the sake of simplicity and since it is not a demanding
 * application, both settings and recording data are being stored in SharedPreferences.
 * 
 * This container caches data, and writes them to the on-disk SharedPreferences only when
 * savePreferences() is called. Setting, adding and changing data is cached in memory, and ONLY
 * writes to on-disk SharedPreferences upon a savePreferences() call.
 * 
 * Note that this container does not enforce any rules like the maximum number of samples or
 * recording duration; it only acts as a container for this information.
 */
public class RecordingData {
	public enum Status {
		NOT_STARTED, RECORDING, PAUSED, FINISHED
	};

	public static String TAG = "RecordingData";
	public static int DEFAULT_SAMPLES_SIZE = 512;

	PersistentStorageService mPersistentStorageService;

	// Recording settings - reflected in sharedprefs
	private long mRecDuration_ms = 0; // 0 = infinite
	private int mRecSamples = 0; // 0 = infinite

	// Recording data to be cached in memory - reflected in SharedPreferences on save/load
	private Status mStatus;
	private List<Long> mEventTimestamps;
	private long mElapsed_ms;
	
	// Temporary internal state
	
	// used to determine whether to read/write the data on loadPreferences() or savePreferences(),
	// since this operation is complex and not necessary if no changes have occurred.
	private boolean mEventsChanged;
	// counts number of changes since the last savePreferences() call - can be used by an external
	// client (via getDelta()) to determine when it is appropriate to save to SharedPreferences
	private int mChanges;

	/**
	 * Instantiate the RecordingData container and immediately load data from the passed
	 * SharedPreferences.
	 * 
	 * @param
	 */
	RecordingData(Context context) {
		Log.i(TAG, "new RecordingData using sqlite");
		mPersistentStorageService = new PersistentStorageService(context);
		setNewRecording(0, 0); // to reset all variables
		loadPreferences();
	}

	/**
	 * Start a new recording. Discards all data and sets up the RecordingData object for a new
	 * recording session.
	 * @param duration The maximum duration to record for.
	 * @param samples The maximum number of samples to record.
	 */
	synchronized public void setNewRecording(long duration, int samples) {
		Log.i(TAG, "setNewRecording(duration=" + duration + "ms, samples=" + samples + ")");
		mStatus = Status.NOT_STARTED;
		mRecDuration_ms = duration;
		mElapsed_ms = 0;
		mRecSamples = samples;
		mEventsChanged = true;
		mEventTimestamps = new ArrayList<Long>(mRecSamples != 0 ? mRecSamples
				: DEFAULT_SAMPLES_SIZE);
	}

	/**
	 * Add an event (data point) to the current list of data.
	 * @param timestamp The timestamp (in milliseconds) at which an event was recorded.
	 */
	synchronized public void addEvent(long timestamp) {
		mEventTimestamps.add(timestamp);
		mEventsChanged = true;
		Log.v(TAG, "addEvent(" + timestamp + ") [total:" + mEventTimestamps.size() + "]");
	}

	/**
	 * Store data in SharedPreferences, overwriting any current data.
	 */
	synchronized public boolean savePreferences() {
		Log.v(TAG, "savePreferences()");
	//TODO: CHANGE THIS. HIJACK THE INTERFACE HERE AND HOOK UP THE DATABASE TO STORE
		DBData data = new DBData(mStatus, mRecDuration_ms, mRecSamples, mElapsed_ms, mEventTimestamps);
		mPersistentStorageService.insertRecordedValues(data);
		return true;
	}

	/**
	 * Load data from SharedPreferences, discarding anything currently stored. Note that this
	 * loads a flat string containing all data and deserialises it, so for large amounts of data
	 * it may be slow to run.
	 */
	synchronized public void loadPreferences() {
		Log.v(TAG, "loadPreferences()");
	//TODO: CHANGE THIS. HIJACK THE INTERFACE HERE AND HOOK UP THE DATABASE TO LOAD THE DATA

		DBData data = mPersistentStorageService.readRecordedValues();
		if (data != null) {
			mStatus = data.getStatus();
			mRecDuration_ms = data.getRecDuration();
			mRecSamples = data.getRecSamples();
			mElapsed_ms = data.getElapsed();
			if (mEventsChanged) {
				mEventTimestamps = data.getEventTimestamps();
				mEventsChanged = false;
			}
			mChanges = 0;
		}
	}

	/**
	 * Return the stored recording status (pause, recording, etc.).
	 * @return Current recording status.
	 */
	public Status getStatus() {
		return mStatus;
	}

	/**
	 * @param status
	 *            Current recording status.
	 */
	synchronized public void setStatus(Status status) {
		mStatus = status;
		++mChanges;
	}

	/**
	 * Return the recording duration setting.
	 * 
	 * @return Current recording duration setting, in milliseconds. Zero for infinite duration.
	 */
	public long getRecordDuration() {
		return mRecDuration_ms;
	}

	/**
	 * Set the recording duration setting.
	 * 
	 * @param duration
	 *            Recording duration value, in milliseconds. Zero for infinite duration.
	 */
	synchronized public void setRecordDuration(long duration) {
		mRecDuration_ms = (duration > 0) ? duration : 0;
		++mChanges;
	}
	
	/**
	 * Return the amount of time that has elapsed for the current recording session.
	 * 
	 * @return Amount of time elapsed during recording so far, in milliseconds.
	 */
	public long getElapsedTime() {
		return mElapsed_ms;
	}

	/**
	 * Set the total amount of time that has elapsed for the current recording session.
	 * 
	 * @param
	 */
	synchronized public void setElapsedTime(long time) {
		mElapsed_ms = time;
		++mChanges;
	}

	/**
	 * Increase the amount of time that has elapsed for teh current recording session by a value.
	 * This adds the passed value to the elapsed time stored.
	 * 
	 * @param
	 */
	synchronized public void addElapsedTime(long timeDelta) {
		mElapsed_ms += timeDelta;
		++mChanges;
	}

	/**
	 * Return the maximum event samples setting.
	 * 
	 * @return Maximum number of samples to record. Zero for infinite.
	 */
	public int getRecordMaxSamples() {
		return mRecSamples;
	}

	/**
	 * Set the maximum event samples setting.
	 * 
	 * @param samples
	 *            Maximum number of samples to record. Zero for infinite.
	 */
	synchronized public void setRecordMaxSamples(int samples) {
		mRecSamples = (samples > 0) ? samples : 0;
		++mChanges;
	}

	/**
	 * @return Number of samples stored so far.
	 */
	public int getSamplesStored() {
		return mEventTimestamps.size();
	}

	/**
	 * @return The list of event data so far, returned as an unmodifiable list.
	 */
	public List<Long> getData() {
		return Collections.unmodifiableList(mEventTimestamps);
	}
	
	/**
	 * @return The number of uncommitted/unsaved changes to the recording data (events as well as
	 * elapsed time, recordMaxSamples, etc.). Every set*() call will increment this value;
	 * loading or saving preferences will reset it.
	 */
	public int getDelta() {
		return mChanges;
	}
	
	/**
	 * Convert the data to a string. Shows data as key-value pairs; this may be useful for internal
	 * usage, e.g. debugging logcats, but is probably not useful to a user.
	 * 
	 * Example return value:
	 * 
	 * "Recording Settings: Delta[3] ElapsedTimep31400] RecordDuration[60000] RecordMaxSamples[1000]
	 * SamplesStored[7] Status[PAUSE] Data[123 456 789 1023 1553 1694 1754]"
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Recording Settings: ").append("Delta[").append(getDelta()).append("] ")
		.append("ElapsedTime[").append(getElapsedTime()).append("] ")
		.append("RecordDuration[ ").append(getRecordDuration()).append("] ")
		.append("RecordMaxSamples[").append(getRecordMaxSamples()).append("] ")
		.append("SamplesStored[").append(getSamplesStored()).append("] ")
		.append("Status[").append(getStatus()).append("] ")
		.append("Data").append(getData().toString());
		return b.toString();
	}

}