package me.dotteam.dotprod;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.redinput.compassview.CompassView;

import java.util.Random;

/**
 * Created by EricTremblay on 15-11-13.
 */
public class HikeFragment extends Fragment implements OnMapReadyCallback {

    private class CompassAnimator extends Thread{

        float currentDegrees = 0.0f;
        float finalDegrees = 0.0f;
        float dampingPercentage = 0.05f;
        boolean runningThread =false;

        @Override
        public void run(){
            runningThread =true;
            while(runningThread){
                if (currentDegrees!=finalDegrees){
                    currentDegrees = lerp(currentDegrees,finalDegrees,dampingPercentage);
                    updateUI(currentDegrees);
                }
                else{
                    runningThread = false;
                }

                try{
                    sleep(34); //30 FPS, no compass needs to be at 60...
                }
                catch (InterruptedException e){
                    runningThread = false;
                }

            }
        }

        private float lerp(float start, float end, float percentage){
            return start+( percentage*(end-start)  );
        }

        private void updateUI(final float value){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCompassView.setDegrees(value);
                }
            });
        }

        public void setNewValue(float newValue){
            finalDegrees = newValue % 360;
            if(!isAlive() && !runningThread)
                this.start();
        }

        public void setAndStop(float newValue){
            updateUI(newValue);
            runningThread = false;
        }

        public void stopAnimation(){
            setAndStop(finalDegrees);
        }
    }


    private String TAG = "HikeFragment";
    private Button mButtonEndHike;
    private Button mButtonPauseHike;
    private ImageView mImageViewEnvArrow;
    private ImageView mImageViewNavArrow;

    private HikeFragmentListener mListener;

    private SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;
    private CompassView mCompassView;

    private CompassAnimator mCompassAnimator;

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
        mCompassAnimator = new CompassAnimator();
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
        mButtonPauseHike = (Button) rootView.findViewById(R.id.buttonPauseHike);
        mImageViewEnvArrow = (ImageView) rootView.findViewById(R.id.imageEnvArrow);
        mImageViewNavArrow = (ImageView) rootView.findViewById(R.id.imageNavArrow);

        mCompassView = (CompassView) rootView.findViewById(R.id.compass);
        mCompassView.setRangeDegrees(180);
        mCompassView.setBackgroundColor(getResources().getColor(R.color.hike_indigo_baltik));
        mCompassView.setLineColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setMarkerColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setTextColor(getResources().getColor(R.color.hike_palisade));
        mCompassView.setShowMarker(true);
        mCompassView.setTextSize(40);
        mCompassView.setDegrees(0);

        mListener.onHikeFragmentReady();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompassAnimator.stopAnimation();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mListener.onMapReady(googleMap);

    }

    public void updateCompass(double value){
        //Tell the animator thread to begin
        if(mCompassAnimator!=null){
            mCompassAnimator.setNewValue((float) value);
        }
    }

    public Button getButtonEndHike() {
        return mButtonEndHike;
    }

    public void setButtonEndHIke(float weight){
        mButtonEndHike.setLayoutParams(
                new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, weight
                ));
    }

    public Button getButtonPauseHike() {
        return mButtonPauseHike;
    }

    public void setButtonPauseHIke(float weight){
        mButtonPauseHike.setLayoutParams(
                new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, weight
                ));
    }

    public ImageView getImageViewEnvArrow(){
        return mImageViewEnvArrow;
    }

    public  ImageView getImageViewNavArrow(){
        return mImageViewNavArrow;
    }

    public CompassView getUICompass(){
        return mCompassView;
    }
}
