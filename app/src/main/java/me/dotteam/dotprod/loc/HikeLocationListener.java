package me.dotteam.dotprod.loc;

import android.location.Location;

/**
 * Listener for HikeLocationEntity location callbacks.
 *
 * Created by EricTremblay on 15-11-20.
 */
public interface HikeLocationListener {

    /**
     * Method called whenever a new location object is obtained
     * @param location Object that contains location information
     * @param distance Distance between the new location and the previous one
     */
    public void onLocationChanged(Location location, float distance);

}
