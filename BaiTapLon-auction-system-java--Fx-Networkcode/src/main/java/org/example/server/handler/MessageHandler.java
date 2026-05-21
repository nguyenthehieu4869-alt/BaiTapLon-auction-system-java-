package org.example.server.handler;

import org.example.database.BidDAO;
import org.example.database.ProductDAO;
import org.example.database.UserDAO;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;
import org.example.server.ServerManager;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class MessageHandler {

    private PrintWriter out;

    public MessageHandler(PrintWriter out) {
        this.out = out;
    }

    public void handle(Message msg) {

        switch (msg.getType()) {

            case LOGIN:
                handleLogin(msg);
                break;

            case GET_PRODUCTS:
                handleGetProducts();
                break;

            case PLACE_BID:
                handlePlaceBid(msg);
                break;

            default:
                System.out.println("❌ Không hiểu message");
        }
    }

    // ✅ 1. LOGIN
    private void handleLogin(Message msg) {

        Map data = (Map) msg.getData();

        String username = (String) data.get("username");
        String password = (String) data.get("password");

        System.out.println("👤 Login: " + username);

        UserDAO dao = new UserDAO();

        boolean result = dao.login(username, password);

        Message response;

        if (result) {
            response = new Message(
                    MessageType.LOGIN_SUCCESS,
                    "Login thành công",
                    true
            );
        } else {
            response = new Message(
                    MessageType.LOGIN_FAIL,
                    "Sai tài khoản hoặc mật khẩu",
                    false
            );
        }

        out.println(Protocol.encode(response));
    }

    // ✅ 2. GET PRODUCTS
    private void handleGetProducts() {

        ProductDAO dao = new ProductDAO();

        List<String> products = dao.getAllProducts();

        Message response = new Message(
                MessageType.PRODUCT_LIST,
                products,
                true
        );

        out.println(Protocol.encode(response));
    }

    // ✅ 3. PLACE BID (REALTIME 🔥)
    private void handlePlaceBid(Message msg) {

        Map data = (Map) msg.getData();

        // ⚠ Gson trả về Double → phải convert
        int userId = Double.valueOf(data.get("userId").toString()).intValue();
        int productId = Double.valueOf(data.get("productId").toString()).intValue();
        double amount = Double.valueOf(data.get("amount").toString());

        BidDAO dao = new BidDAO();

        boolean result = dao.placeBid(userId, productId, amount);

        if (result) {

            System.out.println("✅ Bid thành công: " + amount);

            // ✅ gửi cho tất cả client (REALTIME)
            Message broadcastMsg = new Message(
                    MessageType.BID_UPDATE,
                    data,
                    true
            );

            ServerManager.broadcast(Protocol.encode(broadcastMsg));

        } else {

            Message response = new Message(
                    MessageType.BID_SUCCESS,
                    "Đặt giá thất bại",
                    false
            );

            out.println(Protocol.encode(response));
        }
    }
}

