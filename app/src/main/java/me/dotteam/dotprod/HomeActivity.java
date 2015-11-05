package me.dotteam.dotprod;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import me.dotteam.dotprod.data.HikeDataDirector;

public class HomeActivity extends AppCompatActivity {

   private Button mButtonStartHike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mButtonStartHike = (Button) findViewById(R.id.buttonStartHike);

        mButtonStartHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMainHike = new Intent(HomeActivity.this, HikeActivity.class);
                startActivity(intentMainHike);
            }
        });
        //Show build information for debugging.
        if (BuildConfig.DEBUG) {
            TextView buildField = (TextView) findViewById(R.id.app_build);
            Date buildDate = new Date(BuildConfig.TIMESTAMP);
            Formatter formatter = new Formatter(new StringBuilder(), Locale.US);
            formatter.format("[ %1$s build ] \nBuild: %2$s\nCommitt: %3$s \n[from %4$s]",
                    BuildConfig.BUILD_TYPE,
                    buildDate.toString(),
                    BuildConfig.GIT_COMMIT_INFO,
                    BuildConfig.GIT_BRANCH);
            buildField.setText(formatter.toString());
        }

        Log.d("Home", "Testing DB");
        HikeDataDirector HDD = HikeDataDirector.getInstance(this);
        HDD.testStorage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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
