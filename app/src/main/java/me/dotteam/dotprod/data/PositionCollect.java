package me.dotteam.dotprod.data;
import android.location.Location;

import com.google.android.gms.location.LocationListener;


/**
 * Listener implementation used by {@link SessionCollectionService} to subscribe to location updates
 *
 */
public class PositionCollect implements LocationListener {

    /**
     * Default constructor
     */
    public PositionCollect() {
    }

    @Override
    public void onLocationChanged(Location location) {
        //TODO: Implement here
    }

}