package org.example.server.handler;

import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;
import org.example.server.ServerManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            ServerManager.addClient(this);
            System.out.println("Current clients: " + ServerManager.getClientCount());
        } catch (Exception e) {
            e.printStackTrace();
            closeQuietly();
        }
    }

    @Override
    public void run() {
        try {
            String line;
            MessageHandler handler = new MessageHandler(out);

            while ((line = in.readLine()) != null) {
                System.out.println("Message: " + line);

                try {
                    Message msg = Protocol.decode(line);
                    handler.handle(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    send(Protocol.encode(new Message(
                            MessageType.ERROR,
                            null,
                            false,
                            "Message không hợp lệ"
                    )));
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected");
        } finally {
            ServerManager.removeClient(this);
            closeQuietly();
            System.out.println("Current clients: " + ServerManager.getClientCount());
        }
    }

    public synchronized boolean send(String msg) {
        if (out == null) {
            return false;
        }

        out.println(msg);
        return !out.checkError();
    }

    private void closeQuietly() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }
}
