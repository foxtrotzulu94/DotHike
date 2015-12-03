package me.dotteam.dotprod;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.EnvStatistic;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.SessionData;

public class ResultsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final String TAG = "ResultsActivity";

    private static final int MAP_HEIGHT=900;//DP
    private static final int ALTITUDE_CHART_HEIGHT=500;//DP
    private static final int INST_PACE_CHART_HEIGHT=500;//DP
    private static final int STATISTIC_CHART_HEIGHT=300;//DP

    protected HikeDataDirector mHDD;

    protected List<Coordinates> mCoordinatesList;
    protected List<Double> mInstPaceList;
    protected double mDistanceTraveled;

    protected GoogleMap mMap;

    protected MapView mMapView;
    protected LineChart mAltitudeChart;
    protected LineChart mInstPaceChart;
    protected BarChart mTempChart;
    protected BarChart mPressureChart;
    protected BarChart mHumidityChart;

    protected Button mButtonResultsDone;
//    protected TextView mDumpSpace;

    protected LinearLayout mMapContainer;
    protected LinearLayout mAltitudeChartContainer;
    protected LinearLayout mInstPaceChartContainer;
    protected LinearLayout mTextReadingsContainer;
    protected LinearLayout mTempChartContainer;
    protected LinearLayout mHumidityChartContainer;
    protected LinearLayout mPressureChartContainer;
    protected LinearLayout mTextDistTravlContainer;
    protected LinearLayout mTextHikeTimeContainer;
    protected LinearLayout mTextAvgPaceContainer;
    protected LinearLayout mTextStepCountContainer;

    private void setMemberIDs(){
        mButtonResultsDone = (Button) findViewById(R.id.buttonResultsDone);
//        mDumpSpace = (TextView) findViewById(R.id.textView_dumpspace);

        mMapContainer=(LinearLayout) findViewById(R.id.linlayout_Map);
        mAltitudeChartContainer=(LinearLayout) findViewById(R.id.linlayout_heightResults);
        mInstPaceChartContainer=(LinearLayout) findViewById(R.id.linlayout_InstPace);
        mTextReadingsContainer=(LinearLayout) findViewById(R.id.linlayout_textReadings);
        mTempChartContainer=(LinearLayout) findViewById(R.id.linlayout_TempStat);
        mHumidityChartContainer=(LinearLayout) findViewById(R.id.linlayout_HumidityStat);
        mPressureChartContainer=(LinearLayout) findViewById(R.id.linlayout_PressureStat);
        mTextDistTravlContainer=(LinearLayout) findViewById(R.id.linlayout_textDistTravl);
        mTextHikeTimeContainer=(LinearLayout) findViewById(R.id.linlayout_textHikeTime);
        mTextAvgPaceContainer=(LinearLayout) findViewById(R.id.linlayout_textAvgPace);
        mTextStepCountContainer=(LinearLayout) findViewById(R.id.linlayout_textStepCount);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        setMemberIDs();

        mButtonResultsDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentHome = new Intent(ResultsActivity.this, HomeActivity.class);
                intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentHome);
                mHDD.storeCollectedStatistics();
            }
        });
        mHDD=HikeDataDirector.getInstance(this);

        mCoordinatesList = mHDD.getSessionData().getGeoPoints().getCoordinateList();


        //Setup all the charts!
        if (mCoordinatesList != null && mCoordinatesList.size() != 0) {
            setupMap();
            setupAltitudeChart();

            mDistanceTraveled = 0;
            mInstPaceList = new ArrayList<>();

            for (int i = 1; i < mCoordinatesList.size(); i++) {
                // Get previous location
                double prevLatitude = mCoordinatesList.get(i - 1).getLatitude();
                double prevLongitude = mCoordinatesList.get(i - 1).getLongitude();
                double currLatitude = mCoordinatesList.get(i).getLatitude();
                double currLongitude = mCoordinatesList.get(i).getLongitude();

                // Array to store distance result
                float distanceResults[] = new float[3];

                // Get distance current location and last known location
                Location.distanceBetween(prevLatitude, prevLongitude, currLatitude, currLongitude, distanceResults);

                mDistanceTraveled += distanceResults[0];
            }
            // Note: Removed since we could not implement on time. Eventually this will be added
            //setupInstPaceChart();
        }

        setupEnvReadingsLayout();
        setupOtherInfoLayout();
    }
    protected void setupMap(){
        // GoogleMapOptions to Set Map to Lite Mode
        GoogleMapOptions googleMapOptions = new GoogleMapOptions().liteMode(true);

        mMapView = new MapView(this, googleMapOptions);

        mMapView.setClickable(false);

        mMapView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mMapView.setMinimumHeight(MAP_HEIGHT);
        mMapContainer.addView(mMapView);
        mMapContainer.setMinimumHeight(MAP_HEIGHT);

        mMapView.onCreate(null);
        mMapView.onResume();
        mMapView.getMapAsync(this);
    }

    protected void setupAltitudeChart(){
        TextView textAltitude = new TextView(this);
        textAltitude.setText("Altitude:");
        textAltitude.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mAltitudeChartContainer.addView(textAltitude);

        if(mCoordinatesList!=null) {
            int listSize = mCoordinatesList.size();
            if (listSize > 0) {
                mAltitudeChart = new LineChart(this);
                List<Entry> entries = new ArrayList<>(listSize);

                for (int i = 0; i < listSize; i++) {
                    entries.add(new Entry((float) mCoordinatesList.get(i).getAltitude(), i));
                }
                LineDataSet altitudePoints = new LineDataSet(entries, "Altitude during Hike");

                mAltitudeChart.setData(new LineData(Collections.nCopies(listSize, ""), altitudePoints));
                mAltitudeChart.setDescription("");
                mAltitudeChart.animateX(3500);
                mAltitudeChart.getAxisLeft().setEnabled(false);
                mAltitudeChart.setTouchEnabled(false);
                mAltitudeChart.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                mAltitudeChart.setMinimumHeight(ALTITUDE_CHART_HEIGHT);
                mAltitudeChartContainer.addView(mAltitudeChart);
                mAltitudeChartContainer.setMinimumHeight(ALTITUDE_CHART_HEIGHT);

                return;
            }
        }

        //Else...
        TextView noAltitude = new TextView(this);
        noAltitude.setText("No Altitude Height to Show");
        mAltitudeChartContainer.addView(noAltitude);

    }

    protected void setupInstPaceChart(){
        TextView textInstPace = new TextView(this);
        textInstPace.setText("Pace:");
        textInstPace.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mInstPaceChartContainer.addView(textInstPace);

        if(mCoordinatesList!=null) {
            int listSize = mCoordinatesList.size();
            if (listSize > 0) {
                mInstPaceChart = new LineChart(this);
                List<Entry> entries = new ArrayList<>(listSize);

                for (int i = 0; i < listSize; i++) {
                    //TODO change logic to get the instant pace instead of altidude
                    entries.add(new Entry((float) mCoordinatesList.get(i).getAltitude(), i));
                }
                LineDataSet altitudePoints = new LineDataSet(entries, "Pace during Hike");

                mInstPaceChart.setData(new LineData(Collections.nCopies(listSize, ""), altitudePoints));
                mInstPaceChart.setDescription("");
                mInstPaceChart.animateX(3500);
                mInstPaceChart.getAxisLeft().setEnabled(false);
                mInstPaceChart.setTouchEnabled(false);

                mInstPaceChart.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                mInstPaceChart.setMinimumHeight(INST_PACE_CHART_HEIGHT);
                mInstPaceChartContainer.addView(mInstPaceChart);
                mInstPaceChartContainer.setMinimumHeight(INST_PACE_CHART_HEIGHT);

                return;
            }

            //Else...
            TextView noInstaPace = new TextView(this);
            noInstaPace.setText("No Pace to Show");
            mAltitudeChartContainer.addView(noInstaPace);
        }
    }

    protected void setupEnvReadingsLayout(){
        TextView textReadings = new TextView(this);
        textReadings.setText("Readings:");
        textReadings.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextReadingsContainer.addView(textReadings);

        setupTemperatureChart();
        setupHumidityChart();
        setupPressureChart();
    }

    protected void setupTemperatureChart(){
        EnvStatistic temperature = mHDD.getSessionData().getCurrentStats().getTemperature();
        if(temperature!=null && temperature.isValid()){
            mTempChart = new BarChart(this);
            List<BarEntry> statVals = new ArrayList<>(3);

            statVals.add(new BarEntry((float) temperature.getMin(), 0));
            statVals.add(new BarEntry((float) temperature.getAvg(), 1));
            statVals.add(new BarEntry((float) temperature.getMax(), 2));

            BarDataSet summary = new BarDataSet(statVals,"Temperature");

            mTempChart.setData(new BarData(new String[]{"Min", "Avg", "Max"}, summary));
            mTempChart.setDescription("");
            mTempChart.animateY(2000);
            mTempChart.getAxisRight().setEnabled(false);
            mTempChart.setTouchEnabled(false);

            mTempChart.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mTempChart.setMinimumHeight(STATISTIC_CHART_HEIGHT);
            mTempChartContainer.addView(mTempChart);
        }
        else{
            TextView noGraph = new TextView(this);
            noGraph.setText("No Temperature Statistics to Show");
            noGraph.setGravity(View.TEXT_ALIGNMENT_CENTER);
            mTempChartContainer.addView(noGraph);
        }
    }

    protected void setupPressureChart(){
        EnvStatistic pressure = mHDD.getSessionData().getCurrentStats().getPressure();
        if(pressure!=null && pressure.isValid()){
            mPressureChart = new BarChart(this);
            List<BarEntry> statVals = new ArrayList<>(3);

            statVals.add(new BarEntry((float) pressure.getMin(),0));
            statVals.add(new BarEntry((float) pressure.getAvg(), 1));
            statVals.add(new BarEntry((float) pressure.getMax(), 2));

            BarDataSet summary = new BarDataSet(statVals,"Pressure");

            mPressureChart.setData(new BarData(new String[]{"Min", "Avg", "Max"}, summary));
            mPressureChart.setDescription("");
            mPressureChart.animateY(2000);
            mPressureChart.getAxisRight().setEnabled(false);
            mPressureChart.setTouchEnabled(false);

            mPressureChart.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mPressureChart.setMinimumHeight(STATISTIC_CHART_HEIGHT);
            mPressureChartContainer.addView(mPressureChart);
        }
        else{
            TextView noGraph = new TextView(this);
            noGraph.setText("No Pressure Statistics to Show");
            noGraph.setGravity(View.TEXT_ALIGNMENT_CENTER);
            mPressureChartContainer.addView(noGraph);
        }
    }

    protected void setupHumidityChart(){
        EnvStatistic humidity = mHDD.getSessionData().getCurrentStats().getHumidity();
        if(humidity!=null && humidity.isValid()){
            mHumidityChart = new BarChart(this);
            List<BarEntry> statVals = new ArrayList<>(3);

            statVals.add(new BarEntry((float) humidity.getMin(), 0));
            statVals.add(new BarEntry((float) humidity.getAvg(), 1));
            statVals.add(new BarEntry((float) humidity.getMax(), 2));

            BarDataSet summary = new BarDataSet(statVals,"Humidity");

            mHumidityChart.setData(new BarData(new String[]{"Min", "Avg", "Max"}, summary));
            mHumidityChart.setDescription("");
            mHumidityChart.animateY(2000);
            mHumidityChart.getAxisRight().setEnabled(false);
            mHumidityChart.setTouchEnabled(false);

            mHumidityChart.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mHumidityChart.setMinimumHeight(STATISTIC_CHART_HEIGHT);
            mHumidityChartContainer.addView(mHumidityChart);
        }
        else{
            TextView noGraph = new TextView(this);
            noGraph.setText("No Humidity Statistics to Show");
            noGraph.setGravity(View.TEXT_ALIGNMENT_CENTER);
            mHumidityChartContainer.addView(noGraph);
        }
    }

    protected void setupOtherInfoLayout(){
        SessionData results = mHDD.getSessionData();

        DecimalFormat numberFormat = new DecimalFormat("#.000");

        double hikeDuration = (results.hikeEndTime() - results.hikeStartTime()) * 0.001;

        //Total Distance Travelled
        TextView textDistTravl = new TextView(this);
        textDistTravl.setText("Total Distance Traveled: ");
        textDistTravl.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextDistTravlContainer.addView(textDistTravl);

        TextView textDistTravlVal = new TextView(this);
        textDistTravlVal.setText(String.valueOf(numberFormat.format(mDistanceTraveled)) + " m" + " (" + String.valueOf(numberFormat.format(mDistanceTraveled/1000)) + " km)");
        textDistTravlVal.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextDistTravlContainer.addView(textDistTravlVal);

        //Total Hike Time
        TextView textHikeTime = new TextView(this);
        textHikeTime.setText("Duration of Hike: ");
        textHikeTime.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextHikeTimeContainer.addView(textHikeTime);

        StringBuilder HikeTimeResults = new StringBuilder();
        // TODO: Format Time
        HikeTimeResults.append(String.valueOf(numberFormat.format(hikeDuration)) + " s");
        TextView textHikeTimeResults = new TextView(this);
        textHikeTimeResults.setText(HikeTimeResults.toString());
        textHikeTimeResults.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextHikeTimeContainer.addView(textHikeTimeResults);

        //Average Pace
        TextView textAvgPace = new TextView(this);
        textAvgPace.setText("Average Pace: ");
        textAvgPace.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextAvgPaceContainer.addView(textAvgPace);

        TextView textAvgPaceVal = new TextView(this);
        double average_pace = mDistanceTraveled/hikeDuration;
        textAvgPaceVal.setText(String.valueOf(numberFormat.format(average_pace)) + " m/s" + " (" + String.valueOf(numberFormat.format(average_pace * 3.6)) + " km/h)");
        textAvgPaceVal.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextAvgPaceContainer.addView(textAvgPaceVal);

        //Step Count
        TextView textStepCount = new TextView(this);
        textStepCount.setText("Total Steps Taken: ");
        textStepCount.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextStepCountContainer.addView(textStepCount);

        TextView textStepCountResults = new TextView(this);
        textStepCount.setText(results.getStepCount().toString());
        textStepCount.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextStepCountContainer.addView(textStepCountResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady called");

        mMap = googleMap;

        // Set MapType to Terrain
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        // Change GoogleMap's UI Settings to remove toolbar stuff
        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setMapToolbarEnabled(false);

        // On Map Loaded Callback
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                PolylineOptions polylineOptions = new PolylineOptions();

                // Create LatLngBounds object. This is used to zoom in to the map in such a way
                // that all points are visible
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                for (int i = 0; i < mCoordinatesList.size(); i++) {
                    double lat = mCoordinatesList.get(i).getLatitude();
                    double lng = mCoordinatesList.get(i).getLongitude();

                    LatLng latLng = new LatLng(lat, lng);

                    polylineOptions.add(latLng);

                    // Add to LatLngBounds object
                    boundsBuilder.include(latLng);
                }

                mMap.addPolyline(polylineOptions);
                // Zoom in to map
                LatLngBounds bounds = boundsBuilder.build();

                // TODO: Fix padding. Why does it not work?
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));


                LatLng startPos = new LatLng(mCoordinatesList.get(0).getLatitude(), mCoordinatesList.get(0).getLongitude());
                LatLng endPos = new LatLng(mCoordinatesList.get(mCoordinatesList.size() - 1).getLatitude(),
                        mCoordinatesList.get(mCoordinatesList.size() - 1).getLongitude());

                mMap.addMarker(new MarkerOptions()
                        .position(startPos)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mMap.addMarker(new MarkerOptions().position(endPos));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
