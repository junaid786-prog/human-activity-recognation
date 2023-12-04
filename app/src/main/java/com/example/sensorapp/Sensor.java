package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Sensor extends AppCompatActivity {
    Button calculateButton;
    Button stopButton;
    TextView gyroField;
    TextView gravityField;
    Boolean shouldCalculate;
    private MySensor sensorManagerHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        sensorManagerHelper = new MySensor(this);

        calculateButton = findViewById(R.id.calculate_sensor_values);
        stopButton = findViewById(R.id.stop_sensor_values);
        gravityField = findViewById(R.id.gravity_sensor_value);
        gyroField = findViewById(R.id.gyro_sensor_value);
        shouldCalculate = false;

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sensorManagerHelper.registerSensors();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorManagerHelper.unregisterSensors();
            }
        });
    }
}