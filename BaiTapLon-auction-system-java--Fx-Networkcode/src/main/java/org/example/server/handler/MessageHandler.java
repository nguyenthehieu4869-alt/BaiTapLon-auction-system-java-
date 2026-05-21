package org.example.server.handler;

import org.example.database.BidDAO;
import org.example.database.ProductDAO;
import org.example.database.UserDAO;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;
import org.example.server.ServerManager;
import org.example.util.Logger;

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
                Logger.error("Không hiểu message");
        }
    }

    // ✅ 1. LOGIN
    private void handleLogin(Message msg) {

        Map data = (Map) msg.getData();

        String username = (String) data.get("username");
        String password = (String) data.get("password");

        Logger.info("Login: " + username);

        UserDAO dao = new UserDAO();

        boolean result = dao.login(username, password);

        Message response = new Message(
                result ? MessageType.LOGIN_SUCCESS : MessageType.LOGIN_FAIL,
                result ? "Login thành công" : "Sai tài khoản hoặc mật khẩu",
                result
        );

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

        int userId = ((Number) data.get("userId")).intValue();
        int productId = ((Number) data.get("productId")).intValue();
        double amount = ((Number) data.get("amount")).doubleValue();

        BidDAO dao = new BidDAO();

        boolean result = dao.placeBid(userId, productId, amount);

        if (result) {

            Logger.info("Bid thành công: " + amount);

            // ✅ gửi realtime cho tất cả client
            Message broadcastMsg = new Message(
                    MessageType.BID_UPDATE,
                    data,   // giữ nguyên data (OK)
                    true
            );

            ServerManager.broadcast(Protocol.encode(broadcastMsg));

        } else {

            Logger.error("Bid thất bại");

            Message response = new Message(
                    MessageType.BID_FAIL,   // ✅ SỬA (trước bạn dùng BID_SUCCESS sai logic)
                    "Đặt giá thất bại",
                    false
            );

            out.println(Protocol.encode(response));
        }
    }
}

