package com.auction.server.network.handler;

import com.auction.common.AuctionTime;
import com.auction.common.ProductImageData;
import com.auction.common.ProductStatus;
import com.auction.common.UserRole;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.ProductDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.model.Product;
import com.auction.common.network.dto.BidRequest;
import com.auction.common.network.dto.LoginRequest;
import com.auction.common.network.dto.LoginResponse;
import com.auction.common.network.dto.ProductSaveRequest;
import com.auction.common.network.dto.RegisterRequest;
import com.auction.common.network.dto.UserProfileDTO;
import com.auction.common.network.protocol.Message;
import com.auction.common.network.protocol.MessageType;
import com.auction.common.network.protocol.Protocol;
import com.auction.server.network.ServerManager;
import com.auction.server.service.AccountAuthorization;
import com.auction.server.service.BidResult;
import com.auction.server.service.BidService;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandler {

    private final PrintWriter out;
    private String authenticatedUsername;
    private UserRole authenticatedRole;

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
                case UPDATE_WALLET:
                    handleUpdateWallet(msg);
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
        UserRole role = dao.login(request.getUsername(), request.getPassword());

        if (role == UserRole.ADMIN
                && !AccountAuthorization.isAuthorizedAdmin(request.getUsername(), request.getPassword())) {
            role = null;
        }

        boolean result = role != null;

        authenticatedUsername = result ? request.getUsername() : null;
        authenticatedRole = role;

        send(msg, new Message(
                result ? MessageType.LOGIN_SUCCESS : MessageType.LOGIN_FAIL,
                result ? new LoginResponse(authenticatedUsername, authenticatedRole) : null,
                result,
                result ? "Đăng nhập thành công" : "Sai tài khoản hoặc mật khẩu! "
        ));
    }

    private void handleRegister(Message msg) {
        RegisterRequest request = parseData(msg, RegisterRequest.class);
        String username = requireText(request.getUsername(), "Thiếu username");
        String email = requireText(request.getEmail(), "Thiếu email");
        String password = requireText(request.getPassword(), "Thiếu password");

        AccountAuthorization.validateRegistration(username, password, request.getRole());

        UserDAO dao = new UserDAO();
        boolean result = dao.register(username, email, password, request.getRole());

        send(msg, new Message(
                result ? MessageType.REGISTER_SUCCESS : MessageType.REGISTER_FAIL,
                null,
                result,
                result ? "Đăng ký thành công" : "Username hoặc email đã tồn tại!"
        ));
    }

    private void handleGetUserProfile(Message msg) {
        requireAuthenticated();
        Map<?, ?> data = getDataMap(msg);
        String username = getString(data, "username");

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Thiếu username");
        }

        username = authenticatedUsername;

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
                productDAO.countProductsBySeller(username),
                userDAO.getWalletBalance(username)
        );

        send(msg, new Message(MessageType.USER_PROFILE, profile, true, "Load profile thành công"));
    }

    private void handleUpdateWallet(Message msg) {
        requireRole(UserRole.BIDDER);
        Map<?, ?> data = getDataMap(msg);
        double amount = getDouble(data, "amount");

        BidService bidService = new BidService();
        boolean success = bidService.addWalletBalance(authenticatedUsername, amount);

        send(msg, new Message(
                success ? MessageType.UPDATE_WALLET_SUCCESS : MessageType.UPDATE_WALLET_FAIL,
                null,
                success,
                success ? "Nạp tiền vào ví thành công" : "Nạp tiền vào ví thất bại"
        ));
    }

    private void handleGetProducts(Message msg) {
        requireAuthenticated();
        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.getAllProducts();

        send(msg, new Message(MessageType.PRODUCT_LIST, products, true, "Load sản phẩm thành công"));
    }

    private void handlePlaceBid(Message msg) {
        requireRole(UserRole.BIDDER);
        BidRequest request = parseData(msg, BidRequest.class);

        BidService service = new BidService();
        BidResult result = service.placeBid(request.getProductId(), authenticatedUsername, request.getBidPrice());

        if (!result.isSuccess()) {
            send(msg, new Message(MessageType.BID_FAIL, null, false, result.getMessage()));
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("productId", request.getProductId());
        updateData.put("productName", result.getProductName());
        updateData.put("bidderUsername", authenticatedUsername);
        updateData.put("currentPrice", result.getCurrentPrice());
        updateData.put("endTime", result.getEndTime() == null ? null : result.getEndTime().toString());

        send(msg, new Message(MessageType.BID_SUCCESS, updateData, true, result.getMessage()));

        ServerManager.broadcast(Protocol.encode(new Message(
                MessageType.BID_UPDATE,
                updateData,
                true,
                "Bid updated"
        )));
    }

    private void handleGetProductsBySeller(Message msg) {
        requireRole(UserRole.SELLER);
        Map<?, ?> data = getDataMap(msg);
        String sellerUsername = getString(data, "sellerUsername");
        sellerUsername = authenticatedUsername;

        ProductDAO dao = new ProductDAO();
        List<Product> products = dao.getProductsBySeller(sellerUsername);

        send(msg, new Message(MessageType.PRODUCT_LIST, products, true, "Load sản phẩm seller thành công"));
    }

    private void handleAddProduct(Message msg) {
        requireRole(UserRole.SELLER);
        ProductSaveRequest request = parseData(msg, ProductSaveRequest.class);

        LocalDateTime startTime = parseDateTime(request.getStartTime(), "Thiếu thời điểm bắt đầu");
        LocalDateTime endTime = parseDateTime(request.getEndTime(), "Thiếu thời điểm kết thúc");
        LocalDateTime now = AuctionTime.now();

        if (!startTime.isAfter(now)) {
            throw new IllegalArgumentException("Thời điểm bắt đầu phải sau thời điểm hiện tại");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thời điểm kết thúc phải sau thời điểm bắt đầu");
        }

        if (!endTime.isAfter(now)) {
            throw new IllegalArgumentException("Thời điểm kết thúc phải sau thời điểm hiện tại");
        }

        ProductDAO dao = new ProductDAO();
        String imageReference = validateProductImageReference(request.getImagePath(), false);
        boolean result = dao.addProduct(request.getName(), request.getDescription(), imageReference, request.getStartPrice(), ProductStatus.COMING_SOON, startTime, endTime, authenticatedUsername);

        send(msg, new Message(result ? MessageType.ADD_PRODUCT : MessageType.ERROR, null, result, result ? "Thêm sản phẩm thành công!" : "Thêm sản phẩm thất bại"));

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

    private String validateProductImageReference(String imageReference, boolean allowLegacyPath) {
        if (imageReference == null || imageReference.isBlank()) {
            return null;
        }

        if (ProductImageData.isEmbeddedImage(imageReference)) {
            ProductImageData.decode(imageReference);
            return imageReference;
        }

        if (allowLegacyPath && imageReference.length() <= 500) {
            return imageReference;
        }

        throw new IllegalArgumentException("Ảnh sản phẩm phải được tải lên từ phiên bản client mới.");
    }

    private void handleEditProduct(Message msg) {
        requireRole(UserRole.SELLER);
        ProductSaveRequest request = parseData(msg, ProductSaveRequest.class);

        ProductDAO dao = new ProductDAO();
        String imageReference = validateProductImageReference(request.getImagePath(), true);
        boolean result = dao.editProductBySeller(request.getId(), request.getName(), request.getDescription(), request.getStartPrice(), request.getStatus(), imageReference, authenticatedUsername);

        send(msg, new Message(result ? MessageType.EDIT_PRODUCT : MessageType.ERROR, null, result, result ? "Cập nhật thành công!" : "Cập nhật sản phẩm thất bại!"));

        if (result) {
            broadcastProductChanged("EDIT_PRODUCT", request.getId());
        }
    }

    private void handleDeleteProduct(Message msg) {
        requireRole(UserRole.ADMIN);
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        ProductDAO dao = new ProductDAO();
        boolean result = dao.deleteProduct(productId);

        send(msg, new Message(result ? MessageType.DELETE_PRODUCT : MessageType.ERROR, null, result, result ? "Xoá sản phẩm thành công!" : "Xoá sản phẩm thất bại"));

        if (result) {
            broadcastProductChanged("DELETE_PRODUCT", productId);
        }
    }

    private void handleCloseAuction(Message msg) {
        requireRole(UserRole.ADMIN);
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        ProductDAO dao = new ProductDAO();
        boolean result = dao.closeAuction(productId);

        send(msg, new Message(result ? MessageType.CLOSE_AUCTION : MessageType.ERROR, null, result, result ? "Đóng phiên thành công!" : "Đóng phiên thất bại"));

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

        ServerManager.broadcast(Protocol.encode(new Message(MessageType.PRODUCT_CHANGED, data, true, "Product list changed")));
    }

    private void handleGetBidHistory(Message msg) {
        requireAuthenticated();
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        BidDAO dao = new BidDAO();
        send(msg, new Message(MessageType.BID_HISTORY, dao.getBidsByProductId(productId), true, "Load lịch sử bid thành công!"));
    }

    private void handleGetWinner(Message msg) {
        requireAuthenticated();
        Map<?, ?> data = getDataMap(msg);
        int productId = getInt(data, "productId");

        BidDAO dao = new BidDAO();
        String winner = dao.getWinnerUsernameByProductId(productId);

        send(msg, new Message(MessageType.WINNER_RESULT, winner, true, "Load winner thành công"));
    }

    private <T> T parseData(Message msg, Class<T> clazz) {
        if (msg.getData() == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu request");
        }

        T value = Protocol.gson().fromJson(Protocol.gson().toJson(msg.getData()), clazz);
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
        if (value instanceof Number number) return number.intValue();
        if (value instanceof String text && !text.isBlank()) return Integer.parseInt(text);
        throw new IllegalArgumentException("Thiếu hoặc sai trường: " + key);
    }

    private double getDouble(Map<?, ?> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String text && !text.isBlank()) return Double.parseDouble(text);
        throw new IllegalArgumentException("Thiếu hoặc sai trường: " + key);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void requireAuthenticated() {
        if (authenticatedUsername == null || authenticatedRole == null) {
            throw new IllegalArgumentException("Bạn chưa đăng nhập !");
        }
    }

    private void requireRole(UserRole requiredRole) {
        requireAuthenticated();
        if (authenticatedRole != requiredRole) {
            throw new IllegalArgumentException("Tài khoản không có quyền " + requiredRole.name());
        }
    }

    private void sendError(Message request, String message) {
        send(request, new Message(MessageType.ERROR, null, false, message));
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
