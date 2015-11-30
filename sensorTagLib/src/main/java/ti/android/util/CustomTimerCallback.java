package ti.android.util;

public abstract class CustomTimerCallback {

  protected abstract void onTimeout();

  protected abstract void onTick(int i);
}
