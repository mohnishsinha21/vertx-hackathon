package org.example.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChildVerticle extends AbstractVerticle {

    NetSocket socket;
    public ChildVerticle(NetSocket socket){
        this.socket = socket;
    }


    @Override
    public void start() throws Exception {
        System.out.println("Child Verticle running on thread: " + Thread.currentThread().getName());
        System.out.println("Client connected: " + socket.remoteAddress());

        //Handle incoming data
        socket.handler(buffer -> {
            System.out.println("Received CSE file data: " + buffer.length() + " bytes");

            JsonObject json = buffer.toJsonObject();
            task(json);

            // Echo received data from now
            //socket.write(Buffer.buffer("Server Ack for CSE. Received: " + buffer.length() + " bytes"));

        });

        // Handle client disconnect
        socket.closeHandler(event -> {
            System.out.println("Client disconnected");
        });
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private void task(JsonObject json){
        int millis = json.getInteger("millis");
        String messageId = json.getString("messageId");
        String message = "SSE from server : " + socket.remoteAddress();

        JsonObject jsonObjectForServerEvent = new JsonObject()
        .put("messageId", messageId)
        .put("message" , message);

        scheduler.schedule(()->{
            System.out.println("** Sending SSE to : " + socket.remoteAddress());
            socket.write(Buffer.buffer(jsonObjectForServerEvent.encode()));
        }, millis, TimeUnit.MICROSECONDS);
    }
}
