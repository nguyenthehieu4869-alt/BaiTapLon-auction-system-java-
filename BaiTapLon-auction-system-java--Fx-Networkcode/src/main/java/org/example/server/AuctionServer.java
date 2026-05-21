package org.example.server;

import org.example.server.handler.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(9999);
            System.out.println("✅ Server running...");

            while (true) {
                Socket socket = server.accept();
                System.out.println("✅ Client connected");

                new ClientHandler(socket).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
