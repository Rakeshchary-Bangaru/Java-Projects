-- =========================================================
-- V6 - LINK CUSTOMERS TO AUTHENTICATED USER ACCOUNTS
-- =========================================================
--
-- V7 security requirement:
-- Checkout ownership must come from the authenticated
-- UserAccount rather than a customerId supplied by the client.
--
-- The column is nullable for now because legacy customer rows
-- existed before the users table was introduced.
--
-- UNIQUE guarantees that one UserAccount can be linked to
-- at most one Customer profile.
-- =========================================================

ALTER TABLE customers
    ADD COLUMN user_account_id BIGINT NULL,

    ADD CONSTRAINT uk_customers_user_account
        UNIQUE (user_account_id),

    ADD CONSTRAINT fk_customers_user_account
        FOREIGN KEY (user_account_id)
        REFERENCES users(id);