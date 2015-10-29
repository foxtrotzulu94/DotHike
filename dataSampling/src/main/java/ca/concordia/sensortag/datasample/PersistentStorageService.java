package ca.concordia.sensortag.datasample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by j_fajard on 10/27/2015.
 */
public class PersistentStorageService {

    private final String TAG = "PSS";

    private Context creationContext;
    protected DBFacilitator serviceProvider;
    protected SQLiteDatabase database;

    public static final String DB_TABLE1_NAME="sample_set";
    public static final String TABLE1_COLUMN_NAME="timestamp";
    public static final String DB_TABLE2_NAME="parameters";
    public static final String TABLE2_COLUMN1_NAME="status";
    public static final String TABLE2_COLUMN2_NAME="rec_duration";
    public static final String TABLE2_COLUMN3_NAME="rec_samples";
    public static final String TABLE2_COLUMN4_NAME="time_elapsed";

    public PersistentStorageService(Context context){
        creationContext = context;
        serviceProvider = new DBFacilitator(creationContext);
        Log.d(TAG, "Initializing PersistentStorageService using SQLite3");
        database = serviceProvider.getWritableDatabase();
        Log.d(TAG, database.toString());
    }

    public void insertRecordedValues(DBData data){
        // Delete old data
        // TODO: may consider replacing rather than deleting
        String query = "DELETE FROM " + DB_TABLE2_NAME;
        database.execSQL(query);

        query = "DELETE FROM " + DB_TABLE1_NAME;
        database.execSQL(query);

        // Save new data
        List<Long> recordedValues = data.getEventTimestamps();
        for (int i = 0; i< recordedValues.size(); i++) {
            ContentValues timestamp_value = new ContentValues();
            timestamp_value.put(TABLE1_COLUMN_NAME, recordedValues.get(i));
            database.insert(DB_TABLE1_NAME, null, timestamp_value);
        }

        ContentValues parameterValues = new ContentValues();
        parameterValues.put(TABLE2_COLUMN1_NAME, data.getStatus().toString());
        parameterValues.put(TABLE2_COLUMN2_NAME, data.getRecDuration());
        parameterValues.put(TABLE2_COLUMN3_NAME, data.getRecSamples());
        parameterValues.put(TABLE2_COLUMN4_NAME, data.getElapsed());

        database.insert(DB_TABLE2_NAME, null, parameterValues);
    }

    public DBData readRecordedValues(){
        // Check if there are any parameters
        String query = "SELECT COUNT(*) FROM " + DB_TABLE1_NAME;
        Log.d(TAG, query);
        Cursor c = database.rawQuery(query, null);

        if (c != null) {
            c.moveToFirst();
        } else {
            // Throw exception
            return null;
        }

        int numParameters = c.getInt(0);

        Log.d(TAG, String.valueOf(numParameters));

        if (numParameters == 0) {
            return null;
        }

        // Get parameters
        query = "SELECT * FROM " + DB_TABLE2_NAME;

        Log.d(TAG, query);

        c = database.rawQuery(query, null);

        if (c != null) {
            c.moveToFirst();
        } else {
            // Throw exception
            return null;
        }

        DBData outData = new DBData();

        outData.setStatus(RecordingData.Status.valueOf(c.getString(c.getColumnIndex(TABLE2_COLUMN1_NAME))));
        outData.setRecDuration(c.getLong(c.getColumnIndex(TABLE2_COLUMN2_NAME)));
        outData.setRecSamples(c.getInt(c.getColumnIndex(TABLE2_COLUMN3_NAME)));
        outData.setElapsed(c.getLong(c.getColumnIndex(TABLE2_COLUMN4_NAME)));

        // Get number of recorded values
        query = "SELECT COUNT(*) FROM " + DB_TABLE1_NAME;
        Log.d(TAG, query);
        c = database.rawQuery(query, null);

        if (c != null) {
            c.moveToFirst();
        } else {
            // Throw exception
            return null;
        }

        int numValues = c.getInt(0);

        // Get recorded values
        query = "SELECT * FROM " + DB_TABLE1_NAME;
        Log.d(TAG, query);
        c = database.rawQuery(query, null);

        if (c != null) {
            c.moveToFirst();
        } else {
            // Throw exception
            return null;
        }

        List<Long> recordedValues = new ArrayList<Long>();

        for (int i = 0; i < numValues; i++) {
            recordedValues.add(c.getLong(c.getColumnIndex(TABLE1_COLUMN_NAME)));
            c.moveToNext();
        }

        outData.setEventTimestamps(recordedValues);

        return outData;

    }

}
