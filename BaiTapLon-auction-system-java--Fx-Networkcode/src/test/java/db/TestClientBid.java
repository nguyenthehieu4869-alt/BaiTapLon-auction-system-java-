package db;

import org.example.network.dto.BidRequest;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;

import java.io.PrintWriter;
import java.net.Socket;

public class TestClientBid {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 9999);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BidRequest request = new BidRequest(1, "bidder1", 16000000);

            Message msg = new Message(
                    MessageType.PLACE_BID,
                    request,
                    true
            );

            String json = Protocol.encode(msg);
            System.out.println(json);

            out.println(json);
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}