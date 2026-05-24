package com.auction.network;

import javafx.application.Platform;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AuctionNetworkClient {
    private static final Properties CONFIG = loadConfig();
    private static final String HOST = getConfig("server.host", "AUCTION_SERVER_HOST", "localhost");
    private static final int PORT = getIntConfig("server.port", "AUCTION_SERVER_PORT", 9999);
    private static final AuctionNetworkClient INSTANCE = new AuctionNetworkClient();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;

    private final ConcurrentMap<String, CompletableFuture<Message>> pendingResponses = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<Message> uncorrelatedResponses = new LinkedBlockingQueue<>();
    private final List<BidUpdateListener> bidUpdateListeners = new CopyOnWriteArrayList<>();

    private AuctionNetworkClient() {
    }

    public static AuctionNetworkClient getInstance() {
        return INSTANCE;
    }

    public synchronized boolean connect() {
        if (isConnected()) {
            return true;
        }

        close();

        try {
            socket = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startListener();
            return true;
        } catch (Exception e) {
            close();
            return false;
        }
    }

    public Message sendAndWait(Message message) {
        if (message == null) {
            return errorMessage("Request khong hop le.");
        }

        String requestId = message.getRequestId();
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
            message.setRequestId(requestId);
        }

        CompletableFuture<Message> future = new CompletableFuture<>();

        synchronized (this) {
            if (!connect()) {
                return errorMessage("Khong ket noi duoc server. Hay chay AuctionServer truoc.");
            }

            if (out == null) {
                close();
                return errorMessage("Mat ket noi server.");
            }

            pendingResponses.put(requestId, future);
            out.println(Protocol.encode(message));

            if (out.checkError()) {
                pendingResponses.remove(requestId);
                close();
                return errorMessage("Khong gui duoc request den server.");
            }
        }

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendingResponses.remove(requestId);
            close();
            return errorMessage("Server khong phan hoi.");
        } catch (InterruptedException e) {
            pendingResponses.remove(requestId);
            Thread.currentThread().interrupt();
            return errorMessage("Request bi gian doan.");
        } catch (Exception e) {
            pendingResponses.remove(requestId);
            close();
            return errorMessage("Loi ket noi server: " + e.getMessage());
        }
    }

    public void addBidUpdateListener(BidUpdateListener listener) {
        if (listener != null) {
            bidUpdateListeners.add(listener);
        }
    }

    public void removeBidUpdateListener(BidUpdateListener listener) {
        bidUpdateListeners.remove(listener);
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;

                while ((line = in.readLine()) != null) {
                    Message message = Protocol.decode(line);

                    if (message.getType() == MessageType.BID_UPDATE) {
                        notifyBidUpdate(message);
                    } else {
                        completePendingResponse(message);
                    }
                }
            } catch (Exception e) {
                close();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void completePendingResponse(Message message) {
        String requestId = message.getRequestId();

        if (requestId != null && !requestId.isBlank()) {
            CompletableFuture<Message> future = pendingResponses.remove(requestId);
            if (future != null) {
                future.complete(message);
                return;
            }
        }

        if (pendingResponses.size() == 1) {
            Map.Entry<String, CompletableFuture<Message>> entry = pendingResponses.entrySet().iterator().next();
            if (pendingResponses.remove(entry.getKey(), entry.getValue())) {
                entry.getValue().complete(message);
                return;
            }
        }

        uncorrelatedResponses.offer(message);
    }

    private void notifyBidUpdate(Message message) {
        Platform.runLater(() -> {
            for (BidUpdateListener listener : bidUpdateListeners) {
                listener.onBidUpdate(message);
            }
        });
    }

    private boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }

        socket = null;
        in = null;
        out = null;
        uncorrelatedResponses.clear();
        completeAllPending(errorMessage("Mat ket noi server."));
    }

    private void completeAllPending(Message message) {
        for (Map.Entry<String, CompletableFuture<Message>> entry : pendingResponses.entrySet()) {
            if (pendingResponses.remove(entry.getKey(), entry.getValue())) {
                entry.getValue().complete(message);
            }
        }
    }

    private Message errorMessage(String message) {
        return new Message(
                MessageType.ERROR,
                null,
                false,
                message
        );
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();

        try (InputStream input = AuctionNetworkClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    private static String getConfig(String propertyKey, String envKey, String defaultValue) {
        String systemValue = System.getProperty(propertyKey);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return CONFIG.getProperty(propertyKey, defaultValue);
    }

    private static int getIntConfig(String propertyKey, String envKey, int defaultValue) {
        String value = getConfig(propertyKey, envKey, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
