package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.buffer.Buffer;

public class VertxFileTransferClient {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        NetClient client = vertx.createNetClient();

        client.connect(1234, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("Connected to server!");

                var socket = res.result();

                // Simulate file data
                byte[] fileData = "Hello, this is a test file".getBytes();

                // Send file data
                socket.write(Buffer.buffer(fileData));
                //socket.write(Buffer.buffer(fileData));
                // Handle server responses
                socket.handler(response -> {
                    System.out.println("Server response: " + response.toString());
                });

                // Handle disconnect
                socket.closeHandler(v -> {
                    System.out.println("Disconnected from server");
                });
            } else {
                System.out.println("Failed to connect: " + res.cause().getMessage());
            }
        });
    }
}

