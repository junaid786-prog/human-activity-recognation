package com.example.sensorapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class MySocketHelper {
    public Socket socket;
    private OutputStream outputStream;
    private static final String SOCKET_URL = "ws://192.168.100.172:5000";

    public void connect(){
        //new ConnectTask().execute();
        new Thread(new Thread1()).start();
        //new Thread(new Thread3("message")).start();
    }

    private PrintWriter output;
    private BufferedReader input;
    class Thread1 implements Runnable {
        @Override
        public void run() {
            Socket socket;
            try {
                socket = new Socket("192.168.100.172", 5000);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                new Thread(new Thread2()).start();
                String formattedMessage = String.format("{\"event\": \"%s\", \"data\": \"%s\"}", "connect", "Hello");
                new Thread(new Thread3(formattedMessage)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                       System.out.println("not null" + message);
                    } else {
                        new Thread(new Thread1()).start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(message);
            output.flush();
        }
    }


    private class ConnectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Establish a WebSocket connection
                URI uri = new URI(SOCKET_URL);
                WebSocketClient webSocketClient = new WebSocketClient(uri);
                webSocketClient.run();
                // webSocketClient.connect();

                // Send a message to the server
                System.out.println("Sending");
                webSocketClient.sendMessage("connect", "new");

                // Wait for a response
                Thread.sleep(5000); // Adjust the delay based on your requirements

                // Close the WebSocket connection
                webSocketClient.close();
            } catch (URISyntaxException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class WebSocketClient extends Thread {
        private URI uri;
        private Socket socket;
        private OutputStream outputStream;
        private HttpURLConnection connection;

        public WebSocketClient(URI uri) {
            this.uri = uri;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(uri.getHost(), uri.getPort());
                //socket.connect(Socke);
                System.out.println(socket.isConnected());
                outputStream = socket.getOutputStream();
                outputStream.write("connect".getBytes());
                outputStream.flush();
                connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);

                // Read the server response
                //BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                //String line;
                //while ((line = reader.readLine()) != null) {
                    //System.out.println("Received message: " + line);
                //}
                //reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        public void sendMessage(String eventName, String messageData) throws IOException {
            if (socket != null) {
                // Send a message to the server
                OutputStream outputStream = socket.getOutputStream();
                String formattedMessage = String.format("{\"event\": \"%s\", \"data\": \"%s\"}", eventName, messageData);
                outputStream.write(formattedMessage.getBytes());
                outputStream.flush();
                System.out.println("sent");
            } else {
                System.out.println("connection null");
            }
        }

        public void close() {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
