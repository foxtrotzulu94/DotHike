package me.dotteam.dotprod;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dotteam.dotprod.data.Hike;

/**
 * Created by foxtrot on 16/11/15.
 */
public class HikeArrayAdapter extends ArrayAdapter<Hike> implements OnMapReadyCallback {
    private final String TAG = "HikeArrayAdapter";

    Hike mHike;

    ListView mParent;

    public HikeArrayAdapter(Context context, List<Hike> allHikes){
        super(context,R.layout.lite_map_fragment,allHikes);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView called");
        MapViewHolder holder = null;

        mParent = (ListView) parent;

        Log.d(TAG, "getView " + position + " " + convertView + " " + parent);
        //Using viewholder pattern to recycle everything correctly.
        if(convertView == null){
            LayoutInflater li = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.lite_map_fragment,parent,false);
            MapView mapView = (MapView) convertView.findViewById(R.id.view_map);
            mapView.onCreate(null);
            mapView.setClickable(false);
            holder = new MapViewHolder();
            holder.mMapView = mapView;
            convertView.setTag(holder);
        } else {
            holder = (MapViewHolder) convertView.getTag();
        }

        holder.mMapView.getMapAsync(this);

        GestureOverlayView overlayView = (GestureOverlayView) convertView.findViewById(R.id.gesture_overlay_view);

        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                mParent.performItemClick(v, position, getItemId(position));
            }
        });
        TextView hikeName = (TextView) convertView.findViewById(R.id.text_past_hike_title);

        //TextView hikeName = (TextView) convertView.findViewById(R.id.textView_HikeName);
        //TextView hikeStart = (TextView) convertView.findViewById(R.id.textView_HikeDate);
        //TextView hikeDuration = (TextView) convertView.findViewById(R.id.textView_HikeDuration);

        //TODO: Change to nickname or something
        mHike = super.getItem(position);
        hikeName.setText(Integer.toString(mHike.getUniqueID()));

        //hikeName.setText(Integer.toString(focused.getUniqueID()));
        //hikeStart.setText(new Date(focused.startTime()).toString());
        //hikeDuration.setText(focused.elapsedTime());
        return convertView;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    public static class MapViewHolder {
        public MapView mMapView;
    }

}
