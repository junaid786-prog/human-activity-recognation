package com.example.sensorapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BluetoothHandler {

    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    static final int MESSAGE_READ = 0;
    static final int MESSAGE_WRITE = 1;
    static final int CONNECTING = 2;
    static final int CONNECTED = 3;
    static final int PAIRED_DEVICES = 4;
    static final int NO_SOCKET_FOUND = 5;
    private BluetoothAdapter bluetoothAdapter;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private final Handler handler;
    private final Context context;

    public BluetoothHandler(Context ctx, Handler handler ) {
        this.handler = handler;
        this.context = ctx;
        initBluetooth();
    }

    private void initBluetooth() {
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Your device doesn't support Bluetooth.");
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                showToast("Bluetooth is not enabled.");
            } else {
                if (checkBluetoothConnectPermission()) {
                    // Permissions granted, proceed with your initialization
                    handler.obtainMessage(PAIRED_DEVICES).sendToTarget();
                    startAcceptingConnection();
                }
            }
        }
    }

    public boolean checkBluetoothPermissions() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkBluetoothConnectPermission() {
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.getPackageName());
        return (hasPerm != PackageManager.PERMISSION_GRANTED);
        //return (checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1, 1) == PackageManager.PERMISSION_GRANTED);
    }

    public void requestBluetoothPermissions() {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN},
                REQUEST_ENABLE_BT);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, you can proceed
                // Note: You might want to notify the BluetoothHandler or the activity
                // about the permission result to continue your initialization.
            } else {
                Toast.makeText(context, "Bluetooth permissions not granted. Exiting.", Toast.LENGTH_SHORT).show();
                ((Activity) context).finish();
            }
        }
    }

    public Set<BluetoothDevice> getPairedDevices() {
        if (bluetoothAdapter != null) {
            if (checkBluetoothConnectPermission()) {
                return bluetoothAdapter.getBondedDevices();
            } else {
                showToast("Bluetooth connect permission not granted.");
            }
        } else {
            showToast("Bluetooth adapter not available.");
        }
        return new HashSet<>();
    }


    public void startAcceptingConnection() {
        acceptThread = new AcceptThread();
        acceptThread.start();
        showToast("Accepting");
    }

    public void connectToDevice(BluetoothDevice device) {
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    public void sendDataOverBluetooth(byte[] data) {
        if (connectedThread != null) {
            connectedThread.write(data);
        } else {
            showToast("Thread Not Connected");
        }
    }

    private void showToast(String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public ConnectedThread getConnectedThread(){
        return this.connectedThread;
    }



    class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if(checkBluetoothConnectPermission()) {
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME", MY_UUID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    if (serverSocket != null) socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    handler.obtainMessage(CONNECTED).sendToTarget();
                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                if (checkBluetoothConnectPermission()) {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmSocket = tmp;
        }

        public void run() {
            handler.obtainMessage(CONNECTING).sendToTarget();

            try {
                if(checkBluetoothConnectPermission()) {
                    mmSocket.connect();
                }
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                return;
            }

            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String receivedMessage = new String(buffer, 0, bytes);
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, receivedMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
