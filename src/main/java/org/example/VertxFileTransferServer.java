package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.buffer.Buffer;

public class VertxFileTransferServer {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        NetServer server = vertx.createNetServer();

        server.connectHandler(socket -> {
            System.out.println("Client connected: " + socket.remoteAddress());

            // Handle incoming data
            socket.handler(buffer -> {
                System.out.println("Received file data: " + buffer.length() + " bytes");
                // Echo received data for now
                socket.write(Buffer.buffer("Received: " + buffer.length() + " bytes"));
            });

            // Handle client disconnect
            socket.closeHandler(v -> {
                System.out.println("Client disconnected");
            });
        });

        server.listen(1234, res -> {
            if (res.succeeded()) {
                System.out.println("Server is listening on port 1234");
            } else {
                System.out.println("Failed to bind!");
            }
        });
    }
}
