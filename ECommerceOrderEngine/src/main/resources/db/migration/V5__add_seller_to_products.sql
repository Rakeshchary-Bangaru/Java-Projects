ALTER TABLE products
    ADD COLUMN seller_id BIGINT NULL;

CREATE INDEX idx_products_seller_id
    ON products(seller_id);

ALTER TABLE products
    ADD CONSTRAINT fk_products_seller
        FOREIGN KEY (seller_id)
        REFERENCES sellers(id);