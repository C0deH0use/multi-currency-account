CREATE TABLE currency_accounts
(
    customer_id     INT     NOT NULL,
    currency        TEXT    NOT NULL,
    amount          DECIMAL NOT NULL,
    is_main_account BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX currency_accounts_composite_pk ON currency_accounts (customer_id, currency);