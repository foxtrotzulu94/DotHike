package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.content.ContentValues;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.DBAssistant;
import me.dotteam.dotprod.data.EnvData;
import me.dotteam.dotprod.data.EnvStatistic;
import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.LocationPoints;
import me.dotteam.dotprod.data.SessionData;
import me.dotteam.dotprod.data.StepCount;

/**
 * Unit Test for SessionData Class
 * Tests:
 * - Successful Creation
 * - Statistical Data Consistency
 * - Step Count Consistency
 * - Location Data Consistency
 * - Serialization of Hike Data
 */
public class SessionDataTest extends ApplicationTestCase<Application> {

    public static final int TEST_SIZE=1000;

    private Hike aHike;
    private LocationPoints someLocation;
    private StepCount someSteps;
    private EnvStatistic statTemp;
    private EnvStatistic statHumidity;
    private EnvStatistic statPressure;
    private EnvData someData;

    private SessionData subject;

    private Random valueGenerator;

    private int id = Integer.MAX_VALUE;
    private int startTime = 0;
    private int endTime = 50;
    private List<Coordinates> someCoordinates;
    private double temperature;
    private double humidity;
    private double pressure;


    public SessionDataTest(){
        super(Application.class);
    }

    protected void setUp() throws Exception{
        super.setUp();

        //Create Random Value and all supporting members
        valueGenerator = new Random();

        temperature = valueGenerator.nextDouble();
        humidity = valueGenerator.nextDouble();
        pressure = valueGenerator.nextDouble();

        aHike = new Hike(id,startTime,endTime);
        statHumidity = new EnvStatistic();
        statPressure = new EnvStatistic();
        statTemp = new EnvStatistic();
        someCoordinates = new ArrayList<>(TEST_SIZE);
        for (int i = 0; i < TEST_SIZE; i++) {
            someCoordinates.add(new Coordinates(
                    valueGenerator.nextDouble(),
                    valueGenerator.nextDouble(),
                    valueGenerator.nextDouble()));
        }
        someLocation = new LocationPoints(someCoordinates);
        someSteps = new StepCount(TEST_SIZE);

        statHumidity.insertSample(humidity);
        statPressure.insertSample(pressure);
        statTemp.insertSample(temperature);

        someData = new EnvData(statTemp,statHumidity,statPressure);

        //Complete Construction
        subject = new SessionData(
                aHike,
                someSteps,
                someData,
                someLocation);
    }

    public void testGetStats() throws Exception{
        //Unit tests for stats are elsewhere, so we'll just check the Average is the same
        assertEquals(subject.getCurrentStats(),someData);

        assertEquals(subject.getCurrentStats().getHumidity(),statHumidity);
        assertEquals(subject.getCurrentStats().getHumidity().getAvg(),statHumidity.getAvg());

        assertEquals(subject.getCurrentStats().getPressure(),statPressure);
        assertEquals(subject.getCurrentStats().getPressure().getAvg(),statPressure.getAvg());

        assertEquals(subject.getCurrentStats().getTemperature(),statTemp);
        assertEquals(subject.getCurrentStats().getTemperature().getAvg(),statTemp.getAvg());
    }

    public void testGetSteps() throws Exception{
        assertEquals(subject.getStepCount(),someSteps);
        assertEquals(subject.getStepCount().getStepsTaken(),someSteps.getStepsTaken());
    }

    public void testGetLocationPoints() throws Exception{
        assertEquals(subject.getGeoPoints(),someLocation);
        assertEquals(subject.getGeoPoints().getCoordinateList(),someLocation.getCoordinateList());

        for (int i = 0; i < TEST_SIZE; i++) {
            assertEquals( subject.getGeoPoints().getCoordinateList().get(i),someCoordinates.get(i));
        }
    }

    public void testHikeSerialization() throws Exception{
        assertEquals(subject.hikeToStorage(),aHike.toStorage());
    }

    public void testHikeNameSerialization() throws Exception{
        assertEquals(subject.hikeNameToStorage(),aHike.nameToStorage());
    }
}
