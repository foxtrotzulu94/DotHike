package me.dotteam.dotprod.loc;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles Location Requests to GoogleApiClient and provides an easy way for any object to obtain Location Updates using one common entity.
 *
 * Created by EricTremblay on 15-11-05.
 */
public class HikeLocationEntity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /**
     * Class TAG
     */
    private final String TAG = "HikeLocationEntity";

    /**
     * Default Values for Location Updates
     */
    private final int DEFAULT_INTERVAL = 10000;
    private final int DEFAULT_FASTEST_INTERVAL = 5000;
    private final int DEFAULT_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    /**
     * Location Request Object
     */
    private LocationRequest mLocationRequest;

    /**
     * Actual Values for Location Updates
     */
    private int mInterval;
    private int mFastestInterval;
    private int mPriority;

    /**
     * Static HikeLocationEntity Singleton Reference
     */
    private static HikeLocationEntity mInstance;

    /**
     * Activity Context Object
     */
    private Context mContext;

    /**
     * Reference to Google API Client Object
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * List of Currently Subscribed LocationListeners
     */
    private List<LocationListener> mLocationListeners;

    /**
     * Boolean to save the GoogleApiClient connection state
     */
    private boolean mGoogleApiClientConnected = false;

    /**
     * Boolean to save whether updates are on or off
     */
    private boolean mRequestingLocationUpdates = false;

    /**
     * Singleton method to obtain or generate current instance
     * @param context Context from which the instance is being requested
     * @return Singleton Object instance of HikeLocationEntity
     */
    public static HikeLocationEntity getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HikeLocationEntity(context);
        }
        return mInstance;
    }

    /**
     * Private Constructor for HikeLocationEntity
     * @param context Context from which the object creation is being requested
     */
    private HikeLocationEntity(Context context) {
        // Create LocationListener List
        mLocationListeners = new ArrayList<>();

        // Create Location Request and set variables to default values
        mLocationRequest = new LocationRequest();
        mInterval = DEFAULT_INTERVAL;
        mFastestInterval = DEFAULT_FASTEST_INTERVAL;
        mPriority = DEFAULT_PRIORITY;

        // Save Context
        mContext = context;

        // Build GoogleApiClient
        buildGoogleApiClient();
    }

    /**
     * Starts location updates for all registered listeners
     */
    public void startLocationUpdates() {
        mRequestingLocationUpdates = true;
        if (mGoogleApiClientConnected) {
            for (int i = 0; i < mLocationListeners.size(); i++) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListeners.get(i));
            }
        }
    }

    /**
     * Stops location updates for all registered listeners
     */
    public void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        if (mGoogleApiClientConnected) {
            for (int i = 0; i < mLocationListeners.size(); i++) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListeners.get(i));
            }
        }
    }

    /**
     * Register LocationListener to HikeLocationEntity.
     * Start location updates for the LocationListener if the GoogleApiClient is connected and the location updates have been started
     * @param listener LocationListener to be registered
     */
    public void addLocationListener(LocationListener listener) {
        mLocationListeners.add(listener);
        if (mGoogleApiClientConnected && mRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, listener);
        }
    }

    /**
     * Unregister LocationListener from HikeLocationEntity.
     * This LocationListener will cease receiving any location updates
     * @param listener LocationListener to unregister
     */
    public void removeLocationListener(LocationListener listener) {
        mLocationListeners.remove(listener);
        if (mGoogleApiClientConnected && mRequestingLocationUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, listener);
        }
    }

    /**
     * Method to build GoogleApiClient object from within HikeLocationEntity object
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Method to set the preferred rate at which the application receives location updates
     * @param mInterval New interval between location updates in milliseconds
     */
    public void setInterval(int mInterval) {
        this.mInterval = mInterval;
    }

    /**
     * Method to set the fastest rate at which the application can handle location updates
     * @param mFastestInterval New fastest interval between location updates in milliseconds
     */
    public void setFastestInterval(int mFastestInterval) {
        this.mFastestInterval = mFastestInterval;
    }

    /**
     * Method to set the priority of the location request. This will allow the Google Play services to determine which location sources to use.
     * Possible values for priority: PRIORITY_BALANCED_POWER_ACCURACY,
     * PRIORITY_HIGH_ACCURACY, PRIORITY_LOW_POWER, and PRIORITY_NO_POWER.
     * @param mPriority New priority for location request
     */
    public void setPriority(int mPriority) {
        this.mPriority = mPriority;
    }

    /**
     * Method to obtain the current preferred interval between location updates.
     * @return Current preferred interval value
     */
    public int getInterval() {
        return mInterval;
    }

    /**
     * Method to obtain the current fastest interval between location updates.
     * @return Current fastest interval value
     */
    public int getFastestInterval() {
        return mFastestInterval;
    }

    /**
     * Method to obtain the current priority of the location request.
     * @return Current priority value
     */
    public int getPriority() {
        return mPriority;
    }

    /**
     * Method to obtain the current state of the GoogleApiClient Connection
     * @return True indicates the GoogleApiClient is connected, false indicates it is disconnected
     */
    public boolean isGoogleApiClientConnected() {
        return mGoogleApiClientConnected;
    }

    /**
     * Method to obtain the current state of the location updates.
     * @return True indicates the location updates are on, false indicates that they are off.
     */
    public boolean isRequestingLocationUpdates() {
        return mRequestingLocationUpdates;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleApiClientConnected = true;
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        String cause = "CAUSE_UNKNOWN";
        switch (i) {
            case CAUSE_SERVICE_DISCONNECTED: {
                cause = "CAUSE_SERVICE_DISCONNECTED";
                break;
            }
            case CAUSE_NETWORK_LOST: {
                cause = "CAUSE_NETWORK_LOST";
                break;
            }
        }

        Log.d(TAG, "GoogleApiClient Connection Suspended. Cause: " + cause);
        mGoogleApiClientConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mGoogleApiClientConnected = false;

    }
}
