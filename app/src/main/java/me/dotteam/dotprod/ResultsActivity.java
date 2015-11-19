package me.dotteam.dotprod;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.SessionData;
import me.dotteam.dotprod.data.SessionEnvData;

public class ResultsActivity extends AppCompatActivity {
    private Button mButtonResultsDone;
    private TextView mDumpSpace;
    private HikeDataDirector mHDD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        mButtonResultsDone = (Button) findViewById(R.id.buttonResultsDone);
        mDumpSpace = (TextView) findViewById(R.id.textView_dumpspace);

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

        StringBuilder dump = new StringBuilder();
        SessionData results = mHDD.getSessionData();

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
