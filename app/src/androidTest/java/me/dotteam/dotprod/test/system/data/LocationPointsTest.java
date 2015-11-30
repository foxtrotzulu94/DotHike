package me.dotteam.dotprod.test.system.data;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.LocationPoints;

/**
 * Unit Test of the LocationPoints class.
 * Tests:
 * - Default Construction
 * - Construction with List
 * - Insertion of Coordinates Objects
 */
public class LocationPointsTest extends ApplicationTestCase<Application> {

    public static final int TEST_SIZE=10;

    private Random valueGenerator;


    private LocationPoints subject;
    private List<Coordinates> coordinatesList;

    public LocationPointsTest(){
        super(Application.class);
    }

    protected void setUp() throws Exception{
        super.setUp();
        //Create Random Values
        valueGenerator = new Random();

        subject = new LocationPoints();

        coordinatesList = new ArrayList<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            coordinatesList.add(new Coordinates(
                    valueGenerator.nextDouble(),
                    valueGenerator.nextDouble(),
                    valueGenerator.nextDouble()));
        }

    }

    public void testConstruction() throws Exception{
        subject = new LocationPoints();
        assertNotNull(subject.getCoordinateList());
        assertEquals(subject.getCoordinateList().size(),0);
    }

    public void testConstructionWithList() throws Exception{
        subject = new LocationPoints(coordinatesList);
        List<Coordinates> storedList = subject.getCoordinateList();
        for (int i = 0; i < storedList.size(); i++) {
            assertSame(storedList.get(i),coordinatesList.get(i));
        }
    }

    public void testInsertion() throws Exception{
        subject = new LocationPoints();
        for (Coordinates aCoordinate: coordinatesList) {
            subject.addPoint(aCoordinate);
        }

        List<Coordinates> storedList = subject.getCoordinateList();
        for (int i = 0; i < storedList.size(); i++) {
            assertSame(storedList.get(i),coordinatesList.get(i));
        }
    }
}
