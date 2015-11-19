package me.dotteam.dotprod;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.HikeDataDirector;

/**
 * Activity which displays list of past hikes and allows the user to select a past hike
 * and view the statistics and data of that hike.
 */
public class PastHikesActivity extends AppCompatActivity {

    /**
     * GridView's onClickListener. Calls intent to activity containing statistics and data of selected hike
     */
    private class PastHikeOnClickListener implements AdapterView.OnItemClickListener{
        private final String TAG = "PastHikeClick";
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick");

            //Tell the HDD to load the current element
            mHDD.retrieveSessionFromHike((Hike) parent.getItemAtPosition(position));

            //Load the new activity.
            startActivity(new Intent(PastHikesActivity.this,PastStatisticsActivity.class));
        }
    }

    /**
     * Reference to HikeDataDirector
     */
    private HikeDataDirector mHDD;

    /**
     * Reference to GridView
     */
    GridView pastHikes;

    /**
     * Method to retrieve UI elements and assign them to the associated data member
     */
    private void retrieveInterfaceElements(){
        pastHikes = (GridView) findViewById(R.id.listView_pastHikes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_hikes);

        // Get UI elements
        retrieveInterfaceElements();

        // Get referenece to HikeDataDirector
        mHDD = HikeDataDirector.getInstance(this);

        // Create adapter for GridView
        ArrayAdapter<Hike> listOfHikes = new HikeArrayAdapter(this,mHDD.getAllStoredHikes());

        // Assign adapter to GridView
        pastHikes.setAdapter(listOfHikes);

        // Assign onClickListener to GridView
        pastHikes.setOnItemClickListener(new PastHikeOnClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_past_hikes, menu);
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
