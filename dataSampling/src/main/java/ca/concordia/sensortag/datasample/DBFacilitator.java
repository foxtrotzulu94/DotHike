package ca.concordia.sensortag.datasample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by j_fajard on 10/27/2015.
 */
public class DBFacilitator extends SQLiteOpenHelper {

    private final String TAG = "Facilitator";

    public static final String DB_NAME="DataSamples";
    public static final String DB_TABLE1_NAME="sample_set";
    public static final String TABLE1_COLUMN_ID="_id";
    public static final String TABLE1_COLUMN_NAME="timestamp";
    public static final String DB_TABLE2_NAME="parameters";
    public static final String TABLE2_COLUMN1_NAME="status";
    public static final String TABLE2_COLUMN2_NAME="rec_duration";
    public static final String TABLE2_COLUMN3_NAME="rec_samples";
    public static final String TABLE2_COLUMN4_NAME="time_elapsed";
    public static final String SQL_CREATION_TABLE1="CREATE TABLE "
            + DB_TABLE1_NAME + "(" + TABLE1_COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TABLE1_COLUMN_NAME + " REAL);";
    public static final String SQL_CREATION_TABLE2 = " CREATE TABLE " + DB_TABLE2_NAME + "("
            + TABLE2_COLUMN1_NAME + " TEXT, " + TABLE2_COLUMN2_NAME
            + " REAL, " + TABLE2_COLUMN3_NAME + " INT, "
            + TABLE2_COLUMN4_NAME + " REAL);";
    public static final String SQL_DESTROY="DROP TABLE IF EXIST"+ DB_TABLE1_NAME + ", " + DB_TABLE2_NAME;
    public static final int DB_VERSION = 1;

    protected Context callerContext;


    public DBFacilitator(Context currentContext) {
        super(currentContext, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create the SQL DB with the pre-written statements
        Log.d(TAG, SQL_CREATION_TABLE1);
        sqLiteDatabase.execSQL(SQL_CREATION_TABLE1);

        Log.d(TAG, SQL_CREATION_TABLE2);
        sqLiteDatabase.execSQL(SQL_CREATION_TABLE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int prev, int current) {
        Log.w("DBFacilitator",String.format("Updating DB from version %s to %s. Will destroy previous",prev,current));
        Log.d(TAG, SQL_DESTROY);
        sqLiteDatabase.execSQL(SQL_DESTROY);
        onCreate(sqLiteDatabase);
    }
}
