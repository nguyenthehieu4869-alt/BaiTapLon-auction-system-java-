package org.example.server.handler;

import org.example.common.AuctionTime;
import org.example.common.ProductStatus;
import org.example.database.BidDAO;
import org.example.database.ProductDAO;
import org.example.database.UserDAO;
import org.example.model.Product;
import org.example.network.dto.BidRequest;
import org.example.network.dto.LoginRequest;
import org.example.network.dto.ProductSaveRequest;
import org.example.network.dto.RegisterRequest;
import org.example.network.dto.UserProfileDTO;
import org.example.network.protocol.Message;
import org.example.network.protocol.MessageType;
import org.example.network.protocol.Protocol;
import org.example.server.ServerManager;
import org.example.service.BidResult;
import org.example.service.BidService;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandler {

    private final PrintWriter out;

    public MessageHandler(PrintWriter out) {
        this.out = out;
    }

    public void handle(Message msg) {
        try {
            if (msg == null || msg.getType() == null) {
                sendError(msg, "Message không hợp lệ");
                return;
            }

            switch (msg.getType()) {
                case LOGIN:
                    handleLogin(msg);
                    break;

                case REGISTER:
                    handleRegister(msg);
                    break;

                case GET_USER_PROFILE:
                    handleGetUserProfile(msg);
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
                    sendError(msg, "Không hiểu message type: " + msg.getType());
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendError(msg, e.getMessage());
        } catch (IllegalStateException e) {
            sendError(msg, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(msg, "Server xử lý request bị lỗi");
        }
    }

    private void handleLogin(Message msg) {
        LoginRequest request = parseData(msg, LoginRequest.class);

        UserDAO dao = new UserDAO();
        boolean result = dao.login(request.getUsername(), request.getPassword());

        send(msg, new Message(
                result ? MessageType.LOGIN_SUCCESS : MessageType.LOGIN_FAIL,
                null,
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
                request.getPassword()
        );

        send(msg, new Message(
                result ? MessageType.REGISTER_SUCCESS : MessageType.REGISTER_FAIL,
                null,
                result,
                result ? "Đăng ký thành công" : "Username hoặc email đã tồn tại!"
        ));
    }

    private void handleGetUserProfile(Message msg) {
        Map<?, ?> data = getDataMap(msg);
        String username = getString(data, "username");

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Thiếu username");
        }

        UserDAO userDAO = new UserDAO();
        String email = userDAO.getEmailByUsername(username);

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản");
        }

        BidDAO bidDAO = new BidDAO();
        ProductDAO productDAO = new ProductDAO();
        UserProfileDTO profile = new UserProfileDTO(
                username,
                email,
                bidDAO.countWonAuctionsByBidder(username),
                productDAO.countProductsBySeller(username)
        );

        send(msg, new Message(
                MessageType.USER_PROFILE,
                profile,
                true,
                "Load profile thành công"
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
        BidRequest request = parseData(msg, BidRequest.class);

        BidService service = new BidService();
        BidResult result = service.placeBid(
                request.getProductId(),
                request.getBidderUsername(),
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
        updateData.put("productName", result.getProductName());
        updateData.put("bidderUsername", request.getBidderUsername());
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
        Map<?, ?> data = getDataMap(msg);
        String sellerUsername = getString(data, "sellerUsername");

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
        ProductSaveRequest request = parseData(msg, ProductSaveRequest.class);

        LocalDateTime startTime = parseDateTime(request.getStartTime(), "Thiếu thời điểm bắt đầu");
        LocalDateTime endTime = parseDateTime(request.getEndTime(), "Thiếu thời điểm kết thúc");
        LocalDateTime now = AuctionTime.now();

        if (!startTime.isAfter(now)) {
            throw new IllegalArgumentException("Thời điểm bắt đầu phải sau thời điểm hiện tại");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thoi diem ket thuc phai sau thoi diem bat dau");
        }

        if (!endTime.isAfter(now)) {
            throw new IllegalArgumentException("Thời điểm kết thúc phải sau thời điểm hiện tại");
        }

        ProductDAO dao = new ProductDAO();
        boolean result = dao.addProduct(
                request.getName(),
                request.getDescription(),
                request.getImagePath(),
                request.getStartPrice(),
                ProductStatus.COMING_SOON,
                startTime,
                endTime,
                request.getSellerUsername()
        );

        send(msg, new Message(
                result ? MessageType.ADD_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Thêm sản phẩm thành công!" : "Thêm sản phẩm thất bại"
        ));

        if (result) {
            broadcastProductChanged("ADD_PRODUCT", null);
        }
    }

    private LocalDateTime parseDateTime(String value, String missingMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(missingMessage);
        }

        return LocalDateTime.parse(value);
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

        send(msg, new Message(
                result ? MessageType.EDIT_PRODUCT : MessageType.ERROR,
                null,
                result,
                result ? "Cập nhật thành công!" : "Cập nhật sản phẩm thất bại!"
        ));

        if (result) {
            broadcastProductChanged("EDIT_PRODUCT", request.getId());
        }
    }

    private void handleDeleteProduct(Message msg) {
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

        if (result) {
            broadcastProductChanged("DELETE_PRODUCT", productId);
        }
    }

    private void handleCloseAuction(Message msg) {
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        ProductDAO dao = new ProductDAO();
        boolean result = dao.closeAuction(productId);

        send(msg, new Message(
                result ? MessageType.CLOSE_AUCTION : MessageType.ERROR,
                null,
                result,
                result ? "Đóng phiên thành công!" : "Đóng phiên thất bại"
        ));

        if (result) {
            broadcastProductChanged("CLOSE_AUCTION", productId);
        }
    }

    private void broadcastProductChanged(String action, Integer productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", action);

        if (productId != null) {
            data.put("productId", productId);
        }

        ServerManager.broadcast(Protocol.encode(new Message(
                MessageType.PRODUCT_CHANGED,
                data,
                true,
                "Product list changed"
        )));
    }

    private void handleGetBidHistory(Message msg) {
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        BidDAO dao = new BidDAO();

        send(msg, new Message(
                MessageType.BID_HISTORY,
                dao.getBidsByProductId(productId),
                true,
                "Load lịch sử bid thành công!"
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
                "Load winner thành công"
        ));
    }

    private <T> T parseData(Message msg, Class<T> clazz) {
        if (msg.getData() == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu request");
        }

        T value = Protocol.gson().fromJson(
                Protocol.gson().toJson(msg.getData()),
                clazz
        );

        if (value == null) {
            throw new IllegalArgumentException("Dữ liệu request không hợp lệ");
        }

        return value;
    }

    private Map<?, ?> getDataMap(Message msg) {
        if (!(msg.getData() instanceof Map<?, ?> data)) {
            throw new IllegalArgumentException("Dữ liệu request không hợp lệ");
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

        throw new IllegalArgumentException("Thiếu hoặc sai trường: " + key);
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
}
