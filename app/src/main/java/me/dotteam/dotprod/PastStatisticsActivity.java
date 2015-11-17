package me.dotteam.dotprod;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.SessionData;

/**
 * Created by foxtrot on 16/11/15.
 */
public class PastStatisticsActivity extends AppCompatActivity {
    private Button mButtonResultsDone;
    private TextView mDumpSpace;
    private HikeDataDirector mHDD;
    private SessionData collectedSessionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        mHDD = HikeDataDirector.getInstance(this);
        collectedSessionData = mHDD.getSessionData();

        mDumpSpace = (TextView) findViewById(R.id.textView_dumpspace);
        mButtonResultsDone = (Button) findViewById(R.id.buttonResultsDone);

        StringBuilder dump = new StringBuilder();
        SessionData results = mHDD.getSessionData();

        dump.append(results.toString());
        mDumpSpace.setText(dump.toString());

        mButtonResultsDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
