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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.SessionData;
import me.dotteam.dotprod.data.SessionEnvData;

public class ResultsActivity extends AppCompatActivity {

    private static final int ALTITUDE_CHART_HEIGHT=500;//DP
    private static final int STATISTIC_CHART_HEIGHT=100;//DP

    private HikeDataDirector mHDD;

    Button mButtonResultsDone;
    TextView mDumpSpace;
    LinearLayout mAltitudeChartContainer;
    LinearLayout mTempChartContainer;
    LinearLayout mHumidityChartContainer;
    LinearLayout mPressureChartContainer;

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

        LineChart chart = new LineChart(this);
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(33, 0));
        entries.add(new Entry(31, 1));
        entries.add(new Entry(3, 2));
        LineDataSet testy = new LineDataSet(entries,"testy");

        chart.setData(new LineData(new String[]{"Developers", "Developers", "Developers"}, testy));
        chart.setDescription("");
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        chart.setMinimumHeight(ALTITUDE_CHART_HEIGHT);
        mAltitudeChartContainer.addView(chart);
        mAltitudeChartContainer.setMinimumHeight(ALTITUDE_CHART_HEIGHT);

        dump.append(results.toString());
        mDumpSpace.setText(dump.toString());

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
