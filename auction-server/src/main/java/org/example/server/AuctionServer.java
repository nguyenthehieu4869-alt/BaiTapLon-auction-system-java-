package org.example.server;

import org.example.server.discovery.ServerDiscoveryService;
import org.example.server.handler.ClientHandler;
import org.example.util.Constants;

import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {

    public static void main(String[] args) {

        ServerDiscoveryService discoveryService =
                new ServerDiscoveryService();

        try (ServerSocket server =
                     new ServerSocket(Constants.PORT)) {

            System.out.println(
                    "Server running on port "
                            + Constants.PORT
            );

            Thread discoveryThread =
                    new Thread(discoveryService);

            discoveryThread.start();

            while (true) {

                Socket socket =
                        server.accept();

                System.out.println(
                        "Client connected"
                );

                new ClientHandler(socket)
                        .start();
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            discoveryService.stop();

            System.out.println(
                    "Discovery service stopped"
            );
        }
    }
}