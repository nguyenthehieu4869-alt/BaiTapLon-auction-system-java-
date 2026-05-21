package org.example.server.handler;

import org.example.network.protocol.Message;
import org.example.network.protocol.Protocol;
import org.example.server.ServerManager;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            ServerManager.addClient(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String line;

            while ((line = in.readLine()) != null) {
                System.out.println("📩 " + line);

                Message msg = Protocol.decode(line);

                MessageHandler handler = new MessageHandler(out);
                handler.handle(msg);
            }

        } catch (Exception e) {
            System.out.println("Client disconnected");
        }
    }

    public void send(String msg) {
        out.println(msg);
    }
}
