package com.example.sensorapp;

import java.util.UUID;

public class Constants {
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final int REQUEST_ENABLE_BT = 1;
    static final int MESSAGE_READ = 0;
    static final int MESSAGE_WRITE = 1;
    static final int CONNECTING = 2;
    static final int CONNECTED = 3;
    static final int PAIRED_DEVICES = 4;
    static final int NO_SOCKET_FOUND = 5;
    static final int SHOW_LOG = 6;
}
