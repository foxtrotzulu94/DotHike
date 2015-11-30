package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;

import java.io.File;

import me.dotteam.dotprod.data.DBAssistant;

/**
 * Unit Test of DBAssistant Class
 * Tests only the methods required for Creation and Destruction
 */
public class DBAssistantTest extends ApplicationTestCase<Application> {

    private RenamingDelegatingContext testContext;
    private DBAssistant subject;
    private String subjectName;

    public DBAssistantTest(){
        super(Application.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        //Create the database and take its data
        testContext = new RenamingDelegatingContext(getContext(),"test_");
        subject = new DBAssistant(testContext);
        subjectName = subject.getDatabaseName();
    }

    public void testCreation() throws Exception{
        File physicalDB = getContext().getDatabasePath(subjectName);
        assertTrue(physicalDB.exists());
    }

    public void testRecreation() throws Exception{
        subject.onUpgrade(subject.getWritableDatabase(),-1,1);
        File physicalDB = getContext().getDatabasePath(subjectName);
        assertTrue(physicalDB.exists());
    }

}
