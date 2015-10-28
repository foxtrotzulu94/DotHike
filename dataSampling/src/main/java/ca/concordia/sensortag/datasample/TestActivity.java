/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Example application: Data/Event Sampler
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */
package ca.concordia.sensortag.datasample;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity used for manually testing DataSampling persisting functionality.
 */
public class TestActivity extends Activity {
	static public final String TAG = "TestAct"; // Tag for Android's logcat
	
	static public final Double FREQ_DISP_SCALING = 60.0; // per second --> per minute
	static public final Double SEC_TO_MSEC = 1000.0;
	static protected final DecimalFormat FORMAT_FREQ = new DecimalFormat("###0.00");
	static public final String PREFERENCE_NAME = "ca.concordia.datasample.Recording";
	
	/* Service */
	private RecordService.Binder mRecSvc = null;
	
	/* GUI */
	private TextView mValueTime;
	private TextView mValueEvents;
	private TextView mValueAverage;
	private TextView mValueRmsvar;
	private TextView mValueMax;
	private TextView mValueMin;
	
	/* Data analysis */
	private List<Long> mEventTimestamps = null;
	private List<Double> mEventDifference = null; // time between each event (e.g. each footstep)
	private List<Double> mEventFrequency = null;
	private List<Double> mEventFrequencyTime = null;
	
	/* Calculated data */
	private boolean mIsAnalyzed = false;
	private long mTotalTime = 0;
	private int mNumSteps = 0;
	private Double mFreqAvg = null;
	private Double mFreqRmsVar = null;
	private Double mFreqMax = null;
	private Double mFreqMin = null;

	private SharedPreferences mPrefs = null;
	private RecordingData mData = null;
	
	protected void logData() {
		Log.i(TAG, "PREFS: " + mPrefs.getAll().toString());
		Log.i(TAG, "DATA: " + mData.toString());
	}
	
	/**
	 * Called by Android when the Activity is first created. This sets up the GUI for the Activity,
	 * sets up the variables to control the GUI elements in the program, and prepares the Bluetooth
	 * communication to the SensorTag.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Random r = new Random();
		mPrefs = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		
		Log.i(TAG, "LOAD");
		mData = new RecordingData(mPrefs);
		logData();
		
		Log.i(TAG, "CLEAR");
		mPrefs.edit().clear().commit();
		logData();

		Log.i(TAG, "SETUP BLANK");
		mData = new RecordingData(mPrefs);
		mData.savePreferences();
		logData();
		
		Log.i(TAG, "SETUP SETTINGS");
		mData.setStatus(RecordingData.Status.NOT_STARTED);
		mData.setRecordDuration(10000L);
		mData.setRecordMaxSamples(255);
		logData();
		
		Log.i(TAG, "SAVE SETTINGS");
		mData.savePreferences();
		logData();
		
		Log.i(TAG, "SAVE SOME DATA");
		mData.addElapsedTime(1000);
		mData.setStatus(RecordingData.Status.RECORDING);
		for (int i = 0; i < 10; ++i) {
			mData.addEvent(1000*i + (long)(1000.0*r.nextDouble()));
			mData.addElapsedTime(1000);
		}
		logData();
		
		Log.i(TAG, "SAVE THE DATA");
		mData.savePreferences();
		logData();
		
		Log.i(TAG, "RELOAD ALL THE THINGS");
		mData = new RecordingData(mPrefs);
		logData();
		
		finish();
	}
}
