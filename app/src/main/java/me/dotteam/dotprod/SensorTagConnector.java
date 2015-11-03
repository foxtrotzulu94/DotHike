package me.dotteam.dotprod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ti.android.ble.common.BleDeviceInfo;
import ti.android.ble.common.BluetoothLeService;
import ti.android.util.CustomToast;

/**
 * Created by EricTremblay on 15-10-30.
 */
public class SensorTagConnector {

    public interface STConnectorListener {

        void onSensorTagConnect(BluetoothDevice btdevice);

        void onSensorTagDisconnect();
    }
    // Log
    private static final String TAG = "STConnector";

    // URLs
    private static final Uri URL_FORUM = Uri
            .parse("http://e2e.ti.com/support/low_power_rf/default.aspx?DCMP=hpa_hpa_community&HQS=NotApplicable+OT+lprf-forum");
    private static final Uri URL_STHOME = Uri
            .parse("http://www.ti.com/ww/en/wireless_connectivity/sensortag/index.shtml?INTC=SensorTag&HQS=sensortag");

    // Requests to other activities
    private static final int REQ_ENABLE_BT = 0;
    private static final int REQ_DEVICE_ACT = 1;

    // Housekeeping
    private static final int NO_DEVICE = -1;
    private boolean mInitialised = false;

    // BLE management
    private boolean mBleSupported = true;
    private boolean mScanning = false;
    private int mNumDevs = 0;
    private int mConnIndex = NO_DEVICE;
    private List<BleDeviceInfo> mDeviceInfoList;
    private static BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothLeService mBluetoothLeService = null;
    private IntentFilter mFilter;
    private String[] mDeviceFilter = null;

    Context mContext;
    List<STConnectorListener> mListeners;

    public SensorTagConnector(Context context) {
        mContext = context;
        mListeners = new ArrayList<STConnectorListener>();
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, ca.concordia.sensortag.R.string.ble_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to BluetoothAdapter through BluetoothManager.
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBtAdapter == null) {
            Toast.makeText(mContext, ca.concordia.sensortag.R.string.bt_not_supported, Toast.LENGTH_LONG).show();
            mBleSupported = false;
        }

        // Initialize device list container and device filter
        mDeviceInfoList = new ArrayList<BleDeviceInfo>();
        Resources res = mContext.getResources();
        mDeviceFilter = res.getStringArray(ca.concordia.sensortag.R.array.device_filter);

        // Register the BroadcastReceiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);

        if (!mInitialised) {
            // Broadcast receiver
            mContext.registerReceiver(mReceiver, mFilter);

            if (mBtAdapter.isEnabled()) {
                // Start straight away
                startBluetoothLeService();
            }
            else {
                // Request BT adapter to be turned on
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mContext.startActivity(enableIntent);
            }
            mInitialised = true;
        }
    }

    public void addListener(STConnectorListener listener) {
        mListeners.add(listener);
    }

    private boolean scanLeDevice(boolean enable) {
        if (enable) {
            mScanning = mBtAdapter.startLeScan(mLeScanCallback);
        }
        else {
            mScanning = false;
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
        return mScanning;
    }

    List<BleDeviceInfo> getDeviceInfoList() {
        return mDeviceInfoList;
    }

    private boolean checkDeviceFilter(BluetoothDevice device) {
        if(device == null || device.getName() == null) {
            return false;
        }

        String devName = device.getName();
        int n = mDeviceFilter.length;
        if (n > 0) {
            boolean found = false;
            for (int i = 0; i < n && !found; i++) {
                found = devName.equals(mDeviceFilter[i]);
            }
            return found;
        }
        else
            // Allow all devices if the device filter is empty
            return true;
    }

    private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi) {
        BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi);

        return deviceInfo;
    }

    private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
                    .equals(device.getAddress())) {
                return mDeviceInfoList.get(i);
            }
        }
        return null;
    }

    private void addDevice(BleDeviceInfo device) {
        mNumDevs++;
        mDeviceInfoList.add(device);
        mBluetoothDevice = mDeviceInfoList.get(0).getBluetoothDevice();
        Log.d(TAG, "ConnIndex = " + mConnIndex);
        if (mConnIndex == NO_DEVICE) {
            onConnect();
        }
    }

    void onConnect() {
        Log.d(TAG, "Connect Called");
        if (mNumDevs > 0) {
            int connState = mBluetoothManager.getConnectionState(mBluetoothDevice,
                    BluetoothGatt.GATT);

            switch (connState) {
                case BluetoothGatt.STATE_CONNECTED:
                    mBluetoothLeService.disconnect(null);
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    Log.d(TAG, "Connecting Bluetooth Device");
                    boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
                    if (!ok) {
                        //setError("Connect failed");
                    }
                    scanLeDevice(false);
//                    for (int i = 0; i < mListeners.size(); i++) {
//                        mListeners.get(i).onSensorTagConnect(mBluetoothDevice);
//                    }
                    break;
                default:
                    //setError("Device busy (connecting/disconnecting)");
                    break;
            }
        }
    }

    private boolean deviceInfoExists(String address) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }

    private void startBluetoothLeService() {
        boolean f;

        Intent bindIntent = new Intent(mContext, BluetoothLeService.class);
        mContext.startService(bindIntent);
        f = mContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (f) {
            Log.d(TAG, "BluetoothLeService - success");
            scanLeDevice(true);
        }
        else {
            CustomToast.middleBottom(mContext, "Bind to BluetoothLeService failed");
            //finish();
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Broadcasted actions from Bluetooth adapter and BluetoothLeService
    //
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "ACTION_STATE_CHANGED");
                // Bluetooth adapter state change
                switch (mBtAdapter.getState()) {
                    case BluetoothAdapter.STATE_ON:
                        mConnIndex = NO_DEVICE;
                        startBluetoothLeService();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, ca.concordia.sensortag.R.string.app_closing, Toast.LENGTH_LONG).show();
                        //finish();
                        break;
                    default:
                        Log.w(TAG, "Action STATE CHANGED not processed ");
                        break;
                }

            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "ACTION_GATT_CONNECTED");
                // GATT connect
                /*int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
                        BluetoothGatt.GATT_FAILURE);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //startDeviceActivity();
                    }*/
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onSensorTagConnect(mBluetoothDevice);
                }
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "ACTION_GATT_DISCONNECTED");
                // GATT disconnect
                //TODO: What happens when connection is lost?? Restart scanning and reconnect
                scanLeDevice(true);
                for (int i = 0; i < mListeners.size(); i++) {
                    mListeners.get(i).onSensorTagDisconnect();
                }


                    //stopDeviceActivity();
                //if (status == BluetoothGatt.GATT_SUCCESS) {
                //}
                //else {
                    //setError("Disconnect failed. Status: " + status);
                //}
                mConnIndex = NO_DEVICE;
                mBluetoothLeService.close();
            }
            else {
                Log.w(TAG, "Unknown action: " + action);
            }

        }
    };

    // Code to manage Service life cycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize BluetoothLeService");
                //finish();
                return;
            }
            final int n = mBluetoothLeService.numConnectedDevices();
            if (n > 0) {
                Log.d(TAG,"Multiple connections!");
            }
            else {
                Log.i(TAG, "BluetoothLeService connected");
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i(TAG, "BluetoothLeService disconnected");
        }
    };

    // Device scan callback.
    // NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            Log.d(TAG, "Scan Callback is called" + device.toString());
            // Filter devices
            if (checkDeviceFilter(device)) {
                if (!deviceInfoExists(device.getAddress())) {
                    // New device
                    BleDeviceInfo deviceInfo = createDeviceInfo(device, rssi);
                    Log.d(TAG, "New Device" + deviceInfo.toString());
                    addDevice(deviceInfo);
                } else {
                    // Already in list, update RSSI info
                    BleDeviceInfo deviceInfo = findDeviceInfo(device);
                    Log.d(TAG, "ConnIndex = " + mConnIndex);
                    onConnect();
                    if (mConnIndex == NO_DEVICE) {
                        //onConnect();
                    }
                    deviceInfo.updateRssi(rssi);
                }
            }
        }

    };

    BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

}
