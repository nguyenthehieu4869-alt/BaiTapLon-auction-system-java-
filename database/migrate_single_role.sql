USE auction_db;

ALTER TABLE users
    ADD COLUMN role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL DEFAULT 'BIDDER';

UPDATE users
SET role = 'SELLER'
WHERE username IN ('seller1', 'seller2');

INSERT INTO users (username, email, password, role)
VALUES
    ('Huyadmin', 'huyadmin@gmail.com', '12345654321', 'ADMIN'),
    ('Hieuadmin', 'hieuadmin@gmail.com', '12345654321', 'ADMIN'),
    ('Kienadmin', 'kienadmin@gmail.com', '12345654321', 'ADMIN')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    role = VALUES(role);
