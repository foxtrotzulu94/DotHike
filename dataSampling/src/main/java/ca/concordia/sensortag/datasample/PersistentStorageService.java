package ca.concordia.sensortag.datasample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

/**
 * Created by j_fajard on 10/27/2015.
 */
public class PersistentStorageService {

    private Context creationContext;
    protected DBFacilitator serviceProvider;
    protected SQLiteDatabase database;

    public PersistentStorageService(Context context){
        creationContext = context;
        serviceProvider = new DBFacilitator(creationContext);
        Log.d("PSS","Initializing PersistentStorageService using SQLite3");
    }

    public void insertRecordedValues(List<Long> recordedValues){

    }

    public List<Long> readRecordedValues(){
        return null;
    }

}
