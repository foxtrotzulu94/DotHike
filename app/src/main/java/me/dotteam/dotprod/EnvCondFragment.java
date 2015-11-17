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
 * Environmental Conditions Fragment
 * Created by EricTremblay on 15-11-13.
 */
public class EnvCondFragment extends Fragment {

    /**
     * Reference to host activity which must implement the fragment's listener interface
     */
    private EnvCondFragmentListener mListener;

    /**
     * References to conditions TextViews
     */
    private TextView mTextDisplayHumidity;
    private TextView mTextDisplayTemperature;
    private TextView mTextDisplayPressure;

    /**
     * Interface that must be implemented by the host activity
     */
    public interface EnvCondFragmentListener {
        void onEnvCondFragmentReady();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (EnvCondFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EnvCondFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.activity_env_cond, container, false);

        mTextDisplayHumidity = (TextView) rootView.findViewById(R.id.textDispHum);
        mTextDisplayPressure = (TextView) rootView.findViewById(R.id.textDispPress);
        mTextDisplayTemperature = (TextView) rootView.findViewById(R.id.textDispTemp);

        mListener.onEnvCondFragmentReady();
        return rootView;
    }

    /**
     * Getter for reference to humidity TextView
     * @return
     */
    public TextView getTextDisplayHumidity() {
        return mTextDisplayHumidity;
    }

    /**
     * Getter for reference to temperature TextView
     * @return
     */
    public TextView getTextDisplayTemperature() {
        return mTextDisplayTemperature;
    }

    /**
     * Getter for reference to pressure TextView
     * @return
     */
    public TextView getTextDisplayPressure() {
        return mTextDisplayPressure;
    }
}
