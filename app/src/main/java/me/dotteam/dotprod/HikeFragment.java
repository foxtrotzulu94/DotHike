package me.dotteam.dotprod;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by EricTremblay on 15-11-13.
 */
public class HikeFragment extends Fragment implements OnMapReadyCallback {
    private String TAG = "HikeFragment";
    private Button mButtonEndHike;
    private ImageView mImageViewEnvArrow;
    private ImageView mImageViewNavArrow;

    private HikeFragmentListener mListener;

    private SupportMapFragment mSupportMapFragment;
    private GoogleMap mGoogleMap;

    public interface HikeFragmentListener {
        void onMapReady(GoogleMap googleMap);
        void onHikeFragmentReady();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HikeFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HikeFragmentListener");
        }
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
        mImageViewEnvArrow = (ImageView) rootView.findViewById(R.id.imageEnvArrow);
        mImageViewNavArrow = (ImageView) rootView.findViewById(R.id.imageNavArrow);

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

    public ImageView getImageViewEnvArrow(){
        return mImageViewEnvArrow;
    }

    public  ImageView getImageViewNavArrow(){
        return mImageViewNavArrow;
    }
}
