
-- ==========================================================
-- V10: Enable automatic product ID generation
-- ECommerceOrderEngine V7
--
-- Preserve existing product IDs and foreign-key rules.
--
-- Original constraints:
-- fk_inventory_product   : NO ACTION / NO ACTION
-- fk_order_items_product : NO ACTION / NO ACTION
-- ==========================================================

-- 1. Temporarily remove the foreign keys referencing products.

ALTER TABLE inventory
    DROP FOREIGN KEY fk_inventory_product;

ALTER TABLE order_items
    DROP FOREIGN KEY fk_order_items_product;

-- 2. Enable automatic product ID generation.

ALTER TABLE products
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- 3. Restore the inventory foreign key.

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_product
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

-- 4. Restore the order items foreign key.

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;
