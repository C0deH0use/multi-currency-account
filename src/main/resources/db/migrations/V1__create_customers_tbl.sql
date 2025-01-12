-- Create a sequence for customerDto numbers
CREATE SEQUENCE customer_idx_seq START 1000000;

CREATE TABLE customers
(
    id         INT PRIMARY KEY DEFAULT NEXTVAL('customer_idx_seq'),
    first_name TEXT NOT NULL,
    last_name  TEXT NOT NULL
);
