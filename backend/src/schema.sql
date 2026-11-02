DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
  customer_number INTEGER PRIMARY KEY
    CHECK (customer_number BETWEEN 100000000 AND 999999999),
  full_name       VARCHAR(100) NOT NULL,
  password_hash   VARCHAR(255) NOT NULL
);

CREATE TABLE accounts (
  account_number  CHAR(9) PRIMARY KEY
    CHECK (account_number ~ '^[0-9]{9}$'),
  customer_number INTEGER NOT NULL REFERENCES customers(customer_number),
  balance         NUMERIC(15,3) NOT NULL DEFAULT 0,
  nickname        VARCHAR(255)
);

CREATE INDEX idx_accounts_customer ON accounts(customer_number);