/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Example application: Data/Event Sampler
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */
package ca.concordia.sensortag.datasample;

import android.content.Intent;

/**
 * Activity to allow the user to select a SensorTag device to connect to. This subclass overrides
 * {@link #getDeviceActivityIntent()} in order to specify the main activity to launch once a
 * SensorTag device is selected. See the Minimal example for more detailed explanations.
 */
public class DeviceSelectActivity extends ti.android.ble.sensortag.DeviceSelectActivity {

	/**
	 * Returns an Intent that is destined for the application's main activity. No other extras or
	 * data need be specified for this Intent related to the SensorTag/Bluetooth device (you may,
	 * however, specify other data required by the Activity that is not SensorTag-related).
	 * 
	 * See the Minimal example for more detailed explanations.
	 */
	@Override
	protected Intent getDeviceActivityIntent() {
		return new Intent(this, RecordActivity.class);
	}

}

