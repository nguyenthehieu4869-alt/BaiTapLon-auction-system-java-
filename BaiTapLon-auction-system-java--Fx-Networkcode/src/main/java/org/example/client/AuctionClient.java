package org.example.client;

import org.example.client.network.ClientListener;

import java.io.PrintWriter;
import java.net.Socket;

public class AuctionClient {

    private Socket socket;
    private PrintWriter out;

    public void connect() {
        try {
            socket = new Socket("localhost", 9999);

            out = new PrintWriter(socket.getOutputStream(), true);

            new ClientListener(socket).start();

            System.out.println("✅ Connected");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        out.println(msg);
    }
}
