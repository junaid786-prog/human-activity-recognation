package com.example.sensorapp;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

public class MultiplayerConnect extends AppCompatActivity {

    // Constants
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int MESSAGE_READ = 0;
    private static final int MESSAGE_WRITE = 1;
    private static final int CONNECTING = 2;
    private static final int CONNECTED = 3;
    private static final int NO_SOCKET_FOUND = 4;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> set_pairedDevices;
    private ArrayAdapter<String> adapter_paired_devices;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    // UI Elements
    private ListView lv_paired_devices;
    private Button sendMessageButton;
    private Button stopDataButton;
    private Button connectToSocket;
    private TextView receivedMessageTextView;
    private EditText editTextServerIP;
    private EditText editDeviceName;


    // Sensor Manager
    private MySensorManager sensorManagerHelper;

    private Handler mainHandler;

    // Socket
    private Socket socket;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_connect);

        sensorManagerHelper = new MySensorManager(this);
        // Check Bluetooth permissions
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
        } else {
            init();
        }

        sendMessageButton = findViewById(R.id.send_blutooth_data);
        stopDataButton = findViewById(R.id.stop_send_bt_data);
        receivedMessageTextView = findViewById(R.id.receivedMessageTextView);
        connectToSocket = findViewById(R.id.connect_to_socket_server);
        editTextServerIP = findViewById(R.id.edit_server_ip_input);
        editDeviceName = findViewById(R.id.edit_device_name_input);
        mainHandler = new Handler(Looper.getMainLooper());

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectedThread != null) {
                    sensorManagerHelper.registerSensors(connectedThread, editDeviceName.getText().toString());
                } else {
                    Toast.makeText(MultiplayerConnect.this, "Hello can not send data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sensorManagerHelper != null){
                    sensorManagerHelper.unregisterSensors();
                    if (socket != null){
                        socket.emit("stop", "stop it");
                        socket.close();
                    }
                }
            }
        });
        connectToSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverIP = editTextServerIP.getText().toString();
                //socketHelper.connect();
                // 1. connect to socket
                try {
                    socket = IO.socket(serverIP);// "http://192.168.100.172:8000"
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                System.out.println("Start");
                if (socket != null) {
                    socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Transport transport = (Transport) args[0];
                            // Adding headers when EVENT_REQUEST_HEADERS is called
                            transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    System.out.println("Caught EVENT_REQUEST_HEADERS after EVENT_TRANSPORT, adding headers");
                                    Map<String, List<String>> mHeaders = (Map<String, List<String>>)args[0];
                                    mHeaders.put("Authorization", Arrays.asList("Basic bXl1c2VyOm15cGFzczEyMw=="));
                                }
                            });
                        }
                    });

                    // 2. After socket connected start sending data
                    socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            System.out.println("Socket connected");
                            if (socket != null && socket.connected()) {
                                showMessageOnToast("Server connected successfully");
                            }
                                //sensorManagerHelper.registerSensors(socket);
                        }
                    });

                    socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Exception e = (Exception) args[0];
                            System.out.println("Connection error: " + e.getMessage());
                            showMessageOnToast("Error while connecting server");
                        }
                    });

                    socket.connect();
                }
                else {
                   showMessageOnToast("Error while connecting server");
                }

            }
        });
    }

    private void init(){
        initializeLayout();
        initializeBluetooth();
        startAcceptingConnection();
        initializeClicks();
    }
    private boolean checkBluetoothPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkBluetoothConnectPermission() {
        PackageManager pm = getApplicationContext().getPackageManager();
        int hasPerm = pm.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getApplicationContext().getPackageName());
        return  (hasPerm != PackageManager.PERMISSION_GRANTED);
        //return (checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1, 1) == PackageManager.PERMISSION_GRANTED);
    }
    private void requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                REQUEST_ENABLE_BT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                init();
                //initializeBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth permissions not granted. Exiting.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void startAcceptingConnection() {
        acceptThread = new AcceptThread();
        acceptThread.start();
        Toast.makeText(getApplicationContext(), "Accepting", Toast.LENGTH_SHORT).show();
    }

    private void initializeClicks() {
        lv_paired_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object[] objects = set_pairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) objects[position];

                connectThread = new ConnectThread(device);
                connectThread.start();
            }
        });
    }

    private void initializeLayout() {
        lv_paired_devices = findViewById(R.id.lv_paired_devices);
        adapter_paired_devices = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lv_paired_devices.setAdapter(adapter_paired_devices);
    }

    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Your device doesn't support Bluetooth. You can play as a single player.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Explicitly check for Bluetooth permissions before attempting to enable Bluetooth
            if (checkBluetoothPermissions()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); // TODO: 1. resolve deprecated method
            } else {
                Toast.makeText(this, "Bluetooth permissions not granted. Exiting.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            if(checkBluetoothConnectPermission()){
                System.out.println("Permsission");
                set_pairedDevices = bluetoothAdapter.getBondedDevices();
                if (set_pairedDevices.size() > 0) {
                    for (BluetoothDevice device : set_pairedDevices) {
                        if (device != null && adapter_paired_devices != null) adapter_paired_devices.add(device.getName() + "\n" + device.getAddress());
                    }
                }
            } else {
                System.out.println("Not");
            }
        }
    }

    // Handler for managing messages
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg_type) {
            super.handleMessage(msg_type);

            switch (msg_type.what) {
                case MESSAGE_READ:
                    String receivedMessage = (String) msg_type.obj;
                    // Now you have the received message as a String
                    updateReceivedMessage(receivedMessage);
                    break;

                case MESSAGE_WRITE:
                    if (msg_type.obj != null) {
                        ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket) msg_type.obj);
                        connectedThread.write("bluetooth_message".getBytes());
                    }
                    break;

                case CONNECTED:
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                    break;

                case CONNECTING:
                    Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                    break;

                case NO_SOCKET_FOUND:
                    Toast.makeText(getApplicationContext(), "No socket found", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void sendDataOverBluetooth(){
        if (connectedThread != null) {
            // Convert your message to bytes
            String message = "Hello, World!";
            byte[] messageBytes = message.getBytes();

            // Send the message
            connectedThread.write(messageBytes);
        } else {
            Toast.makeText(getApplicationContext(), "Thread Not Connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessageOnToast(String message){
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    // AcceptThread for handling incoming connections
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (checkBluetoothConnectPermission()) {
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
                    if(serverSocket != null) socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    mHandler.obtainMessage(CONNECTED).sendToTarget();
                    // Do work to manage the connection (in a separate thread)
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

    // ConnectThread for initiating outgoing connections
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                if(checkBluetoothConnectPermission()){
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmSocket = tmp;
        }

        public void run() {
            mHandler.obtainMessage(CONNECTING).sendToTarget();

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

            // Do work to manage the connection (in a separate thread)
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

    // ConnectedThread for managing the connection
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
                    // Read from the InputStream into the buffer
                    bytes = mmInStream.read(buffer);

                    // Convert the buffer to a String
                    String receivedMessage = new String(buffer, 0, bytes);

                    // Send the String message to the main thread
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, receivedMessage).sendToTarget();

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

        public void sendData(String message){
            if (connectedThread != null) {
                // Convert your message to bytes
                byte[] messageBytes = message.getBytes();

                // Send the message
                connectedThread.write(messageBytes);
            } else {
                Toast.makeText(getApplicationContext(), "Thread Not Connected", Toast.LENGTH_SHORT).show();
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

    // Method to update the UI with the received message
    private void updateReceivedMessage(String message) {
        if (socket != null && socket.connected()){
            socket.emit("sensor_data", message);
        } else {
            System.out.println("Socket not connected 2");
        }
        // Update your UI elements with the received message
        // For example, if you have a TextView with the ID "receivedMessageTextView":
        receivedMessageTextView.setText("Received: " + message);
    }
}


// 1. Start bluetooth server to accept connections (AcceptThread) => device as a server => BluetoothServerSocket
// When connection request is received it starts ConnectThread
// 2. ConnectThread establish connection between connecting and remote device => client side => initiate outgoing connection => BluetoothSocket
// After successful connection it starts ConnectedThread to start communication
// 3. ConnectedThread manages actual data transfer
// => manages established connection via AcceptThread or ConnectThread
// => reads from input stream and write to output stream
