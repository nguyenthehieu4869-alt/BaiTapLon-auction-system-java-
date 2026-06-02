package com.auction.server.network;

import com.auction.common.AuctionTime;
import com.auction.server.dao.DatabaseManager;
import com.auction.server.network.handler.ClientHandler;
import com.auction.server.util.Constants;

import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {

    public static void main(String[] args) {
        AuctionTime.installAsDefaultTimeZone();

        try {
            DatabaseManager.ensureSchema();
        } catch (Exception e) {
            System.err.println("Không thể cập nhật schema database: " + e.getMessage());
            e.printStackTrace();
            return;
        }

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
