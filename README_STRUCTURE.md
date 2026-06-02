# Cấu trúc project đã sắp xếp

Project được chia theo kiến trúc Client–Server và Maven multi-module:

```text
auction-system
├── auction-common      # DTO, protocol, enum dùng chung cho client và server
├── auction-server      # Server socket, DAO, service, domain OOP, database logic
├── auction-client-gui  # JavaFX client, FXML controller, remote service
└── database            # Script tạo database
```

## Package chính

### auction-common

```text
com.auction.common
├── AuctionTime.java
├── ProductImageData.java
├── ProductStatus.java
├── UserRole.java
└── network
    ├── dto
    └── protocol
```

### auction-server

```text
com.auction.server
├── dao        # Truy cập database
├── model      # Model dữ liệu map DB
├── service    # Business logic: login, product, bidding
├── network    # Socket server, client handler, message handler
├── util       # Hằng số, helper
└── domain     # OOP/domain model theo yêu cầu đề
    ├── model
    ├── factory
    ├── manager
    ├── observer
    └── exception
```

### auction-client-gui

```text
com.auction
├── controller      # JavaFX controllers
├── model           # Model hiển thị phía client
├── network         # Socket client/listener
├── service.remote  # Gọi server qua socket
├── util
└── view/resources  # FXML, ảnh, CSS
```

## Lưu ý

- Client không truy cập database trực tiếp.
- DAO chỉ nằm ở server.
- Domain OOP nằm ở `com.auction.server.domain` để phục vụ yêu cầu OOP/design pattern của đề.
- Các lớp dùng chung qua mạng nằm ở `auction-common`.
