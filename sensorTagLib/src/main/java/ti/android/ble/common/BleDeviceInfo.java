package ti.android.ble.common;

import android.bluetooth.BluetoothDevice;

public class BleDeviceInfo {
  // Data
  private BluetoothDevice mBtDevice;
  private int mRssi;

  public BleDeviceInfo(BluetoothDevice device, int rssi) {
    mBtDevice = device;
    mRssi = rssi;
  }

  public BluetoothDevice getBluetoothDevice() {
    return mBtDevice;
  }

  public int getRssi() {
    return mRssi;
  }

  public void updateRssi(int rssiValue) {
    mRssi = rssiValue;
  }

}
