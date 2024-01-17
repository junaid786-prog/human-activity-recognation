package com.example.sensorapp;
import java.net.URISyntaxException;

import io.socket.client.IO;
import  io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIO {
   private Socket socket;
   private Emitter.Listener onMessage;
   SocketIO(){
      try {
         System.out.println("Connecting to socket");
         socket = IO.socket("wss://medexto.com");

         onMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
               System.out.println("Message recieved");
            }
         };


      } catch (URISyntaxException e){
         e.printStackTrace();
      }
   }

   public void connect(){
      socket.connect();
      socket.on("new message", this.onMessage);
   }

   public void sendMessage(String event, String message){
      if(socket.connected()){
         socket.emit(event, message);
      }
   }

   public void destroy(){
      socket.disconnect();

      socket.off("new message");
   }
}

