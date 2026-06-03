# BÁO CÁO BÀI TẬP LỚN: HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN

**Nhóm 14 - Lập trình Nâng cao (LTNC)**

---

## 1. GIỚI THIỆU MỤC TIÊU VÀ PHẠM VI THỰC HIỆN

### 1.1 Mục tiêu

Xây dựng một hệ thống đấu giá trực tuyến đầy đủ chức năng, áp dụng kiến trúc client-server với giao tiếp qua socket, hỗ trợ đa người dùng, quản lý phiên đấu giá, ví điện tử, tự động đặt giá và các tính năng nâng cao như anti-sniping, realtime notification.

### 1.2 Phạm vi hệ thống

Hệ thống bao gồm các luồng chính:
- **Xác thực & Phân quyền**: Đăng ký, đăng nhập, quản lý phiên đăng nhập (token-based)
- **Quản lý Phiên Đấu Giá**: Tạo, sửa, xóa, theo dõi trạng thái phiên (OPEN → RUNNING → FINISHED → PAID/CANCELED)
- **Đặt Giá & Tự động Đặt Giá**: Đặt giá real-time, luật tự động đặt giá với logic ưu tiên
- **Ví Điện tử & Thanh toán**: Quản lý số dư, giữ tiền (reserve), thanh toán, hoàn tiền
- **Thông Báo Real-time**: Hệ thống thông báo cho người dùng về tình trạng phiên và bid
- **Quản Trị**: Admin quản lý tài khoản, phiên, thông báo

---

## 2. KIẾN TRÚC TỔNG THỂ CỦA HỆ THỐNG

### 2.1 Sơ đồ kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER (JavaFX GUI)                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  GUI Screens (FXML)                                       │  │
│  │  - Login/Register, Auction List, Auction Detail           │  │
│  │  - Bidding, Wallet, Admin Dashboard                       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↕ (Socket)                             │
└─────────────────────────────────────────────────────────────────┘
                    ↓ TCP (Cloud Server)
┌─────────────────────────────────────────────────────────────────┐
│                  SERVER LAYER (Socket Server)                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Request Handler (ClientHandler threads)                 │  │
│  │  Session Manager & Token Validation                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Business Logic Services                                 │  │
│  │  ├─ UserService, AuctionService, BidService              │  │
│  │  ├─ WalletService, NotificationService                   │  │
│  │  └─ SchedulerService (background tasks)                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Data Access Layer (DAO)                                 │  │
│  │  ├─ UserDAO, AuctionDAO, BidDAO, AutoBidDAO              │  │
│  │  ├─ WalletDAO, NotificationDAO                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            ↓ JDBC
┌─────────────────────────────────────────────────────────────────┐
│              DATABASE LAYER (MySQL / Cloud DB)                   │
│  Tables: users, auctions, bids, auto_bids, wallets,             │
│          topup_transactions, wallet_transactions, notifications  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Mô tả kiến trúc

**Client Layer (JavaFX)**
- Giao diện dùng JavaFX, FXML, CSS - không truy cập DB trực tiếp
- Gửi request qua socket đến cloud server, nhận response
- Hỗ trợ cấu hình server qua: `config.properties` → environment variable → system property
- Tự động kết nối cloud server `40.83.76.228:9999`

**Server Layer (Socket)**
- Lắng nghe port (mặc định 9999), xử lý multiple client qua thread pool
- Mỗi client connection tạo một `ClientHandler` thread riêng
- Session Manager kiểm tra token trước thực hiện action nhạy cảm
- Hỗ trợ định cấu hình qua environment variable `AUCTION_SERVER_HOST`, `AUCTION_SERVER_PORT`

**Business Logic Layer**
- UserService: xác thực, quản lý hồ sơ
- AuctionService: CRUD phiên, transition trạng thái
- BidService: xử lý đặt giá, kiểm tra điều kiện
- WalletService: quản lý số dư, reserve/capture/refund
- NotificationService: thông báo người dùng
- SchedulerService: auto transition, trigger auto-bid

**Data Access Layer**
- DAOs (Data Access Objects) xử lý CRUD
- Sử dụng PreparedStatement tránh SQL injection

**Database Layer**
- Cloud DB (Akamai DB hoặc tương tự) lưu trữ dữ liệu
- Các bảng chính: users, auctions, bids, auto_bids, wallets, transactions, notifications
- Index trên cột hay query (auction_id, user_id, start_time, end_time)

---

## 3. CÁC CHỨC NĂNG ĐẠT ĐƯỢC THEO BAREM ĐIỂM

### 3.1 Chức năng cơ bản (8 điểm)

| # | Chức năng | Hướng giải quyết | Lý do lựa chọn |
|---|-----------|------------------|-----------------|
| 1 | Đăng ký, đăng nhập | UserService + token-based session | Bảo mật, hỗ trợ đa client |
| 2 | Phân quyền (ADMIN, SELLER, BIDDER) | Role-based access control ở server | Kiểm tra quyền trước action nhạy cảm |
| 3 | CRUD phiên đấu giá | AuctionService + AuctionDAO | Kiểm tra ownership, state transition |
| 4 | Đặt giá | BidService + validation logic | Kiểm tra giá, thời gian, số dư ví |
| 5 | Ví điện tử | WalletService + reserve balance | Quản lý số dư, tiền giữ, available |
| 6 | Danh sách phiên | AuctionDAO + sorting/pagination | Search, filter, sort theo status |
| 7 | Lịch sử bid | BidDAO + query by auction | Tracking, audit trail |
| 8 | Thông báo | NotificationService + inbox | Thông báo event quan trọng |

### 3.2 Chức năng nâng cao (2 điểm)

| # | Chức năng | Hướng giải quyết | Lý do lựa chọn |
|---|-----------|------------------|-----------------|
| 1 | Tự động đặt giá (Auto-bid) | AutoBidService + trigger logic | Ưu tiên: maxPrice, ngày tạo, ID |
| 2 | Anti-sniping | Kéo dài endTime khi bid sát hạn chót | Giữ lại tính cạnh tranh của phiên |
| 3 | Concurrent bidding | Thread-safe + synchronized | Xử lý multiple bid cùng lúc |
| 4 | Reserved balance reconciliation | Batch job on server startup | Đối soát tiền giữ khi restart |
| 5 | Real-time update via notification | Broadcast khi bid/status thay đổi | Bidder biết ngay mình bị vượt |
| 6 | Refund/Settlement logic | Transaction log + status check | Xử lý hoàn tiền đúng trạng thái |
| 7 | Multi-module Maven | Separation: auction-common, auction-server, auction-client-gui | Reuse code, dễ maintain |
| 8 | Cloud deployment ready | Config dùng environment variable | Deploy trên cloud không cần sửa code |

### 3.3 Hướng giải quyết chi tiết

**Auto-bid Logic**
```
Khi có bid mới:
  → Lấy danh sách auto-bid rule (auction_id, không phải người đặt giá hiện tại)
  → Sort theo maxPrice DESC, creation_date ASC, user_id ASC
  → Duyệt từng rule:
     - Kiểm tra maxPrice >= currentHighestBid + bidStep
     - Kiểm tra số dư ví >= bidAmount
     - Tự động đặt giá
     - Break nếu thành công
```

**Anti-sniping**
```
Khi có bid trong 60 giây trước endTime:
  → Kéo dài endTime thêm 60 giây
  → Tăng counter, tối đa 5 lần
  → Thông báo bidders về thời gian kết thúc mới
```

**Reserve Balance**
```
Khi bidder dẫn đầu:
  → Giữ: reserved_balance = currentHighestBid
  → Khả dụng: available_balance = total - reserved

Khi bidder bị vượt giá:
  → Release reserved amount
  
Khi phiên kết thúc (FINISHED):
  → Capture reserved (nếu winner tồn tại)
  → Trừ khỏi available_balance
```

---

## 4. PHÂN CHIA CÔNG VIỆC GIỮA CÁC THÀNH VIÊN

| Thành viên | Phần trăm | Công việc chính |
|-----------|----------|-----------------|
| Thành viên 1 | 30% | Backend server: UserService, Authentication, SessionManager, DAO layer |
| Thành viên 2 | 30% | Backend server: AuctionService, BidService, AutoBid, Scheduler |
| Thành viên 3 | 25% | Frontend JavaFX: GUI screens (login, auction, bidding, wallet) |
| Thành viên 4 | 15% | Database design, WalletService, integration test, deployment |

**Công việc chung**
- Requirements & architecture: Tất cả
- Testing & bug fix: Tất cả
- Documentation & demo: Tất cả

---

## 5. CÔNG NGHỆ & MÔI TRƯỜNG CHẠY

| Thành phần | Công nghệ |
|-----------|-----------|
| **Ngôn ngữ** | Java 17+ |
| **Build Tool** | Maven 3.6.3+ (Multi-module) |
| **GUI Framework** | JavaFX 21, FXML, CSS |
| **Communication** | Socket + JSON Protocol |
| **Database** | MySQL / Cloud DB |
| **Testing** | JUnit 5 |

**Yêu cầu cài đặt**
- JDK 17+ (khuyến khích JDK 21)
- Maven 3.6.3+ hoặc Maven Wrapper
- Internet kết nối cloud server

---

## 6. HƯỚNG DẪN CHẠY HỆ THỐNG

### 6.1 Chạy Client (Chỉ cần 1 dòng lệnh)

**Windows PowerShell:**
```powershell
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run
```

**Linux/macOS:**
```bash
./mvnw -ntp -f auction-client-gui/pom.xml clean javafx:run
```

**Nếu muốn thay đổi server (tuỳ chọn):**
```powershell
# Windows
$env:AUCTION_SERVER_HOST="your-server-ip"
$env:AUCTION_SERVER_PORT="9999"
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run
```

```bash
# Linux/macOS
export AUCTION_SERVER_HOST="your-server-ip"
export AUCTION_SERVER_PORT="9999"
./mvnw -ntp -f auction-client-gui/pom.xml clean javafx:run
```

### 6.2 Chạy Server (Nếu cần local - optional)

**Build server:**
```bash
mvn -ntp -pl auction-server -am package -DskipTests
```

**Chạy server (Windows PowerShell):**
```powershell
$env:DB_PASSWORD="your_db_password"
java -Dapp.server.port=9999 -jar auction-server/target/auction-server-1.0-SNAPSHOT.jar
```

**Chạy server (Linux/macOS):**
```bash
export DB_PASSWORD="your_db_password"
java -Dapp.server.port=9999 -jar auction-server/target/auction-server-1.0-SNAPSHOT.jar
```

### 6.3 Chạy Multiple Clients Cùng Lúc

Mở nhiều terminal/PowerShell rồi chạy lệnh client ở mục 6.1 ở mỗi terminal:

```powershell
# Terminal 1
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run

# Terminal 2 (mở thêm)
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run

# Terminal 3 (mở thêm)
mvnw.cmd -ntp -f auction-client-gui/pom.xml clean javafx:run
```

---

## 7. CẤU TRÚC THƯ MỤC

```
BaiTapLon-auction-system-java-/
├── pom.xml                          # Maven parent POM
├── auction-common/                  # Shared code (models, protocol)
│   ├── src/main/java/com/auction/common
│   └── pom.xml
├── auction-server/                  # Server socket application
│   ├── src/main/java/com/auction/server
│   │   ├── network/                 # Socket handler
│   │   ├── service/                 # Business logic
│   │   └── dao/                     # Database access
│   ├── pom.xml
│   └── target/auction-server-1.0-SNAPSHOT.jar
├── auction-client-gui/              # JavaFX client application
│   ├── src/main/java/com/auction
│   │   ├── Main.java                # Entry point
│   │   ├── AuctionApplication.java  # JavaFX app
│   │   ├── controller/              # FXML controllers
│   │   ├── network/                 # Network client
│   │   └── util/                    # Utilities
│   ├── src/main/resources/
│   │   ├── config.properties        # Server config
│   │   └── com/auction/view/        # FXML files
│   ├── pom.xml
│   └── target/
├── docs/
│   └── BAO_CAO_LTNC.md              # Detailed report
├── README.md                        # This file
└── .mvn/wrapper/                    # Maven Wrapper
```

---

## 8. CHỨC NĂNG HỖ TRỢ TRỊ CHỨNG

### Tài khoản & Xác thực
- ✅ Đăng ký với role SELLER/BIDDER
- ✅ Đăng nhập, đăng xuất
- ✅ Token-based session management
- ✅ Mật khẩu hash (PasswordUtil)

### Quản lý Phiên Đấu Giá
- ✅ CRUD auction (create, read, update, delete)
- ✅ State transition: OPEN → RUNNING → FINISHED → PAID/CANCELED
- ✅ Scheduled state transition
- ✅ Anti-sniping (extend endTime)
- ✅ Early-close countdown

### Đấu Giá (Bidding)
- ✅ Đặt giá real-time
- ✅ Kiểm tra điều kiện: giá, thời gian, số dư
- ✅ Lịch sử bid tracking
- ✅ Tự động đặt giá (Auto-bid) với logic ưu tiên
- ✅ Concurrent bidding (thread-safe)

### Ví Điện Tử
- ✅ Quản lý số dư: total, reserved, available
- ✅ Nạp tiền (top-up)
- ✅ Reserve tiền khi dẫn đầu
- ✅ Capture khi thắng
- ✅ Refund khi hủy phiên
- ✅ Transaction log

### Thông Báo & Admin
- ✅ Notification system
- ✅ Admin quản lý tài khoản (lock/unlock)
- ✅ Admin quản lý phiên
- ✅ Tạo announcement

### Kiểm Thử
- ✅ Unit tests (validation, password, network)
- ✅ Integration tests (bidding, auto-bid, settlement)
- ✅ Concurrent test scenarios

---

## 9. KẾT LUẬN

Hệ thống đấu giá trực tuyến hoàn thiện với:
- ✅ Kiến trúc client-server, cloud-ready
- ✅ Đủ chức năng cơ bản + nâng cao
- ✅ Chỉ cần 1 dòng lệnh để chạy client
- ✅ Tự động kết nối cloud server `40.83.76.228:9999`
- ✅ Hỗ trợ đa client concurrent
- ✅ Xử lý auto-bid, anti-sniping, ví điện tử

---

**Link Github:** https://github.com/nguyenthehieu4869-alt/BaiTapLon-auction-system-java-

**Link Báo cáo PDF:** [Cập nhật link]

**Link Video Demo:** [Cập nhật link]
