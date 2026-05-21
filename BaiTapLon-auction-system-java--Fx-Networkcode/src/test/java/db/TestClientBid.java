import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TestClientBid {

    public static void main(String[] args) {

        try {
            // ✅ connect tới server (đổi IP nếu chạy khác máy)
            Socket socket = new Socket("localhost", 9999);

            // ✅ tạo luồng gửi dữ liệu
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // ✅ tạo data BID
            Map<String, Object> data = new HashMap<>();
            data.put("userId", 1);
            data.put("productId", 1);
            data.put("amount", 5000);

            // ✅ tạo message
            Message msg = new Message(
                    MessageType.PLACE_BID,
                    data,
                    true
            );

            // ✅ encode → gửi JSON
            String json = Protocol.encode(msg);

            System.out.println("📤 Gửi lên server:");
            System.out.println(json);

            out.println(json); // gửi đi

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}