package org.example.server;

import org.example.server.handler.ClientHandler;

import java.util.ArrayList;
import java.util.List;

public class ServerManager {

    public static List<ClientHandler> clients = new ArrayList<>();

    public static void addClient(ClientHandler client) {
        clients.add(client);
    }

    public static void broadcast(String msg) {
        for (ClientHandler c : clients) {
            c.send(msg);
        }
    }
}
