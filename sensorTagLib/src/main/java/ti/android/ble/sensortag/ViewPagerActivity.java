package ti.android.ble.sensortag;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

public class ViewPagerActivity extends FragmentActivity {
  // Constants
  private static final String TAG = "ViewPagerActivity";

  // GUI
  protected static ViewPagerActivity mThis = null;
  protected SectionsPagerAdapter mSectionsPagerAdapter;
  private ViewPager mViewPager;
  protected int mResourceFragmentPager;
  protected int mResourceIdPager;

  private int mCurrentTab = 0;

  protected ViewPagerActivity() {
    Log.d(TAG, "construct");
    mThis = this;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(mResourceFragmentPager);

    // Set up the action bar
    final ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }
    ImageView view = (ImageView) findViewById(android.R.id.home);
    view.setPadding(10, 0, 20, 10);

    // Set up the ViewPager with the sections adapter.
    mViewPager = (ViewPager) findViewById(mResourceIdPager);
    mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int n) {
        Log.d(TAG, "onPageSelected: " + n);
        actionBar.setSelectedNavigationItem(n);
      }
    });
    // Create the adapter that will return a fragment for each section
    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    // Set up the ViewPager with the sections adapter.
    mViewPager.setAdapter(mSectionsPagerAdapter);
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy");
    mSectionsPagerAdapter = null;
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (mCurrentTab != 0)
      getActionBar().setSelectedNavigationItem(0);
    else
      super.onBackPressed();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, "onOptionsItemSelected");
    // Handle presses on the action bar items
    switch (item.getItemId()) {
    // Respond to the action bar's Up/Home button
    case android.R.id.home:
      onBackPressed();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  protected void openAboutDialog() {
    final Dialog dialog = new AboutDialog(this);
    dialog.show();
  }

  public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragmentList;
    private List<String> mTitles;

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
      mFragmentList = new ArrayList<Fragment>();
      mTitles = new ArrayList<String>();
    }

    public void addSection(Fragment fragment, String title) {
      final ActionBar actionBar = getActionBar();
      mFragmentList.add(fragment);
      mTitles.add(title);
      actionBar.addTab(actionBar.newTab().setText(title).setTabListener(tabListener));
      notifyDataSetChanged();
      Log.d(TAG, "Tab: " + title);
    }

    @Override
    public Fragment getItem(int position) {
      return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
      return mTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (position < getCount()) {
        return mTitles.get(position);
      } else {
        return null;
      }
    }
  }

  // Create a tab listener that is called when the user changes tabs.
  ActionBar.TabListener tabListener = new ActionBar.TabListener() {

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
      int n = tab.getPosition();
      Log.d(TAG, "onTabSelected: " + n);
      mCurrentTab = n;
      mViewPager.setCurrentItem(n);
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
      int n = tab.getPosition();
      Log.d(TAG, "onTabUnselected: " + n);
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
      int n = tab.getPosition();
      Log.d(TAG, "onTabReselected: " + n);
    }
  };
}
