package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;

public class Sensor extends AppCompatActivity {
    Button calculateButton;
    Button stopButton;
    TextView gyroField;
    TextView gravityField;
    Boolean shouldCalculate;
    private MySensor sensorManagerHelper;
    private  MySocketHelper socketHelper;
    private Socket socket;
    private SocketIO mSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        socketHelper = new MySocketHelper();
        sensorManagerHelper = new MySensor(this);

        calculateButton = findViewById(R.id.calculate_sensor_values);
        stopButton = findViewById(R.id.stop_sensor_values);
        gravityField = findViewById(R.id.gravity_sensor_value);
        gyroField = findViewById(R.id.gyro_sensor_value);
        shouldCalculate = false;

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mSocket = new SocketIO();
                            mSocket.connect();
                            System.out.println("Connected 1");

                            socket = new Socket("192.168.100.172", 5000);
                            System.out.println("Connected");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("Setting");
                                    gravityField.setText("Connected");
                                }
                            });
                        } catch (IOException e){
                            System.out.println(e);
                        }
                    }
                }).start();

                System.out.println("Start");
                //socketHelper.connect();
                //sensorManagerHelper.registerSensors();
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