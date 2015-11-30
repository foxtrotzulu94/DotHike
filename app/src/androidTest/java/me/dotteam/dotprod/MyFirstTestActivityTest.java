package me.dotteam.dotprod;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by foxtrot on 17/11/15.
 */
public class MyFirstTestActivityTest
        extends ActivityInstrumentationTestCase2<HomeActivity> {

    private HomeActivity mFirstTestActivity;
    private TextView mFirstTestText;
    private LinearLayout mLinearLayoutHike;

    public MyFirstTestActivityTest() {
        super(HomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFirstTestActivity = getActivity();
        mFirstTestText = (TextView) mFirstTestActivity.findViewById(R.id.textAppName);
        mLinearLayoutHike = (LinearLayout) mFirstTestActivity.findViewById(R.id.linearLayoutStartHike);
    }

    public void testCheckPreconditions(){
        assertNotNull("mFirstTestActivity is null", mFirstTestActivity);
        assertNotNull("mFirstTestText is null", mFirstTestText);
    }

    public void testMyFirstTestTextView_labelText() {
        final String expected = mFirstTestActivity.getString(R.string.textAppName);
        final String actual = mFirstTestText.getText().toString();
        assertEquals(expected, actual);
    }

}