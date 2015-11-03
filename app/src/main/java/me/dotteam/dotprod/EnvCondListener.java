package me.dotteam.dotprod;

import android.util.Log;
import android.widget.TextView;

/**
 * Created by as on 2015-10-23.
 */
public class EnvCondListener implements SensorListenerInterface {

    public static EnvCondListener myInstance;

    String TAG = "EnvCondListener";
    private TextView mTextDisplayHumidity;
    private TextView mTextDisplayTemperature;
    private TextView mTextDisplayPressure;

    public EnvCondListener(){
        myInstance = this;
    }

    @Override
    public void update(HikeSensors hikesensors, double value) {
        TextView updatingTesty;
        switch (hikesensors) {
            case TEMPERATURE: {
                Log.d(TAG, "Temperature: " + String.valueOf(value));
                updatingTesty=mTextDisplayTemperature;
            }
            case HUMIDITY: {
                Log.d(TAG, "Humidity: " + String.valueOf(value));
                updatingTesty=mTextDisplayHumidity;
            }
            case PRESSURE: {
                Log.d(TAG, "Pressure: " + String.valueOf(value));
                updatingTesty=mTextDisplayPressure;
            }
            default:{
                updatingTesty=mTextDisplayPressure;
            }
        }
        updatingTesty.setText(Double.toString(value));
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