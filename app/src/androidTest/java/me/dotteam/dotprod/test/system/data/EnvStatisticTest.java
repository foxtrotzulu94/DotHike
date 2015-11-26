package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.content.ContentValues;
import android.test.ApplicationTestCase;

import java.util.Random;

import me.dotteam.dotprod.data.EnvStatistic;

/**
 * Created by foxtrot on 22/11/15.
 */
//TODO: COMPLETE
public class EnvStatisticTest extends ApplicationTestCase<Application> {

    private Random valueGenerator;
    private EnvStatistic subject;

    private int id = Integer.MAX_VALUE;

    public EnvStatisticTest(){
        super(Application.class);
    }

    protected void setUp() throws Exception{
        super.setUp();

        //create the value generator and the test subject
        subject = new EnvStatistic();
    }

    public void testInsertion() throws Exception{

    }

    public void testInitialization() throws Exception{
        assertEquals(Float.NaN,subject.getAvg());
        assertEquals(Float.POSITIVE_INFINITY,subject.getMin());
        assertEquals(0,subject.getMax());
    }

    public void testValidity() throws Exception{
        //This should be false since insertion is not yet done
        assertFalse(subject.isValid());
    }

    public void testSerialization() throws Exception{
        testInsertion();
        ContentValues serialized = subject.toStorage(id);

    }

}
