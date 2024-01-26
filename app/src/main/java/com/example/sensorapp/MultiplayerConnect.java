package com.example.sensorapp;

import android.bluetooth.BluetoothDevice;
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

import java.util.Set;

public class MultiplayerConnect extends AppCompatActivity {

    // UI Elements
    private ListView lv_paired_devices;
    private Button sendMessageButton;
    private Button stopDataButton;
    private Button connectToSocket;
    private TextView receivedMessageTextView;
    private EditText editTextServerIP;
    private EditText editDeviceName;

    // Bluetooth
    private BluetoothHandler bluetoothHandler;
    // Socket
    private SocketIO socketHandler;
    // Sensor
    private MySensorManager sensorManagerHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_connect);

        socketHandler = new SocketIO(this);
        sensorManagerHelper = new MySensorManager(this);
        initializeLayout();
        initializeBluetooth();
        initializeClicks();
    }

    private void initializeLayout() {
        lv_paired_devices = findViewById(R.id.lv_paired_devices);
        sendMessageButton = findViewById(R.id.send_blutooth_data);
        stopDataButton = findViewById(R.id.stop_send_bt_data);
        receivedMessageTextView = findViewById(R.id.receivedMessageTextView);
        connectToSocket = findViewById(R.id.connect_to_socket_server);
        editTextServerIP = findViewById(R.id.edit_server_ip_input);
        editDeviceName = findViewById(R.id.edit_device_name_input);
    }

    private void initializeBluetooth() {
        bluetoothHandler = new BluetoothHandler(this, new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg_type) {
                switch (msg_type.what) {
                    case BluetoothHandler.MESSAGE_READ:
                        String receivedMessage = (String) msg_type.obj;
                        socketHandler.sendMessage("sensor_data", receivedMessage);
                        updateReceivedMessage(receivedMessage);
                        break;
                    case BluetoothHandler.CONNECTED:
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothHandler.CONNECTING:
                        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothHandler.NO_SOCKET_FOUND:
                        Toast.makeText(getApplicationContext(), "No socket found", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothHandler.PAIRED_DEVICES:
                        // Update your UI with paired devices
                        updatePairedDevices();
                        break;
                }
            }
        });

        // Check and request Bluetooth permissions
        if (!bluetoothHandler.checkBluetoothPermissions()) {
            bluetoothHandler.requestBluetoothPermissions();
        } else {
            // Permissions already granted, proceed with your initialization
            bluetoothHandler.startAcceptingConnection();
        }
    }

    private void initializeClicks() {
        lv_paired_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Set<BluetoothDevice> pairedDevices = bluetoothHandler.getPairedDevices();
                Object[] objects = pairedDevices.toArray();
                BluetoothDevice device = (BluetoothDevice) objects[position];
                bluetoothHandler.connectToDevice(device);
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothHandler.ConnectedThread connectedThread = bluetoothHandler.getConnectedThread();
                if (connectedThread != null) {
                    sensorManagerHelper.registerSensors(connectedThread, editDeviceName.getText().toString());
                } else {
                    Toast.makeText(MultiplayerConnect.this, "Cannot send data. Not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sensorManagerHelper != null){
                    sensorManagerHelper.unregisterSensors();
                    socketHandler.stop();
                }
            }
        });

        connectToSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverIP = editTextServerIP.getText().toString();
                try {
                    socketHandler.connect(serverIP);
                } catch (Exception e){
                    System.out.println("Error occurred: " + e.getMessage());
                }
            }
        });
    }

    // Method to update the UI with the received message
    private void updateReceivedMessage(String message) {
        receivedMessageTextView.setText("Received: " + message);
    }
    private void updatePairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothHandler.getPairedDevices();
        ArrayAdapter<String> adapter_paired_devices = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        for (BluetoothDevice device : pairedDevices) {
            adapter_paired_devices.add(device.getName() + "\n" + device.getAddress());
        }

        lv_paired_devices.setAdapter(adapter_paired_devices);
    }
}
