package ti.android.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/* This class encapsulates utility functions */
public class CustomToast {

  public static void middleBottom(Context c, String txt) {
    Toast m = Toast.makeText(c, txt, Toast.LENGTH_SHORT);
    m.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 300);
    m.show();
  }

}