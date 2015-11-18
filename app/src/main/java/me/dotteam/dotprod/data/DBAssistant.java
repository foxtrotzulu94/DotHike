package me.dotteam.dotprod.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Class in charge of interacting with Android to use the OS-provided SQLite3 Database
 */
public class DBAssistant extends SQLiteOpenHelper {

    public static final String DB_NAME="dot_hike";

    public static final String HIKE="hikes";
    public static final String HIKE_START="startTime";
    public static final String HIKE_END="endTime";

    public static final String HIKE_ID="hike_id";

    public static final String COORDS="hike_coordinates";
    public static final String ENVTEMP="temperature";
    public static final String ENVPRES="pressure";
    public static final String ENVHUMD="humidity";
    public static final String STEPS="steps";

    public static final String MIN_COL="min";
    public static final String AVG_COL="avg";
    public static final String MAX_COL="max";

    public static final String LONG_COL="longitude";
    public static final String LAT_COL="latitude";
    public static final String ALT_COL="altitude";

    public static final String STEP_COUNT="count";

    public static final String SCHEME_CREATE_HIKES_TABLE="CREATE TABLE "+HIKE+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+HIKE_START+" INTEGER NOT NULL, "+HIKE_END+" INTEGER NOT NULL);";
    public static final String SCHEME_CREATE_TEMPERATURE_TABLE="CREATE TABLE "+ENVTEMP+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+MIN_COL+" REAL NOT NULL, "+AVG_COL+" REAL NOT NULL, "+MAX_COL+" REAL NOT NULL);";
    public static final String SCHEME_CREATE_HUMIDITY_TABLE="CREATE TABLE "+ENVHUMD+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+MIN_COL+" REAL NOT NULL, "+AVG_COL+" REAL NOT NULL, "+MAX_COL+" REAL NOT NULL); ";
    public static final String SCHEME_CREATE_PRESSURE_TABLE="CREATE TABLE "+ENVPRES+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+MIN_COL+" REAL NOT NULL, "+AVG_COL+" REAL NOT NULL, "+MAX_COL+" REAL NOT NULL); ";
    public static final String SCHEME_CREATE_COORDS_TABLE="CREATE TABLE "+COORDS+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+LONG_COL+" REAL NOT NULL, "+LAT_COL+" REAL NOT NULL, "+ALT_COL+" REAL); ";
    public static final String SCHEME_CREATE_STEPS_TABLE="CREATE TABLE "+STEPS+" (id INTEGER PRIMARY KEY AUTOINCREMENT, "+HIKE_ID+" INTEGER REFERENCES "+HIKE+" (id) NOT NULL, "+STEP_COUNT+" INTEGER)";

    public static final String[] SCHEME_CREATE = {
            SCHEME_CREATE_HIKES_TABLE,
            SCHEME_CREATE_COORDS_TABLE,
            SCHEME_CREATE_TEMPERATURE_TABLE,
            SCHEME_CREATE_HUMIDITY_TABLE,
            SCHEME_CREATE_PRESSURE_TABLE,
            SCHEME_CREATE_STEPS_TABLE
    };

    public static final String SCHEME_DESTROY="DROP TABLE IF EXISTS ";

    public static final int SCHEME_VERSION=2; //Update scheme version to add stepCount

    public static final String TAG ="HikeDBA";


    /**
     * Default constructor
     */
    public DBAssistant(Context currentContext) {
        super(currentContext, DB_NAME, null, SCHEME_VERSION);
    }

    /**
     * Method called when creating the database scheme if inexistent
     * @param sqlDB Android's Database Handle
     */
    public void onCreate(SQLiteDatabase sqlDB) {
        Log.d(TAG,"Creating DB for First Time: "+DB_NAME+SCHEME_VERSION);
        for (int i = 0; i < SCHEME_CREATE.length; i++) {
            sqlDB.execSQL(SCHEME_CREATE[i]);
        }
    }

    /**
     * Method called when upgrading scheme version
     * @param sqlDB Android's Database Handle
     * @param prev old version of database to replace
     * @param curr current schema version, defined by SCHEME_VERSION.
     */
    public void onUpgrade(SQLiteDatabase sqlDB, int prev, int curr) {
        Log.w(TAG, String.format("Updating DB from version %s to %s. Will destroy previous", prev, curr));
        sqlDB.execSQL(SCHEME_DESTROY+COORDS);
        sqlDB.execSQL(SCHEME_DESTROY+ENVPRES);
        sqlDB.execSQL(SCHEME_DESTROY+ENVTEMP);
        sqlDB.execSQL(SCHEME_DESTROY+ENVHUMD);
        sqlDB.execSQL(SCHEME_DESTROY + STEPS);
        sqlDB.execSQL(SCHEME_DESTROY + HIKE);
        onCreate(sqlDB);
    }

}