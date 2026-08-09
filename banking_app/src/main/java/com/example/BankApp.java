package com.example;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.example.exceptions.InvalidAmountException;
import com.example.models.Bank;
import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Customer;
import com.example.models.TransferResult;
import com.example.services.BankDAO;
import com.example.services.CustomerDAO;
import com.example.utils.ConnectionUtil;

/**
 * Hello world!
 *
 */
public class BankApp {

    private Optional<Customer> customer = Optional.empty();

    private Scanner scanner;
    private CustomerDAO customerDao;
    private BankDAO bankDao;

    public BankApp(Scanner scanner, CustomerDAO customerDao, BankDAO bankDao) {
        this.scanner = scanner;
        this.customerDao = customerDao;
        this.bankDao = bankDao;
    }

    public void start()
    {
        System.out.println( "Hello World! Timmy :)" );
        postgresConnectionSanityTest();
        while (true) {
            if (customer.isPresent()) {
                showMain();
            } else {
                showLogin();
            }
        }
    }

    public void showLogin() {
        String message = 
            "Welcome\n"
            + "1. Login\n"
            + "2. Register\n";
        System.out.println(message);
        System.out.print("Please enter command: ");
        String option = scanner.nextLine();

        if (option.equals("1")) {
            handleLogin();
        } else if (option.equals("2")) {
            handleRegistration();
        }
    }

    private void handleLogin() {
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
    }
    
    private void handleRegistration() {
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

    // Welcome <user.name>!
    // 1. View all accounts
    // 2. Open Bank account (Create bank account, savings/checkings)
    // 3. View transaction history
    // 4. Update Profile
    public void showMain() {
        String message = 
            "Welcome, " + customer.get().getFirstName() + "!\n" 
            + "1. View all accounts\n"
            + "2. Open Bank account\n"
            + "3. View transaction history\n"
            + "4. Update profile";
        System.out.println(message);
        System.out.print("Please enter command: ");
        String option = scanner.nextLine();

        if (option.equals("1")) {
            // get all account
            handleViewAllAccounts();
        }
    }

    // View all accounts
    // 1. Bank Of America (#0)
    // 2. Chase (#1)
    // 2. Chase (#2)
    public void handleViewAllAccounts() {
        String message = 
            "My bank accounts\n";
        System.out.println(message);

        StringBuilder sb = new StringBuilder();
        // Fetch all accounts from user
        List<BankAccountByCustomerResult> bankAccountResult = bankDao.getAllBankAccountsByCustomerId(customer.get().getId());
        for (int i = 0; i < bankAccountResult.size(); i++) {
            BankAccountByCustomerResult account = bankAccountResult.get(i);
            sb.append(String.format("%d. %s (#%d)\n", i + 1, account.getBankName(), account.getBankAccountId()));
        }

        System.out.println(sb.toString());
        // Append to message
        System.out.print("Please enter command: ");
        String option = scanner.nextLine();
        int accountIndex = Integer.parseInt(option) - 1;
        
        int selectedAccountId = bankAccountResult.get(accountIndex).getBankAccountId();
        handleBankAccountDetail(selectedAccountId);
    }

    // Please enter command: 1 (or back)
    // Bank Of America
    // Total Balance: $100
    // Account type: (Savings | Checkings)
    // 1. Deposit
    // 2. Widthdraw
    // 3. Transfer money to another accounts
    // 4. Transfer moeny to another user
    // 5. Close account
    public void handleBankAccountDetail(int bankAccountId) {
        // get bank account by id
        try {
            BankAccount account = null;
            Bank bank = null;

            Optional<BankAccount> optAccount = bankDao.getBankAccountById(bankAccountId);
            if (!optAccount.isPresent()) {
                System.out.printf("Bank account with id %d not found", bankAccountId);
                return;
            }

            account = optAccount.get();
            Optional<Bank> optBank = bankDao.getBankById(account.getBankId());  // optimization: could've made one query using join

            if (!optBank.isPresent()) {
                System.out.printf("Bank with id %d not found", bankAccountId);
                return;
            }

            bank = optBank.get();

            String message = 
                bank.getName() + "\n"
                + "Total balance: " + account.getBalanceFormatted() + "\n"
                + "Account Type: " + account.getAccountType() + "\n"
                + "1. Deposit\n"
                + "2. Withdraw\n"
                + "3. Transfer money to another accounts\n"
                + "4. Transfer money to another user\n"
                + "5. Close account\n";
            System.out.println(message);

            System.out.print("Please enter command: ");
            String option = scanner.nextLine();
            int accountIndex = Integer.parseInt(option) - 1;

        
            // TODO: implement 5 features
            if (option.equals("1")) {
                handleDeposit(bank.getName(), bankAccountId);
            } else if (option.equals("2")) {
                handleWithdraw(bank.getName(), bankAccountId);
            } else if (option.equals("3")) {
                handleTransferMoneyToAnotherAccount(bank, bankAccountId);
            }


        } catch (SQLException e) {
            // dao knows database fails, but service knows what to do about it so propagate it
            System.out.printf("Database error, failed to get bank account details id %d: %s%n", bankAccountId, e.getMessage());
        }
    }

    // (1.1)
    // Deposit into "Bank Of America" how much money?
    // Please enter amount: 100
    // Successfully deposited $100 to "Bank of America".
    // New Balance: $100
    public void handleDeposit(String bankName, int bankAccountId) {
        System.out.println("Deposit into " + bankName + " how much money?");
        System.out.print("Please enter amount: ");

        BigDecimal dollars = new BigDecimal(scanner.nextLine());
        long amount = dollars
            .movePointRight(2)
            .longValueExact();
        
        try {
            long newBalance = bankDao.deposit(bankAccountId, amount);
            System.out.printf("Successfully deposited $%s to \"%s\".%n", dollars, bankName);
            System.out.printf("New Balance: $%.2f%n", newBalance / 100.0);
        } catch (SQLException e) {
            // dao knows database fails, but service knows what to do about it so propagate it
            System.out.printf("Database error, failed to deposit into %s: %s%n", bankName, e.getMessage());
        }
    }

    // (1.3)
    // Transfer money from "Bank of America" into another account?
    // 1. Chase

    // Please enter command: 1
    // You selected "Chase"
    // Please enter amount to transfer: 100
    // Successfully transfered $100 to "Chase".
    // Bank of America New Balance: $0
    // Chase New Balance: $100
    public void handleTransferMoneyToAnotherAccount(Bank bank, int sourceAccountId) {
        // Fetch all other banks thats not the selected one

        System.out.printf("Transfer money from %s into another account?%n", bank.getName());

        StringBuilder sb = new StringBuilder();
        // Fetch all accounts from user
        List<BankAccountByCustomerResult> bankAccountResult = bankDao.getAllBankAccountsByCustomerId(customer.get().getId());
        bankAccountResult.removeIf(account -> account.getBankAccountId() == sourceAccountId); //

        for (int i = 0; i < bankAccountResult.size(); i++) {
            BankAccountByCustomerResult account = bankAccountResult.get(i);
            sb.append(String.format("%d. %s (#%d)\n", i + 1, account.getBankName(), account.getBankAccountId()));
        }

        System.out.println(sb.toString());
        // Append to message
        System.out.print("Please enter command: ");
        String option = scanner.nextLine();
        int accountIndex = Integer.parseInt(option) - 1;
        
        int destinationAccountId = bankAccountResult.get(accountIndex).getBankAccountId();

        System.out.print("Please enter amount: ");

        BigDecimal dollars = new BigDecimal(scanner.nextLine());
        long amount = dollars
            .movePointRight(2)
            .longValueExact();

        try {
            TransferResult result = bankDao.transferMoney(amount, sourceAccountId, destinationAccountId);
            System.out.printf("Successfully transfered $%s to %s.%n", dollars, bankAccountResult.get(accountIndex).getBankName());
            System.out.printf("%s New Balance: $%.2f%n", bank.getName(), result.getSourceNewBalance() / 100.0);
            System.out.printf("%s New Balance: $%.2f%n", bankAccountResult.get(accountIndex).getBankName(), result.getDestinationNewBalance() / 100.0);
        } catch (SQLException e) {
            System.out.printf("Database error, failed to transfeer: %s%n", e.getMessage());
        }
    }

    // (1.1)
    // Withdraw from "Bank Of America" how much money?
    // Please enter amount: 100
    // Successfully withdrew $100 to "Bank of America".
    // New Balance: $100
    public void handleWithdraw(String bankName, int bankAccountId) {
        System.out.println("Withdraw from " + bankName + " how much money?");
        System.out.print("Please enter amount: ");

        // use bigdecimal for currency to avoid decimal arithmetic rounding issues (later convert to pennies)
        BigDecimal dollars = new BigDecimal(scanner.nextLine());
        long amount = dollars
            .movePointRight(2)
            .longValueExact();

        try {
            if (amount <= 0) {
                throw new InvalidAmountException("Withdrawal amount must be positive");
            }

            long newBalance = bankDao.withdraw(bankAccountId, amount);
            System.out.printf("Successfully withdrew $%s to \"%s\".%n", dollars, bankName);
            System.out.printf("New Balance: $%.2f%n", newBalance / 100.0);
        } catch (InvalidAmountException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            // dao knows database fails, but service knows what to do about it so propagate it
            System.out.printf("Database error, failed to withdraw from %s: %s%n", bankName, e.getMessage());
        }
    }

    public void postgresConnectionSanityTest() {
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
