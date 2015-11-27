package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.content.ContentValues;
import android.test.ApplicationTestCase;

import me.dotteam.dotprod.data.DBAssistant;
import me.dotteam.dotprod.data.Hike;

/**
 * Unit Test of Hike Class
 * Tests:
 * - Time Recording
 * - Completeness Self-Check
 * - Serialization for storage in DB
 */
public class HikeClassTest extends ApplicationTestCase<Application> {

    private Hike subject;
    private long startTime;
    private long endTime;

    public HikeClassTest(){
        super(Application.class);
    }

    protected void setUp() throws Exception{
        super.setUp();

        subject = new Hike();
    }

    public void testTime() throws Exception{
        subject.start();
        try{
            Thread.sleep(100);
        }
        catch (Exception e){}
        subject.end();

        assertTrue(subject.endTime() - subject.startTime() >= 100);
        startTime = subject.startTime();
        endTime = subject.endTime();
    }

    public void testNonCompleteness() throws Exception{
        assertFalse(new Hike().isComplete());
    }

    public void testCompleteness() throws Exception{
        testTime();
        assertTrue(subject.isComplete());
    }

    public void testNameSerialization() throws Exception{
        subject.setNickName("Test");
        assertEquals(subject.getNickName(),"Test");
    }

    public void testSerialization() throws Exception{
        testTime();
        subject.setUniqueID(Integer.MAX_VALUE);
        ContentValues serialized = subject.toStorage();

        assertTrue(serialized.containsKey(DBAssistant.HIKE_START));
        assertTrue(serialized.containsKey(DBAssistant.HIKE_END));

        assertEquals(serialized.get(DBAssistant.HIKE_START),startTime);
        assertEquals(serialized.get(DBAssistant.HIKE_END),endTime);
    }


}
