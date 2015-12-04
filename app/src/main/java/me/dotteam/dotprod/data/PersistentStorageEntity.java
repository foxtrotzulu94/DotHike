package me.dotteam.dotprod.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to handle Persistent Storage of Data structures defined in the application
 */
public class PersistentStorageEntity {

    public class CacheMap<K,V> extends LinkedHashMap<K,V>{
        private int cacheMapSize=3;
        public CacheMap(int size){
            cacheMapSize = size;
        }

        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return (super.size() <= cacheMapSize);
        }
    }

    private final String TAG="PSE";

    private Context mCreateContext;
    private DBAssistant mProvider;
    private SQLiteDatabase mDB;

    private LinkedHashMap<Integer,SessionData> cachedObjects;
    private int cacheCapacity=5; //Quite a lot

    private Map<Integer,LocationPoints> mLoadedLocationPoints;
    private boolean mMapsLoaded=false;

    /**
     * Default constructor
     */
    public PersistentStorageEntity(Context currentContext) {
        mCreateContext = currentContext;
        mProvider = new DBAssistant(mCreateContext);
        mDB=mProvider.getWritableDatabase();
        cachedObjects = new CacheMap<>(cacheCapacity);
        Thread offload = new Thread(){
            @Override
            public void run(){
                mLoadedLocationPoints = loadMaps();
            }
        };
//        offload.start();

    }

    /**
     * Method to retrieved the saved {@link Hike} objects. This should be called first before requesting to
     * load any other object from persistent storage in order to guarantee a non-null instance is returned
     * @return List of stored Hike Objects
     */
    public List<Hike> getHikesList() {

        //Create a New Hikes List.
        List <Hike> allHikes = null;

        //Query and keep an index on the columns
        Cursor cursor = mDB.query(DBAssistant.HIKE, null, null, null, null, null, "id");
        int idColumn = cursor.getColumnIndex("id");
        int startTimeColumn = cursor.getColumnIndex(DBAssistant.HIKE_START);
        int endTimeColumn = cursor.getColumnIndex(DBAssistant.HIKE_END);

        if(cursor.getCount()>0) {
            allHikes = new ArrayList<>(cursor.getCount());

            cursor.moveToFirst();
            do {
                allHikes.add(
                        new Hike(
                                cursor.getInt(idColumn),
                                cursor.getLong(startTimeColumn),
                                cursor.getLong(endTimeColumn))
                );
            } while (cursor.moveToNext());
        }

        cursor.close();

        return allHikes;
    }

    public Map<Integer,LocationPoints> loadMaps(){
//        if(mMapsLoaded){
//            return mLoadedLocationPoints;
//        }

        Cursor cursor = mDB.query(DBAssistant.COORDS, null, null, null, DBAssistant.HIKE_ID, null, null);
        Map<Integer,LocationPoints> retVal = null;

        if(cursor.getCount()>0){
            retVal = new HashMap<>(cursor.getCount());
            Log.e(TAG, "loadMaps LOADING ALL COORDS "+cursor.getCount());
            int altColumn = cursor.getColumnIndex(DBAssistant.ALT_COL);
            int latColumn = cursor.getColumnIndex(DBAssistant.LAT_COL);
            int longColumn = cursor.getColumnIndex(DBAssistant.LONG_COL);
            int hikeColumn = cursor.getColumnIndex(DBAssistant.HIKE_ID);


            cursor.moveToFirst();
            do{

                //Get the Hike ID
                int setHikeID = cursor.getInt(hikeColumn);

                Log.d(TAG, "loadMaps Hike number "+setHikeID);

                List<Coordinates> setList = new ArrayList<>();
                while(!cursor.isAfterLast() && setHikeID == cursor.getInt(hikeColumn)){
                    setList.add(new Coordinates(
                            cursor.getDouble(longColumn),
                            cursor.getDouble(latColumn),
                            cursor.getDouble(altColumn)
                    ));
                    cursor.moveToNext();
                }

                //If we break out of the loop, it means we need to save
                retVal.put(setHikeID, new LocationPoints(setList));

            }while(!cursor.isAfterLast());
        }

        cursor.close();
        mMapsLoaded = true;
        mLoadedLocationPoints = retVal;
        return retVal;
    }

    /**
     * Method to retrieve the entire {@link SessionData} object representation associated to a Hike object
     * @param specificHike the HikeObject that defines the SessionData
     * @return a SessionData object with the indicators that it was in the DB
     */
    public SessionData loadHikeData(Hike specificHike) {
        int hikeID = specificHike.getUniqueID();
        if(hikeID<1){
            return null;
        }

        //Check if Cached
        if(cachedObjects.containsKey(hikeID)){
            return cachedObjects.get(hikeID);
        }

        //Already got hike, so...
        //Get EnvData
        EnvData retrievedStatistics = new EnvData(
                retrieveEnvDataTable(DBAssistant.ENVTEMP,hikeID),
                retrieveEnvDataTable(DBAssistant.ENVHUMD,hikeID),
                retrieveEnvDataTable(DBAssistant.ENVPRES,hikeID));

        //Get GeoPoints
        LocationPoints retrievedPoints = null;
//        if(mMapsLoaded){
//            retrievedPoints = mLoadedLocationPoints.get(hikeID);
//        }
//        else {
            retrievedPoints = new LocationPoints(retrieveCoordinates(hikeID));
//        }
        //And finally, get steps
        StepCount retrievedStepCount = retrieveSteps(hikeID);

        if(retrievedPoints!=null && retrievedStatistics!=null){
            return new SessionData(specificHike,retrievedStepCount,retrievedStatistics,retrievedPoints);
        }
        else{
            Log.d(TAG,"DID NOT FIND IN DB");
            return null;
        }
    }

    /**
     * Method to retrieve the entire {@link SessionData} object from an arbitrary hikeID
     * The hikeID must be linked to an actual DB entry or it will return null
     * @param hikeID The hike_id value used in the database
     * @return a SessionData object with the indicators that it was in the DB, null otherwise.
     */
    public SessionData loadHikeData(int hikeID) {
        //Check if Valid
        if(hikeID<1){
            return null;
        }

        //Check if Cached
        if(cachedObjects.containsKey(hikeID)){
            return cachedObjects.get(hikeID);
        }

        //First, load the Hike
        Cursor cursor = mDB.query(DBAssistant.HIKE,null,"id=?",
                new String[]{Integer.toString(hikeID)},null,null,null);
        if(cursor.getCount()<1){
            return null;
        }
        cursor.moveToFirst();
        Hike loadedHike = new Hike(hikeID,
                cursor.getLong(cursor.getColumnIndex(DBAssistant.HIKE_START)),
                cursor.getLong(cursor.getColumnIndex(DBAssistant.HIKE_END)));
        cursor.close();

        //Then delegate the rest...
        return loadHikeData(loadedHike);
    }

    private EnvStatistic retrieveEnvDataTable(String tableName, int uniqueID){
        EnvStatistic retrievedValue = new EnvStatistic();
        //Get the data
        Cursor cursor = mDB.query(tableName,null,DBAssistant.HIKE_ID+"=?",
                new String[]{Integer.toString(uniqueID)}, null, null, null);
        //Check at least an element was returned
        if(cursor.getCount()<1){
            return null;
        }
        cursor.moveToFirst();
        //Now, Put in order:
        //First put MAX
        retrievedValue.insertSample(cursor.getDouble(cursor.getColumnIndex(DBAssistant.MAX_COL)));
        //Then put MIN
        retrievedValue.insertSample(cursor.getDouble(cursor.getColumnIndex(DBAssistant.MIN_COL)));
        //Then insert Avg (AKA the last recorded value)
        retrievedValue.insertSample(cursor.getDouble(cursor.getColumnIndex(DBAssistant.AVG_COL)));

        cursor.close();

        return retrievedValue;
    }

    private List<Coordinates> retrieveCoordinates(int uniqueID){

        List<Coordinates> retrievedList = null;
        Cursor cursor = mDB.query(DBAssistant.COORDS,null,DBAssistant.HIKE_ID+"=?",
                new String[]{Integer.toString(uniqueID)},null,null,DBAssistant.HIKE_ID);
        if (cursor.getCount() <1){
            return null;
        }
        retrievedList =new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        int latColumn = cursor.getColumnIndex(DBAssistant.LAT_COL);
        int longColumn = cursor.getColumnIndex(DBAssistant.LONG_COL);
        int altColumn = cursor.getColumnIndex(DBAssistant.ALT_COL);

        do {
            retrievedList.add(
                    new Coordinates(cursor.getDouble(longColumn),
                            cursor.getDouble(latColumn),
                            cursor.getDouble(altColumn))
            );
        }while (cursor.moveToNext());

        cursor.close();

        return retrievedList;
    }

    private StepCount retrieveSteps(int uniqueID){
        StepCount retrievedSteps = null;
        Cursor cursor = mDB.query(DBAssistant.STEPS,null,DBAssistant.HIKE_ID+"=?",
                new String[]{Integer.toString(uniqueID)},null,null,DBAssistant.HIKE_ID);
        if (cursor.getCount()>0){
            cursor.moveToFirst();
            retrievedSteps = new StepCount(cursor.getInt(cursor.getColumnIndex(DBAssistant.STEP_COUNT)));
        }
        return retrievedSteps;
    }

    /**
     * Send a {@link SessionData} object to be stored in the database
     * @param givenSession the SessionData object to be stored
     * @return True if successfully stored, false otherwise
     */
    public boolean saveSession(SessionData givenSession) {
        mDB = mProvider.getWritableDatabase();

        if(givenSession==null || givenSession.hikeID()>0){
            Log.e(TAG, "Given a null SessionData. Cannot Proceed to Storage");
            return false;
        }

        //Get the hike as contentValue and insert it into the DB
        mDB.insert(DBAssistant.HIKE,null,givenSession.hikeToStorage());

        mDB = mProvider.getReadableDatabase();

        //After inserting, query the DB to retrieve its assigned hike ID
        Cursor cursor = mDB.query(DBAssistant.HIKE,null,null,
                null,null,null,"id");

        cursor.moveToLast();
        Log.w(TAG,"Statement returned "+cursor.getColumnCount()+" "+cursor.getCount());
        int assignedID = cursor.getInt(cursor.getColumnIndex("id"));

        //Modify the Hike ID of the original object
        givenSession.setHikeID(assignedID);

        //Cache it
        cachedObjects.put(assignedID,givenSession);

        //Insertion done in O(1) time
        //Insert all EnvStatistics
        mDB.insert(DBAssistant.ENVTEMP,null,givenSession.getCurrentStats().getSerializedTemp(assignedID));
        mDB.insert(DBAssistant.ENVHUMD,null,givenSession.getCurrentStats().getSerializedHumidity(assignedID));
        mDB.insert(DBAssistant.ENVPRES,null,givenSession.getCurrentStats().getSerializedPressure(assignedID));
        //Insert Step Count
        mDB.insert(DBAssistant.STEPS,null,givenSession.getStepCount().toStorage(assignedID));
        //If there's a name, save it
        mDB.insert(DBAssistant.HIKE_NAME,null,givenSession.hikeNameToStorage()); //TODO: Change later. All hike data should be inserted in one-shot

        //Insertion takes O(n). This is a HUGE bottleneck actually and we have no way of optimizing it...
        //Continue insertion of objects with the associated ID.
        List<Coordinates> allCoordinates =givenSession.getGeoPoints().getCoordinateList();
        int length = allCoordinates.size();
        for(int i=0; i<allCoordinates.size();++i) {
            mDB.insert(DBAssistant.COORDS, null,allCoordinates.get(i).toStorage(assignedID));
        }

        //save to cache
//        mLoadedLocationPoints.put(assignedID,givenSession.getGeoPoints());

        //CHANGE if operation fails at any point!
        return true;
    }

    /**
     * Deletes all information associated to a SessionData object
     * @param givenSession the SessionData object to be deleted
     * @return True if successfully deleted, false otherwise
     */
    public boolean deleteSession(SessionData givenSession){
        mDB = mProvider.getWritableDatabase();

        //Check if Cached and remove
        if(cachedObjects.containsKey(givenSession.hikeID())){
            cachedObjects.remove(givenSession.hikeID());
        }

        if(givenSession.hikeID()>0){
            //Verify the ID is in the DB
            Cursor cursor = mDB.query(DBAssistant.HIKE,null,"id=?",
                    new String[]{Integer.toString(givenSession.hikeID())},null,null,null);
            if(cursor.getCount()!=1){
                //DB is inconsistent in this case, or the Hike was not saved.
                return false;
            }

            //Now we send this to all tables for deletion
            String[] idParam= new String[]{String.valueOf(givenSession.hikeID())};

            //Delete from all tables
            for (int i = 1; i < DBAssistant.VALID_TABLES.length ; i++) {
                mDB.delete(DBAssistant.VALID_TABLES[i],
                        DBAssistant.HIKE_ID+"=?",
                        idParam);
            }

            //Finally, delete from hikes
            mDB.delete(DBAssistant.HIKE,"id=?",idParam);
            return true;
        }

        return false;
    }

    /**
     * Delete all information associated to a hike
     * This would be similar to calling {@link #deleteSession(SessionData)} deleteSession } with a constructed SessionData object
     * @param givenHike The hike to delete
     * @return True if successfully deleted, false otherwise
     */
    public boolean deleteHike(Hike givenHike){
        mDB = mProvider.getWritableDatabase();

        //Check if Cached and remove
        if(cachedObjects.containsKey(givenHike.getUniqueID())){
            cachedObjects.remove(givenHike.getUniqueID());
        }

        if(givenHike.getUniqueID()>0){
            //Verify the ID is in the DB
            Cursor cursor = mDB.query(DBAssistant.HIKE,null,"id=?",
                    new String[]{Integer.toString(givenHike.getUniqueID())},null,null,null);
            if(cursor.getCount()!=1){
                //DB is inconsistent in this case, or the Hike was not saved.
                return false;
            }

            //Now we send this to all tables for deletion
            String[] idParam= new String[]{String.valueOf(givenHike.getUniqueID())};

            //Delete from all tables
            for (int i = 1; i < DBAssistant.VALID_TABLES.length ; i++) {
                mDB.delete(DBAssistant.VALID_TABLES[i],
                        DBAssistant.HIKE_ID+"=?",
                        idParam);
            }

            //Finally, delete from hikes
            mDB.delete(DBAssistant.HIKE,"id=?",idParam);
            return true;
        }

        return false;
    }

    /**
     * Request a complete deletion of any stored data
     * WARNING: This entirely eliminates and recreates the database. ALL DATA IS LOST.
     */
    public void reset(){
        mProvider.onUpgrade(mDB,-1,DBAssistant.SCHEME_VERSION);
    }

    public boolean hasCoordinatesCached(){
        return mMapsLoaded;
    }

}