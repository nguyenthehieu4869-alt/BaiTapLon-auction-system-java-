# Hệ Thống Đấu Giá Trực Tuyến

## 1. Mô Tả Bài Toán Và Phạm Vi Hệ Thống

Hệ thống đấu giá trực tuyến là một ứng dụng client-server cho phép người dùng tham gia các phiên đấu giá sản phẩm. Hệ thống hỗ trợ nhiều vai trò người dùng (BIDDER, SELLER, ADMIN), quản lý phiên đấu giá, đặt giá, quản lý ví điện tử và theo dõi lịch sử.

**Phạm vi hệ thống:**
- Xác thực người dùng (Đăng ký, Đăng nhập)
- Quản lý sản phẩm (Tạo, chỉnh sửa, xoá, đóng phiên đấu giá)
- Đặt giá và lịch sử đặt giá
- Quản lý ví điện tử (Nạp tiền, trừ tiền)
- Hỗ trợ đa người dùng concurrent
- Real-time cập nhật khi có bid mới

---

## 2. Công Nghệ Sử Dụng, Môi Trường Chạy Và Yêu Cầu Cài Đặt

### Công Nghệ Sử Dụng

| Thành phần | Công nghệ |
|-----------|-----------|
| **Ngôn ngữ** | Java 17+ |
| **Build Tool** | Maven 3.6.3+ |
| **GUI Framework** | JavaFX 21 |
| **Giao tiếp** | Socket + JSON (Gson) |
| **Database** | MySQL,Azure |
| **Testing** | JUnit 5 |

### Yêu Cầu Cài Đặt

- **JDK 17 trở lên** (khuyến khích JDK 21)
- **Maven 3.6.3 trở lên** hoặc sử dụng Maven Wrapper (`.mvn`)
- **Internet** để kết nối đến cloud server

### Thiết Lập Môi Trường

```bash
# Kiểm tra JDK
java -version

# Kiểm tra Maven
mvn -version
```

---

## 3. Cấu Trúc Module Chính

```
auction-system/
├── pom.xml                           # Parent POM (multi-module)
│
├── auction-common/                   # Module chia sẻ (DTOs, Protocol)
│   ├── src/main/java/com/auction/common/
│   │   ├── network/
│   │   │   ├── dto/                  # LoginRequest, ProductDTO, BidDTO, etc.
│   │   │   └── protocol/             # Message, MessageType, Protocol
│   │   ├── ProductStatus.java
│   │   ├── UserRole.java
│   │   └── AuctionTime.java
│   └── pom.xml
│
├── auction-server/                   # Server Socket (chạy trên Cloud)
│   ├── src/main/java/com/auction/server/
│   │   ├── network/
│   │   │   ├── AuctionServer.java    # Main server
│   │   │   ├── handler/
│   │   │   │   ├── ClientHandler.java
│   │   │   │   ├── MessageHandler.java
│   │   │   │   └── ServerManager.java
│   │   ├── service/
│   │   │   ├── BidService.java       # Xử lý đặt giá
│   │   │   ├── AccountAuthorization.java
│   │   │   └── BidResult.java
│   │   ├── dao/
│   │   │   ├── UserDAO.java
│   │   │   ├── ProductDAO.java
│   │   │   ├── BidDAO.java
│   │   │   └── DatabaseManager.java
│   │   ├── model/
│   │   │   └── Product.java
│   │   └── util/
│   │       └── Constants.java
│   └── pom.xml
│
├── auction-client-gui/               # Client JavaFX (UI)
│   ├── src/main/java/com/auction/
│   │   ├── Main.java                 # Entry point
│   │   ├── AuctionApplication.java   # JavaFX Application
│   │   ├── controller/               # FXML Controllers (các màn hình UI)
│   │   ├── network/
│   │   │   └── AuctionNetworkClient.java  # Socket client
│   │   └── util/
│   │       └── FxmlUtil.java
│   ├── src/main/resources/
│   │   ├── config.properties         # Server config (40.83.76.228:9999)
│   │   └── com/auction/view/         # FXML files
│   └── pom.xml
│
└── .mvn/wrapper/                     # Maven Wrapper
```

---

## 4. Lệnh Chạy Chương Trình

### Chạy Client (Windows, Linux, macOS)

**Windows PowerShell:**
```powershell
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run
```

**Linux/macOS:**
```bash
./mvnw -ntp -f auction-client-gui/pom.xml clean javafx:run
```

### Chạy Multiple Clients Cùng Lúc

Mở nhiều terminal/PowerShell và chạy lệnh trên ở mỗi terminal:

**Windows:**
```powershell
# Terminal 1
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run

# Terminal 2 (mở thêm)
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run

# Terminal 3 (mở thêm)
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run
```

**Linux/macOS:**
```bash
# Terminal 1
./mvnw -ntp -f auction-client-gui/pom.xml clean javafx:run

# Terminal 2 (mở thêm)
./mvnw -ntp -f auction-client-gui/pom.xml clean javafx:run

# Terminal 3 (mở thêm)
./mvnw -ntp -f auction-client-gui/pom.xml clean javafx:run
```

> **Lưu ý:** Client sẽ tự động kết nối đến cloud server `40.83.76.228:9999` (cấu hình trong `config.properties`)

---

## 5. Hướng Dẫn Chạy Server/Client

### Quy Trình Chạy

1. **Server đã chạy sẵn trên cloud** (`40.83.76.228:9999`) - không cần chạy local
2. **Chạy Client** - Mở terminal và chạy lệnh ở mục 4
3. **Ứng dụng JavaFX sẽ mở** - Đăng nhập với tài khoản hoặc đăng ký
4. **Client tự động kết nối** đến cloud server

### Các Vai Trò Người Dùng

- **BIDDER:** Xem sản phẩm, đặt giá, nạp tiền ví
- **SELLER:** Tạo/chỉnh sửa/xoá sản phẩm, xem phiên của mình
- **ADMIN:** Quản lý tài khoản, xoá sản phẩm, đóng phiên

---

## 6. Danh Sách Chức Năng Đã Hoàn Thành

### Chức Năng Cơ Bản

- ✅ **Đăng Ký / Đăng Nhập**
  - Đăng ký tài khoản với vai trò SELLER hoặc BIDDER
  - Đăng nhập, kiểm tra mật khẩu
  - Validation username, email, mật khẩu

- ✅ **Quản Lý Tài Khoản**
  - Xem hồ sơ (username, email, số phiên thắng, số sản phẩm bán)
  - Quản lý ví điện tử

- ✅ **Quản Lý Sản Phẩm (Phiên Đấu Giá)**
  - Tạo sản phẩm (SELLER): tên, mô tả, giá khởi điểm, thời gian bắt đầu/kết thúc, ảnh
  - Chỉnh sửa sản phẩm (SELLER): cập nhật thông tin khi chưa có bid
  - Xoá sản phẩm (ADMIN): xoá phiên đấu giá
  - Đóng phiên (ADMIN): kết thúc phiên sớm
  - Danh sách sản phẩm (BIDDER): xem toàn bộ phiên đấu giá
  - Danh sách sản phẩm của seller (SELLER): xem phiên của chính mình

- ✅ **Đặt Giá (Bidding)**
  - Đặt giá: kiểm tra giá cao hơn giá hiện tại, số dư ví đủ
  - Real-time cập nhật: khi có bid mới, tất cả client nhận update
  - Kiểm tra anti-sniping: nếu bid trong 15 giây trước hết giờ, kéo dài thời gian
  - Lịch sử bid: xem toàn bộ bid của một phiên

- ✅ **Ví Điện Tử**
  - Nạp tiền vào ví
  - Trừ tiền khi đặt giá
  - Xem số dư ví

- ✅ **Xác Định Người Thắng**
  - Lấy người thắng (highest bidder) của phiên

### Chức Năng Nâng Cao

- ✅ **Concurrent Bidding**
  - Xử lý nhiều bid từ nhiều client cùng lúc
  - Sử dụng database lock (SELECT FOR UPDATE) để tránh race condition
  - Transaction rollback nếu lỗi

- ✅ **Anti-Sniping**
  - Khi có bid trong 15 giây trước hết giờ, kéo dài endTime thêm 15 giây
  - Cập nhật realtime thời gian mới cho tất cả bidders

- ✅ **Real-time Update**
  - ServerManager broadcast MESSAGE khi có bid/sản phẩm thay đổi
  - Client nhận update qua listener thread

- ✅ **Socket Communication**
  - Client-Server giao tiếp qua socket + JSON
  - Request/Response với requestId tracking
  - Handle disconnect gracefully

- ✅ **Database Transaction**
  - Rollback tự động khi lỗi
  - Đảm bảo consistency khi đặt giá

---

## 7. Link Báo Cáo PDF Và Video Demo

- **Báo Cáo PDF:** [Link báo cáo](https://drive.google.com/file/d/1faUV4H84B3077ZmzusCq4Q2ZOISmeWvk/view?usp=sharing)
- **Video Demo:** https://youtu.be/I1jSCyBltM0

---

## Ghi Chú

- Server chạy sẵn trên cloud, client chỉ cần chạy lệnh Maven
- Mỗi client là một process riêng, có thể mở nhiều client để test concurrent bidding
- Database được quản lý bởi cloud server, client không truy cập trực tiếp
