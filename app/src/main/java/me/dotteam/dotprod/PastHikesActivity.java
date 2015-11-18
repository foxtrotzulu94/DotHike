package me.dotteam.dotprod;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.HikeDataDirector;
import me.dotteam.dotprod.data.SessionData;

public class PastHikesActivity extends AppCompatActivity {

    private class PastHikeOnClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Tell the HDD to load the current element
            mHDD.retrieveSessionFromHike((Hike) parent.getItemAtPosition(position));
            //Load the new activity.
            startActivity(new Intent(PastHikesActivity.this,PastStatisticsActivity.class));
        }
    }

    private HikeDataDirector mHDD;

    ListView pastHikes;
    TextView titleText;

    private void retrieveInterfaceElements(){
        pastHikes = (ListView) findViewById(R.id.listView_pastHikes);
        titleText = (TextView) findViewById(R.id.textView_pastHikesTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_hikes);

        retrieveInterfaceElements();

        mHDD = HikeDataDirector.getInstance(this);
        List<Hike> storedHikes = mHDD.getAllStoredHikes();
        if(storedHikes!=null) {
            ArrayAdapter<Hike> listOfHikes = new HikeArrayAdapter(this, storedHikes);
            pastHikes.setAdapter(listOfHikes);
            pastHikes.setOnItemClickListener(new PastHikeOnClickListener());
        }
        else{
            titleText.setText(titleText.getText().toString()+"\n No Hikes to Display");
        }
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
