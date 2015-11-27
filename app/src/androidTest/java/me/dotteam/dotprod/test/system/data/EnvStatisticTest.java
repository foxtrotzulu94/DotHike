package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.content.ContentValues;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.util.Random;

import me.dotteam.dotprod.data.DBAssistant;
import me.dotteam.dotprod.data.EnvStatistic;

/**
 * Unit Test of EnvStatistic Class
 * Tests:
 * - Creation
 * - Validity
 * - Insertion of Random Samples
 * - Serialization for DB Storage
 */
public class EnvStatisticTest extends ApplicationTestCase<Application> {

    public static final int TEST_SIZE=1000;

    private Random valueGenerator;
    private EnvStatistic subject;

    private float randomMax;
    private float randomAvg;
    private float randomMin;
    private int id = Integer.MAX_VALUE;

    public EnvStatisticTest(){
        super(Application.class);
    }

    protected void setUp() throws Exception{
        super.setUp();

        //create the value generator and the test subject
        valueGenerator = new Random();
        subject = new EnvStatistic();

        randomMax = valueGenerator.nextFloat() * Integer.MAX_VALUE;
        randomAvg = valueGenerator.nextFloat();
        randomMin = valueGenerator.nextFloat() * Integer.MIN_VALUE;
    }

    public void testInsertion() throws Exception{
        subject.insertSample(randomAvg);
        subject.insertSample(randomMax);
        subject.insertSample(randomMin);

        assertEquals(subject.getAvg(),randomAvg);
        assertEquals(subject.getMin(),randomMin);
        assertEquals(subject.getMax(),randomMax);
    }

    public void testRandomInsertion() throws Exception{
        testInsertion();
        //Insert really random values that are >1 and <+-IntegerMaxVal
        for (int i = 0; i < TEST_SIZE; i++) {
            subject.insertSample(valueGenerator.nextDouble() * Integer.MAX_VALUE * Math.pow(-1,i)*0.5);
        }
        testInsertion();
    }

    public void testInitialization() throws Exception{
        assertEquals(Float.NaN,subject.getAvg());
        assertEquals(Float.POSITIVE_INFINITY,subject.getMin());
        assertEquals(0,subject.getMax());
    }

    public void testValidity() throws Exception{
        //This should be false since insertion is not yet done
        assertFalse(subject.isValid());

        //Carry out the Test Insertion
        testInsertion();

        //Should be good now
        assertTrue(subject.isValid());
    }

    public void testSerialization() throws Exception{
        testInsertion();
        ContentValues serialized = subject.toStorage(id);
        assertTrue(serialized.containsKey(DBAssistant.HIKE_ID));
        assertTrue(serialized.containsKey(DBAssistant.AVG_COL));
        assertTrue(serialized.containsKey(DBAssistant.MAX_COL));
        assertTrue(serialized.containsKey(DBAssistant.MIN_COL));

        Assert.assertEquals((int) serialized.get(DBAssistant.HIKE_ID), id);
        Assert.assertEquals((double)serialized.get(DBAssistant.AVG_COL),randomAvg);
        Assert.assertEquals((double)serialized.get(DBAssistant.MAX_COL),randomMax);
        Assert.assertEquals((double)serialized.get(DBAssistant.MIN_COL),randomMin);
    }

}
