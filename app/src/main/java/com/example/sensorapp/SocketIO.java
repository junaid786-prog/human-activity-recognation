package com.example.sensorapp;

import android.content.Context;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import io.socket.client.Ack;

public class SocketIO {
   Context context;
   private Socket socket;

   SocketIO(Context ctx){
      this.context = ctx;
   }
   public void connect(String uri){
      // 1. connect to socket
      try {
         socket = IO.socket(uri); // "http://192.168.100.172:8000"
      }
      catch (Exception e) {
         System.out.println(e.getMessage());
      }
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
         // showMessageOnToast("Error while connecting server");
      }

   }

   public void stop(){
      if (socket == null) return;
      socket.emit("stop", "i want to stop", new Ack() {
         @Override
         public void call(Object... args) {
            // The callback is invoked when the server acknowledges the event
            System.out.println("Setup event acknowledged by server");
            if (socket != null && socket.connected()) {
               socket.disconnect();
            }
         }
      });
   }

   public void sendMessage(String event, String message){
      if (socket != null && socket.connected()){
         socket.emit(event, message);
      } else {
         System.out.println("Socket not connected 2");
      }
   }

   public void showMessageOnToast(String message){
      //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
      System.out.println("LOG: " + message);
   }
}