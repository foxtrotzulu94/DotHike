package me.dotteam.dotprod;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.hw.TestSensorListener;
import me.dotteam.dotprod.loc.HikeLocationEntity;

public class HikeActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private String TAG = "HikeActivity";

    private GoogleMap mMap;
    private boolean mMapReady = false;
    private PolylineOptions mMapPolylineOptions;

    private Button mButtonEndHike;
    private Button mButtonEnvCond;
    private Button mButtonNavigationActivity;
    private boolean mGotLocation = false;

    private HikeHardwareManager mHHM;
    private HikeLocationEntity mHLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() Called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mButtonEndHike = (Button) findViewById(R.id.buttonEndHike);
        mButtonEnvCond = (Button) findViewById(R.id.buttonEnvCond);
        mButtonNavigationActivity = (Button) findViewById(R.id.buttonNavigationActivity);

        mButtonEndHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentResults = new Intent(HikeActivity.this, ResultsActivity.class);
                startActivity(intentResults);
            }
        });
        mButtonEnvCond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentEnvCond = new Intent(HikeActivity.this, EnvCondActivity.class);
                startActivity(intentEnvCond);
            }
        });

        mButtonNavigationActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNavigationActivity = new Intent(HikeActivity.this, NavigationActivity.class);
                startActivity(intentNavigationActivity);
            }
        });

        // Test to see if SensorTag readings are still captured when app is in the background
        mHHM = HikeHardwareManager.getInstance(this);
        mHHM.startSensorTagConnector();
        mHHM.addListener(new TestSensorListener());

        // Get HLE reference and add listener
        mHLE = HikeLocationEntity.getInstance(this);
        mHLE.addListener(this);
        mHLE.startLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Changed!"
                + "\nLatitude: " + location.getLatitude()
                + "\nLongitude: " + location.getLongitude()
                + "\nAltitude: " + location.getAltitude()
                + "\nBearing: " + location.getBearing()
                + "\nAccuracy :" + location.getAccuracy());

        if (location.getAccuracy() <= 40) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMapPolylineOptions.add(latLng);
            mMap.addPolyline(mMapPolylineOptions);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
                                        Toast.makeText(HikeActivity.this, "Current location could not be found", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                    t.start();
                }
            }
        });

        // Add a marker in Montreal and LaPrairie and move the camera
        /*LatLng montreal = new LatLng(45.5017, -73.5673);
        LatLng laprairie = new LatLng(45.4167, -73.5);
        mMap.addMarker(new MarkerOptions().position(montreal).title("Montreal"));
        mMap.addMarker(new MarkerOptions().position(laprairie).title("LaPrairie"));


        // Add Polyline between Montreal and LaPrairie
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(montreal)
                .add(laprairie);

        Polyline polyline = mMap.addPolyline(polylineOptions);*/

    }

    private void mapZoomCameraToCurrentLocation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Location location = mMap.getMyLocation();
                if (location != null){
                    final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    mGotLocation = true;
                }
            }
        });
    }
}
