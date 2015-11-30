package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.DBAssistant;
import me.dotteam.dotprod.data.EnvData;
import me.dotteam.dotprod.data.EnvStatistic;
import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.LocationPoints;
import me.dotteam.dotprod.data.PersistentStorageEntity;
import me.dotteam.dotprod.data.SessionData;
import me.dotteam.dotprod.data.StepCount;

/**
 * Unit test of PersistentStorageEntity class
 * This class is responsible for accessing the DB, so it verifies everything has been written
 * Test:
 * - Creation
 * - Insertion of SessionData
 * - Deletion of SessionData
 * - Reconstruction of SessionData from HikeID
 * - Reconstruction of SessionData from Hike Object
 */
public class PersistentStorageEntityTest extends ApplicationTestCase<Application> {

    public static final int TEST_SIZE=1000;

    private Hike aHike;
    private LocationPoints someCoordinates;
    private StepCount someSteps;
    private EnvStatistic statTemp;
    private EnvStatistic statHumidity;
    private EnvStatistic statPressure;
    private EnvData someData;
    private SessionData someSession;

    private PersistentStorageEntity subject;

    private Random valueGenerator;
    private RenamingDelegatingContext testContext;

    private int id = -1;
    private int startTime = 0;
    private int endTime = 50;

    public PersistentStorageEntityTest(){
        super(Application.class);
    }

    protected void setUp() throws Exception{
        super.setUp();

        //Create the Value generator
        valueGenerator = new Random();
        testContext = new RenamingDelegatingContext(getContext(),"test_");

        aHike = new Hike(id,startTime,endTime);
        statHumidity = new EnvStatistic();
        statPressure = new EnvStatistic();
        statTemp = new EnvStatistic();
        someCoordinates = new LocationPoints();
        for (int i = 0; i < TEST_SIZE; i++) {
            someCoordinates.addPoint(new Coordinates(
                    valueGenerator.nextDouble(),
                    valueGenerator.nextDouble(),
                    valueGenerator.nextDouble()));
        }
        someSteps = new StepCount(TEST_SIZE);

        statHumidity.insertSample(valueGenerator.nextDouble());
        statPressure.insertSample(valueGenerator.nextDouble());
        statTemp.insertSample(valueGenerator.nextDouble());

        someData = new EnvData(statTemp,statHumidity,statPressure);

        //Complete Construction
        someSession = new SessionData(
                aHike,
                someSteps,
                someData,
                someCoordinates);

        //Now for the test subject
        subject = new PersistentStorageEntity(testContext);
    }

    public void testEmptyHikeListRetrieval() throws Exception{
        List<Hike> emptyHikes = subject.getHikesList();
        assertNull(emptyHikes);
    }

    public void testHikeListRetrieval() throws Exception{
        testSessionSave();
        List<Hike> hikeList = subject.getHikesList();
        assertNotNull(hikeList);
        assertEquals(hikeList.size(), 1);
    }

    public void testSessionSave() throws Exception{
        assertTrue(subject.saveSession(someSession));
        id = someSession.hikeID();
        //The only way we can check if it was correctly written is if we retrieve it ourselves
        SQLiteDatabase testDB = new DBAssistant(testContext).getReadableDatabase();
        //We'll need to check each table here
        Cursor cursor = null;
        //Start with hikes
        cursor = testDB.query(DBAssistant.HIKE,null,null,null,null,null,null);
        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == 1);
        assertEquals(cursor.getInt(cursor.getColumnIndex("id")), id);
        cursor.close();

        //Now Check location Points
        cursor = testDB.query(DBAssistant.COORDS,null,null,null,null,null,null);
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getCount(), TEST_SIZE);
        do {
            assertEquals(cursor.getInt(cursor.getColumnIndex(DBAssistant.HIKE_ID)), id);
        }while (cursor.moveToNext());
        cursor.close();

        //Now Check Stepcount
        cursor = testDB.query(DBAssistant.STEPS,null,null,null,null,null,null);
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getInt(cursor.getColumnIndex(DBAssistant.STEP_COUNT)), TEST_SIZE);
        cursor.close();

        //Finally, just do a quick verification of the EnvStatistics
        cursor = testDB.query(DBAssistant.ENVHUMD,null,null,null,null,null,null);
        assertEquals(cursor.getCount(), 1);
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getInt(cursor.getColumnIndex(DBAssistant.HIKE_ID)), id);
        cursor.close();
        cursor = testDB.query(DBAssistant.ENVPRES,null,null,null,null,null,null);
        assertEquals(cursor.getCount(),1);
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getInt(cursor.getColumnIndex(DBAssistant.HIKE_ID)),id);
        cursor.close();
        cursor = testDB.query(DBAssistant.ENVPRES,null,null,null,null,null,null);
        assertEquals(cursor.getCount(),1);
        assertTrue(cursor.moveToFirst());
        assertEquals(cursor.getInt(cursor.getColumnIndex(DBAssistant.HIKE_ID)),id);
        cursor.close();
    }

    public void testSessionLoadById() throws Exception{
        testSessionSave();
        SessionData retrieved = subject.loadHikeData(id);
        equalityCheck(retrieved);
    }

    public void testSessionLoadByObject() throws Exception{
        testSessionSave();
        SessionData retrieved = subject.loadHikeData(aHike);
        equalityCheck(retrieved);
    }

    public void testSessionDelete() throws Exception{
        testSessionSave();
        subject.deleteSession(someSession);
        testEmptyHikeListRetrieval();
    }

    private void equalityCheck(SessionData retrieved) throws Exception{
        assertEquals(retrieved.getCurrentStats().getHumidity().getAvg(),statHumidity.getAvg());

        assertEquals(retrieved.getCurrentStats().getPressure().getAvg(),statPressure.getAvg());

        assertEquals(retrieved.getCurrentStats().getTemperature().getAvg(),statTemp.getAvg());

        assertEquals(retrieved.getStepCount().getStepsTaken(),someSteps.getStepsTaken());

        for (int i = 0; i < TEST_SIZE; i++) {
            assertEquals( retrieved.getGeoPoints().getCoordinateList().get(i).toString(),
                    someCoordinates.getCoordinateList().get(i).toString());
        }
    }


}
