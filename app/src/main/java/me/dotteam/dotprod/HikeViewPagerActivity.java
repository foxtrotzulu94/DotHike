package me.dotteam.dotprod;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

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
    private Button mButtonPauseHike;
    private ImageView mImageViewEnvArrow;
    private ImageView mImageViewNavArrow;
    private boolean mGotLocation = false;
    private boolean mHikeCurrentlyPaused = false;
    private boolean mEndHikeButtonLocked = true;
    private boolean mPauseHikeButtonLocked = false;
    private boolean mMapReady = false;

    /**
     * EnvCondFragment variables and UI element references
     */
    private TextView mTextDisplayHumidity;
    private TextView mTextDisplayTemperature;
    private TextView mTextDisplayPressure;
    private double mHumidity = Double.NaN;
    private double mTemperature = Double.NaN;
    private double mPressure = Double.NaN;

    /**
     * NavigationFragment variables and UI element references
     */
    private TextView mTextLatitude;
    private TextView mTextLongitude;
    private TextView mTextAltitude;
    private TextView mTextDistanceTraveled;
    private TextView mTextStepCount;
    private float mDistanceTravelled = 0;
    private int mStepCount = 0;
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

        //Cleanup any garbage
        Runtime.getRuntime().gc();
        System.gc();
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
        mHHM.stopCompass();
    }

    @Override
    public void onBackPressed() {
        if(mPager.getCurrentItem()!=1) {
            mPager.setCurrentItem(1);
        }
        else{
            endHike();
        }
    }

    @Override
    public void onLocationChanged(Location location, float distance) {
        Log.d(TAG, "onLocationChanged");
        // Set TextViews to new values
        if (mTextLatitude != null) {
            mTextLatitude.setText(String.format("%.7f˚", location.getLatitude()));
        }
        if (mTextLongitude != null) {
            mTextLongitude.setText(String.format("%.7f˚", location.getLongitude()));
        }
        if (mTextAltitude != null) {
            mTextAltitude.setText(String.format("%.2f m", location.getAltitude()));
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
                mTextDistanceTraveled.setText(String.format("%.2f m", mDistanceTravelled));
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
        SharedPreferences prefMan=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(prefMan.contains("display_maptype")){
            mMap.setMapType(
                    Integer.parseInt(prefMan.getString(
                            "display_maptype",
                            Integer.toString(GoogleMap.MAP_TYPE_TERRAIN))));
        }
        else {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
    }

    @Override
    public void onHikeFragmentReady() {
        Log.d(TAG, "onHikeFragmentReady() Called");

        // Get references to UI elements
        mButtonEndHike = mHikeFragment.getButtonEndHike();
        mButtonPauseHike = mHikeFragment.getButtonPauseHike();
        mImageViewEnvArrow = mHikeFragment.getImageViewEnvArrow();
        mImageViewNavArrow = mHikeFragment.getImageViewNavArrow();

        // Set callback for End Hike Button
        mButtonEndHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Button currently locked
                if (!mEndHikeButtonLocked) {
                    endHike();

                    //Unlocking Button
                } else {
                    mEndHikeButtonLocked = false;
                    mPauseHikeButtonLocked = true;

                    mHikeFragment.setButtonEndHIke(0.2f);
                    mHikeFragment.setButtonPauseHIke(0.8f);
                }
            }
        });

        // Set callback for Pause Hike Button
        mButtonPauseHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPauseHikeButtonLocked) {
                    if (!mHikeCurrentlyPaused) {
                        //Pause the collection and saving of data
                        mHLE.stopLocationUpdates();
                        mHHM.stopSensors();
                        mHHM.startCompass(); //Keep compass on
                        mHDD.setPauseStatus(true);
                        mHikeCurrentlyPaused = true;

                        mButtonPauseHike.setText("Resume Hike");

                        //Alert notifying User that the Hike is Paused
                        AlertDialog.Builder builder = new AlertDialog.Builder(HikeViewPagerActivity.this);
                        builder.setTitle("Hike Paused");
                        builder.setMessage("The Hike has been Paused");
                        AlertDialog pauseAlert = builder.create();
                        pauseAlert.show();
                    } else {
                        //UnPause the collection and saving of data
                        mHLE.startLocationUpdates(HikeViewPagerActivity.this);
                        mHHM.startSensors(HikeViewPagerActivity.this);
                        mHDD.setPauseStatus(false);
                        mHikeCurrentlyPaused = false;

                        mButtonPauseHike.setText("Pause Hike");

                        //Alert notifying User that the Hike Resumed
                        AlertDialog.Builder builder = new AlertDialog.Builder(HikeViewPagerActivity.this);
                        builder.setTitle("Hike Resumed");
                        builder.setMessage("The Hike has Resumed");
                        AlertDialog resumeAlert = builder.create();
                        resumeAlert.show();
                    }
                } else {
                    //Unlocking Button
                    mEndHikeButtonLocked = true;
                    mPauseHikeButtonLocked = false;
                    mHikeFragment.setButtonEndHIke(0.8f);
                    mHikeFragment.setButtonPauseHIke(0.2f);
                }
            }
        });

        //Set callback for Nav-Arrow ImageView
        mImageViewNavArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(0);
            }
        });

        //Set callback for Env-Arrow ImageView
        mImageViewEnvArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(2);
            }
        });
    }

    private void endHike(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HikeViewPagerActivity.this);
        builder.setPositiveButton("End Hike", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //End the Hike
                mHHM.resetPedometer();
                mHLE.removeListener(HikeViewPagerActivity.this);
                mHLE.stopLocationUpdates();
                mLocation = null;
                Intent intentResults = new Intent(HikeViewPagerActivity.this, ResultsActivity.class);
                startActivity(intentResults);
                mHDD.endCollectionService();
                mHHM.stopSensors();
                finish();
            }
        });

        builder.setNegativeButton("Continue Hike", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Continue the Hike
            }
        });

        builder.setMessage("Are you sure you would like to end the Hike?");
        builder.setTitle("End Hike");
        AlertDialog EndHikeAlert = builder.create();
        EndHikeAlert.show();
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

    void updateTemperature(final double temp) {
        mTemperature = temp;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextDisplayTemperature != null) {
                    if (Double.isNaN(mTemperature)) {
                        mTextDisplayTemperature.setText("N/A");
                    } else {
                        mTextDisplayTemperature.setText(String.format("%.2f ˚C", mTemperature));
                    }
                }
            }
        });
    }

    void updateHumidity(final double hum) {
        mHumidity = hum;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextDisplayHumidity != null) {
                    if (Double.isNaN(mHumidity)) {
                        mTextDisplayHumidity.setText("N/A");
                    } else {
                        mTextDisplayHumidity.setText(String.format("%.2f %%", mHumidity));
                    }
                }
            }
        });
    }

    void updatePressure(final double pressure) {
        mPressure = pressure;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextDisplayPressure != null) {
                    if (Double.isNaN(mPressure)) {
                        mTextDisplayPressure.setText("N/A");
                    } else {
                        mTextDisplayPressure.setText(String.format("%.2f kPa", mPressure));
                    }
                }
            }
        });
    }
    void updateStepCount(final double stepcount) {
        mStepCount = (int)stepcount;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextStepCount != null) {
                    mTextStepCount.setText(String.valueOf(mStepCount));
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
        if (Double.isNaN(mTemperature)) {
            mTextDisplayTemperature.setText("N/A");
        } else {
            mTextDisplayTemperature.setText(String.format("%.2f ˚C", mTemperature));
        }
        if (Double.isNaN(mHumidity)) {
            mTextDisplayHumidity.setText("N/A");
        } else {
            mTextDisplayHumidity.setText(String.format("%.2f %%", mHumidity));
        }
        if (Double.isNaN(mPressure)) {
            mTextDisplayPressure.setText("N/A");
        } else {
            mTextDisplayPressure.setText(String.format("%.2f kPa", mPressure));
        }
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
        mTextDistanceTraveled = mNavFragment.getTextDistanceTraveled();
        mTextStepCount = mNavFragment.getTextStepCount();

        // Set Values to previous values
        if (mLocation != null) {
            mTextLatitude.setText(String.format("%.7f˚", mLocation.getLatitude()));
            mTextLongitude.setText(String.format("%.7f˚", mLocation.getLongitude()));
            mTextAltitude.setText(String.format("%.2f m", mLocation.getAltitude()));
        } else {
            mTextLatitude.setText("N/A");
            mTextLongitude.setText("N/A");
            mTextAltitude.setText("N/A");
        }
        mTextDistanceTraveled.setText(String.format("%.2f m", mDistanceTravelled));
        mTextStepCount.setText(String.valueOf(mStepCount));
    }


}
