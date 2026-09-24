# Console Banking Application

A console-based banking application built with Java. The application allows customers to register, manage bank accounts, perform banking transactions, and review their transaction history.

The application supports both **PostgreSQL** and **MongoDB** as database options. A DAO-based architecture separates the banking business logic from the database implementation, allowing the database to be changed through configuration without modifying the service or presentation layers.

## Features

* Customer registration
* Customer login and authentication
* Customer profile management
* Secure password/PIN storage
* Checking account management
* Savings account management
* View customer accounts
* View account balances
* Deposit money
* Withdraw money
* Overdraft prevention
* Transfer money between accounts
* Transfer money to another customer's account
* Transaction history
* Filter transactions by type or date
* Input validation
* Business rule validation
* Error and exception handling
* PostgreSQL database support
* MongoDB database support
* Configuration-based database selection
* Unit testing for important banking rules and service logic

## Technologies

* Java
* Maven
* PostgreSQL
* JDBC
* MongoDB
* MongoDB Java Driver
* JUnit
* Git
* GitHub

## Architecture

The application follows a layered architecture with separate responsibilities for the presentation, service, data access, model, and configuration layers.

```text
+-----------------------+
|  Presentation Layer   |
|    Console / CLI      |
+-----------+-----------+
            |
            v
+-----------------------+
|    Service Layer      |
|   Business Logic      |
+-----------+-----------+
            |
            v
+-----------------------+
|     DAO Interfaces    |
+-----------+-----------+
            |
       +----+----+
       |         |
       v         v
+----------+ +-----------+
|PostgreSQL| |  MongoDB  |
|   DAO    | |    DAO    |
+----------+ +-----------+
```

### Presentation Layer

The presentation layer manages all interaction with the user through the console.

Responsibilities include:

* Displaying menus and available actions
* Reading user input
* Validating console input
* Displaying results and error messages
* Sending requests to the service layer
* Avoiding direct database access

### Service Layer

The service layer contains the application's banking rules and business workflows.

Responsibilities include:

* Customer registration and authentication
* Customer and account management
* Deposits and withdrawals
* Transfers
* Account ownership validation
* Account status validation
* Balance validation
* Transaction processing
* Enforcing banking rules
* Creating and retrieving transaction records

### Data Access Layer

The data access layer communicates with the application's databases.

DAO interfaces define the operations required by the service layer. Both PostgreSQL and MongoDB provide their own implementations of these interfaces.

Responsibilities include:

* Defining DAO interfaces
* PostgreSQL DAO implementations
* MongoDB DAO implementations
* Create, read, update, and delete operations
* Mapping database records/documents to Java objects
* Database-specific error handling
* Database connection management

### Model Layer

The model layer represents the application's core business data.

The application includes models for:

* Customers
* Bank accounts
* Transactions

These models contain the data and relationships required by the application.

### Configuration Layer

The configuration layer controls which database implementation the application uses.

Responsibilities include:

* Selecting PostgreSQL or MongoDB
* Managing database connection settings
* Providing the appropriate DAO implementation
* Keeping credentials outside of source code
* Allowing the database implementation to change without modifying business logic

## DAO Design

The application uses the **DAO (Data Access Object) design pattern** to separate business logic from database-specific code.

The service layer communicates with DAO interfaces instead of directly interacting with PostgreSQL or MongoDB.

For example:

```text
Service
   |
   v
CustomerDAO
   |
   +---- PostgreSQLCustomerDAO
   |
   +---- MongoDBCustomerDAO
```

Both database implementations follow the same DAO interface, allowing the application to switch between databases without changing the service or console layers.

This design provides:

* Separation of concerns
* Database independence
* Easier testing
* Easier maintenance
* Ability to replace the persistence layer

## Database Support

The application supports two database systems.

### PostgreSQL

PostgreSQL is accessed using JDBC.

The relational database stores information related to:

* Customers
* Bank accounts
* Transactions

PostgreSQL operations use appropriate SQL queries and prepared statements.

### MongoDB

MongoDB is accessed using the MongoDB Java Driver.

MongoDB stores the same core banking information required by the application using MongoDB collections and documents.

The MongoDB design may use embedded documents or references where appropriate.

## Database Selection

The application selects its database implementation through configuration.

The database selection follows this general structure:

```text
Configuration
     |
     v
Selected Database
     |
 +---+---+
 |       |
 v       v
PostgreSQL  MongoDB
DAO         DAO
```

Changing the database should not require changes to:

* Console menus
* Service classes
* Banking business logic

Only the database configuration should need to change.

## Application Operations

### Customer Management

Customers can:

* Register for an account
* Log in
* Update profile information
* Access their banking accounts

### Bank Account Management

Customers can:

* Open checking accounts
* Open savings accounts
* View all accounts
* View account balances
* Close eligible accounts

### Deposits and Withdrawals

Customers can:

* Deposit money
* Withdraw money
* Check available balances

The application prevents invalid operations such as overdrawing an account.

### Transfers

Customers can:

* Transfer money between their own accounts
* Transfer money to another valid account

Transfers should be processed as a single operation so that funds are not removed from the source account without being successfully deposited into the destination account.

### Transaction History

Customers can:

* View transaction history
* View transaction amounts
* View transaction dates
* View transaction types
* View resulting balances
* Filter transactions by type or date

## Validation and Error Handling

The application validates both user input and banking operations.

Examples of conditions that should be handled include:

* Invalid input
* Missing records
* Duplicate customer information
* Unauthorized actions
* Invalid account ownership
* Closed or invalid accounts
* Insufficient balances
* Invalid transaction amounts
* Invalid transfers
* Database errors

Errors should be handled gracefully and displayed to the user without unexpectedly terminating the application.

## Security

Sensitive configuration values should not be stored directly in source code.

Do **not** commit:

* Database passwords
* Database credentials
* Private connection strings
* Other sensitive configuration values

Database connection information should be stored using external configuration.

## Requirements

The project demonstrates the following concepts:

* Object-oriented programming
* Interfaces and abstraction
* Encapsulation
* Collections
* Exception handling
* Input validation
* Layered architecture
* DAO design pattern
* CRUD operations
* SQL queries
* NoSQL document operations
* Configuration-based database selection
* Unit testing

## Setup

### Prerequisites

Before running the application, make sure the following are installed:

* Java
* Maven
* PostgreSQL or MongoDB
* Git

### Clone the Repository

```bash
git clone https://github.com/timmypass17/banking_app.git
cd banking_app
```

### Configure the Database

Configure the database connection values required by the application.

The application supports:

```text
PostgreSQL
MongoDB
```

Select the desired database through the application's configuration.

Do not commit database credentials or passwords to the repository.

### Build the Project

```bash
mvn clean install
```

### Run the Application

Run the application's main entry point using your configured Java/Maven setup.

The application will start as a console-based program and provide menus for customer and banking operations.

## Testing

The project uses **JUnit** for automated testing.

Tests should cover important business rules and service-layer operations, including both successful and rejected operations.

Run the test suite with:

```bash
mvn test
```

The service layer should be testable without requiring a live production database.

## Project Structure

A typical organization of the application is:

```text
src/
├── main/
│   └── java/
│       └── ...
│           ├── model/
│           ├── dao/
│           ├── service/
│           ├── config/
│           └── presentation/
│
└── test/
    └── java/
        └── ...
```

The exact package structure may vary depending on the implementation.

## Banking Rules

The application enforces rules such as:

* Customers can only access accounts they are authorized to use.
* Withdrawals cannot exceed the available account balance.
* Transfers must use valid source and destination accounts.
* Invalid transactions are rejected.
* Account status must be validated before performing operations.
* Transaction information is recorded for banking activity.

## Future Enhancements

Potential future improvements could include:

* Additional account types
* More advanced transaction filtering
* Improved console formatting
* Additional automated tests
* Transaction pagination
* Additional authentication mechanisms
* Expanded reporting functionality

## Project Goals

The primary goals of this project are to demonstrate:

1. Clean separation of application responsibilities
2. Object-oriented Java development
3. Database abstraction using the DAO pattern
4. Relational and NoSQL database integration
5. Business logic validation
6. Exception handling
7. Automated testing
8. Configuration-based database selection

## License

This project was created for educational and development purposes.
