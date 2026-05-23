package org.example.server.handler;

import org.example.database.ProductDAO;
import org.example.database.UserDAO;
import org.example.database.BidDAO;
import org.example.model.Product;
import org.example.network.dto.BidRequest;
import org.example.network.dto.LoginRequest;
import org.example.network.dto.ProductSaveRequest;
import org.example.network.dto.RegisterRequest;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;
import org.example.server.ServerManager;
import org.example.service.BidResult;
import org.example.service.BidService;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
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

            case REGISTER:
                handleRegister(msg);
                break;

            case GET_PRODUCTS:
                handleGetProducts();
                break;

            case GET_PRODUCTS_BY_SELLER:
                handleGetProductsBySeller(msg);
                break;

            case ADD_PRODUCT:
                handleAddProduct(msg);
                break;

            case EDIT_PRODUCT:
                handleEditProduct(msg);
                break;

            case DELETE_PRODUCT:
                handleDeleteProduct(msg);
                break;

            case CLOSE_AUCTION:
                handleCloseAuction(msg);
                break;

            case PLACE_BID:
                handlePlaceBid(msg);
                break;

            case GET_BID_HISTORY:
                handleGetBidHistory(msg);
                break;

            case GET_WINNER:
                handleGetWinner(msg);
                break;

            default:
                sendError("Không hiểu message type: " + msg.getType());
                break;
        }
    }

    // ✅ 1. LOGIN
    private void handleLogin(Message msg) {
        LoginRequest request = parseData(msg, LoginRequest.class);

        UserDAO dao = new UserDAO();
        boolean result = dao.login(request.getUsername(), request.getPassword());

        Message response = new Message(
                result ? MessageType.LOGIN_SUCCESS : MessageType.LOGIN_FAIL,
                null,
                result,
                result ? "Đăng nhập thành công" : "Sai tài khoản hoặc mật khẩu"
        );

        out.println(Protocol.encode(response));
    }

    private void handleRegister(Message msg) {
        RegisterRequest request = parseData(msg, RegisterRequest.class);

        UserDAO dao = new UserDAO();
        boolean result = dao.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        Message response = new Message(
                result ? MessageType.REGISTER_SUCCESS : MessageType.REGISTER_FAIL,
                null,
                result,
                result ? "Đăng ký thành công" : "Username hoặc email đã tồn tại"
        );

        out.println(Protocol.encode(response));
    }

    // ✅ 2. GET PRODUCTS
    private void handleGetProducts() {

        ProductDAO dao = new ProductDAO();

        List<Product> products = dao.getAllProducts();

        Message response = new Message(
                MessageType.PRODUCT_LIST,
                products,
                true,
                "Load sản phẩm thành công"
        );

        out.println(Protocol.encode(response));
    }

    // ✅ 3. PLACE BID (REALTIME 🔥)
    private void handlePlaceBid(Message msg) {
        BidRequest request = parseData(msg, BidRequest.class);

        BidService service = new BidService();
        BidResult result = service.placeBid(
                request.getProductId(),
                request.getBidderUsername(),
                request.getBidPrice()
        );

        if (!result.isSuccess()) {
            Message response = new Message(
                    MessageType.BID_FAIL,
                    null,
                    false,
                    result.getMessage()
            );

            out.println(Protocol.encode(response));
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("productId", request.getProductId());
        updateData.put("bidderUsername", request.getBidderUsername());
        updateData.put("currentPrice", result.getCurrentPrice());
        updateData.put("endTime", result.getEndTime() == null ? null : result.getEndTime().toString());

        out.println(Protocol.encode(new Message(
                MessageType.BID_SUCCESS,
                updateData,
                true,
                result.getMessage()
        )));

        ServerManager.broadcast(Protocol.encode(new Message(
                MessageType.BID_UPDATE,
                updateData,
                true,
                "Bid updated"
        )));
    }



    private void handleGetProductsBySeller(Message msg) {
        Map data = getDataMap(msg);
        String sellerUsername = getString(data, "sellerUsername");

        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.getProductsBySeller(sellerUsername);

        Message response = new Message(
                MessageType.PRODUCT_LIST,
                products,
                true,
                "Load sản phẩm seller thành công"
        );

        out.println(Protocol.encode(response));
    }

    private void handleAddProduct(Message msg) {
        ProductSaveRequest request = parseData(msg, ProductSaveRequest.class);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(request.getDurationMinutes());

        ProductDAO dao = new ProductDAO();
        boolean result = dao.addProduct(
                request.getName(),
                request.getDescription(),
                request.getImagePath(),
                request.getStartPrice(),
                Timestamp.valueOf(startTime),
                Timestamp.valueOf(endTime),
                request.getSellerUsername()
        );

        Message response = new Message(
                result ? MessageType.ADD_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Thêm sản phẩm thành công" : "Thêm sản phẩm thất bại"
        );

        out.println(Protocol.encode(response));
    }

    private void handleEditProduct(Message msg) {
        ProductSaveRequest request = parseData(msg, ProductSaveRequest.class);

        ProductDAO dao = new ProductDAO();
        boolean result = dao.editProduct(
                request.getId(),
                request.getName(),
                request.getDescription(),
                request.getStartPrice(),
                request.getStatus(),
                request.getImagePath()
        );

        Message response = new Message(
                result ? MessageType.EDIT_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Cập nhật sản phẩm thành công" : "Cập nhật sản phẩm thất bại"
        );

        out.println(Protocol.encode(response));
    }

    private void handleDeleteProduct(Message msg) {
        Map data = getDataMap(msg);
        int productId = getInt(data, "productId");

        ProductDAO dao = new ProductDAO();
        boolean result = dao.deleteProduct(productId);

        Message response = new Message(
                result ? MessageType.DELETE_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Xóa sản phẩm thành công" : "Xóa sản phẩm thất bại"
        );

        out.println(Protocol.encode(response));
    }

    private void handleCloseAuction(Message msg) {
        Map data = getDataMap(msg);
        int productId = getInt(data, "productId");

        ProductDAO dao = new ProductDAO();
        boolean result = dao.closeAuction(productId);

        Message response = new Message(
                result ? MessageType.CLOSE_AUCTION : MessageType.ERROR,
                null,
                result,
                result ? "Đóng phiên thành công" : "Đóng phiên thất bại"
        );

        out.println(Protocol.encode(response));
    }

    private <T> T parseData(Message msg, Class<T> clazz) {
        return Protocol.gson().fromJson(
                Protocol.gson().toJson(msg.getData()),
                clazz
        );
    }

    private String getString(Map data, String key) {
        Object value = data.get(key);
        return value == null ? null : value.toString();
    }

    private void sendError(String message) {
        Message response = new Message(
                MessageType.ERROR,
                null,
                false,
                message
        );

        out.println(Protocol.encode(response));
    }

    private void handleGetBidHistory(Message msg) {
        Map data = getDataMap(msg);
        int productId = getInt(data, "productId");

        BidDAO dao = new BidDAO();

        Message response = new Message(
                MessageType.BID_HISTORY,
                dao.getBidsByProductId(productId),
                true,
                "Load lịch sử bid thành công"
        );

        out.println(Protocol.encode(response));
    }

    private void handleGetWinner(Message msg) {
        Map data = getDataMap(msg);
        int productId = getInt(data, "productId");

        BidDAO dao = new BidDAO();
        String winner = dao.getWinnerUsernameByProductId(productId);

        Message response = new Message(
                MessageType.WINNER_RESULT,
                winner,
                true,
                "Load winner thành công"
        );

        out.println(Protocol.encode(response));
    }

    private Map getDataMap(Message msg) {
        return (Map) msg.getData();
    }

    private int getInt(Map data, String key) {
        return ((Number) data.get(key)).intValue();
    }
}

