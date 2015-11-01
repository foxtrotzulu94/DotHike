package me.dotteam.dotprod.data;

import android.content.ContentValues;

/**
 * 
 */
public class Coordinates {

    /**
     * 
     */
    protected float longitude;

    /**
     * 
     */
    protected float latitude;

    /**
     * 
     */
    protected float altitude;


    /**
     * Default constructor
     */
    public Coordinates(float longitude, float lat, float alt) {
        this.longitude=longitude;
        latitude=lat;
        altitude=alt;
    }

    /**
     * @return
     */
    public ContentValues toStorage(int ID) {
        ContentValues retVal = new ContentValues();
        retVal.put(DBAssistant.HIKE_ID,ID);
        retVal.put(DBAssistant.LONG_COL,this.longitude);
        retVal.put(DBAssistant.LAT_COL,this.latitude);
        retVal.put(DBAssistant.ALT_COL,this.altitude);
        return retVal;
    }

}