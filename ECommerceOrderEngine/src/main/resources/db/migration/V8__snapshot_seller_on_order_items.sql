-- =========================================================
-- SNAPSHOT SELLER OWNERSHIP ON ORDER ITEMS
-- ---------------------------------------------------------
-- Product.seller represents CURRENT ownership.
--
-- OrderItem.seller_id will represent the seller who owned
-- the product when the sale occurred.
-- =========================================================

ALTER TABLE order_items
    ADD COLUMN seller_id BIGINT NULL;


-- Backfill existing historical orders using the product's
-- current seller assignment.
--
-- This preserves the seller/order data we already created
-- during V7 testing.
UPDATE order_items oi
JOIN products p
    ON p.id = oi.product_id
SET oi.seller_id = p.seller_id
WHERE p.seller_id IS NOT NULL;


CREATE INDEX idx_order_items_seller_id
    ON order_items (seller_id);


ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_seller
        FOREIGN KEY (seller_id)
        REFERENCES sellers(id);