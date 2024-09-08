package org.example.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

public class MultipleVerticleServer extends AbstractVerticle {
    Vertx vertx;

    public static void main(String[] args) {
        MultipleVerticleServer obj = new MultipleVerticleServer();
        obj.process();
    }

    void process(){
        vertx = Vertx.vertx();
        vertx.deployVerticle(new MultipleVerticleServer(), res -> {
            if (res.succeeded()) {
                System.out.println("MultipleVerticleServer deployed successfully.");
            } else {
                System.out.println("Failed to deploy MultipleVerticleServer: " + res.cause().getMessage());
            }
        });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            startServer();
            startPromise.complete();
        } catch (Exception e) {
            // Log any exception and fail the start promise
            System.out.println("Exception during MultipleVerticleServer start: " + e.getMessage());
            startPromise.fail(e);
        }
    }

    private void startServer() {
        Vertx vertx = Vertx.vertx();
        NetServer server = vertx.createNetServer();
        System.out.println("Running on thread: " + Thread.currentThread().getName());

        server.connectHandler(socket -> {
            System.out.println("New client connected!");
            vertx.deployVerticle(new ChildVerticle(socket), res -> {
                if (res.succeeded()) {
                    System.out.println("ChildVerticle deployed for new connection.");
                } else {
                    System.out.println("Failed to deploy ChildVerticle: " + res.cause().getMessage());
                }
            });
        });

        server.listen(1234, res -> {
            if (res.succeeded()) {
                System.out.println("Server is listening on port 1234");
            } else {
                System.out.println("Failed to bind server: " + res.cause().getMessage());
            }
        });
        System.out.println("Completed server setup.");
    }
}
