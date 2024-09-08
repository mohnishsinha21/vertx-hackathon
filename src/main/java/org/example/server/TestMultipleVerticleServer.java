package org.example.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class TestMultipleVerticleServer extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MultipleVerticleServer(), res -> {
            if (res.succeeded()) {
                System.out.println("MultipleVerticleServer deployed successfully.");
            } else {
                System.out.println("Failed to deploy MultipleVerticleServer: " + res.cause());
                res.cause().printStackTrace();  // Print stack trace for further debugging
            }
        });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            startServer(startPromise);
        } catch (Exception e) {
            e.printStackTrace();
            startPromise.fail(e);
        }
    }

    private void startServer(Promise<Void> startPromise) {
        try {
            NetServer server = vertx.createNetServer();
            System.out.println("Running on thread: " + Thread.currentThread().getName());

            server.connectHandler(socket -> {
                System.out.println("New client connected!");
                vertx.deployVerticle(new ChildVerticle(socket), res -> {
                    if (res.succeeded()) {
                        System.out.println("ChildVerticle deployed successfully.");
                    } else {
                        System.out.println("Failed to deploy ChildVerticle: " + res.cause());
                        res.cause().printStackTrace(); // Print the full stack trace for debugging
                    }
                });
            });

            server.listen(1234, res -> {
                if (res.succeeded()) {
                    System.out.println("Server is listening on port 1234");
                    startPromise.complete();
                } else {
                    System.out.println("Failed to bind server: " + res.cause());
                    res.cause().printStackTrace();
                    startPromise.fail(res.cause());
                }
            });
        } catch (Exception e) {
            System.out.println("Exception during server setup: " + e.getMessage());
            e.printStackTrace(); // Print stack trace to identify exact location of issue
            startPromise.fail(e);
        }
    }
}

class TestChildVerticle extends AbstractVerticle {
    private final NetSocket socket;

    public TestChildVerticle(NetSocket socket) {
        this.socket = socket;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            socket.handler(buffer -> {
                String message = buffer.toString();
                System.out.println("Received message: " + message);
                socket.write("Received: " + message);
            });

            socket.closeHandler(v -> {
                System.out.println("Client disconnected");
            });
            startPromise.complete();
        } catch (Exception e) {
            System.out.println("Exception during ChildVerticle start: " + e.getMessage());
            e.printStackTrace();
            startPromise.fail(e);
        }
    }
}

