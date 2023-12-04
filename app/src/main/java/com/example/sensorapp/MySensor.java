package com.example.sensorapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MySensor implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    // Constructor
    public MySensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        // Get the accelerometer and gyroscope sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    // Register sensor listeners
    public void registerSensors() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Unregister sensor listeners
    public void unregisterSensors() {
        sensorManager.unregisterListener(this);
    }

    // Sensor event callback
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle sensor data here
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] accelerometerValues = event.values;
            System.out.println("acc: " + accelerometerValues);
            // Process accelerometer data
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float[] gyroscopeValues = event.values;
            System.out.println("gyro: " + gyroscopeValues);
            // Process gyroscope data
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float[] gravityValues = event.values;
            System.out.println("gyro: " + gravityValues);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        System.out.println("Accuracy changed: " + accuracy);
        // Handle accuracy changes if needed
    }
}