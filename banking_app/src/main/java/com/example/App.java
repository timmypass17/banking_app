package com.example;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.text.DateFormatter;

import com.example.models.Customer;
import com.example.services.CustomerDAO;
import com.example.services.PostgresCustomerDAO;
import com.example.utils.ConnectionUtil;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Hello world!
 *
 */
public class App 
{
    static Optional<Customer> customer = Optional.empty();

    public static void main( String[] args )
    {
        System.out.println( "Hello World! Timmy :)" );
        postgresConnectionSanityTest();
        Scanner scanner = new Scanner(System.in);
        CustomerDAO customerDao = new PostgresCustomerDAO();

        while (true) {
            if (customer.isPresent()) {
                showMain();
            } else {
                showLogin(scanner, customerDao);
            }
        }
    }

    public static void showLogin(Scanner scanner, CustomerDAO customerDao) {
        String message = 
            "Welcome\n"
            + "1. Login\n"
            + "2. Register\n";
        System.out.println(message);
        System.out.print("Please enter command: ");
        String option = scanner.nextLine();

        if (option.equals("1")) {
            String email;
            String password;

            System.out.print("Please enter email: ");
            email = scanner.nextLine();
            System.out.print("Please enter pasword: ");
            password = scanner.nextLine();

            Optional<Customer> optCustomer = customerDao.login(email, password);
            if (optCustomer.isPresent()) {
                customer = optCustomer;
            } 
        } else if (option.equals("2")) {
            String firstName;
            String lastName;
            String dobStr;
            LocalDate dob;
            String email;
            String password;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate dateOfBirth = LocalDate.parse("08/07/2026", formatter);

            System.out.print("Please enter first name: ");
            firstName = scanner.nextLine();
            System.out.print("Please enter last name: ");
            lastName = scanner.nextLine();
            while (true) {
                System.out.print("Please enter dob (MM/dd/yyyy): ");
                dobStr = scanner.nextLine();
                try {
                    dob = LocalDate.parse(dobStr, formatter);
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date of birth format.");
                }
            }
            System.out.print("Please enter email: ");
            email = scanner.nextLine();
            System.out.print("Please enter pasword: ");
            password = scanner.nextLine();
            Customer newCustomer = new Customer(firstName, lastName, dateOfBirth, email, password);
            Optional<Customer> optCustomer = customerDao.register(newCustomer);
        }
    }

    public static void showMain() {
        System.out.println("Show main");
    }

    public static void postgresConnectionSanityTest() {
        // This acts as a sanity test to make sure our connection is working
        try {
            ConnectionUtil.getConnection();
            System.out.println("Connection was successful");
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection was NOT sucessful");
        }
    }
}

// Welcome
// 1. login
// 2. register

// Please enter command: 1
// Enter email:
// Enter password:

// Login successful!

// Welcome
// 1. login
// 2. register
// Enter first name:
// Enter last name:
// Enter dob ("mm/dd/yyyy"):
// Enter email:
// Enter password:


// Welcome <user.name>!
// 1. View all accounts
// 2. Open Bank account (Create bank account, savings/checkings)
// 3. View transaction history
// 4. Update Profile

// (1)
// All my accounts
// 1. Bank Of America
// 2. Chase
// Please enter command: 1 (or back)
// Bank Of America
// Total Balance: $100
// Checking type: Savings
// 1. Deposit
// 2. Widthdraw
// 3. Transfer money to another accounts
// 4. Transfer moeny to another user
// 5. Close account

// (1.1)
// Deposit into "Bank Of America" how much money?
// Please enter amount: 100
// Successfully deposited $100 to "Bank of America".
// New Balance: $100

// (1.3)
// Transfer money from "Bank of America" into another account?
// 1. Chase

// Please enter command: 1
// You selected "Chase"
// Please enter amount to transfer: 100
// Successfully transfered $100 to "Chase".
// Bank of America New Balance: $0
// Chase New Balance: $100

// (3)
// View transaction history
// (8/7/2026) Deposit $100 to "Bank of America" 
// - New balance: $200
// (8/4/2026) Withdraw $50 from "Bank of America" 
// - New balance: $150
// (8/3/2026) Transfer $10 from "Bank of America" to "Chase" 
// - New Bank of America balance: $100
// - New chase balance: $50
// (8/3/2026) Transfer $15 to "Timmy"

// Filter by
// 1. Date (single date, or range)
// 2. Type (checking, savings, transfer)
// Please Enter Command: 2 checkings

// (4)
// Update Profile
// 1. Update first name
// 2. Update last name

// Please enter command: 1
// Enter new first name: Bob
// Successfully changed name to Bob
