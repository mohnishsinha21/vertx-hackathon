package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

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

                    // Simulate sending data
                    //socket.write(Buffer.buffer("Hello from client " + clientId));

                    // Function to send sequential messages with delay
                    sendSequentialMessages(vertx, socket, clientId);

                    // Handle responses from the server
                    socket.handler(response -> {
                        System.out.println("Client " + clientId + " received: " + response.toString());
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

    private static void sendSequentialMessages(Vertx vertx, NetSocket socket, int clientId) {
        // Send three messages with a delay between each
        for (int j = 1; j <= 3; j++) {
            final int messageId = j;
            vertx.setTimer(2000 * j, id -> {
                String message = "Message " + messageId + " from client " + clientId;
                System.out.println("Sending: " + message);
                socket.write(Buffer.buffer(message));
            });
        }
    }
}
