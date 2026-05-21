import org.example.client.AuctionClient;
import org.example.network.protocol.*;

import java.util.HashMap;
import java.util.Map;

public class TestClient {

    public static void main(String[] args) {

        AuctionClient client = new AuctionClient();
        client.connect();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1);
        data.put("productId", 1);
        data.put("amount", 5000);

        Message msg = new Message(
                MessageType.PLACE_BID,
                data,
                true
        );

        String json = Protocol.encode(msg);

        client.send(json);
    }
}
