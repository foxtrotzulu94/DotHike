package ti.android.util;

import java.util.Timer;
import java.util.TimerTask;

import android.widget.ProgressBar;

public class CustomTimer {
  private Timer mTimer;
  private CustomTimerCallback mCb = null;
  private ProgressBar mProgressBar;
  private int mTimeout;

  public CustomTimer(ProgressBar progressBar, int timeout, CustomTimerCallback cb) {
    mTimeout = timeout;
    mProgressBar = progressBar;
    mTimer = new Timer();
    ProgressTask t = new ProgressTask();
    mTimer.schedule(t, 0, 1000); // One second tick
    mCb = cb;
  }

  public void stop() {
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
  }

  private class ProgressTask extends TimerTask {
    int i = 0;

    @Override
    public void run() {
      i++;
      if (mProgressBar != null)
        mProgressBar.setProgress(i);
      if (i >= mTimeout) {
        mTimer.cancel();
        mTimer = null;
        if (mCb != null)
          mCb.onTimeout();
      } else {
        if (mCb != null)
          mCb.onTick(i);
      }
    }
  }
}
