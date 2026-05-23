package org.example.client.network;

import org.example.network.protocol.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

public class ClientListener extends Thread {

    private BufferedReader in;

    public ClientListener(Socket socket) {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String line;

            while ((line = in.readLine()) != null) {

                System.out.println("📥 " + line);

                Message msg = Protocol.decode(line);

                if (msg.getType() == MessageType.BID_UPDATE) {
                    Map data = (Map) msg.getData();

                    System.out.println("🔥 Giá mới: " + data.get("currentPrice"));
                }
            }
        } catch (Exception e) {
            System.out.println("Disconnected");
        }
    }
}