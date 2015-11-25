package ti.android.ble.sensortag;

import ti.android.util.Point3D;


/**
 * As a last-second hack i'm storing the barometer coefficients in a global.
 */
public enum MagnetometerCalibrationCoefficients {
  INSTANCE;
  public Point3D val = new Point3D(0.0,0.0,0.0);
}
