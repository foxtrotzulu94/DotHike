package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * Created by foxtrot on 18/11/15.
 */
public class StepCount {
    private int stepsTaken=0;


    public StepCount(int steps){
        stepsTaken = steps;
    }

    public StepCount(double steps){
        //There are probably more cases to protect against when casting double such as NaN, +- Inf
        //For now, we just directly cast it.
        stepsTaken = (int) steps;
    }

    public int getStepsTaken(){
        return stepsTaken;
    }

    public ContentValues toStorage(int ID){
        ContentValues retVal = new ContentValues();
        retVal.put(DBAssistant.HIKE_ID,ID);
        retVal.put(DBAssistant.STEP_COUNT,this.stepsTaken);
        return retVal;
    }

    public String toString(){
        if(stepsTaken>0)
            return String.format("Steps Taken: %s\n",stepsTaken);
        else
            return "No steps recorded\n";
    }

}
