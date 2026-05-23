package com.auction.network;

import javafx.application.Platform;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AuctionNetworkClient {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    private static final AuctionNetworkClient INSTANCE = new AuctionNetworkClient();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;

    private final LinkedBlockingQueue<Message> responses = new LinkedBlockingQueue<>();
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

    public synchronized Message sendAndWait(Message message) {
        if (!connect()) {
            return errorMessage("Khong ket noi duoc server. Hay chay AuctionServer truoc.");
        }

        if (out == null) {
            close();
            return errorMessage("Mat ket noi server.");
        }

        out.println(Protocol.encode(message));

        if (out.checkError()) {
            close();
            return errorMessage("Khong gui duoc request den server.");
        }

        try {
            Message response = responses.poll(5, TimeUnit.SECONDS);

            if (response == null) {
                return errorMessage("Server khong phan hoi.");
            }

            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorMessage("Request bi gian doan.");
        } catch (Exception e) {
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
                        responses.offer(message);
                    }
                }
            } catch (Exception e) {
                close();
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
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
    }

    private Message errorMessage(String message) {
        return new Message(
                MessageType.ERROR,
                null,
                false,
                message
        );
    }
}
