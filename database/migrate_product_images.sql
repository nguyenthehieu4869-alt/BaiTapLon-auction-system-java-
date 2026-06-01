USE auction_db;

ALTER TABLE products
    MODIFY COLUMN image_path LONGTEXT NULL;
