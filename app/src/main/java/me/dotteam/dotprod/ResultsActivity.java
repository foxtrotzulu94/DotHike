package me.dotteam.dotprod;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.EnvStatistic;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.SessionData;

public class ResultsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MAP_HEIGHT=900;//DP
    private static final int ALTITUDE_CHART_HEIGHT=500;//DP
    private static final int INST_PACE_CHART_HEIGHT=500;//DP
    private static final int STATISTIC_CHART_HEIGHT=300;//DP

    protected HikeDataDirector mHDD;

    protected MapView mMapView;
    protected LineChart mAltitudeChart;
    protected LineChart mInstPaceChart;
    protected BarChart mTempChart;
    protected BarChart mPressureChart;
    protected BarChart mHumidityChart;

    protected Button mButtonResultsDone;
//    protected TextView mDumpSpace;

    protected LinearLayout mMapContainer;
    protected LinearLayout mTextAltitudeContainer;
    protected LinearLayout mAltitudeChartContainer;
    protected LinearLayout mTextInstPaceContainer;
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
        mTextAltitudeContainer=(LinearLayout) findViewById(R.id.linlayout_textAltitude);
        mAltitudeChartContainer=(LinearLayout) findViewById(R.id.linlayout_heightResults);
        mTextInstPaceContainer=(LinearLayout) findViewById(R.id.linlayout_textInstPace);
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

//        StringBuilder dump = new StringBuilder();
    SessionData results = mHDD.getSessionData();

        //Setup all the charts!
        setupMap();
        setupAltitudeChart();
        setupInstPaceChart();
        setupEnvReadingsLayout();
        setupOtherInfoLayout();
//        dump.append(results.toString());
//        mDumpSpace.setText(dump.toString());
    }
    protected void setupMap(){
        mMapView = new MapView(this);

       mMapView.setLayoutParams(new LinearLayout.LayoutParams(
               ViewGroup.LayoutParams.MATCH_PARENT,
               ViewGroup.LayoutParams.MATCH_PARENT));
        mMapView.setMinimumHeight(MAP_HEIGHT);
        mMapContainer.addView(mMapView);
        mMapContainer.setMinimumHeight(MAP_HEIGHT);

        mMapView.onCreate(null);
        mMapView.getMapAsync(this);
    }

    protected void setupAltitudeChart(){
        TextView textAltitude = new TextView(this);
        textAltitude.setText("Altitude:");
        textAltitude.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextAltitudeContainer.addView(textAltitude);

        List<Coordinates> coordinates = mHDD.getSessionData().getGeoPoints().getCoordinateList();

        if(coordinates!=null) {
            int listSize = coordinates.size();
            if (listSize > 0) {
                mAltitudeChart = new LineChart(this);
                List<Entry> entries = new ArrayList<>(listSize);

                for (int i = 0; i < listSize; i++) {
                    entries.add(new Entry((float) coordinates.get(i).getAltitude(), i));
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
        mTextInstPaceContainer.addView(textInstPace);

        List<Coordinates> coordinates = mHDD.getSessionData().getGeoPoints().getCoordinateList();

        if(coordinates!=null) {
            int listSize = coordinates.size();
            if (listSize > 0) {
                mInstPaceChart = new LineChart(this);
                List<Entry> entries = new ArrayList<>(listSize);

                for (int i = 0; i < listSize; i++) {
                    //TODO change logic to get the instant pace instead of altidude
                    entries.add(new Entry((float) coordinates.get(i).getAltitude(), i));
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

        //Total Distance Travelled
        TextView textDistTravl = new TextView(this);
        textDistTravl.setText("Total Distance Traveled:");
        textDistTravl.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextDistTravlContainer.addView(textDistTravl);

        //Total Hike Time
        TextView textHikeTime = new TextView(this);
        textHikeTime.setText("Duration of Hike:");
        textHikeTime.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextHikeTimeContainer.addView(textHikeTime);

        StringBuilder HikeTimeResults = new StringBuilder();
        HikeTimeResults.append(String.valueOf(results.hikeEndTime()));
        TextView textHikeTimeResults = new TextView(this);
        textHikeTimeResults.setText(HikeTimeResults.toString());
        textHikeTimeResults.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextStepCountContainer.addView(textHikeTimeResults);

        //Average Pace
        TextView textAvgPace = new TextView(this);
        textAvgPace.setText("Average Pace:");
        textAvgPace.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextAvgPaceContainer.addView(textAvgPace);

        //Step Count
        TextView textStepCount = new TextView(this);
        textStepCount.setText("Total Steps Taken:");
        textStepCount.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextStepCountContainer.addView(textStepCount);

        StringBuilder StepCountResults = new StringBuilder();
        StepCountResults.append(results.getStepCount().toString());
        TextView textStepCountResults = new TextView(this);
        textStepCount.setText(StepCountResults.toString());
        textStepCount.setTextColor(getResources().getColor(R.color.hike_blue_grey));
        mTextStepCountContainer.addView(textStepCountResults);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

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
