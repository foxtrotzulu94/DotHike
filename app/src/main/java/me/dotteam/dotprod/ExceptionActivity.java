package me.dotteam.dotprod;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import me.dotteam.dotprod.data.SessionCollectionService;

public class ExceptionActivity extends AppCompatActivity {

    TextView mStackTraceView;
    Button mButtonReset;
    Button mNotifyButton;

    private void setMemberIDs(){
        mStackTraceView = (TextView) findViewById(R.id.textView_stackTrace);
        mButtonReset = (Button) findViewById(R.id.button_resetapp);
        mNotifyButton = (Button) findViewById(R.id.button_crashnotification);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);

        //Set the member IDs
        setMemberIDs();

        //Dump the stack Trace
        if(getIntent().hasExtra("trace")) {
            mStackTraceView.setText(getIntent().getStringExtra("trace"));
            Log.wtf(".HIKE",getIntent().getStringExtra("trace"));
        }
        //Kill the service (if any)
        stopService(new Intent(this, SessionCollectionService.class));

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Trigger to reboot the app
                Intent restart = new Intent(ExceptionActivity.this, HomeActivity.class);
                restart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(restart);
                finish();
            }
        });

        mNotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send an Email
                Intent sendMail = new Intent(Intent.ACTION_SEND);
                sendMail.setType("message/rfc822");
                sendMail.putExtra(Intent.EXTRA_EMAIL, new String[]{"foxtrotzulu94@gmail.com"});
                sendMail.putExtra(Intent.EXTRA_SUBJECT, String.format("DotProd Crash branch %s on %s",
                        BuildConfig.GIT_BRANCH,
                        new Date(System.currentTimeMillis()).toString()));
                sendMail.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra("trace"));
                try {
                    startActivity(Intent.createChooser(sendMail, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ExceptionActivity.this,
                            "There are no email clients installed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exception, menu);
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
