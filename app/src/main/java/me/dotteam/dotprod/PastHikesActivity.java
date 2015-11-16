package me.dotteam.dotprod;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import me.dotteam.dotprod.data.Hike;
import me.dotteam.dotprod.data.HikeDataDirector;

public class PastHikesActivity extends AppCompatActivity {

    private HikeDataDirector mHDD;

    ListView pastHikes;

    private void retrieveInterfaceElements(){
        pastHikes = (ListView) findViewById(R.id.listView_pastHikes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_hikes);

        retrieveInterfaceElements();

        mHDD = HikeDataDirector.getInstance(this);

        ArrayAdapter<Hike> listOfHikes = new HikeArrayAdapter(this,mHDD.getAllStoredHikes());
        pastHikes.setAdapter(listOfHikes);
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
