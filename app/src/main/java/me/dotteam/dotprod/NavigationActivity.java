package me.dotteam.dotprod;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import me.dotteam.dotprod.data.Coordinates;
import me.dotteam.dotprod.data.LocationPoints;
import me.dotteam.dotprod.loc.HikeLocationEntity;

public class NavigationActivity extends AppCompatActivity implements LocationListener {
    private String TAG = "NavigationActivity";
    private Button mButtonBackToMainHike;

    private HikeLocationEntity mHLE;

    private float mDistanceTravelled = 0;
    private LocationPoints mLocationPoints;

    // TextViews References
    TextView mTextLatitude;
    TextView mTextLongitude;
    TextView mTextAltitude;
    TextView mTextBearing;
    TextView mTextAccuracy;
    TextView mTextDistanceTraveled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        //mButtonBackToMainHike = (Button) findViewById(R.id.buttonBack);
        mButtonBackToMainHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMainHike = new Intent(NavigationActivity.this, HikeActivity.class);
                startActivity(intentMainHike);
            }
        });

        // Get Reference to HLE and add listener
        mHLE = HikeLocationEntity.getInstance(this);
        mHLE.addListener(this);
        mHLE.startLocationUpdates();

        // New LocationPoints object to save coordinates
        mLocationPoints = new LocationPoints();

        mTextLatitude = (TextView) findViewById(R.id.textLatitude);
        mTextLongitude = (TextView) findViewById(R.id.textLongitude);
        mTextAltitude = (TextView) findViewById(R.id.textAltitude);
        mTextBearing = (TextView) findViewById(R.id.textBearing);
        mTextAccuracy = (TextView) findViewById(R.id.textAccuracy);
        mTextDistanceTraveled = (TextView) findViewById(R.id.textDistanceTraveled);
        mTextDistanceTraveled.setText(String.valueOf(mDistanceTravelled));

    }

    @Override
    public void onLocationChanged(Location location) {

        mTextLatitude.setText(String.valueOf(location.getLatitude()));
        mTextLongitude.setText(String.valueOf(location.getLongitude()));
        mTextAltitude.setText(String.valueOf(location.getAltitude()));
        mTextBearing.setText(String.valueOf(location.getBearing()));
        mTextAccuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.getAccuracy() <= 40) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mLocationPoints.addPoint(new Coordinates((float) location.getLongitude(),
                    (float) location.getLatitude(), (float) location.getAltitude()));

            List<Coordinates> coordinatesList = mLocationPoints.getCoordinateList();
            int numberOfPoints = coordinatesList.size();

            if (numberOfPoints > 1) {
                // Array to store results
                float results[] = new float[3];

                // Get previous and current longitude and latitude
                double prevLongitude = coordinatesList.get(numberOfPoints - 2).getLongitude();
                double prevLatitude = coordinatesList.get(numberOfPoints - 2).getLatitude();
                double currLongitude = coordinatesList.get(numberOfPoints - 1).getLongitude();
                double currLatitude = coordinatesList.get(numberOfPoints - 1).getLatitude();

                // Calculate distance between both points and add it to total
                Location.distanceBetween(prevLatitude, prevLongitude, currLatitude, currLongitude, results);
                mDistanceTravelled += results[0];
                mTextDistanceTraveled.setText(String.valueOf(mDistanceTravelled));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
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
