//package com.example.sensorapp;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothServerSocket;
//import android.bluetooth.BluetoothSocket;
//import android.content.Context;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.widget.Toast;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Set;
//import java.util.UUID;
//
//public class BluetoothManager {
//
//    // Constants
//    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    private static final int MESSAGE_READ = 0;
//    private static final int MESSAGE_WRITE = 1;
//    private static final int CONNECTING = 2;
//    private static final int CONNECTED = 3;
//    private static final int NO_SOCKET_FOUND = 4;
//
//    private BluetoothAdapter bluetoothAdapter;
//    private Set<BluetoothDevice> set_pairedDevices;
//
//    private AcceptThread acceptThread;
//    private ConnectThread connectThread;
//    private ConnectedThread connectedThread;
//
//    private Handler mHandler;
//    private Context context;
//
//    public BluetoothManager(Handler handler, Context ctx) {
//        this.context = ctx;
//        this.mHandler = handler;
//        initializeBluetooth();
//        startAcceptingConnection();
//    }
//
//    private void initializeBluetooth() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null) {
//            Toast.makeText(null, "Your device doesn't support Bluetooth.", Toast.LENGTH_SHORT).show();
//            // Handle accordingly or throw an exception
//            return;
//        }
//
//        if (!bluetoothAdapter.isEnabled()) {
//            // Handle Bluetooth not enabled case
//            return;
//        }
//
//        set_pairedDevices = bluetoothAdapter.getBondedDevices();
//    }
//
//    private void startAcceptingConnection() {
//        acceptThread = new AcceptThread();
//        acceptThread.start();
//    }
//
//    public void connectToDevice(BluetoothDevice device) {
//        connectThread = new ConnectThread(device);
//        connectThread.start();
//    }
//
//    public void sendData(String message) {
//        if (connectedThread != null) {
//            byte[] messageBytes = message.getBytes();
//            connectedThread.write(messageBytes);
//        }
//    }
//
//    // Other methods as needed...
//
//    private class AcceptThread extends Thread {
//        private final BluetoothServerSocket serverSocket;
//
//        public AcceptThread() {
//            BluetoothServerSocket tmp = null;
//            try {
//                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME", MY_UUID);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            serverSocket = tmp;
//        }
//
//        public void run() {
//            BluetoothSocket socket = null;
//            while (true) {
//                try {
//                    socket = serverSocket.accept();
//                } catch (IOException e) {
//                    break;
//                }
//
//                if (socket != null) {
//                    mHandler.obtainMessage(CONNECTED).sendToTarget();
//                    connectedThread = new ConnectedThread(socket);
//                    connectedThread.start();
//                }
//            }
//        }
//
//        public void cancel() {
//            try {
//                serverSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private class ConnectThread extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final BluetoothDevice mmDevice;
//
//        public ConnectThread(BluetoothDevice device) {
//            BluetoothSocket tmp = null;
//            mmDevice = device;
//
//            try {
//                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mmSocket = tmp;
//        }
//
//        public void run() {
//            try {
//                mmSocket.connect();
//            } catch (IOException e) {
//                try {
//                    mmSocket.close();
//                } catch (IOException closeException) {
//                    closeException.printStackTrace();
//                }
//                return;
//            }
//
//            mHandler.obtainMessage(CONNECTING).sendToTarget();
//            connectedThread = new ConnectedThread(mmSocket);
//            connectedThread.start();
//        }
//
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private class ConnectedThread extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final InputStream mmInStream;
//        private final OutputStream mmOutStream;
//
//        public ConnectedThread(BluetoothSocket socket) {
//            mmSocket = socket;
//            InputStream tmpIn = null;
//            OutputStream tmpOut = null;
//
//            try {
//                tmpIn = socket.getInputStream();
//                tmpOut = socket.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
//        }
//
//        public void run() {
//            byte[] buffer = new byte[1024];
//            int bytes;
//
//            while (true) {
//                try {
//                    bytes = mmInStream.read(buffer);
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//                } catch (IOException e) {
//                    break;
//                }
//            }
//        }
//
//        public void write(byte[] bytes) {
//            try {
//                mmOutStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
//
