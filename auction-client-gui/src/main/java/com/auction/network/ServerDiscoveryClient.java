package com.auction.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

public class ServerDiscoveryClient {

    private static final String REQUEST =
            "AUCTION_DISCOVER_REQUEST";

    private static final String RESPONSE_PREFIX =
            "AUCTION_DISCOVER_RESPONSE:";

    public static ServerInfo discoverServer(
            int discoveryPort,
            int timeout
    ) {

        try (DatagramSocket socket =
                     new DatagramSocket()) {

            socket.setBroadcast(true);

            socket.setSoTimeout(timeout);

            byte[] requestData =
                    REQUEST.getBytes(
                            StandardCharsets.UTF_8
                    );

            System.out.println(
                    "Searching for server..."
            );

            for (InetAddress address : getBroadcastAddresses()) {
                DatagramPacket requestPacket =
                        new DatagramPacket(
                                requestData,
                                requestData.length,
                                address,
                                discoveryPort
                        );

                socket.send(requestPacket);
            }

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

    private static Set<InetAddress> getBroadcastAddresses()
            throws Exception {

        Set<InetAddress> addresses =
                new LinkedHashSet<>();

        addresses.add(
                InetAddress.getByName(
                        "255.255.255.255"
                )
        );

        Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface =
                    interfaces.nextElement();

            if (!networkInterface.isUp()
                    || networkInterface.isLoopback()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress
                    : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast =
                        interfaceAddress.getBroadcast();

                if (broadcast != null) {
                    addresses.add(broadcast);
                }
            }
        }

        return addresses;
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
