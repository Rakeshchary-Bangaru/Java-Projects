
-- V11: Product image metadata
-- Images are stored outside MySQL.
-- This table stores their metadata and product relationship.

CREATE TABLE product_images (

    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    storage_key VARCHAR(512) NOT NULL,

    original_filename VARCHAR(255) NOT NULL,

    content_type VARCHAR(100) NOT NULL,

    file_size BIGINT NOT NULL,

    sort_order INT NOT NULL DEFAULT 0,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    -- Ensures at most one primary image per product.
    primary_product_id BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN is_primary = 1 THEN product_id
                ELSE NULL
            END
        ) STORED,

    PRIMARY KEY (id),

    UNIQUE KEY uk_product_images_storage_key
        (storage_key),

    UNIQUE KEY uk_product_images_primary
        (primary_product_id),

    KEY idx_product_images_product_order
        (product_id, sort_order, id),

    CONSTRAINT chk_product_images_file_size
        CHECK (file_size > 0),

    CONSTRAINT chk_product_images_sort_order
        CHECK (sort_order >= 0)

) ENGINE=InnoDB;

ALTER TABLE product_images
    ADD CONSTRAINT fk_product_images_product
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;
