package me.dotteam.dotprod;

import android.app.Activity;
import android.content.Context;

import com.redinput.compassview.CompassView;

/**
 * Created by foxtrot on 01/12/15.
 */
public class CompassAnimator extends Thread{

    float currentDegrees = 0.0f;
    float finalDegrees = 0.0f;
    float dampingPercentage = 0.05f;
    boolean runningThread =false;

    private Activity mRunningActivity;
    private CompassView mCompassView;

    public CompassAnimator(Activity thisActivity, CompassView uiCompass){
        mRunningActivity = thisActivity;
        mCompassView = uiCompass;
    }

    @Override
    public void run(){
        runningThread =true;
        while(runningThread){
            if (currentDegrees!=finalDegrees){
                currentDegrees = lerp(currentDegrees,finalDegrees,dampingPercentage);
                updateUI(currentDegrees);
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
        mRunningActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCompassView.setDegrees(value);
            }
        });
    }

    public void setNewValue(float newValue){
        finalDegrees = newValue % 360;
        if(!isAlive() && !runningThread) {
            this.start();
            runningThread = true;
        }
    }

    public void setAndStop(float newValue){
        updateUI(newValue);
        runningThread = false;
    }

    public void stopAnimation(){
        setAndStop(finalDegrees);
    }
}
