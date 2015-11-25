package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.content.ContentValues;
import android.content.pm.PackageInfo;
import android.test.ApplicationTestCase;
import android.test.MoreAsserts;

import junit.framework.Assert;

import java.util.Random;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.DBAssistant;

/**
 * Created by foxtrot on 22/11/15.
 */
public class CoordinatesTest extends ApplicationTestCase<Application> {

    private Coordinates subject;

    private double randomAltitude,randomLatitude,randomLongitude;
    private int id = Integer.MAX_VALUE;
    private Random valueGenerator;

    public CoordinatesTest(){
        super(Application.class);
    }
    protected void setUp() throws Exception {
        super.setUp();
        //Create Random Values
        valueGenerator = new Random();
        randomAltitude = valueGenerator.nextDouble();
        randomLatitude = valueGenerator.nextDouble();
        randomLongitude = valueGenerator.nextDouble();

        //Create a new object
        subject = new Coordinates(randomLongitude,randomLatitude,randomAltitude);

    }

    public void testInsertion() throws Exception {
        //Check if all values are equal
        Assert.assertEquals(subject.getAltitude(),randomAltitude);
        Assert.assertEquals(subject.getLatitude(),randomLatitude);
        Assert.assertEquals(subject.getLongitude(),randomLongitude);
    }

    public void testSerialization() throws Exception{
        ContentValues serialized = subject.toStorage(id);
        Assert.assertTrue(serialized.containsKey(DBAssistant.HIKE_ID));
        Assert.assertTrue(serialized.containsKey(DBAssistant.LAT_COL));
        Assert.assertTrue(serialized.containsKey(DBAssistant.LONG_COL));
        Assert.assertTrue(serialized.containsKey(DBAssistant.ALT_COL));

        Assert.assertEquals((int) serialized.get(DBAssistant.HIKE_ID), id);
        Assert.assertEquals((double)serialized.get(DBAssistant.LONG_COL),randomLongitude);
        Assert.assertEquals((double)serialized.get(DBAssistant.LAT_COL),randomLatitude);
        Assert.assertEquals((double)serialized.get(DBAssistant.ALT_COL),randomAltitude);
    }

    public void testToString() throws Exception{
        String expected = String.format("Lat %.3f Long %.3f Alt %.3f",
                randomLatitude,randomLongitude,randomAltitude);
        Assert.assertEquals(subject.toString(),expected);
    }

}
