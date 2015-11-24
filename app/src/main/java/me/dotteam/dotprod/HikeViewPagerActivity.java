package me.dotteam.dotprod;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Random;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.LocationPoints;
import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.loc.HikeLocationEntity;
import me.dotteam.dotprod.loc.HikeLocationListener;

/**
 * Hike ViewPager Activity
 * Created by EricTremblay on 15-11-13.
 */
public class HikeViewPagerActivity extends FragmentActivity implements HikeLocationListener, HikeFragment.HikeFragmentListener, NavigationFragment.NavigationFragmentListener, EnvCondFragment.EnvCondFragmentListener {
    /**
     * Activity's TAG for logging
     */
    private String TAG = "HikeViewPagerActivity";
    /**
     * The number of pages the view pager has.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    /**
     * References to Fragments within the viewpager
     * This maintains one instance of each fragment throughout the life of this activity
     */
    private HikeFragment mHikeFragment;
    private EnvCondFragment mEnvCondFragment;
    private NavigationFragment mNavFragment;

    /**
     * HikeFragment variables and UI element references
     */
    private GoogleMap mMap;
    private PolylineOptions mMapPolylineOptions;
    private Button mButtonEndHike;
    private boolean mGotLocation = false;
    private boolean mMapReady = false;

    /**
     * EnvCondFragment variables and UI element references
     */
    private TextView mTextDisplayHumidity;
    private TextView mTextDisplayTemperature;
    private TextView mTextDisplayPressure;
    private String mHumidityString = "0.0";
    private String mTemperatureString = "0.0";
    private String mPressureString = "0.0";

    /**
     * NavigationFragment variables and UI element references
     */
    private TextView mTextLatitude;
    private TextView mTextLongitude;
    private TextView mTextAltitude;
    private TextView mTextBearing;
    private TextView mTextAccuracy;
    private TextView mTextDistanceTraveled;
    private TextView mTextStepCount;
    private float mDistanceTravelled = 0;
    private String mStepCountString = "0";
    private Location mLocation;
    private LocationPoints mLocationPoints;

    /**
     * Reference to HikeHardwareManager
     */
    private HikeHardwareManager mHHM;

    /**
     * Listener for HikeHardwareManager
     */
    private HikeSensorListener mSensorListener;

    /**
     * Reference to HikeLocationEntity
     */
    private HikeLocationEntity mHLE;

    /**
     * Reference to HikeDataDirector
     */
    private HikeDataDirector mHDD;

    /**
     * Reference to itself
     */
    HikeViewPagerActivity mThis;

    /**
     * A simple pager adapter that represents 3 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return mNavFragment;
                }
                case 1: {
                    return mHikeFragment;
                }
                case 2: {
                    return mEnvCondFragment;
                }
                default: {
                    return mHikeFragment;
                }
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() Called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);

        mThis = this;

        // Instantiate Fragments
        mHikeFragment = new HikeFragment();
        mEnvCondFragment = new EnvCondFragment();
        mNavFragment = new NavigationFragment();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        // Set ViewPager Page to the HikeFragment
        mPager.setCurrentItem(1);

        // Instantiate HikeHardwareManager
        mHHM = HikeHardwareManager.getInstance(this);

        // Start SensorTag connection and pedometer
        mHHM.startSensors(this);

        // Create Listener for HikeHardwareManager
        mSensorListener = new HikeSensorListener(this);

        // Instantiate HikeDataDirector
        mHDD = HikeDataDirector.getInstance(this);

        // Begin collecition service
        mHDD.beginCollectionService();

        // Get HLE reference and add listener
        mHLE = HikeLocationEntity.getInstance(this);

        // New LocationPoints object to save coordinates
        mLocationPoints = new LocationPoints();

        // Add Listener to HLE
        mHLE.addListener(this);

        // Start Location Updates
        mHLE.startLocationUpdates(this);

        Thread testy = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(5000);
                }
                catch (Exception e){
                    //Do Nothing
                }
                Log.d(TAG, "run Starting randy");
                Random randy = new Random();
                for (int i = 0; i < 100; i++) {
                    try{
                        sleep(250);
                    }
                    catch (Exception e){
                        //Do nothing
                    }
                    mHikeFragment.updateCompass(randy.nextDouble() * 360.0);
                    Log.d(TAG, "Updating Compass now");
                }
            }
        };
//        testy.start();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart() Called");
        super.onStart();

        // Add Listener to HHM
        mHHM.addListener(mSensorListener);
        mHHM.startCompass();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() Called");
        super.onStop();

        // Remove Listener from HHM
        mHHM.removeListener(mSensorListener);
        mHHM.endCompass();
    }

    @Override
    public void onBackPressed() {
        mPager.setCurrentItem(1);
    }

    @Override
    public void onLocationChanged(Location location, float distance) {
        Log.d(TAG, "onLocationChanged");
        // Set TextViews to new values
        if (mTextLatitude != null) {
            mTextLatitude.setText(String.valueOf(location.getLatitude()));
        }
        if (mTextLongitude != null) {
            mTextLongitude.setText(String.valueOf(location.getLongitude()));
        }
        if (mTextAltitude != null) {
            mTextAltitude.setText(String.valueOf(location.getAltitude()));
        }
        if (mTextBearing != null) {
            mTextBearing.setText(String.valueOf(location.getBearing()));
        }
        if (mTextAccuracy != null) {
            mTextAccuracy.setText(String.valueOf(location.getAccuracy()));
        }

        if (mLocation == null) {
            mLocation = new Location(location);

            if (mMapReady) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mapZoomCameraToLocation(latLng);
                mMapPolylineOptions.add(latLng);
                mMap.addPolyline(mMapPolylineOptions);
            }
        } else {
            mLocation = location;

            if (mMapReady) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMapPolylineOptions.add(latLng);
                mMap.addPolyline(mMapPolylineOptions);
            }

            mDistanceTravelled += distance;

            if (mTextDistanceTraveled != null) {
                mTextDistanceTraveled.setText(String.valueOf(mDistanceTravelled));
            }
        }
    }

    /// ===========================================
    //
    //  HikeFragment Methods and Callbacks
    //
    /// ===========================================

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady() Called");
        mMap = googleMap;
        mMapPolylineOptions = new PolylineOptions();
        mMapReady = true;

        // Set Maps Settings
        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setTiltGesturesEnabled(false);
        mapSettings.setMyLocationButtonEnabled(true);
        mapSettings.setCompassEnabled(true);

        // Enable MyLocation
        mMap.setMyLocationEnabled(true);

        // Set Map Type to Terrain
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

    }

    @Override
    public void onHikeFragmentReady() {
        Log.d(TAG, "onHikeFragmentReady() Called");

        // Get references to UI elements
        mButtonEndHike = mHikeFragment.getButtonEndHike();

        // Set callback for End Hike Button
        mButtonEndHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset Pedometer
                // TODO Save Pedometer value
                mHHM.resetPedometer();
                mHLE.removeListener(mThis);
                mHLE.stopLocationUpdates();
                Intent intentResults = new Intent(HikeViewPagerActivity.this, ResultsActivity.class);
                startActivity(intentResults);
                mHDD.endCollectionService();
                mHHM.stopSensorTag();
                finish();
            }
        });


    }

    private void mapZoomCameraToLocation(final LatLng latlng) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                mGotLocation = true;
            }
        });
    }

    private void mapZoomCameraToLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mapZoomCameraToLocation(latLng);
    }

    void updateTemperature(final String temp) {
        mTemperatureString = temp;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextDisplayTemperature != null) {
                    mTextDisplayTemperature.setText(temp);
                }
            }
        });
    }

    void updateHumidity(final String hum) {
        mHumidityString = hum;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextDisplayHumidity != null) {
                    mTextDisplayHumidity.setText(hum);
                }
            }
        });
    }

    void updatePressure(final String pressure) {
        mPressureString = pressure;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextDisplayPressure != null) {
                    mTextDisplayPressure.setText(pressure);
                }
            }
        });
    }
    void updateStepCount(final String stepcount) {
        mStepCountString = stepcount;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextStepCount != null) {
                    mTextStepCount.setText(stepcount);
                }
            }
        });
    }

    void updateCompass(double degrees){
        //Send it of to the fragment
        mHikeFragment.updateCompass(degrees);
    }


    /// ===========================================
    //
    //  EnvCondFragment Methods and Callbacks
    //
    /// ===========================================

    @Override
    public void onEnvCondFragmentReady() {
        Log.d(TAG, "onEnvCondFragmentReady() Called");

        // Get references to UI elements
        mTextDisplayHumidity = mEnvCondFragment.getTextDisplayHumidity();
        mTextDisplayPressure = mEnvCondFragment.getTextDisplayPressure();
        mTextDisplayTemperature = mEnvCondFragment.getTextDisplayTemperature();

        // Set Text Initial Values
        mTextDisplayHumidity.setText(mHumidityString);
        mTextDisplayPressure.setText(mPressureString);
        mTextDisplayTemperature.setText(mTemperatureString);

    }

    /// ===========================================
    //
    //  NavigationFragment Methods and Callbacks
    //
    /// ===========================================

    @Override
    public void onNavigationFragmentReady() {
        Log.d(TAG, "onNavigationFragmentReady() Called");
        mTextLatitude = mNavFragment.getTextLatitude();
        mTextLongitude = mNavFragment.getTextLongitude();
        mTextAltitude = mNavFragment.getTextAltitude();
        mTextBearing = mNavFragment.getTextBearing();
        mTextAccuracy = mNavFragment.getTextAccuracy();
        mTextDistanceTraveled = mNavFragment.getTextDistanceTraveled();
        mTextStepCount = mNavFragment.getTextStepCount();

        // Set Values to previous values
        if (mLocation != null) {
            mTextLatitude.setText(String.valueOf(mLocation.getLatitude()));
            mTextLongitude.setText(String.valueOf(mLocation.getLongitude()));
            mTextAltitude.setText(String.valueOf(mLocation.getAltitude()));
            mTextBearing.setText(String.valueOf(mLocation.getBearing()));
            mTextAccuracy.setText(String.valueOf(mLocation.getAccuracy()));
        } else {
            mTextLatitude.setText("0.0");
            mTextLongitude.setText("0.0");
            mTextAltitude.setText("0.0");
            mTextBearing.setText("0.0");
            mTextAccuracy.setText("0.0");
        }
        mTextDistanceTraveled.setText(String.valueOf(mDistanceTravelled));
        mTextStepCount.setText(mStepCountString);
    }


}
