package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.LocationPoints;
import me.dotteam.dotprod.data.SessionCollectionService;
import me.dotteam.dotprod.data.SessionData;
import me.dotteam.dotprod.hw.HikeHardwareManager;
import me.dotteam.dotprod.hw.SensorListenerInterface;

/**
 * Created by foxtrot on 22/11/15.
 */
public class HikeDataDirectorTest extends ServiceTestCase<SessionCollectionService> {


    private HikeDataDirector subject;

    private Random valueGenerator;
    private Context testContext;

    private HardwareMocker hardwareUpdates;

    public HikeDataDirectorTest(){
        super(SessionCollectionService.class);
    }

    private class HardwareMocker extends Thread{

        private Context testContext;
        private Random valueGenerator;

        public HardwareMocker(Context someContext, Random prng){
            testContext = someContext;
            valueGenerator = prng;
        }

        boolean canRun=true;
        @Override
        public void run(){
            Method updater=null;
            HikeHardwareManager HHM= HikeHardwareManager.getInstance(testContext);
            try {
                updater = HHM.getClass().getDeclaredMethod("broadcastUpdate", new Class[]{SensorListenerInterface.HikeSensors.class, Double.TYPE});
            }
            catch (Exception e){}
            updater.setAccessible(true);

            while(canRun){
                try {
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.HUMIDITY, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.PRESSURE, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.TEMPERATURE, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.PEDOMETER, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.COMPASS, valueGenerator.nextDouble());
                }
                catch (Exception e){}
            }
        }

        public void stopMock(){
            canRun = false;
        }
    }

    private class LocationMocker extends Thread{

        private Context testContext;
        private Random valueGenerator;

        public LocationMocker(Context someContext, Random prng){
            testContext = someContext;
            valueGenerator = prng;
        }

        boolean canRun=true;
        @Override
        public void run(){
            Method updater=null;
            HikeHardwareManager HHM= HikeHardwareManager.getInstance(testContext);
            try {
                updater = HHM.getClass().getMethod("broadcastUpdate", new Class[]{SensorListenerInterface.HikeSensors.class, float.class});
            }
            catch (Exception e){return;}
            updater.setAccessible(true);

            while(canRun){
                try {
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.HUMIDITY, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.PRESSURE, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.TEMPERATURE, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.PEDOMETER, valueGenerator.nextDouble());
                    updater.invoke(HHM, SensorListenerInterface.HikeSensors.COMPASS, valueGenerator.nextDouble());
                }
                catch (Exception e){}
            }
        }

        public void stopMock(){
            canRun = false;
        }
    }

    protected void setUp() throws Exception{
        super.setUp();

        valueGenerator = new Random();
        testContext = new RenamingDelegatingContext(getContext(),"test_");
//        testContext = getSystemContext();
        subject = HikeDataDirector.getInstance(testContext);
        hardwareUpdates =  new HardwareMocker(testContext,valueGenerator);
        hardwareUpdates.start();
    }

    @SmallTest
    public void testPreconditions() {
    }

    public void testDatabase() throws Exception{
        testPartialCollection();
        SessionData someSession = subject.getSessionData();
        assertTrue(subject.storeCollectedStatistics());

        testPartialCollection();
        assertTrue(subject.storeCollectedStatistics());

        List<Hike> storedHikes = subject.getAllStoredHikes();
        assertNotNull(storedHikes);
        assertTrue(storedHikes.size() > 1);

        assertTrue(subject.retrieveSessionFromHike(new Hike(someSession.hikeID(), someSession.hikeStartTime(), someSession.hikeEndTime())));

        SessionData retrievedSession = subject.getSessionData();
        equalityCheck(retrievedSession,someSession);
    }

    public void testPartialCollection() throws Exception{
        subject.beginCollectionService();
//        startService(new Intent(testContext,SessionCollectionService.class));
        try{
            Thread.sleep(1000);
        }
        catch (Exception e){}

        subject.endCollectionService();
//        shutdownService();
        try{
            Thread.sleep(100);
        }
        catch (Exception e){}

        SessionData someSession = subject.getSessionData();
        assertNotNull(someSession);

        assertNotNull(someSession.getCurrentStats());
        assertNotNull(someSession.getCurrentStats().getHumidity());
        assertNotNull(someSession.getCurrentStats().getPressure());
        assertNotNull(someSession.getCurrentStats().getTemperature());

        assertTrue(someSession.getCurrentStats().getHumidity().isValid());
        assertTrue(someSession.getCurrentStats().getPressure().isValid());
        assertTrue(someSession.getCurrentStats().getTemperature().isValid());

    }

    private void equalityCheck(SessionData retrieved, SessionData expected) throws Exception{
        assertEquals(retrieved.getCurrentStats().getHumidity().getAvg(), expected.getCurrentStats().getHumidity().getAvg());

        assertEquals(retrieved.getCurrentStats().getPressure().getAvg(), expected.getCurrentStats().getPressure().getAvg());

        assertEquals(retrieved.getCurrentStats().getTemperature().getAvg(), expected.getCurrentStats().getTemperature().getAvg());

        assertEquals(retrieved.getStepCount().getStepsTaken(),expected.getStepCount().getStepsTaken());

        assertNull(retrieved.getGeoPoints().getCoordinateList());
        //Can't really test Location yet
//        List<Coordinates> retrievedList = retrieved.getGeoPoints().getCoordinateList();
//        List<Coordinates> expectedList = expected.getGeoPoints().getCoordinateList();
//        assertEquals(retrievedList.size(),expectedList.size());
//        for (int i = 0; i < expectedList.size(); i++) {
//            assertEquals( retrievedList.get(i).toString(),
//                    expectedList.get(i).toString());
//        }
    }

    protected void tearDown(){
        //End the updater
        hardwareUpdates.stopMock();
    }
}
