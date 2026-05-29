package org.example.server.handler;

import org.example.database.BidDAO;
import org.example.database.ProductDAO;
import org.example.database.UserDAO;
import org.example.model.Product;
import org.example.model.User;
import org.example.model.UserRole;
import org.example.network.dto.BidRequest;
import org.example.network.dto.LoginRequest;
import org.example.network.dto.LoginResponse;
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

    private final PrintWriter out;
    private String currentUsername;
    private UserRole currentRole;

    public MessageHandler(PrintWriter out) {
        this.out = out;
    }

    public void handle(Message msg) {
        try {
            if (msg == null || msg.getType() == null) {
                sendError(msg, "Message khong hop le");
                return;
            }

            switch (msg.getType()) {
                case LOGIN:
                    handleLogin(msg);
                    break;

                case REGISTER:
                    handleRegister(msg);
                    break;

                case GET_PRODUCTS:
                    handleGetProducts(msg);
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
                    sendError(msg, "Khong hieu message type: " + msg.getType());
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendError(msg, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(msg, "Server xu ly request bi loi");
        }
    }

    private void handleLogin(Message msg) {
        LoginRequest request = parseData(msg, LoginRequest.class);

        UserDAO dao = new UserDAO();
        User user = dao.findByCredentials(request.getUsername(), request.getPassword());
        boolean result = user != null;

        if (result) {
            currentUsername = user.getUsername();
            currentRole = user.getRole();
        }

        send(msg, new Message(
                result ? MessageType.LOGIN_SUCCESS : MessageType.LOGIN_FAIL,
                result ? new LoginResponse(user.getUsername(), user.getRole().name()) : null,
                result,
                result ? "Đăng nhập thành công" : "Sai tài khoản hoặc mật khẩu! "
        ));
    }

    private void handleRegister(Message msg) {
        RegisterRequest request = parseData(msg, RegisterRequest.class);

        UserDAO dao = new UserDAO();
        boolean result = dao.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        send(msg, new Message(
                result ? MessageType.REGISTER_SUCCESS : MessageType.REGISTER_FAIL,
                null,
                result,
                result ? "Dang ky thanh cong" : "Username hoặc email đã tồn tại!"
        ));
    }

    private void handleGetProducts(Message msg) {
        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.getAllProducts();

        send(msg, new Message(
                MessageType.PRODUCT_LIST,
                products,
                true,
                "Load sản phẩm thành công"
        ));
    }

    private void handlePlaceBid(Message msg) {
        requireRole(UserRole.BIDDER);

        BidRequest request = parseData(msg, BidRequest.class);
        if (request.getBidderUsername() != null
                && !request.getBidderUsername().isBlank()
                && !currentUsername.equals(request.getBidderUsername())) {
            throw new IllegalArgumentException("Bidder khong khop voi tai khoan dang nhap");
        }

        BidService service = new BidService();
        BidResult result = service.placeBid(
                request.getProductId(),
                currentUsername,
                request.getBidPrice()
        );

        if (!result.isSuccess()) {
            send(msg, new Message(
                    MessageType.BID_FAIL,
                    null,
                    false,
                    result.getMessage()
            ));
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("productId", request.getProductId());
        updateData.put("bidderUsername", currentUsername);
        updateData.put("currentPrice", result.getCurrentPrice());
        updateData.put("endTime", result.getEndTime() == null ? null : result.getEndTime().toString());

        send(msg, new Message(
                MessageType.BID_SUCCESS,
                updateData,
                true,
                result.getMessage()
        ));

        ServerManager.broadcast(Protocol.encode(new Message(
                MessageType.BID_UPDATE,
                updateData,
                true,
                "Bid updated"
        )));
    }

    private void handleGetProductsBySeller(Message msg) {
        requireRole(UserRole.SELLER, UserRole.ADMIN);

        Map<?, ?> data = getDataMap(msg);
        String sellerUsername = getString(data, "sellerUsername");
        if (currentRole == UserRole.SELLER) {
            sellerUsername = currentUsername;
        }

        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.getProductsBySeller(sellerUsername);

        send(msg, new Message(
                MessageType.PRODUCT_LIST,
                products,
                true,
                "Load sản phẩm seller thành công"
        ));
    }

    private void handleAddProduct(Message msg) {
        requireRole(UserRole.SELLER, UserRole.ADMIN);

        ProductSaveRequest request = parseData(msg, ProductSaveRequest.class);

        LocalDateTime startTime = parseDateTime(request.getStartTime(), "Thieu thoi diem bat dau");
        LocalDateTime endTime = parseDateTime(request.getEndTime(), "Thieu thoi diem ket thuc");
        String sellerUsername = currentRole == UserRole.ADMIN ? request.getSellerUsername() : currentUsername;

        if (sellerUsername == null || sellerUsername.isBlank()) {
            sellerUsername = currentUsername;
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thoi diem ket thuc phai sau thoi diem bat dau");
        }

        if (!endTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thoi diem ket thuc phai sau thoi diem hien tai");
        }

        ProductDAO dao = new ProductDAO();
        boolean result = dao.addProduct(
                request.getName(),
                request.getDescription(),
                request.getImagePath(),
                request.getStartPrice(),
                startTime.isAfter(LocalDateTime.now()) ? "OPEN" : "RUNNING",
                Timestamp.valueOf(startTime),
                Timestamp.valueOf(endTime),
                sellerUsername
        );

        send(msg, new Message(
                result ? MessageType.ADD_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Thêm sản phẩm thành công!" : "Thêm sản phẩm thất bại"
        ));
    }

    private LocalDateTime parseDateTime(String value, String missingMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(missingMessage);
        }

        return LocalDateTime.parse(value);
    }

    private void handleEditProduct(Message msg) {
        requireRole(UserRole.SELLER, UserRole.ADMIN);

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

        send(msg, new Message(
                result ? MessageType.EDIT_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Cập nhật thành công!" : "Cập nhật sản phẩm thất bại!"
        ));
    }

    private void handleDeleteProduct(Message msg) {
        requireRole(UserRole.SELLER, UserRole.ADMIN);

        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        ProductDAO dao = new ProductDAO();
        boolean result = dao.deleteProduct(productId);

        send(msg, new Message(
                result ? MessageType.DELETE_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Xoá sản phẩm thành công!" : "Xoá sản phẩm thất bại"
        ));
    }

    private void handleCloseAuction(Message msg) {
        requireRole(UserRole.SELLER, UserRole.ADMIN);

        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        ProductDAO dao = new ProductDAO();
        boolean result = dao.closeAuction(productId);

        send(msg, new Message(
                result ? MessageType.CLOSE_AUCTION : MessageType.ERROR,
                null,
                result,
                result ? "Đóng phiên thành công!" : "Dong phien that bai"
        ));
    }

    private void handleGetBidHistory(Message msg) {
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        BidDAO dao = new BidDAO();

        send(msg, new Message(
                MessageType.BID_HISTORY,
                dao.getBidsByProductId(productId),
                true,
                "Load lịch sửa bid thành công!"
        ));
    }

    private void handleGetWinner(Message msg) {
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        BidDAO dao = new BidDAO();
        String winner = dao.getWinnerUsernameByProductId(productId);

        send(msg, new Message(
                MessageType.WINNER_RESULT,
                winner,
                true,
                "Load winner thanh cong"
        ));
    }

    private <T> T parseData(Message msg, Class<T> clazz) {
        if (msg.getData() == null) {
            throw new IllegalArgumentException("Thieu du lieu request");
        }

        T value = Protocol.gson().fromJson(
                Protocol.gson().toJson(msg.getData()),
                clazz
        );

        if (value == null) {
            throw new IllegalArgumentException("Du lieu request khong hop le");
        }

        return value;
    }

    private Map<?, ?> getDataMap(Message msg) {
        if (!(msg.getData() instanceof Map<?, ?> data)) {
            throw new IllegalArgumentException("Du lieu request khong hop le");
        }

        return data;
    }

    private String getString(Map<?, ?> data, String key) {
        Object value = data.get(key);
        return value == null ? null : value.toString();
    }

    private int getInt(Map<?, ?> data, String key) {
        Object value = data.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }

        throw new IllegalArgumentException("Thieu hoac sai truong: " + key);
    }

    private void sendError(Message request, String message) {
        send(request, new Message(
                MessageType.ERROR,
                null,
                false,
                message
        ));
    }

    private void send(Message request, Message response) {
        if (request != null) {
            response.setRequestId(request.getRequestId());
        }

        if (out != null) {
            out.println(Protocol.encode(response));
        }
    }

    private void requireRole(UserRole... allowedRoles) {
        if (currentUsername == null || currentRole == null) {
            throw new IllegalArgumentException("Vui long dang nhap truoc");
        }

        for (UserRole allowedRole : allowedRoles) {
            if (currentRole == allowedRole) {
                return;
            }
        }

        throw new IllegalArgumentException("Tai khoan khong co quyen thuc hien chuc nang nay");
    }
}
