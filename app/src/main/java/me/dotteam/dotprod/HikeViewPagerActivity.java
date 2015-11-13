package me.dotteam.dotprod;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by EricTremblay on 15-11-13.
 */
public class HikeViewPagerActivity extends FragmentActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    // References to Fragments
    private Fragment mHikeFragment;
    private Fragment mEnvCondFragment;
    private Fragment mNavFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);

        mHikeFragment = new HikeFragment();
        mEnvCondFragment = new EnvCondFragment();
        mNavFragment = new NavigationFragment();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        mPager.setCurrentItem(1);
        //super.onBackPressed();
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return mNavFragment;
                }
                case 1: {
                    return mHikeFragment;
                }
                case 2: {
                    return mEnvCondFragment;
                }
                default: {
                    return mHikeFragment;
                }
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
