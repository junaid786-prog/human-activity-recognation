package com.example.sensorapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.gson.Gson;

import org.json.JSONObject;

import io.socket.client.Socket;

public class MySensor implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor gravity;

    private Socket socket;
    // Constructor
    public MySensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    // Register sensor listeners
    public void registerSensors(Socket socket) {
        this.socket = socket;
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
        SensorData data = new SensorData("", sensor_type, values[0], values[1], values[2]);
        String jsonData = new Gson().toJson(data);
        System.out.println(jsonData);
        if (socket != null && socket.connected()){
            socket.emit("sensor_data", new Gson().toJson(data));
        } else {
            System.out.println("Socket not connected 2");
        }
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            float xAxis = event.values[0];
//            float yAxis = event.values[1];
//            float zAxis = event.values[2];
//            System.out.println(
//                    "x: " + xAxis + ", y: " + yAxis + ", z: " + zAxis
//            );
//        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            float[] gyroscopeValues = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
//            float[] gravityValues = event.values;
//            System.out.println("gyro: " + gravityValues);
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("Accuracy changed: " + accuracy);
        // Handle accuracy changes if needed
    }
}

class SensorData {
    private String device;
    private int type;
    private float x;
    private float y;
    private float z;

    public SensorData(String device, int type, float x, float y, float z) {
        this.device = device;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getter methods for x, y, and z

    @Override
    public String toString() {
        return "{" +
                "type='" + type + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
