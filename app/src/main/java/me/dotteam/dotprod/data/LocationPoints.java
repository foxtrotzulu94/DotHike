package me.dotteam.dotprod.data;


import java.util.ArrayList;
import java.util.List;

/**
 * Data structure to represent a traversed set of locations
 */
public class LocationPoints {

    protected List<Coordinates> points;

    /**
     * Default constructor
     */
    public LocationPoints() {
        points=new ArrayList<Coordinates>();
    }

    public LocationPoints(List<Coordinates> pointList){
        points = pointList;
    }

    public void addPoint(Coordinates newPoint){
        points.add(newPoint);
    }

    public List<Coordinates> getCoordinateList(){
        return points;
    }

    public String toString(){
        if(points!=null && points.size()>0)
            return String.format("Location Points has %s points\nFirst: %s\nLast: %s",
                    points.size(),points.get(0).toString(),points.get(points.size()-1).toString());
        else
            return String.format("Location Points has no points");
    }

}