package me.dotteam.dotprod.data;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class PersistentStorageEntity {

    private Context mCreateContext;

    /**
     * 
     */
    private DBAssistant mProvider;

    private SQLiteDatabase mDB;

    private int hikesInDatabase;

    /**
     * Default constructor
     */
    public PersistentStorageEntity(Context currentContext) {
        mCreateContext = currentContext;
        mProvider = new DBAssistant(mCreateContext);
        mDB=mProvider.getWritableDatabase();
    }

    /**
     * @return
     */
    public List<Hike> getHikesList() {
        List<Hike> allHikes = new ArrayList<>();

        //Query and keep an index on the columns
        Cursor cursor = mDB.query(DBAssistant.HIKE, null, null, null, null, null,"id");
        int idColumn = cursor.getColumnIndex("id");
        int startTimeColumn = cursor.getColumnIndex(DBAssistant.HIKE_START);
        int endTimeColumn = cursor.getColumnIndex(DBAssistant.HIKE_END);

        cursor.moveToFirst();
        do {
            allHikes.add(
                    new Hike(
                    cursor.getInt(idColumn),
                    cursor.getLong(startTimeColumn),
                    cursor.getLong(endTimeColumn))
            );
        }while (cursor.moveToNext());

        cursor.close();

        return allHikes;
    }

    /**
     * @param specificHike 
     * @return
     */
    public SessionData loadHikeData(Hike specificHike) {
        // TODO implement here
        return null;
    }

    /**
     * @param hikeID 
     * @return
     */
    public SessionData loadHikeData(int hikeID) {
        // TODO implement here
        return null;
    }

    /**
     * @param givenSession 
     * @return
     */
    public boolean saveSession(SessionData givenSession) {
        mDB = mProvider.getWritableDatabase();

        //Get the hike as contentValue and insert it into the DB
        mDB.insert(DBAssistant.HIKE,null,givenSession.hikeToStorage());

        mDB = mProvider.getReadableDatabase();

        //After inserting, query the DB to retrieve its assigned hike ID
        Cursor cursor = mDB.query(DBAssistant.HIKE,null,null,
                null,null,null,"id");

        cursor.moveToFirst();
        Log.w("","Statement returned "+cursor.getColumnCount()+" "+cursor.getCount());
        int assignedID = cursor.getInt(cursor.getColumnIndex("id"));


        //Continue insertion of objects with the associated ID.
        List<Coordinates> allCoordinates =givenSession.getGeoPoints().getCoordinateList();
        for(int i=0; i<allCoordinates.size();++i) {
            mDB.insert(DBAssistant.COORDS, null,allCoordinates.get(i).toStorage(assignedID));
        }
        mDB.insert(DBAssistant.ENVTEMP,null,givenSession.getCurrentStats().getSerializedTemp(assignedID));
        mDB.insert(DBAssistant.ENVHUMD,null,givenSession.getCurrentStats().getSerializedHumidity(assignedID));
        mDB.insert(DBAssistant.ENVPRES,null,givenSession.getCurrentStats().getSerializedPressure(assignedID));

        //CHANGE if operation fails at any point!
        return true;
    }

    public void reset(){
        mProvider.onUpgrade(mDB,-1,1);
    }

}