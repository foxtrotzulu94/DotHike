package me.dotteam.dotprod;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.redinput.compassview.CompassView;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by EricTremblay on 15-11-13.
 */
public class HikeFragment extends Fragment implements OnMapReadyCallback {
    private String TAG = "HikeFragment";
    private Button mButtonEndHike;

    private HikeFragmentListener mListener;

    private SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;
    private CompassView mCompassView;

    private float curVal=0;

    public interface HikeFragmentListener {
        void onMapReady(GoogleMap googleMap);
        void onHikeFragmentReady();
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HikeFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HikeFragmentListener");
        }


        Thread testy = new Thread(){
            @Override
            public void run(){
                final Random randy = new Random();
                for (int i = 0; i < 100; i++) {
                    curVal+=randy.nextFloat();
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    float value = curVal;
                                    mCompassView.setDegrees(curVal);
                                }
                            }
                    );

                    try{
                        sleep(300);
                    }
                    catch (Exception e){
                        //Diaper Pattern
                    }
                }
            }
        };
        testy.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.activity_hike, container, false);

        mSupportMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mSupportMapFragment).commit();
        }
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(this);

        }

        mButtonEndHike = (Button) rootView.findViewById(R.id.buttonEndHike);
        mCompassView = (CompassView) rootView.findViewById(R.id.compass);
        mCompassView.setRangeDegrees(180);
        mCompassView.setBackgroundColor(getResources().getColor(R.color.hike_naval));
        mCompassView.setLineColor(getResources().getColor(R.color.hike_black_tricorn));
        mCompassView.setMarkerColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setTextColor(Color.BLACK);
        mCompassView.setShowMarker(true);
        mCompassView.setTextSize(37);
        mCompassView.setDegrees(90);



        mListener.onHikeFragmentReady();
        return rootView;
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
        mListener.onMapReady(googleMap);

    }


    public Button getButtonEndHike() {
        return mButtonEndHike;
    }

    public CompassView getUICompass(){
        return mCompassView;
    }
}
