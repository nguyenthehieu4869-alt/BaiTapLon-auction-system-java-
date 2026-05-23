DROP DATABASE IF EXISTS auction_db;
CREATE DATABASE auction_db;
USE auction_db;

CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL
);

CREATE TABLE products (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          image_path VARCHAR(500),
                          start_price DOUBLE NOT NULL,
                          current_price DOUBLE NOT NULL,
                          status VARCHAR(20) DEFAULT 'OPEN',
                          start_time DATETIME,
                          end_time DATETIME,
                          seller_username VARCHAR(50)
);

CREATE TABLE bids (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      product_id INT NOT NULL,
                      bidder_username VARCHAR(50) NOT NULL,
                      bid_price DOUBLE NOT NULL,
                      bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (product_id) REFERENCES products(id)
);

INSERT INTO users (username, email, password)
VALUES
    ('seller1', 'seller1@gmail.com', '123456'),
    ('seller2', 'seller2@gmail.com', '123456'),
    ('bidder1', 'bidder1@gmail.com', '123456');

INSERT INTO products (name, description, start_price, current_price, status, start_time, end_time, seller_username)
VALUES
    ('iPhone 15 Pro', 'Dien thoai Apple 256GB', 15000000, 15000000, 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 60 MINUTE), 'seller1'),
    ('Laptop Gaming', 'Laptop choi game cau hinh cao', 20000000, 20000000, 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 60 MINUTE), 'seller1'),
    ('Tai nghe Sony', 'Tai nghe chong on', 2000000, 2000000, 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 60 MINUTE), 'seller2');
