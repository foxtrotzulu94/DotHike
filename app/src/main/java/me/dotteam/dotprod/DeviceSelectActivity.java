package me.dotteam.dotprod;

/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Example application: Weather Station
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */

import android.content.Intent;
/**
 * Activity to allow the user to select a SensorTag device to connect to. All of the functionality
 * for scanning for Bluetooth devices, letting the user select one and then launching the main app
 * Activity is provided by the SensorTag library (see
 * {@link ti.android.ble.sensortag.DeviceSelectActivity}). However, in order for the library's
 * DeviceSelectActivity to know what the main Activity is for your own app and be able to launch it,
 * each project somehow needs to specify it: in this case you specify the intent that will launch
 * the main activity by overriding {@link getDeviceActivityIntent()} in a subclass (= this class).
 */
public class DeviceSelectActivity extends ti.android.ble.sensortag.DeviceSelectActivity {

	/**
	 * Returns an Intent that is destined for the application's main activity.
	 * 
	 * After the user selects a SensorTag device to connect to, the DeviceSelectActivity calls this
	 * method to get the Intent it can use to launch your application's main Activity. This Intent
	 * has to be destined for that main activity (in this case we called it simply MainActivity) and
	 * is the only way for the superclass to know which activity it needs to launch.
	 */
	@Override
	protected Intent getDeviceActivityIntent() {
		return new Intent(this, HomeActivity.class);
	}

}
