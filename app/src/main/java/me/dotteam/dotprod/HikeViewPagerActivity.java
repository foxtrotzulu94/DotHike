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
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.LocationPoints;
import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.loc.HikeLocationEntity;

/**
 * Hike ViewPager Activity
 * Created by EricTremblay on 15-11-13.
 */
public class HikeViewPagerActivity extends FragmentActivity implements LocationListener, HikeFragment.HikeFragmentListener, NavigationFragment.NavigationFragmentListener, EnvCondFragment.EnvCondFragmentListener {
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
    private boolean mMapReady = false;
    private PolylineOptions mMapPolylineOptions;
    private Button mButtonEndHike;
    private boolean mGotLocation = false;

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
        mHHM.startSensors(this); //TODO: CHANGE THIS!! Throws exception if Bluetooth is NOT ON

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
        mHLE.startLocationUpdates();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart() Called");
        super.onStart();

        // Add Listener to HHM
        mHHM.addListener(mSensorListener);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() Called");
        super.onStop();

        // Remove Listener from HHM
        mHHM.removeListener(mSensorListener);
    }

    @Override
    public void onBackPressed() {
        mPager.setCurrentItem(1);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Log values
        Log.i(TAG, "Location Changed!"
                + "\nLatitude: " + location.getLatitude()
                + "\nLongitude: " + location.getLongitude()
                + "\nAltitude: " + location.getAltitude()
                + "\nBearing: " + location.getBearing()
                + "\nAccuracy :" + location.getAccuracy());

        if (mLocation == null) {
            mLocation = new Location(location);
        } else {
            mLocation = location;
        }

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

        if (location.getAccuracy() <= 40) {
            List<Coordinates> coordinatesList = mLocationPoints.getCoordinateList();
            int numberOfPoints = coordinatesList.size();
            if (mMapReady) {
                if (numberOfPoints == 0) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMapPolylineOptions.add(latLng);
                    mMap.addPolyline(mMapPolylineOptions);

                    mLocationPoints.addPoint(new Coordinates((float) location.getLongitude(),
                            (float) location.getLatitude(), (float) location.getAltitude()));
                } else {
                    // Array to store results
                    float results[] = new float[3];

                    // Get previous and current longitude and latitude
                    double prevLongitude = coordinatesList.get(numberOfPoints - 1).getLongitude();
                    double prevLatitude = coordinatesList.get(numberOfPoints - 1).getLatitude();
                    double currLongitude = location.getLongitude();
                    double currLatitude = location.getLatitude();

                    // Calculate distance between both points and add it to total
                    Location.distanceBetween(prevLatitude, prevLongitude, currLatitude, currLongitude, results);
                    if (results[0] > location.getAccuracy()) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMapPolylineOptions.add(latLng);
                        mMap.addPolyline(mMapPolylineOptions);

                        mLocationPoints.addPoint(new Coordinates((float) location.getLongitude(),
                                (float) location.getLatitude(), (float) location.getAltitude()));
                        mDistanceTravelled += results[0];
                        if (mTextDistanceTraveled != null) {
                            mTextDistanceTraveled.setText(String.valueOf(mDistanceTravelled));
                        }
                    }
                }
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

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Attempt to find and zoom to current location
                mapZoomCameraToCurrentLocation();

                // If finding current location failed, start a thread to retry
                if (!mGotLocation) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Entered find current location thread");
                            int counter = 0;
                            // This will attempt to find location numberOfAttempts times
                            int numberofAttempts = 5; //Number of attempts
                            int waitBetweenAttempts = 1000; // how long to wait between attempts in milliseconds
                            while (counter < numberofAttempts) {
                                try {
                                    Thread.sleep(waitBetweenAttempts);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // Attempt to find current location
                                mapZoomCameraToCurrentLocation();

                                // If attempt was successful, break out of while loop to exit thread
                                if (mGotLocation) {
                                    Log.d(TAG, "Current location found");
                                    break;
                                }
                                // If attempt failed, increment counter
                                else {
                                    counter++;
                                }
                            }

                            // If the counter is equal numberOfAttempts, give up
                            if (counter == numberofAttempts) {
                                Log.e(TAG, "Current location could not be found");

                                // Show a Toast to inform user
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(HikeViewPagerActivity.this, "Current location could not be found", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                    t.start();
                }
            }
        });
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
                Intent intentResults = new Intent(HikeViewPagerActivity.this, ResultsActivity.class);
                startActivity(intentResults);
                mHDD.endCollectionService();
                mHHM.stopSensorTag();
                finish();
            }
        });
    }

    private void mapZoomCameraToCurrentLocation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Location location = mMap.getMyLocation();
                if (location != null) {
                    final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    mGotLocation = true;
                }
            }
        });
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

}
