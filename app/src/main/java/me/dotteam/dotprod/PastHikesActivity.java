package me.dotteam.dotprod;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.GridView;

import java.util.Collections;
import java.util.List;

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
            Intent targetIntent = new Intent(PastHikesActivity.this,PastStatisticsActivity.class);
            targetIntent.putExtra("id",((Hike) parent.getItemAtPosition(position)).getUniqueID());
            startActivity(targetIntent);
        }
    }

    private class PastHikeLongClickListener implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
            Log.d("PastHikes", "onItemLongClick Called!");
            AlertDialog.Builder builder = new AlertDialog.Builder(PastHikesActivity.this);
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Delete the hike!
                    mHDD.deleteStoredHike((Hike) parent.getItemAtPosition(position));
                    HikeArrayAdapter hikeList = (HikeArrayAdapter) parent.getAdapter();
                    if(hikeList!=null){
                        hikeList.remove((Hike) parent.getItemAtPosition(position));
                        hikeList.notifyDataSetChanged();
                    }
                }
            });

            builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Keep the hike.
                }
            });
            builder.setMessage("This will delete the selected hike. Are you sure you want to continue?");
            builder.setTitle("Delete Hike");
            AlertDialog deleteAlert = builder.create();
            deleteAlert.setCancelable(true);
            deleteAlert.show();
            return true;
        }
    }

    /**
     * Reference to HikeDataDirector
     */
    private HikeDataDirector mHDD;
    private Button mbuttonDone;

    /**
     * Reference to GridView
     */
    GridView pastHikes;
    TextView titleText;

    /**
     * Method to retrieve UI elements and assign them to the associated data member
     */
    private void retrieveInterfaceElements(){
        mbuttonDone = (Button) findViewById(R.id.buttonDone);
        titleText = (TextView) findViewById(R.id.textView_pastHikesTitle);
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

        // Get all saved hikes and reverse the list
        List<Hike> storedHikes = mHDD.getAllStoredHikes();
        if (storedHikes != null && storedHikes.size() > 1) {
            Collections.reverse(storedHikes);
        }

        if(storedHikes!=null) {
            // Create adapter for GridView
            ArrayAdapter<Hike> listOfHikes = new HikeArrayAdapter(this, storedHikes);
            // Assign adapter to GridView
            pastHikes.setAdapter(listOfHikes);
            // Assign onClickListener to GridView
            pastHikes.setOnItemClickListener(new PastHikeOnClickListener());
            pastHikes.setOnItemLongClickListener(new PastHikeLongClickListener());
            Runtime.getRuntime().gc();
            System.gc();
        }
        else{
            titleText.setText(titleText.getText().toString()+"\n No Hikes to Display");
        }

        mbuttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
