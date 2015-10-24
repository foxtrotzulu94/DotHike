package me.dotteam.dotprod;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainHikeActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String TAG = "MainHikeActivity";
    private Button buttonEndHike;
    private Button buttonEnvCond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate() Called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainhike);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buttonEndHike = (Button) findViewById(R.id.buttonEndHike);
        buttonEnvCond = (Button) findViewById(R.id.buttonEnvCond);

        buttonEndHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentResults = new Intent(MainHikeActivity.this, ResultsActivity.class);
                startActivity(intentResults);
            }
        });
        buttonEnvCond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentEnvCond = new Intent(MainHikeActivity.this, EnvCondActivity.class);
                startActivity(intentEnvCond);
            }
        });
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
        Log.i(TAG, "onMapReady() Called");
        mMap = googleMap;

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
                Location location = mMap.getMyLocation();
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
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


}
