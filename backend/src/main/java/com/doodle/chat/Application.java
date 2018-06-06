package com.doodle.chat;

import com.doodle.chat.server.Server;
import com.doodle.chat.server.WebSocketHandler;

public class Application {

    private static Server server;

    public static void main(String args[]) throws InterruptedException {

        // final Driver driver = new Driver();
        // driver.connect();

        server = new Server();
        server.start();
    }

    public static void stop() {
        server.stop();
    }
}
