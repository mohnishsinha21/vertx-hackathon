package org.example.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class SingleServerVerticle extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new SingleServerVerticle());
    }

    @Override
    public void start() throws Exception {
        VertxFileTransferServer obj = new VertxFileTransferServer();
        obj.startServer();
        obj.startClient();
    }
}
