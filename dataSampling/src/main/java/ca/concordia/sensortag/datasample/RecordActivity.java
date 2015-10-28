/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Example application: Data/Event Sampler
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */
package ca.concordia.sensortag.datasample;

import ca.concordia.sensortag.datasample.RecordService.RecordServiceListener;
import ca.concordia.sensortag.datasample.RecordService.Status;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is the main activity that allows setting up a recording session and recording data. This
 * activity lets you count "steps" or "shakes", usually from a SensorTag in your pocket (this lets
 * you use your phone without worrying about falsifying data from having it in your hand or moving
 * it). Once the recording process has started, this activity does not need to be in the foreground.
 * 
 * You can use this app as a template for any kind of data sampling or tracking application: for
 * example, for measuring temperature over time, or for tracking events like steps, or full
 * rotations of a wheel (using the magnetometer and a magnet), or over/under-temperature events and
 * the time that these events occur. From there you can analyse the data and present visualisations
 * and statistics to the user.
 */
public class RecordActivity extends Activity implements RecordServiceListener {
	static public final String TAG = "RecordAct"; // Tag for Android's logcat
	static public final Long SEC_TO_MSEC = 1000L;

	/* Service */
	RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;

	/* GUI objects */
	private EditText mInputTime;
	private CheckBox mCheckTimeForever;
	private EditText mInputSamples;
	private CheckBox mCheckSamplesInfinite;
	private Button mButtonRecordPause;
	private Button mButtonReset;
	private Button mButtonAnalyze;

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
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_record);

		// Get the Bluetooth device selected by the user - should be set by DeviceSelectActivity
		// upon launching this application
		Intent receivedIntent = getIntent();
		if(receivedIntent != null) {
			BluetoothDevice dev = (BluetoothDevice) receivedIntent
					.getParcelableExtra(DeviceSelectActivity.EXTRA_DEVICE);
			if(dev != null) {
				// Only update mBtDevice if a new device was passed
				// This avoids an error if we are returning to the RecordActivity while the service
				// had already started and/or was recording in the background...
				mBtDevice = dev;
			}
		}

		// Usually we'd detect mBtDevice == null and error out...
		// But the service might already be running with a bluetooth device (if we're returning to
		// the RecordActivity)... so don't check that, let the Service handle it.
		
		connectService();
		setupGui();
	}

	/**
	 * Start the RecordService and then bind (connect) to it.
	 */
	private void connectService() {
		if (mBtDevice == null) Log.e(TAG, "connectService(): No Bluetooth device");

		// Bind this Activity to the Service described in the recordIntent, starting it if necessary
		// This creates a bound service, which will be destroyed if all bound Activities unbind or
		// close. However, in onServiceConnected() we take measures to make the service persistent
		// even if the user moves away from this Activity.
		Log.i(TAG, "Binding to RecordService...");
		Intent recordIntent = new Intent(this, RecordService.class);
		bindService(recordIntent, mSvcConnection, Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT);
	}

	/**
	 * Get references to the GUI elements and set up listeners.
	 */
	private void setupGui() {
		// Show the "back"/"up" button on the Action Bar (top left corner)
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get a reference to the Java object corresponding to the GUI elements on the layout. This
		// allows us to use the variables later on to change what's shown on the GUI and respond to
		// clicks, etc.
		mInputTime = (EditText) findViewById(R.id.inputTime);
		mCheckTimeForever = (CheckBox) findViewById(R.id.checkboxForever);
		mInputSamples = (EditText) findViewById(R.id.inputSamples);
		mCheckSamplesInfinite = (CheckBox) findViewById(R.id.checkboxInfiniteSamples);
		mButtonRecordPause = (Button) findViewById(R.id.buttonRecordPause);
		mButtonReset = (Button) findViewById(R.id.buttonReset);
		mButtonAnalyze = (Button) findViewById(R.id.buttonAnalyze);

		// Set up the listeners for the GUI elements
		mInputTime.setOnEditorActionListener(mOnEditorActionDurationListener);
		mInputTime.setOnFocusChangeListener(mOnFocusChangeDurationListener);
		mInputSamples.setOnEditorActionListener(mOnEditorActionSamplesListener);
		mInputSamples.setOnFocusChangeListener(mOnFocusChangeSamplesListener);

		mCheckTimeForever.setOnCheckedChangeListener(mOnCheckedTimeListener);
		mCheckSamplesInfinite.setOnCheckedChangeListener(mOnCheckedSampleListener);

		// Button states are set once the service is loaded in mSvcConnection.onServiceConnected
		// Record/Pause button listener is also set at that point in time.
		mButtonReset.setOnClickListener(mOnClickResetListener);
		mButtonAnalyze.setOnClickListener(mOnClickAnalyzeListener);
		updateButtons(); // will disable all buttons, since the service hasn't yet loaded
	}

	/**
	 * Service connector for RecordService. Handles messages about the service being connected or
	 * disconnected.
	 */
	private ServiceConnection mSvcConnection = new ServiceConnection() {
		/**
		 * Called when the service is connected (is bound). Initialises the service using
		 * startService to set up the SensorTag and storage backend, and sets up the initial GUI
		 * state using previously stored information retrieved from the service.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "RecordService connected.");
			mRecSvc = (RecordService.Binder) service;
			mRecSvc.addListener(RecordActivity.this);
			mRecSvc.startService(mBtDevice);
			
			mInputTime.getEditableText().clear();
			mInputTime.getEditableText().append(Long.toString(mRecSvc.getRecordDuration()/SEC_TO_MSEC));
			
			mInputSamples.getEditableText().clear();
			mInputSamples.getEditableText().append(Integer.toString(mRecSvc.getRecordMaxSamples()));
			
			mCheckTimeForever.setChecked(mRecSvc.getRecordDuration() == RecordService.RECORD_DURATION_INFINITE);
			mCheckSamplesInfinite.setChecked(mRecSvc.getRecordMaxSamples() == RecordService.RECORD_SAMPLES_INFINITE);
		}

		/**
		 * Called when the service is disconnected (unbound). Update the GUI to disable service-
		 * dependent functionality.
		 */
		@Override
		public void onServiceDisconnected(android.content.ComponentName name) {
			Log.i(TAG, "RecordService disconnected.");
			mRecSvc = null;
			updateButtons();
		}
	};

	/**
	 * Called by Android when the Activity comes back into the foreground (i.e. on-screen). When
	 * called, enables processing sensor measurements (which are received by {@link ManagerListener}
	 * ).
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}

	/**
	 * Called by Android when the Activity goes out of focus (for example, if another Application
	 * pops up on top of it and partially obscures it). When called, this method disables processing
	 * sensor measurements but does not close the Bluetooth connection or disable the sensors,
	 * allowing the application to save power/CPU by not processing sensor measurement info but
	 * restore quickly when it comes into the foreground again.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	/**
	 * Called when the Activity is destroyed by Android. Stops the RecordService and unbinds from
	 * it.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		Log.d(TAG, "Unbinding from service...");
		if(mRecSvc != null) {
			// Only unbind here: if the Activity is killed but the service is recording, we don't
			// want the recording to be interrupted in the background, so we do not call
			// mRecSvc.stopService()
			unbindService(mSvcConnection);
			mRecSvc = null;
		}
	}
	
	/**
	 * Called when a menu item is pressed. In this case we don't have an explicit menu, but we do
	 * have the "back" button in the Action Bar (top bar). We want it to act like the regular Back
	 * button, that is to say, pressing either Back buttons closes the current Activity and returns
	 * to the previous activity in the stack.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	        case android.R.id.home:
	            this.finish();
	            return true;
            default:
            	return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Handler that is called from a listener whenever the Duration text box is updated. Passes the
	 * duration on to the RecordService; if the duration value is invalid or the RecordService is
	 * not yet available, resets the value in the textbox to the old value in order to ensure that
	 * the value shown and the value in the RecordService are in sync.
	 */
	private void onDurationUpdated(Editable s) {
		Log.d(TAG, "Detected recording duration text change.");
		if (mRecSvc != null) {
			if (!mCheckTimeForever.isChecked()) {
				try {
					mRecSvc.setRecordDuration(SEC_TO_MSEC*Long.parseLong(s.toString()));
				}
				catch(NumberFormatException e) {
					Log.e(TAG, "Value \"" + s.toString() + "\" is not a valid " +
							"RecordDuration value. Resetting.");
					s.clear();
					s.append(Long.toString(mRecSvc.getRecordDuration()/SEC_TO_MSEC));
				}
			}
			else {
				mRecSvc.setRecordDuration(RecordService.RECORD_DURATION_INFINITE);
			}
		}
		else {
			Toast.makeText(RecordActivity.this, "Record Service not started: starting...",
					Toast.LENGTH_SHORT).show();
			Log.w(TAG, "Service not running; cannot pass settings change to GUI.");
			connectService();
			s.clear();
		}
	}

	/**
	 * Handler that is called from a listener whenever the Max Samples text box is updated. Passes
	 * the number on to the RecordService; if the max samples value is invalid or the RecordService
	 * is not yet available, resets the value in the textbox to the old value in order to ensure
	 * that the value shown and the value in the RecordService are in sync.
	 */
	private void onMaxSamplesUpdated(Editable s) {

		Log.d(TAG, "Detected maximum samples text change.");
		if (mRecSvc != null) {
			if (!mCheckSamplesInfinite.isChecked()) {
				try {
					mRecSvc.setRecordMaxSamples(Integer.parseInt(s.toString()));
				}
				catch(NumberFormatException e) {
					Log.e(TAG, "Value \"" + s.toString() + "\" is not a valid " +
							"Max Samples value. Resetting.");
					s.clear();
					s.append(Integer.toString(mRecSvc.getRecordMaxSamples()));
				}
			}
			else {
				mRecSvc.setRecordMaxSamples(RecordService.RECORD_SAMPLES_INFINITE);
			}
		}
		else {
			Toast.makeText(RecordActivity.this, "Record Service not started: starting...",
					Toast.LENGTH_SHORT).show();
			Log.w(TAG, "Service not running; cannot pass settings change to GUI.");
			connectService();
			s.clear();
		}
	}
	
	/**
	 * Listener called whenever the user presses Enter in the Duration text box.
	 */
	private OnEditorActionListener mOnEditorActionDurationListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			onDurationUpdated(v.getEditableText());
			return false;
		}
		
	};

	/**
	 * Listener called whenever the user presses Enter in the Max Samples text box.
	 */
	private OnEditorActionListener mOnEditorActionSamplesListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			onMaxSamplesUpdated(v.getEditableText());
			return false;
		}
		
	};

	/**
	 * Listener called whenever the Duration text box gains or loses focus.
	 */
	private OnFocusChangeListener mOnFocusChangeDurationListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(!hasFocus) onDurationUpdated(((TextView)v).getEditableText());
		}
	};

	/**
	 * Listener called whenever the Max Samples text box gains or loses focus.
	 */
	private OnFocusChangeListener mOnFocusChangeSamplesListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(!hasFocus) onMaxSamplesUpdated(((TextView)v).getEditableText());
		}
	};

	/**
	 * Listener for the Forever checkbox (recording duration setting).
	 */
	private OnCheckedChangeListener mOnCheckedTimeListener = new OnCheckedChangeListener() {

		/**
		 * Called when the Forever checkbox (recording duration settings) is clicked. When the
		 * checkbox is checked, sets the recording duration to infinite and disables the Duration
		 * text box. When the checkbox is unchecked, sets the recording duration to the current
		 * contents of the Duration text box and re-enables the textbox.
		 * 
		 * If the RecordService is not available at call time, undoes the click and attempts to
		 * connect to the service.
		 */
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Log.d(TAG, "Detected Forever (recording duration) checkbox click.");
			if (mRecSvc != null) {
				if (isChecked) {
					mRecSvc.setRecordDuration(RecordService.RECORD_DURATION_INFINITE);
					mInputTime.setEnabled(false);
					mInputTime.setFocusable(false);
				}
				else {
					try {
						mRecSvc.setRecordDuration(Long.parseLong(mInputTime.getText().toString())*SEC_TO_MSEC);
					}
					catch (NumberFormatException e) {
						mInputTime.getEditableText().clear();
						mInputTime.getEditableText().append(
								Long.toString(RecordService.RECORD_DURATION_INFINITE));
					}
					mInputTime.setEnabled(true);
					mInputTime.setFocusable(true);
					mInputTime.setFocusableInTouchMode(true);
				}
			}
			else {
				Toast.makeText(RecordActivity.this, "Record Service not started: starting...",
						Toast.LENGTH_SHORT).show();
				Log.w(TAG, "Service not running; cannot pass settings change to GUI.");
				connectService();
				buttonView.setChecked(!isChecked); // undo the change
			}
		}

	};

	/**
	 * Listener for the Infinite checkbox (recording max samples setting).
	 */
	private OnCheckedChangeListener mOnCheckedSampleListener = new OnCheckedChangeListener() {

		/**
		 * Called when the Infinite checkbox (max samples settings) is clicked. When the
		 * checkbox is checked, sets the recording samples to infinite and disables the Samples
		 * text box. When the checkbox is unchecked, sets the recording samples to the current
		 * contents of the Duration text box and re-enables the textbox.
		 * 
		 * If the RecordService is not available at call time, undoes the click and attempts to
		 * connect to the service.
		 */
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Log.d(TAG, "Detected Infinite samples checkbox click.");
			if (mRecSvc != null) {
				if (isChecked) {
					mRecSvc.setRecordMaxSamples(RecordService.RECORD_SAMPLES_INFINITE);
					mInputSamples.setEnabled(false);
					mInputSamples.setFocusable(false);
				}
				else {
					try {
						mRecSvc.setRecordMaxSamples(
								Integer.parseInt(mInputSamples.getText().toString()));
					}
					catch(NumberFormatException e) {
						mInputSamples.getEditableText().clear();
						mInputSamples.getEditableText().append(
								Integer.toString(RecordService.RECORD_SAMPLES_INFINITE));
					}
					mInputSamples.setEnabled(true);
					mInputSamples.setFocusable(true);
					mInputSamples.setFocusableInTouchMode(true);
				}
			}
			else {
				Toast.makeText(RecordActivity.this, "Record Service not started: starting...",
						Toast.LENGTH_SHORT).show();
				Log.w(TAG, "Service not running; cannot pass settings change to GUI.");
				connectService();
				buttonView.setChecked(!isChecked); // undo the change
			}
		}

	};

	/**
	 * Update the state of all buttons, based on the current RecordService state. For example,
	 * while stopped, the main button is Record, but while running it is Pause.
	 * 
	 * Requires service connection. If the RecordService is not connected, disables all buttons.
	 */
	protected void updateButtons() {
		if (mRecSvc == null) {
			Log.w(TAG, "updateButtons(): No RecordService connection; disabling buttons");
			mButtonRecordPause.setEnabled(false);
			mButtonRecordPause.setClickable(false);
			mButtonAnalyze.setEnabled(false);
			mButtonAnalyze.setClickable(false);
			mButtonReset.setEnabled(false);
			mButtonReset.setClickable(false);
			return;
		}

		RecordService.Status svcStatus = mRecSvc.getStatus();
		String logPre = "updateButtons(): Service state is " + svcStatus.name();

		switch (svcStatus) {
		case ERROR: // fallthrough
		case ERROR_SENSORTAG: // fallthrough
		case ERROR_SENSORTAG_NULL:
			Log.w(TAG, logPre + "(\"" + mRecSvc.getLastError() + "\"); disabling buttons.");
			mButtonRecordPause.setEnabled(false);
			mButtonRecordPause.setClickable(false);
			mButtonAnalyze.setEnabled(false);
			mButtonAnalyze.setClickable(false);
			mButtonReset.setEnabled(false);
			mButtonReset.setClickable(false);
			break;

		case FINISHED:
			Log.i(TAG, logPre + "; RecordPauseButton=Record; AnalyzeButton=enabled; ResetButton=enabled");
			mButtonRecordPause.setEnabled(false);
			mButtonRecordPause.setClickable(false);
			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0,
					0, 0);
			mButtonRecordPause.setText(R.string.act_samp_button_record_complete);
			mButtonRecordPause.setOnClickListener(mOnClickRecordListener);
			
			mButtonAnalyze.setEnabled(true);
			mButtonAnalyze.setClickable(true);
			mButtonReset.setEnabled(true);
			mButtonReset.setClickable(true);
			break;
			
		case STANDBY:
			Log.i(TAG, logPre + "; RecordPauseButton=Record; AnalyzeButton=disabled; ResetButton=disabled");
			mButtonRecordPause.setEnabled(true);
			mButtonRecordPause.setClickable(true);
			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0,
					0, 0);
			mButtonRecordPause.setText(R.string.act_samp_button_record);
			mButtonRecordPause.setOnClickListener(mOnClickRecordListener);
			
			mButtonAnalyze.setEnabled(false);
			mButtonAnalyze.setClickable(false);
			mButtonReset.setEnabled(false);
			mButtonReset.setClickable(false);
			break;
			
		case PAUSE:
			Log.i(TAG, logPre + "; RecordPauseButton=Resume; AnalyzeButton=enabled; ResetButton=enabled");
			mButtonRecordPause.setEnabled(true);
			mButtonRecordPause.setClickable(true);
			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0,
					0, 0);
			mButtonRecordPause.setText(R.string.act_samp_button_resume);
			mButtonRecordPause.setOnClickListener(mOnClickRecordListener);
			
			mButtonAnalyze.setEnabled(true);
			mButtonAnalyze.setClickable(true);
			mButtonReset.setEnabled(true);
			mButtonReset.setClickable(true);
			break;
			
		case RECORD:
			Log.i(TAG, logPre + "; RecordPauseButton=Pause; AnalyzeButton=disabled; ResetButton=disabled");
			mButtonRecordPause.setEnabled(true);
			mButtonRecordPause.setClickable(true);
			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0,
					0, 0);
			mButtonRecordPause.setText(R.string.act_samp_button_pause);
			mButtonRecordPause.setOnClickListener(mOnClickPauseListener);
			
			mButtonAnalyze.setEnabled(false);
			mButtonAnalyze.setClickable(false);
			mButtonReset.setEnabled(false);
			mButtonReset.setClickable(false);
			break;

		case NOT_STARTED:
		default:
			Log.i(TAG, "updateButtons(): unknown Service state; disabling buttons.");
			mButtonRecordPause.setEnabled(false);
			mButtonRecordPause.setClickable(false);
			mButtonAnalyze.setEnabled(false);
			mButtonAnalyze.setClickable(false);
			mButtonReset.setEnabled(false);
			mButtonReset.setClickable(false);
			break;
		}
	}
	
	/**
	 * Listener for the Record button (not the Pause button).
	 */
	private Button.OnClickListener mOnClickRecordListener = new Button.OnClickListener() {

		/**
		 * Called when the Record button is clicked (not the Pause button). Calls the RecordService
		 * to start recording.
		 */
		@Override
		public void onClick(View v) {
			mRecSvc.record();
		}
		
	};

	/**
	 * Listener for the Pause button (not the Record button).
	 */
	private Button.OnClickListener mOnClickPauseListener = new Button.OnClickListener() {

		/**
		 * Called when the Pause button is clicked (not the Record button). Calls the RecordService
		 * to pause recording.
		 */
		@Override
		public void onClick(View v) {
			mRecSvc.pause();
		}
		
	};

	/**
	 * Listener for the Reset button.
	 */
	private Button.OnClickListener mOnClickResetListener = new Button.OnClickListener() {

		/**
		 * Called when the Reset button is clicked.. Calls the RecordService to clear all collected
		 * data.
		 */
		@Override
		public void onClick(View v) {
			mRecSvc.reset();
		}
		
	};

	/**
	 * Listener for the Analyze button.
	 */
	private Button.OnClickListener mOnClickAnalyzeListener = new Button.OnClickListener() {

		/**
		 * Called when the Analyze button is clicked. Starts the Analyze activity.
		 */
		@Override
		public void onClick(View v) {
			Log.i(TAG, "Starting Analysis activity.");
			Intent intent = new Intent(RecordActivity.this, AnalyzeActivity.class);
			startActivity(intent);
		}
		
	};

	/**
	 * Called when RecordService's status changes.
	 * @param s The new status value.
	 */
	@Override
	public void onStatusChanged(Status s) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updateButtons();
			}
			
		});
	}
}
