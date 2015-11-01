package me.dotteam.dotprod.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.*;

/**
 * 
 */
public class DBAssistant extends SQLiteOpenHelper {

    public static final String DB_NAME="dot_hike";

    /**
     * 
     */
    public static final String HIKE="hikes";

    /**
     *
     */
    public static final String HIKE_ID="hike_id";

    /**
     * 
     */
    public static final String COORDS="hike_coordinates";

    /**
     * 
     */
    public static final String ENVTEMP="temperature";

    /**
     * 
     */
    public static final String ENVPRES="pressure";

    /**
     * 
     */
    public static final String ENVHUMD="humidity";

    public static final String MIN_COL="min";
    public static final String AVG_COL="avg";
    public static final String MAX_COL="max";

    public static final String LONG_COL="longitude";
    public static final String LAT_COL="latitude";
    public static final String ALT_COL="altitude";

    /**
     * 
     */
    public static final String SCHEME_CREATE="PRAGMA foreign_keys = off;\n" +
            "BEGIN TRANSACTION;\n" +
            "\n" +
            "CREATE TABLE "+ENVHUMD+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+MIN_COL+" REAL NOT NULL, "+AVG_COL+" REAL NOT NULL, "+MAX_COL+" REAL NOT NULL)\n" +
            "\n" +
            "CREATE TABLE "+ENVTEMP+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+MIN_COL+" REAL NOT NULL, "+AVG_COL+" REAL NOT NULL, "+MAX_COL+" REAL NOT NULL)\n" +
            "\n" +
            "CREATE TABLE "+COORDS+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+LONG_COL+" REAL NOT NULL, "+LAT_COL+" REAL NOT NULL, "+ALT_COL+" REAL)\n" +
            "\n" +
            "CREATE TABLE "+HIKE+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, startTime TIME NOT NULL, endTime TIME NOT NULL)\n" +
            "\n" +
            "CREATE TABLE "+ENVPRES+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+MIN_COL+" REAL NOT NULL, "+AVG_COL+" REAL NOT NULL, "+MAX_COL+" REAL NOT NULL)\n" +
            "\n" +
            "COMMIT TRANSACTION;\n" +
            "PRAGMA foreign_keys = on;\n";

    /**
     * 
     */
    public static final String SCHEME_DESTROY="PRAGMA foreign_keys = off;\n"+
            "DROP TABLE IF EXIST "+HIKE+","+COORDS+","+ENVHUMD+","+ENVTEMP+","+ENVPRES;

    /**
     * 
     */
    public static final int SCHEME_VERSION=1;

    public static final String LOG_ID="HikeDBA";


    /**
     * Default constructor
     */
    public DBAssistant(Context currentContext) {
        super(currentContext, DB_NAME, null, SCHEME_VERSION);
    }

    /**
     * @param sqlDB
     */
    public void onCreate(SQLiteDatabase sqlDB) {
        Log.d(LOG_ID,"Creating DB for First Time: "+DB_NAME+SCHEME_VERSION);
    }

    /**
     * @param sqlDB 
     * @param prev 
     * @param curr
     */
    public void onUpgrade(SQLiteDatabase sqlDB, int prev, int curr) {
        Log.w(LOG_ID,String.format("Updating DB from version %s to %s. Will destroy previous",prev,curr));
        sqlDB.execSQL(SCHEME_DESTROY);
        onCreate(sqlDB);
    }

}