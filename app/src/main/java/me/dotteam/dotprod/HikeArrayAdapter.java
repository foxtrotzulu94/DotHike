package me.dotteam.dotprod;

import android.content.Context;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.LocationPoints;

/**
 * Created by foxtrot on 16/11/15.
 */
public class HikeArrayAdapter extends ArrayAdapter<Hike>  {
    private final String TAG = "HikeArrayAdapter";

    /**
     * Reference to parent ViewGroup object. In this case, this is a GridView
     */
    GridView mParent;

    /**
     * Reference to calling activity context
     */
    Context mContext;

    /**
     *
     */
    Map<Integer, MapReady> mMapCallbacks;

    /**
     *
     */
    Map<Integer, LocationPoints> mLoadedLocationPoints;

    /**
     * Default Constructor
     * @param context
     * @param allHikes list of hikes to display in GridView
     */
    public HikeArrayAdapter(Context context, List<Hike> allHikes){
        super(context,R.layout.lite_map_fragment,allHikes);
        mContext = context;
        mMapCallbacks = new HashMap<>();

        //Do Async with this
        //mLoadedLocationPoints = HikeDataDirector.getInstance(mContext).
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView " + position + " " + convertView + " " + parent);

        // Reference to MapViewHolder object
        MapViewHolder holder;

        // Set Parent
        mParent = (GridView) parent;

        //Using viewholder pattern to recycle everything correctly.
        if(convertView == null){
            LayoutInflater li = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.lite_map_fragment,parent,false);

            // Get reference to MapView and set it up
            MapView mapView = (MapView) convertView.findViewById(R.id.view_map);
            mapView.onCreate(null);
            mapView.setClickable(false);

            // Create new MapViewHolder object
            holder = new MapViewHolder();
            holder.mMapView = mapView;

            // Save MapViewHolder to view's tag
            convertView.setTag(holder);
        }
        else {
            // Recall MapViewHolder
            holder = (MapViewHolder) convertView.getTag();
        }

        // Get reference to GestureOverlayView
        // Note: This is used to capture the touch events on the MapViews and TextViews
        GestureOverlayView overlayView = (GestureOverlayView) convertView.findViewById(R.id.gesture_overlay_view);

        // Set its onClickListener to call parent's onItemClick
        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                mParent.performItemClick(v, position, getItemId(position));
            }
        });
        overlayView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onLongClick");
                //HACK: Since there is no performItemLongClick, we get the method reference and
                //          call it ourselves
                AdapterView.OnItemLongClickListener result = mParent.getOnItemLongClickListener();
                if(result!=null){
                    Log.d(TAG, "onLongClick Calling result method!");
                    result.onItemLongClick(mParent, v, position, getItemId(position));
                }
                return true;
            }
        });

        // Get References to TextViews
        TextView hikeName = (TextView) convertView.findViewById(R.id.text_past_hike_title);
        TextView hikeDate = (TextView) convertView.findViewById(R.id.text_past_hike_date);
        TextView hikeDuration = (TextView) convertView.findViewById(R.id.text_past_hike_duration);

        // Get Hike object
        Hike hike = super.getItem(position);

        //TODO: Change to nickname or something
        Integer id = hike.getUniqueID();
        hikeName.setText(Integer.toString(id));

        // Set Hike date
        hikeDate.setText(new Date(hike.startTime()).toString());

        // Set Hike duration
        hikeDuration.setText(hike.elapsedTime());

        // Get callback object
        MapReady mapReadyCallback = mMapCallbacks.get(id);

        if (mapReadyCallback == null) {

            // Get Location Points
            HikeDataDirector hdd = HikeDataDirector.getInstance(mContext);

            //TODO: OPTIMIZE AND BULK LOAD
            hdd.retrieveSessionFromHike(hike);
            List<Coordinates> coordinatesList = hdd.getSessionData().getGeoPoints().getCoordinateList();

            mapReadyCallback = new MapReady(id);

            mapReadyCallback.mCoordinatesList = coordinatesList;

            mMapCallbacks.put(id, mapReadyCallback);
        }

        // Create GoogleMap object and pass callback
        holder.mMapView.getMapAsync(mapReadyCallback);

        // Return View
        return convertView;
    }

    /**
     * Used to hold objects that are associated with GridViewItems.
     * This avoids having to create them every time.
     * This is saved as a view's tag so that it can be easily recalled.
     */
    public static class MapViewHolder {
        public MapView mMapView;
    }

    /**
     * Class that serves as the callback for when the GoogleMaps are ready.
     * These are given a hike's unique id when instantiated so the coordinates
     * can be obtained from the HashMap.
     * The callback creates the polylines and zooms in the approriate place on the map
     */
    public class MapReady implements OnMapReadyCallback {

        /**
         * Unique Hike ID
         */
        Integer mHikeId;

        // List of Coordinates for associated Hike
        List<Coordinates> mCoordinatesList;

        /**
         * Default Constructor
         * @param id unique hike id
         */
        public MapReady(Integer id) {
            mHikeId = id;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.d(TAG, "onMapReady: " + mHikeId);
            googleMap.clear();

            // Set Map Type to Terrain
            SharedPreferences prefMan= PreferenceManager.getDefaultSharedPreferences(getContext());
            if(prefMan.contains("display_maptype")){
                googleMap.setMapType(
                        Integer.parseInt(prefMan.getString(
                                "display_maptype",
                                Integer.toString(GoogleMap.MAP_TYPE_TERRAIN))));
            }
            else {
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }

            // Change GoogleMap's UI Settings to remove toolbar stuff
            UiSettings mapSettings = googleMap.getUiSettings();
            mapSettings.setMapToolbarEnabled(false);

            // Create Polyline object
            PolylineOptions mapPolylineOptions = new PolylineOptions();

            // Create LatLngBounds object. This is used to zoom in to the map in such a way
            // that all points are visible
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

            if (mCoordinatesList != null && mCoordinatesList.size()>0) {
                for (int i = 0; i < mCoordinatesList.size(); i++) {
                    // Get latitude and longitude
                    LatLng latLng = new LatLng(mCoordinatesList.get(i).getLatitude(), mCoordinatesList.get(i).getLongitude());

                    // Add to LatLngBounds object
                    boundsBuilder.include(latLng);

                    // Add to Polyline object
                    mapPolylineOptions.add(latLng);
                }

                // Draw polylines
                googleMap.addPolyline(mapPolylineOptions);

                // Zoom in to map
                LatLngBounds bounds = boundsBuilder.build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
            }
            else {
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(0));
            }
        }
    }

}
