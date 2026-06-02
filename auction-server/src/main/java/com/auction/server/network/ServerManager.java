package com.auction.server.network;

import com.auction.server.network.handler.ClientHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerManager {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void addClient(ClientHandler client) {
        if (client != null) {
            clients.add(client);
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void broadcast(String msg) {
        for (ClientHandler client : clients) {
            boolean sent = client.send(msg);

            if (!sent) {
                removeClient(client);
            }
        }
    }

    public static int getClientCount() {
        return clients.size();
    }
}