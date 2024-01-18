package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import  io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

public class Sensor extends AppCompatActivity {
    Button calculateButton;
    Button stopButton;
    TextView gyroField;
    TextView gravityField;
    Boolean shouldCalculate;
    private MySensor sensorManagerHelper;
    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        //socketHelper = new MySocketHelper();
        sensorManagerHelper = new MySensor(this);

        calculateButton = findViewById(R.id.calculate_sensor_values);
        stopButton = findViewById(R.id.stop_sensor_values);
        gravityField = findViewById(R.id.gravity_sensor_value);
        gyroField = findViewById(R.id.gyro_sensor_value);
        shouldCalculate = false;


        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //socketHelper.connect();
                //sensorManagerHelper.registerSensors();

                try {

                    socket = IO.socket("http://192.168.100.172:8000");

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
                    socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            System.out.println("Socket connected");
                        }
                    });

                    socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Exception e = (Exception) args[0];
                            System.out.println("Connection error: " + e.getMessage());
                        }
                    });

                    socket.connect();

                }

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socket.emit("setup", "hello", new Ack() {
                    @Override
                    public void call(Object... args) {
                        // The callback is invoked when the server acknowledges the event
                        System.out.println("Setup event acknowledged by server");

                        // Now, you can check the connection status
                        System.out.println(socket.connected());

                        // Unregister sensors
                        //sensorManagerHelper.unregisterSensors();
                    }
                });
                //sensorManagerHelper.unregisterSensors();
            }

        });

    }

    @Override
    protected void onDestroy() {
        if(socket != null)
            if (socket.connected()) socket.disconnect();
        super.onDestroy();
    }
}