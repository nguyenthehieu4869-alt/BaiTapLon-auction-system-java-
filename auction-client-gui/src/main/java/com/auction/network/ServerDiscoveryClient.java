package com.auction.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class ServerDiscoveryClient {

    private static final int DISCOVERY_PORT = 9998;
    private static final int TIMEOUT = 2000;

    private static final String REQUEST =
            "AUCTION_DISCOVER_REQUEST";

    private static final String RESPONSE_PREFIX =
            "AUCTION_DISCOVER_RESPONSE:";

    public static ServerInfo discoverServer() {

        try (DatagramSocket socket =
                     new DatagramSocket()) {

            socket.setBroadcast(true);

            socket.setSoTimeout(TIMEOUT);

            byte[] requestData =
                    REQUEST.getBytes(
                            StandardCharsets.UTF_8
                    );

            DatagramPacket requestPacket =
                    new DatagramPacket(
                            requestData,
                            requestData.length,
                            InetAddress.getByName(
                                    "255.255.255.255"
                            ),
                            DISCOVERY_PORT
                    );

            System.out.println(
                    "Searching for server..."
            );

            socket.send(requestPacket);

            byte[] buffer =
                    new byte[1024];

            DatagramPacket responsePacket =
                    new DatagramPacket(
                            buffer,
                            buffer.length
                    );

            socket.receive(responsePacket);

            String response =
                    new String(
                            responsePacket.getData(),
                            0,
                            responsePacket.getLength(),
                            StandardCharsets.UTF_8
                    );

            if (!response.startsWith(RESPONSE_PREFIX)) {

                System.out.println(
                        "Invalid discovery response"
                );

                return null;
            }

            int port =
                    Integer.parseInt(
                            response.substring(
                                    RESPONSE_PREFIX.length()
                            )
                    );

            String serverIp =
                    responsePacket
                            .getAddress()
                            .getHostAddress();

            System.out.println(
                    "Server found: "
                            + serverIp
                            + ":"
                            + port
            );

            return new ServerInfo(
                    serverIp,
                    port
            );

        } catch (SocketTimeoutException e) {

            System.out.println(
                    "No server found"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    public static class ServerInfo {

        private final String ip;

        private final int port;

        public ServerInfo(
                String ip,
                int port
        ) {

            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {

            return ip + ":" + port;
        }
    }
}