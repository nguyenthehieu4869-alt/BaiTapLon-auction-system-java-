package org.example.server.discovery;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ServerDiscoveryService implements Runnable {

    private static final int DISCOVERY_PORT = 9998;
    private static final String REQUEST = "AUCTION_DISCOVER_REQUEST";
    private static final String RESPONSE = "AUCTION_DISCOVER_RESPONSE:9999";

    private boolean running = true;

    @Override
    public void run() {

        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {

            System.out.println("Discovery Service running on UDP port " + DISCOVERY_PORT);

            byte[] buffer = new byte[1024];

            while (running) {

                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        0,
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println(
                        "Discovery request from "
                                + packet.getAddress().getHostAddress()
                                + " : "
                                + message
                );

                if (REQUEST.equals(message)) {

                    byte[] responseData =
                            RESPONSE.getBytes(StandardCharsets.UTF_8);

                    InetAddress clientAddress =
                            packet.getAddress();

                    int clientPort =
                            packet.getPort();

                    DatagramPacket responsePacket =
                            new DatagramPacket(
                                    responseData,
                                    responseData.length,
                                    clientAddress,
                                    clientPort
                            );

                    socket.send(responsePacket);

                    System.out.println(
                            "Discovery response sent to "
                                    + clientAddress.getHostAddress()
                    );
                }
            }

        } catch (Exception e) {
            System.out.println("Discovery Service Error:");
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}