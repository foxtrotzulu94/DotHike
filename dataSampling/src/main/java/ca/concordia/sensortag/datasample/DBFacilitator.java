package ca.concordia.sensortag.datasample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by j_fajard on 10/27/2015.
 */
public class DBFacilitator extends SQLiteOpenHelper {

    public static final String DB_NAME="DataSamples";
    public static final String DB_TABLE_NAME="sample_set";
    public static final String SQL_CREATION="";
    public static final String SQL_DESTROY="DROP TABLE IF EXIST"+DB_TABLE_NAME;
    public static final int DB_VERSION = 1;

    protected Context callerContext;


    public DBFacilitator(Context currentContext){
        super(currentContext, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create the SQL DB with the pre-written statements
        sqLiteDatabase.execSQL(SQL_CREATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int prev, int current) {
        Log.w("DBFacilitator",String.format("Updating DB from version %s to %s. Will destroy previous",prev,current));
        sqLiteDatabase.execSQL(SQL_DESTROY);
        onCreate(sqLiteDatabase);
    }
}
