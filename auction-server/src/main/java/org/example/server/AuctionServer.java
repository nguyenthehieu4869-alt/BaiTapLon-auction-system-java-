package org.example.server;

import org.example.common.AuctionTime;
import org.example.server.handler.ClientHandler;
import org.example.util.Constants;

import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {

    public static void main(String[] args) {
        AuctionTime.installAsDefaultTimeZone();

        try (ServerSocket server = new ServerSocket(Constants.PORT)) {
            System.out.println("Server running on port " + Constants.PORT + " using timezone " + AuctionTime.zone());

            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected");

                new ClientHandler(socket).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
