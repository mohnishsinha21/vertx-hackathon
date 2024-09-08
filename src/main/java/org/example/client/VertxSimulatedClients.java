package org.example.client;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class VertxSimulatedClients {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // Number of simulated clients
        final int numberOfClients = 10;

        for (int i = 0; i < numberOfClients; i++) {
            final int clientId = i;

            NetClient client = vertx.createNetClient();
            client.connect(1234, "localhost", res -> {
                if (res.succeeded()) {
                    System.out.println("Client " + clientId + " connected to server! " + "using socket: "+ res.result());

                    var socket = res.result();

                    // A map to store send time for each client
                    Map<String, Long> sendTimes = new HashMap<>();

                    // Simulate sending data
                    //socket.write(Buffer.buffer("Hello from client " + clientId));

                    // Function to send sequential messages with delay
                    sendSequentialMessages(vertx, socket, clientId, sendTimes);

                    // Handle responses from the server
                    socket.handler(response -> {
                        System.out.println("Raw response from server: " + response.toString());
                        long receivedTime = System.currentTimeMillis();
                        JsonObject jsonResponse = response.toJsonObject();
                        String messageId = response.toJsonObject().getString("messageId");

                        Long sentTime = sendTimes.get(messageId);
                        if(sentTime != null){
                            long actualDelay = receivedTime - sentTime;
                            System.out.println("Client " + clientId + " received: " + response);

                            if(actualDelay < 1000){
                                System.out.println("Timing verified successfully for client " + clientId + ".");
                            } else {
                                System.out.println("Timing verification failed for client " + clientId);
                            }
                        }
                    });

                    // Handle disconnect
                    socket.closeHandler(v -> {
                        System.out.println("Client " + clientId + " disconnected from server");
                    });
                } else {
                    System.out.println("Client " + clientId + " failed to connect: " + res.cause().getMessage());
                }
            });
        }
    }

    private static void sendSequentialMessages(Vertx vertx, NetSocket socket, int clientId, Map<String, Long> sendTimes) {
        // Send three messages with a delay between each
        for (int j = 1; j <= 1; j++) {
            String messageId = UUID.randomUUID().toString();
            vertx.setTimer(2000 * j, id -> {

                Random random = new Random();
                int delay = 2000 + random.nextInt(8000);

                JsonObject message = new JsonObject()
                        .put("messageId", messageId)
                        .put("message", "CSE")
                        .put("from", clientId)
                        .put("millis", delay);

                //String message = "Message " + messageId + " from client " + clientId;
                System.out.println("Sending: " + message);
                sendTimes.put(messageId, System.currentTimeMillis());
                socket.write(Buffer.buffer(message.encode()));
            });
        }
    }
}
