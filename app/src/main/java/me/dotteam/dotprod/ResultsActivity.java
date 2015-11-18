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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.EnvStatistic;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.SessionData;

public class ResultsActivity extends AppCompatActivity {

    private static final int ALTITUDE_CHART_HEIGHT=500;//DP
    private static final int STATISTIC_CHART_HEIGHT=300;//DP

    protected HikeDataDirector mHDD;

    protected LineChart mAltitudeChart;
    protected BarChart mTempChart;
    protected BarChart mPressureChart;
    protected BarChart mHumidityChart;

    protected Button mButtonResultsDone;
    protected TextView mDumpSpace;
    protected LinearLayout mAltitudeChartContainer;
    protected LinearLayout mTempChartContainer;
    protected LinearLayout mHumidityChartContainer;
    protected LinearLayout mPressureChartContainer;

    private void setMemberIDs(){
        mButtonResultsDone = (Button) findViewById(R.id.buttonResultsDone);
        mDumpSpace = (TextView) findViewById(R.id.textView_dumpspace);
        mAltitudeChartContainer=(LinearLayout) findViewById(R.id.linlayout_heightResults);
        mTempChartContainer=(LinearLayout) findViewById(R.id.linlayout_TempStat);
        mHumidityChartContainer=(LinearLayout) findViewById(R.id.linlayout_HumidityStat);
        mPressureChartContainer=(LinearLayout) findViewById(R.id.linlayout_PressureStat);
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
                startActivity(intentHome);
                mHDD.storeCollectedStatistics();
            }
        });
        mHDD=HikeDataDirector.getInstance(this);

        StringBuilder dump = new StringBuilder();
        SessionData results = mHDD.getSessionData();

        //Setup all the charts!
        setupAltitudeChart();
        setupTemperatureChart();
        setupHumidityChart();
        setupPressureChart();

        dump.append(results.toString());
        mDumpSpace.setText(dump.toString());
    }

    protected void setupAltitudeChart(){
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
        EnvStatistic pressure = mHDD.getSessionData().getCurrentStats().getTemperature();
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
        EnvStatistic humidity = mHDD.getSessionData().getCurrentStats().getTemperature();
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
