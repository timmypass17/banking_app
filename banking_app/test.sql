DROP TABLE customers;

CREATE TABLE customers(
	id SERIAL PRIMARY KEY,
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255) NOT NULL,
	date_of_birth DATE NOT NULL,
	email VARCHAR(255) NOT NULL,
	password VARCHAR(255) NOT NULL 
);

-- INSERT INTO TABLE customers (first_name, last_name, date_of_birth, email, password)
-- VALUES ('Timmy', 'Nguyen', '2001-10-29', 'timmy@gmail.com', 'password')

CREATE TABLE banks(
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL
);

DROP TABLE bank_accounts;

DROP TYPE bank_account_type;
CREATE TYPE bank_account_type AS ENUM ('CHECKINGS', 'SAVINGS');

CREATE TABLE bank_accounts (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES customers(id),
	bank_id INTEGER NOT NULL REFERENCES banks(id),
    bank_account_type bank_account_type NOT NULL,
    balance BIGINT NOT NULL
);

SELECT * FROM customers;

DELETE FROM customers;

INSERT INTO banks (name)
VALUES 
	('Bank of America'),
	('Chase'),
	('Wells Fargo'),
	('Citibank');

SELECT * FROM banks;

INSERT INTO bank_accounts (customer_id, bank_id, bank_account_type, balance)
VALUES (3, 1, 'SAVINGS', 100);

INSERT INTO bank_accounts (customer_id, bank_id, bank_account_type, balance)
VALUES (3, 2, 'CHECKING', 50);

SELECT * FROM bank_accounts;

SELECT ba.id AS bank_account_id, b.name AS bank_name 
FROM bank_accounts AS ba
JOIN banks AS b ON ba.bank_id = b.id
JOIN customers AS c ON ba.customer_id = c.id
WHERE ba.customer_id = 3;

