DROP TABLE customers;

CREATE TABLE customers(
	id VARCHAR(255) PRIMARY KEY NOT NULL,
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255) NOT NULL,
	date_of_birth DATE NOT NULL,
	email VARCHAR(255) NOT NULL,
	password VARCHAR(255) NOT NULL 
);

-- INSERT INTO TABLE customers (first_name, last_name, date_of_birth, email, password)
-- VALUES ('Timmy', 'Nguyen', '2001-10-29', 'timmy@gmail.com', 'password')

CREATE TABLE banks(
	id VARCHAR(255) PRIMARY KEY NOT NULL,
	name VARCHAR(255) NOT NULL
);

DROP TABLE bank_accounts;

DROP TYPE bank_account_type;
CREATE TYPE bank_account_type AS ENUM ('CHECKINGS', 'SAVINGS');

CREATE TABLE bank_accounts (
	id VARCHAR(255) PRIMARY KEY NOT NULL,
    customer_id VARCHAR(255) NOT NULL REFERENCES customers(id),
	bank_id VARCHAR(255) NOT NULL REFERENCES banks(id),
    bank_account_type bank_account_type NOT NULL,
    balance BIGINT NOT NULL,
	is_active BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE bank_accounts
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

SELECT * FROM customers;

DELETE FROM customers;

-- insert default banks
INSERT INTO banks (name)
VALUES 
	('1', 'Bank of America'),
	('1', 'Chase'),
	('1', 'Wells Fargo'),
	('1', 'Citibank');

SELECT * FROM banks;

INSERT INTO bank_accounts (customer_id, bank_id, bank_account_type, balance)
VALUES (3, 1, 'SAVINGS', 100);

INSERT INTO bank_accounts (customer_id, bank_id, bank_account_type, balance)
VALUES (3, 2, 'CHECKING', 50);

SELECT * FROM bank_accounts;

SELECT ba.id AS bank_account_id,
	b.name AS bank_name ,
	c.first_name AS customer_name
FROM bank_accounts AS ba
JOIN banks AS b ON ba.bank_id = b.id
JOIN customers AS c ON ba.customer_id = c.id;

SELECT * FROM customers;

UPDATE customers 
SET 
	first_name = 'Josuke',
	last_name = 'Higashikata',
	date_of_birth = '2026-08-05',
	email = '',
	password = ''
WHERE id = 2;

-- Transaction

CREATE TYPE transaction_action AS ENUM (
    'DEPOSIT',
    'WITHDRAW',
    'TRANSFER'
);

DROP TABLE transactions;

CREATE TABLE transactions (
	id VARCHAR(255) PRIMARY KEY NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action transaction_action NOT NULL,

    source_amount BIGINT,
    source_account_id VARCHAR(255) REFERENCES bank_accounts(id),
    source_result_balance BIGINT,

    destination_amount BIGINT,
    destination_account_id VARCHAR(255) REFERENCES bank_accounts(id),
    destination_result_balance BIGINT
);

SELECT
    t.*,

    -- Source
    sc.id AS source_customer_id,
    sb.id AS source_bank_id,
    sc.first_name AS source_customer_name,
    sb.name AS source_bank_name,

    -- Destination
    dc.id AS destination_customer_id,
    db.id AS destination_bank_id,
    dc.first_name AS destination_customer_name,
    db.name AS destination_bank_name

FROM transactions AS t

LEFT JOIN bank_accounts AS sa
    ON t.source_account_id = sa.id

LEFT JOIN bank_accounts AS da
    ON t.destination_account_id = da.id

LEFT JOIN customers AS sc
    ON sa.customer_id = sc.id

LEFT JOIN customers AS dc
    ON da.customer_id = dc.id

LEFT JOIN banks AS sb
    ON sa.bank_id = sb.id

LEFT JOIN banks AS db
    ON da.bank_id = db.id;

SELECT * FROM bank_accounts;

-- reset db
DROP TABLE customers;
DROP TABLE bank_accounts;
DROP TABLE banks;
DROP TABLE transactions;

-- select all
SELECT * FROM customers;
