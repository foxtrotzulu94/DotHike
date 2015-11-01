package me.dotteam.dotprod.data;


import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class LocationPoints {

    /**
     * 
     */
    protected List<Coordinates> points;

    /**
     * Default constructor
     */
    public LocationPoints() {
        points=new ArrayList<Coordinates>();
    }

    public void addPoint(Coordinates newPoint){
        points.add(newPoint);
    }

    public List<Coordinates> getCoordinateList(){
        return points;
    }


}