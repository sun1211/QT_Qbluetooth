package org.qtproject.example.bluetooth;

import org.qtproject.qt5.android.bindings.QtApplication;
import org.qtproject.qt5.android.bindings.QtActivity;
import android.content.Context;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import java.lang.String;
import java.util.Set;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.io.OutputStreamWriter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;


public class Bluetooth extends QtActivity{

    public static Bluetooth m_instance = null;
    private BluetoothAdapter mBluetoothAdapter;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private boolean mStateBuff;

    private BluetoothDevice saveDevice;


    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private static final UUID SerialPortServiceClass_UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int CONNECTION_FAIL = 4;
    public static final int CLOSE_SOCKET_FAIL = 5;

    private boolean mAllowInsecureConnections;

    public void onCreate(Bundle savedInstanceState)
    {
        m_instance = this;
        super.onCreate(savedInstanceState);
        Log.d(QtApplication.QtTAG, "onCreate");
//        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAllowInsecureConnections = true;
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        m_instance.registerReceiver(m_instance.mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        m_instance.registerReceiver(m_instance.mReceiver, filter);
    }

    public synchronized void onResume() {
        super.onResume();
        Log.d(QtApplication.QtTAG, "onResume");
        if (mBluetoothAdapter != null) {
        mAllowInsecureConnections = true;
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();  // Always call the superclass method first
//        Log.d(QtApplication.QtTAG, "onStop");
//    }

     @Override
    public void onDestroy()
    {
        Log.d(QtApplication.QtTAG, "onDestroy");
        super.onDestroy();
        m_instance = null;

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        m_instance.unregisterReceiver(mReceiver);

        if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
        }

        if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
        }

        //setState(STATE_NONE);
        m_instance.mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_NONE, -1).sendToTarget();
    }



    public static void OpenBluetooth(){
        Log.d(QtApplication.QtTAG, "Java OpenBluetooth");
        m_instance.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(m_instance.mBluetoothAdapter == null)
        {
             Log.d(QtApplication.QtTAG, "Bluetooth adapter is not found");
            return;
        }

        if (!m_instance.mBluetoothAdapter.isEnabled()) {
            Log.d(QtApplication.QtTAG, "Bluetooth is off");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            m_instance.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        m_instance.mState = STATE_NONE;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                //m_instance.connectDevice(data, true);
                Log.d(QtApplication.QtTAG, "REQUEST_CONNECT_DEVICE_SECURE");
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                //m_instance.connectDevice(data, false);
                Log.d(QtApplication.QtTAG, "REQUEST_CONNECT_DEVICE_INSECURE");
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                //m_instance.setupChat();
                Log.d(QtApplication.QtTAG, "Bluetooth is now enabled");
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(QtApplication.QtTAG, "User did not enable Bluetooth or an error occurred");
                 //finish();
            }
        }
    }

    // The Handler that gets information back from the BluetoothChatService
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case MESSAGE_STATE_CHANGE:
                 NativeFunctions.onReceiveStateChange(msg.arg1);
                 break;
             case MESSAGE_WRITE:
                 byte[] writeBuf = (byte[]) msg.obj;
                 // construct a string from the buffer
                 String writeMessage = new String(writeBuf);
                 Log.d(QtApplication.QtTAG, "WriteBuf: "+writeMessage);
                 break;
             case MESSAGE_READ:
                 byte[] readBuf = (byte[]) msg.obj;
                 // construct a string from the valid bytes in the buffer
                 String readMessage = new String(readBuf, 0, msg.arg1);

                 String readData ="";
                 for(int i = 0;i < 32; i++)
                 {
                    int j = readBuf[i] & 0xFF;
                    readData += Integer.toString(j)+"-";
                    Log.d(QtApplication.QtTAG, "java - QT :"+j+ "-"+Integer.toHexString(j));
                 }
                 NativeFunctions.onReceiveReadData(readData);
                 break;

             }
         }
     };
/*
     public static void sendChat(String message){
         if (message.length() > 0) {
             // Get the message bytes and tell the BluetoothChatService to write
             byte[] send = message.getBytes();
             ConnectedThread r = m_instance.mConnectedThread;
             r.write(send);

         }

     }

     public static void getVersion(){
         Log.d(QtApplication.QtTAG, "Java get version");
    //send commend byte to get version
     byte[] getVerSionAuto   = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};

     byte[] getVerSionByHand = {0xffffffcd, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00};

         ConnectedThread r = m_instance.mConnectedThread;
         r.write(getVerSionAuto);

     }
     public static void readDataChannel(){
         Log.d(QtApplication.QtTAG, "read data channel");
    //send commend byte to get version

     byte[] readChanHeader = {0xffffffcd, 0x17, 0x00, 0x0c, 0x69, 0x08, 0x00, 0x00,};

     byte[] readChanData = {0x02, 0x00, 0x02, 0x00, 0x40, 0x3b, 0x4a, 0x00, 0xfffffff0, 0x00, 0xfffffff0, 0x01};

         ConnectedThread r = m_instance.mConnectedThread;
         r.write(readChanHeader);
         r.write(readChanData);
     }


*/

    public static void ScanDevice(){

        // If we're already discovering, stop it
        Log.d(QtApplication.QtTAG, "ScanDevice - Java discovering)");
        if (m_instance.mBluetoothAdapter.isDiscovering()) {
            m_instance.mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
         m_instance.mBluetoothAdapter.startDiscovery();
    }

    public static void connectDevice(String address) {
        // Get the BluetoothDevice object
        Log.d(QtApplication.QtTAG, "connectDevice - get bluetoothdevice object");
        BluetoothDevice device = m_instance.mBluetoothAdapter.getRemoteDevice(address);
        m_instance.saveDevice = device;
        Log.d(QtApplication.QtTAG, "quoc");
        // Cancel any thread attempting to make a connection
        if (m_instance.mConnectThread != null) {
            m_instance.mConnectThread.cancel();
            m_instance.mConnectThread = null;
                Log.d(QtApplication.QtTAG, "quoc1");
        }

        // Cancel any thread currently running a connection
        if (m_instance.mConnectedThread != null) {
            m_instance.mConnectedThread.cancel();
            m_instance.mConnectedThread = null;
            Log.d(QtApplication.QtTAG, "quoc2");
        }
        m_instance.mConnectThread = new ConnectThread(device);
        m_instance.mConnectThread.start();
        Log.d(QtApplication.QtTAG, "connectDevices - mconnectThread.start");
        //setState(STATE_CONNECTING);
        m_instance.mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTING, -1).sendToTarget();
    }
    public static void sendDataValue(String data){
        Log.d(QtApplication.QtTAG, "QT - java :" + data);
        String[] parts = data.split("-");

        byte[] sendData = new byte[parts.length];

        for(int i = 0; i <parts.length; i++ ){
        //Log.d(QtApplication.QtTAG, "data java receive1:" + parts[i]);
        byte b = (byte)(Integer.parseInt(parts[i]) & 0xff);
         sendData[i] = b;
       //Log.d(QtApplication.QtTAG, " "+sendData[i]);
        }

        ConnectedThread r = m_instance.mConnectedThread;
        r.write(sendData);
    }

/*
    private void pairDevice(BluetoothDevice device) {
          System.out.println("DeviceListActivity: pair devices ....");
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
          System.out.println("DeviceListActivity: Unpair devices ....");
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
 //--------------------------------------------------------------------
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private static  class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.d(QtApplication.QtTAG, "begin connect Thread");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                if ( m_instance.mAllowInsecureConnections ) {
                        Method method;
                        Log.d(QtApplication.QtTAG, "quoc3");
                        method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                        tmp = (BluetoothSocket) method.invoke(device, 1);
                }
                else {
                        tmp = device.createRfcommSocketToServiceRecord( SerialPortServiceClass_UUID );
                        Log.d(QtApplication.QtTAG, "quoc4");
                }
            } catch (Exception e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            m_instance.mBluetoothAdapter.cancelDiscovery();
            Log.d(QtApplication.QtTAG, "quoc5");

            try {
                // Connect the device through the socket. This will block
               // until it succeeds or throws an exception
                mmSocket.connect();
                Log.d(QtApplication.QtTAG, "quoc6");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.d(QtApplication.QtTAG, "quoc7");
                m_instance.mHandler.obtainMessage(MESSAGE_STATE_CHANGE, CONNECTION_FAIL, -1).sendToTarget();
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    m_instance.mHandler.obtainMessage(MESSAGE_STATE_CHANGE, CLOSE_SOCKET_FAIL, -1).sendToTarget();
                }
                return;
            }
			       // Reset the ConnectThread because we're done
            synchronized (this) {
                m_instance.mConnectThread = null;
            }

            // Do work to manage the connection (in a separate thread)
            m_instance.manageConnectedSocket(mmSocket);

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            Log.d(QtApplication.QtTAG, "quoc8");
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket){
        // Start the thread to manage the connection and perform transmissions
		        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
            Log.d(QtApplication.QtTAG, "quoc9");
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
            Log.d(QtApplication.QtTAG, "quoc10");
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTED, -1).sendToTarget();
    }

    //------------------------------------------------------------------
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            byte[] buffer1 = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            Log.d(QtApplication.QtTAG, "quoc11");
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    if(mStateBuff){

                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    mStateBuff=false;
                    Log.d(QtApplication.QtTAG, " Send the obtained bytes - buffer");

                    for(int i = 0;i < buffer[3]; i++)
                    {
                    Log.d(QtApplication.QtTAG, "data to buffer: "+Integer.toHexString(buffer[i]));
                    }

                }
                else{

                    bytes = mmInStream.read(buffer1);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer1).sendToTarget();
                    mStateBuff =true;
                    Log.d(QtApplication.QtTAG, " Send the obtained bytes - buffer 1");

                    for(int i = 0;i < buffer1[3]; i++)
                    {
                    Log.d(QtApplication.QtTAG, "data to buffer1: "+Integer.toHexString(buffer1[i]));
                    }
                }

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Log.d(QtApplication.QtTAG, String.valueOf(bytes));

            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            Log.d(QtApplication.QtTAG, "quoc12");
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    // call the native method when it receives a new notification
                    String action = intent.getAction();
                    // When discovery finds a device
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        // Add the name and address to an array adapter to show in a ListView
                        Log.d(QtApplication.QtTAG,device.getName()+" "+device.getAddress());
                        NativeFunctions.onReceiveNativeDevice(device.getName()+"\n"+device.getAddress());

                    }
                    // When discovery is finished, change the Activity title
                    else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        Log.d(QtApplication.QtTAG, "ACTION_DISCOVERY_FINISHED");
                        NativeFunctions.onReceiveScanFinised();
                    }
                }
    };
}






