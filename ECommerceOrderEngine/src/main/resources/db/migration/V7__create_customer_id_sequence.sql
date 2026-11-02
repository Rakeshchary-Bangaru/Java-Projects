-- =========================================================
-- V7 - CUSTOMER ID SEQUENCE
-- ---------------------------------------------------------
-- Existing legacy customers use manually assigned BIGINT ids.
--
-- New authenticated CUSTOMER registrations need a safe,
-- database-generated customer id without changing the
-- existing customers.id strategy.
-- =========================================================

CREATE TABLE customer_id_sequence (

    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_customer_id_sequence
        PRIMARY KEY (id)
);


/*
 * Start new V7-generated customer ids well above the
 * legacy/demo customer-id range.
 */
ALTER TABLE customer_id_sequence
    AUTO_INCREMENT = 1000000;