package com.example.sensorapp;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.gson.Gson;

import org.json.JSONObject;

import io.socket.client.Socket;

public class MySensorManager implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor gravity;
    private String deviceName;
    private Socket socket;
    BluetoothHandler.ConnectedThread connectedThread;
    // Constructor
    public MySensorManager(Context context) {
        //this.connectedThread = connectedThread;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    // Register sensor listeners
    public void registerSensors(BluetoothHandler.ConnectedThread connectedThread, String deviceName) {
        //this.socket = socket;
        this.connectedThread = connectedThread;
        this.deviceName = deviceName;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Unregister sensor listeners
    public void unregisterSensors() {
        sensorManager.unregisterListener(this);
    }

    // Sensor event callback
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle sensor data here
        int sensor_type = event.sensor.getType();

        float[] values = event.values;
        SensorData data = new SensorData(deviceName, sensor_type, values[0], values[1], values[2]);
        String jsonData = new Gson().toJson(data);
        //System.out.println(jsonData);
        if (connectedThread != null){
            //socket.emit("sensor_data", new Gson().toJson(data));
            System.out.println("My sensory data");
            connectedThread.write(jsonData.getBytes());
        } else {
            System.out.println("Socket not connected 2");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("Accuracy changed: " + accuracy);
        // Handle accuracy changes if needed
    }
}


