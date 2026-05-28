package org.example.server.discovery;

import org.example.util.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ServerDiscoveryService implements Runnable {
    private static final String REQUEST = "AUCTION_DISCOVER_REQUEST";
    private static final String RESPONSE_PREFIX = "AUCTION_DISCOVER_RESPONSE:";

    private volatile boolean running = true;

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(Constants.DISCOVERY_PORT)) {
            socket.setBroadcast(true);

            System.out.println("Discovery Service running on UDP port " + Constants.DISCOVERY_PORT);

            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println(
                        "Discovery request from "
                                + packet.getAddress().getHostAddress()
                                + " : "
                                + message
                );

                if (!REQUEST.equals(message)) {
                    continue;
                }

                byte[] responseData =
                        (RESPONSE_PREFIX + Constants.PORT).getBytes(StandardCharsets.UTF_8);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                DatagramPacket responsePacket = new DatagramPacket(
                        responseData,
                        responseData.length,
                        clientAddress,
                        clientPort
                );

                socket.send(responsePacket);

                System.out.println(
                        "Discovery response sent to "
                                + clientAddress.getHostAddress()
                                + ":"
                                + clientPort
                );
            }
        } catch (Exception e) {
            if (running) {
                System.out.println("Discovery Service Error:");
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }
}
