package org.example.client;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

public class SampleClient {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        int numberOfClients = 10; // Corrected number of clients

        for (int i = 0; i < numberOfClients; ++i) {
            NetClient client = vertx.createNetClient();
            int finalI = i;
            client.connect(1234, "localhost", (res) -> {
                if (res.succeeded()) {
                    System.out.println("Client " + finalI + " connected to server! using socket: " + res.result());
                    NetSocket socket = res.result();
                    sendSequentialMessages(vertx, socket, finalI);

                    // Send a request to get server performance metrics after sending messages
                    vertx.setTimer(8000, id -> {
                        socket.write(Buffer.buffer("GET_METRICS"));
                    });

                    // Handle server responses
                    socket.handler((response) -> {
                        String receivedMessage = response.toString();
                        System.out.println("Client " + finalI + " received: " + receivedMessage);
                    });

                    // Handle disconnect
                    socket.closeHandler((v) -> {
                        System.out.println("Client " + finalI + " disconnected from server");
                    });
                } else {
                    System.out.println("Client " + finalI + " failed to connect: " + res.cause().getMessage());
                }
            });
        }
    }

    private static void sendSequentialMessages(Vertx vertx, NetSocket socket, int clientId) {
        for (int j = 1; j <= 3; ++j) {
            int finalJ = j;
            vertx.setTimer((long)(2000 * j), (id) -> {
                String message = "Message " + finalJ + " from client " + clientId;
                long sendTime = System.currentTimeMillis();  // Log time of sending message
                System.out.println("Sending: " + message + " at " + sendTime);

                // Measure round-trip time
                socket.write(Buffer.buffer(message), writeResult -> {
                    if (writeResult.succeeded()) {
                        long sendDuration = System.currentTimeMillis() - sendTime;
                        System.out.println("Client " + clientId + " sent message: " + message + " - Time taken to send: " + sendDuration + " ms");
                    } else {
                        System.out.println("Failed to send message: " + message);
                    }
                });
            });
        }
    }
}

