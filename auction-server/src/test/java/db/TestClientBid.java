package db;

import org.example.network.dto.BidRequest;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;
import org.example.util.Constants;

import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class TestClientBid {
    public static void main(String[] args) {
        try (Socket socket = new Socket(Constants.HOST, Constants.PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            BidRequest request = new BidRequest(1, "bidder1", 16000000);

            Message msg = new Message(
                    MessageType.PLACE_BID,
                    request,
                    true
            );

            String json = Protocol.encode(msg);
            System.out.println(json);

            out.println(json);

        } catch (ConnectException e) {
            System.err.println("Khong ket noi duoc server tai " + Constants.HOST + ":" + Constants.PORT);
            System.err.println("Hay chay org.example.server.AuctionServer truoc, roi moi chay TestClientBid.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
