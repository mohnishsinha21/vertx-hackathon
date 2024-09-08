package org.example.server;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Entry{
    String address;
    NetSocket socket;
}
public class VertxFileTransferServer {
    static List<Entry> entries;
    VertxFileTransferServer(){
        entries = new ArrayList<>();
    }

    public static void main(String[] args) {
        VertxFileTransferServer  obj = new VertxFileTransferServer();
        obj.startServer();
        obj.startClient();
    }

    public void startClient() {
        Thread thread = new Thread(()->{
            while(entries.size() == 0){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            for (Entry entry:entries){
                System.out.println(entry.address + "\t" + entry.socket);
            }

            while(true){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Random random = new Random();
                int index = random.nextInt(entries.size());
                System.out.println("Sending SSE to : " + entries.get(index).address);
                entries.get(index).socket.write("SSE from server\n");
            }
        });
        // Start the thread
        thread.start();
    }

    public void startServer() {
        Vertx vertx = Vertx.vertx();
        NetServer server = vertx.createNetServer();
        System.out.println("Running on thread: " + Thread.currentThread().getName());
        server.connectHandler(socket -> {
            System.out.println("Client connected: " + socket.remoteAddress());
            Entry entry = new Entry();
            entry.address = String.valueOf(socket.remoteAddress());
            entry.socket = socket;
            entries.add(entry);

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
