package me.dotteam.dotprod;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by EricTremblay on 15-11-13.
 */
public class NavigationFragment extends Fragment {
    private NavigationFragmentListener mListener;

    // TextViews References
    private TextView mTextLatitude;
    private TextView mTextLongitude;
    private TextView mTextAltitude;
    private TextView mTextBearing;
    private TextView mTextAccuracy;
    private TextView mTextDistanceTraveled;
    private TextView mTextStepCount;

    public interface NavigationFragmentListener {
        public void onNavigationFragmentReady();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NavigationFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NavigationFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.activity_navigation, container, false);

        mTextLatitude = (TextView) rootView.findViewById(R.id.textLatitude);
        mTextLongitude = (TextView) rootView.findViewById(R.id.textLongitude);
        mTextAltitude = (TextView) rootView.findViewById(R.id.textAltitude);
        mTextBearing = (TextView) rootView.findViewById(R.id.textBearing);
        mTextAccuracy = (TextView) rootView.findViewById(R.id.textAccuracy);
        mTextDistanceTraveled = (TextView) rootView.findViewById(R.id.textDistanceTraveled);
        mTextStepCount = (TextView) rootView.findViewById(R.id.textStepCount);

        mListener.onNavigationFragmentReady();

        return rootView;
    }

    public TextView getTextLatitude() {
        return mTextLatitude;
    }

    public TextView getTextLongitude() {
        return mTextLongitude;
    }

    public TextView getTextAltitude() {
        return mTextAltitude;
    }

    public TextView getTextBearing() {
        return mTextBearing;
    }

    public TextView getTextAccuracy() {
        return mTextAccuracy;
    }

    public TextView getTextDistanceTraveled() {
        return mTextDistanceTraveled;
    }

    public TextView getTextStepCount() {
        return mTextStepCount;
    }
}
