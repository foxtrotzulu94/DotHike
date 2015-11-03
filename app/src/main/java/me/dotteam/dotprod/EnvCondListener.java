package me.dotteam.dotprod;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by as on 2015-10-23.
 */
public class EnvCondListener implements SensorListenerInterface {

    public static EnvCondListener myInstance;
    private Activity owner;

    String TAG = "EnvCondListener";
    private TextView mTextDisplayHumidity;
    private TextView mTextDisplayTemperature;
    private TextView mTextDisplayPressure;

    public EnvCondListener(){
        myInstance = this;
    }

    public void setOwner(Activity owner) {
        this.owner = owner;
    }

    @Override
    public void update(HikeSensors hikesensors, double value) {
        final TextView updatingTesty;
        final double newVal=value;
        switch (hikesensors) {
            case TEMPERATURE: {
                Log.d(TAG, "Temperature: " + String.valueOf(value));
                updatingTesty=mTextDisplayTemperature;
                break;
            }
            case HUMIDITY: {
                Log.d(TAG, "Humidity: " + String.valueOf(value));
                updatingTesty=mTextDisplayHumidity;
                break;
            }
            case PRESSURE: {
                Log.d(TAG, "Pressure: " + String.valueOf(value));
                updatingTesty=mTextDisplayPressure;
                break;
            }
            default:{
                updatingTesty=mTextDisplayPressure;
                break;
            }
        }

        owner.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updatingTesty.setText(Double.toString(newVal));
            }
        });

    }


    public void setmTextDisplayHumidity(TextView mTextDisplayHumidity) {
        this.mTextDisplayHumidity = mTextDisplayHumidity;
    }

    public void setmTextDisplayTemperature(TextView mTextDisplayTemperature) {
        this.mTextDisplayTemperature = mTextDisplayTemperature;
    }

    public void setmTextDisplayPressure(TextView mTextDisplayPressure) {
        this.mTextDisplayPressure = mTextDisplayPressure;
    }
}