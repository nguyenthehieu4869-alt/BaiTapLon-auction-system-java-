DROP DATABASE IF EXISTS auction_db;
CREATE DATABASE auction_db;
USE auction_db;

CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100),
                       password VARCHAR(100) NOT NULL
);

CREATE TABLE products (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          start_price DOUBLE NOT NULL,
                          current_price DOUBLE NOT NULL,
                          status VARCHAR(20) DEFAULT 'OPEN',
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
    ('bidder1', 'bidder1@gmail.com', '123456');

INSERT INTO products (name, description, start_price, current_price, status, seller_username)
VALUES
    ('iPhone 15 Pro', 'Dien thoai Apple 256GB', 15000000, 15000000, 'OPEN', 'seller1'),
    ('Laptop Gaming', 'Laptop choi game cau hinh cao', 20000000, 20000000, 'OPEN', 'seller1'),
    ('Tai nghe Sony', 'Tai nghe chong on', 2000000, 2000000, 'OPEN', 'seller2');