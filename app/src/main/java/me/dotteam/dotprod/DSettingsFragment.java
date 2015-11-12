package me.dotteam.dotprod;

/**
 * Created by Corentin on 2015-11-11.
 */

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DSettingsFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialogue_settings_fragment, container,
                false);
        getDialog().setTitle("Settings");
        // Do something else
        return rootView;
    }
}
