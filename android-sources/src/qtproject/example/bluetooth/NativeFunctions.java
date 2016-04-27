package org.qtproject.example.bluetooth;

import org.qtproject.qt5.android.bindings.QtApplication;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.util.Log;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import java.lang.String;

public class NativeFunctions {
    // declare the native functions
    // these functions will be called by the BroadcastReceiver object
    // when it receives a new notification
    public static native void onReceiveNativeDevice(String deviceName);
    public static native void onReceiveScanFinised();
    public static native void onReceiveChat(String message);
    //add
    public static native void onReceiveVersion(int getVersion);
    public static native void onReceiveStateChange(int state);

    public static native void onReceiveReadData(String readData);

}
