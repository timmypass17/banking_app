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
import com.example.models.BankAccountType;
import com.example.models.Customer;
import com.example.models.Transaction;
import com.example.models.TransferResult;
import com.example.models.helpers.BankAccountSummary;
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
        LocalDate dateOfBirth;
        String email;
        String password;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        System.out.print("Please enter first name: ");
        firstName = scanner.nextLine();
        System.out.print("Please enter last name: ");
        lastName = scanner.nextLine();
        while (true) {
            System.out.print("Please enter dob (yyyy-MM-dd): ");
            dobStr = scanner.nextLine();
            try {
                dateOfBirth = LocalDate.parse(dobStr, formatter);
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
            handleViewAllAccounts();
        } else if (option.equals("2")) {
            handleOpenBankAccount();
        } else if (option.equals("3")) {
            handleTransactionHistory();
        } else if (option.equals("4")) {
            handleUpdateProfile();
        }
    }

    // (3)
    // View transaction history
    // (8/7/2026) Deposit $100 to Timmy's Bank of America
    // - Timmy's Bank of America New balance: $200
    // (8/4/2026) Withdraw $50 from Timmy's Bank of America 
    // - Timmy's Bank of America New balance: $150
    // (8/3/2026) Transfer $10 from Timmy's Bank of America to Jojo's Chase
    // - Timmy's Bank of America New balance: $100
    // - Jojo's Chase New balance: $50
    // (8/3/2026) Transfer $15 to Jojo's Chase
    // Filter By:
    // 1. By type (deposit, withdraw, transfer)
    // 2. By date (range)
    // Please enter command: 
    public void handleTransactionHistory() {
        System.out.println("View transaction history");

        try {
            int customerId = customer.get().getId();
            List<Transaction> transactions = bankDao.getTransactionHistory(customerId);

            for (Transaction transaction : transactions) {
                printTransaction(transaction, customerId);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void printTransaction(Transaction transaction, int customerId) {
        String date = transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("M/d/yyyy"));

        System.out.printf(
            "(%s) ",
            date
        );

        switch (transaction.getAction()) {
            case DEPOSIT: 
                System.out.printf(
                    "Deposit $%.2f to %s%n",
                    transaction.getDestinationAmount() / 100.0,
                    transaction.getDestinationBankName()
                );

                System.out.printf(
                    "- %s New balance: $%.2f%n",
                    transaction.getDestinationBankName(),
                    transaction.getDestinationResultBalance() / 100.0
                );
                break;
            case WITHDRAW:
                System.out.printf(
                    "Withdraw $%.2f from %s%n",
                    transaction.getSourceAmount() / 100.0,
                    transaction.getSourceBankName()
                );

                System.out.printf(
                    "- %s New balance: $%.2f%n",
                    transaction.getSourceBankName(),
                    transaction.getSourceResultBalance() / 100.0
                );
                break;
            case TRANSFER:
                boolean isSource = transaction.getSourceCustomerId() == customerId;

                if (isSource) {
                    System.out.printf(
                        "Transfer $%.2f from %s's %s to %s's %s%n",
                        transaction.getSourceAmount() / 100.0,
                        transaction.getSourceCustomerName(),
                        transaction.getSourceBankName(),
                        transaction.getDestinationCustomerName(),
                        transaction.getDestinationBankName()
                    );

                    System.out.printf(
                        "- %s's %s New balance: $%.2f%n",
                        transaction.getSourceCustomerName(),
                        transaction.getSourceBankName(),
                        transaction.getSourceResultBalance() / 100.0
                    );

                    System.out.printf(
                        "- %s's %s New balance: $%.2f%n",
                        transaction.getDestinationCustomerName(),
                        transaction.getDestinationBankName(),
                        transaction.getDestinationResultBalance() / 100.0
                    );
                } else {
                    System.out.printf(
                        "Received $%.2f from %s's %s to %s's %s%n",
                        transaction.getDestinationAmount() / 100.0,
                        transaction.getSourceCustomerName(),
                        transaction.getSourceBankName(),
                        transaction.getDestinationCustomerName(),
                        transaction.getDestinationBankName()
                    );

                    System.out.printf(
                        "- %s's %s New balance: $%.2f%n",
                        transaction.getDestinationCustomerName(),
                        transaction.getDestinationBankName(),
                        transaction.getDestinationResultBalance() / 100.0
                    );
                }
                break;
        }
    }

    public void handleUpdateProfile() {
        String message = 
            "Update profile:\n" 
            + "1. Update first name\n"
            + "2. Update last name\n"
            + "3. Update date of birth\n"
            + "4. Update email\n"
            + "5. Update password\n";
        System.out.println(message);
        System.out.print("Please enter command: ");
        String option = scanner.nextLine();

        if (option.equals("1")) {
            handleUpdateFirstName();
        } else if (option.equals("2")) {
            handleUpdateLastName();
        } else if (option.equals("3")) {
            handleUpdateDateOfBirth();
        } else if (option.equals("4")) {
            handleUpdateEmail();
        } else if (option.equals("5")) {
            handleUpdatePassword();
        } else {
            System.out.println("Invalid command.");
        }
    }

    public void handleUpdateFirstName() {
        System.out.print("Please enter new first name: ");
        String firstName = scanner.nextLine();

        Customer currentCustomer = customer.get();

        Customer newProfile = new Customer(
            currentCustomer.getId(),
            firstName,
            currentCustomer.getLastName(),
            currentCustomer.getDateOfBirth(),
            currentCustomer.getEmail(),
            currentCustomer.getPassword()
        );

        try {
            if (customerDao.updateProfile(currentCustomer.getId(), newProfile).isPresent()) {
                System.out.println("Successfully updated profile!");
                currentCustomer.setFirstName(firstName);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleUpdateLastName() {
        System.out.print("Please enter new last name: ");
        String lastName = scanner.nextLine();

        Customer currentCustomer = customer.get();

        Customer newProfile = new Customer(
            currentCustomer.getId(),
            currentCustomer.getFirstName(),
            lastName,
            currentCustomer.getDateOfBirth(),
            currentCustomer.getEmail(),
            currentCustomer.getPassword()
        );

        try {
            if (customerDao.updateProfile(currentCustomer.getId(), newProfile).isPresent()) {
                System.out.println("Successfully updated profile!");
                currentCustomer.setLastName(lastName);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleUpdateDateOfBirth() {
        System.out.print("Please enter new date of birth (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();

        LocalDate dateOfBirth;

        try {
            dateOfBirth = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            return;
        }

        Customer currentCustomer = customer.get();

        Customer newProfile = new Customer(
            currentCustomer.getId(),
            currentCustomer.getFirstName(),
            currentCustomer.getLastName(),
            dateOfBirth,
            currentCustomer.getEmail(),
            currentCustomer.getPassword()
        );

        try {
            if (customerDao.updateProfile(currentCustomer.getId(), newProfile).isPresent()) {
                System.out.println("Successfully updated profile!");
                currentCustomer.setDateOfBirth(dateOfBirth);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleUpdateEmail() {
        System.out.print("Please enter new email: ");
        String email = scanner.nextLine();

        Customer currentCustomer = customer.get();

        Customer newProfile = new Customer(
            currentCustomer.getId(),
            currentCustomer.getFirstName(),
            currentCustomer.getLastName(),
            currentCustomer.getDateOfBirth(),
            email,
            currentCustomer.getPassword()
        );

        try {
            if (customerDao.updateProfile(currentCustomer.getId(), newProfile).isPresent()) {
                System.out.println("Successfully updated profile!");
                currentCustomer.setEmail(email);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleUpdatePassword() {
        System.out.print("Please enter new password: ");
        String password = scanner.nextLine();

        Customer currentCustomer = customer.get();

        Customer newProfile = new Customer(
            currentCustomer.getId(),
            currentCustomer.getFirstName(),
            currentCustomer.getLastName(),
            currentCustomer.getDateOfBirth(),
            currentCustomer.getEmail(),
            password
        );

        try {
            Optional<Customer> updatedProfile = customerDao.updatePassword(currentCustomer.getId(), newProfile);
            if (updatedProfile.isPresent()) {
                System.out.println("Successfully updated password!");
                currentCustomer.setPassword(updatedProfile.get().getPassword());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // TODO: Make mongo version
    // TODO: Add tests to test dao (mock?)

    // View all accounts
    // 1. Bank Of America #0)
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

        if (bankAccountResult.isEmpty()) {
            System.out.println("No bank accounts opened.");
            return;
        }

        // Append to message
        System.out.print("Please enter command: ");
        String option = scanner.nextLine();
        int accountIndex = Integer.parseInt(option) - 1;
        
        int selectedAccountId = bankAccountResult.get(accountIndex).getBankAccountId();
        handleBankAccountDetail(selectedAccountId);
    }
    
    // Open bank account for
    // 1. Bank Of America
    // 2. Chase
    // Please enter command: 1
    // You selected Bank of America
    // Please select account type:
    // 1. Savings
    // 2. Checkings
    // Please enter account type: 1
    // You succesfully created savings account for Bank of America!
    public void handleOpenBankAccount() {
        // Get all banks
        List<Bank> banks;

        try {
            banks = bankDao.getAllBanks();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        // Display banks
        System.out.println("Open bank account for:");

        for (int i = 0; i < banks.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, banks.get(i).getName());
        }

        System.out.print("Please enter command: ");
        int bankChoice = Integer.parseInt(scanner.nextLine());

        if (bankChoice < 1 || bankChoice > banks.size()) {
            System.out.println("Invalid bank selection.");
            return;
        }

        Bank selectedBank = banks.get(bankChoice - 1);

        System.out.printf("You selected %s%n", selectedBank.getName());

        // Account type
        System.out.println("Please select account type:");
        System.out.println("1. Savings");
        System.out.println("2. Checking");

        System.out.print("Please enter account type: ");
        int accountTypeChoice = Integer.parseInt(scanner.nextLine());

        BankAccountType accountType;

        if (accountTypeChoice == 1) {
            accountType = BankAccountType.SAVINGS;
        } else if (accountTypeChoice == 2) {
            accountType = BankAccountType.CHECKINGS;
        } else {
            System.out.println("Invalid account type.");
            return;
        }

        // Create account
        try {
            bankDao.createBankAccount(new BankAccount(customer.get().getId(), selectedBank.getId(), accountType, 0, true));
            System.out.printf(
                "You successfully created %s account for %s!%n",
                accountType.toString().toLowerCase(),
                selectedBank.getName()
            );
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
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
                + "Is Active: " + account.getIsActive() + "\n"
                + "1. Deposit\n"
                + "2. Withdraw\n"
                + "3. Transfer money to another accounts\n"
                + "4. Transfer money to another user\n"
                + "5. Close/Reactive account\n";
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
            } else if (option.equals("4")) {
                handleTransferMoneyToAnotherUserAccount(bank, bankAccountId);
            } else if (option.equals("5")) {
                handleCloseReactiveAccount(account);
            }


        } catch (SQLException e) {
            // dao knows database fails, but service knows what to do about it so propagate it
            System.out.printf("Database error, failed to get bank account details id %d: %s%n", bankAccountId, e.getMessage());
        }
    }

    // Close account?
    public void handleCloseReactiveAccount(BankAccount account) {
        try {
            if (account.getIsActive()) {
                bankDao.closeAccount(account.getId());
            } else {
                bankDao.reactivateAccount(account.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
            long newBalance = bankDao.deposit(customer.get().getId(), bankAccountId, amount);
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
            TransferResult result = bankDao.transferMoney(amount, sourceAccountId, destinationAccountId, customer.get().getId());
            System.out.printf("Successfully transfered $%s to %s.%n", dollars, bankAccountResult.get(accountIndex).getBankName());
            System.out.printf("%s New Balance: $%.2f%n", bank.getName(), result.getSourceNewBalance() / 100.0);
            System.out.printf("%s New Balance: $%.2f%n", bankAccountResult.get(accountIndex).getBankName(), result.getDestinationNewBalance() / 100.0);
        } catch (InvalidAmountException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.printf("Database error, failed to transfeer: %s%n", e.getMessage());
        }
    }

    // Transfer money to another user's account?
    // 1. Person A (Bank A)
    // 2. Person A (Bank B)
    // 3. Person B (Bank A)
    public void handleTransferMoneyToAnotherUserAccount(Bank bank, int sourceAccountId) {
        System.out.printf(
            "Transfer money from %s into another user's account?%n",
            bank.getName()
        );

        // Fetch all bank accounts
        List<BankAccountSummary> accounts;

        try {
            accounts = bankDao.getAllBankAccountsSummary();
        } catch (SQLException e) {
            System.out.printf(
                "Database error, failed to retrieve accounts: %s%n",
                e.getMessage()
            );
            return;
        }

        // Remove accounts belonging to the current user
        int currentCustomerId = customer.get().getId();

        accounts.removeIf(
            account -> account.getCustomerId() == currentCustomerId
        );

        // Display available destination accounts
        for (int i = 0; i < accounts.size(); i++) {
            BankAccountSummary account = accounts.get(i);

            System.out.printf(
                "%d. %s (%s #%d)%n",
                i + 1,
                account.getCustomerName(),
                account.getBankName(),
                account.getBankAccountId()
            );
        }

        System.out.print("Please enter command: ");
        int accountIndex = Integer.parseInt(scanner.nextLine()) - 1;

        if (accountIndex < 0 || accountIndex >= accounts.size()) {
            System.out.println("Invalid account selection.");
            return;
        }

        BankAccountSummary destinationAccount = accounts.get(accountIndex);

        System.out.print("Please enter amount: ");

        BigDecimal dollars;

        try {
            dollars = new BigDecimal(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        long amount;

        try {
            amount = dollars
                .movePointRight(2)
                .longValueExact();
        } catch (ArithmeticException e) {
            System.out.println("Amount must have at most 2 decimal places.");
            return;
        }

        try {
            TransferResult result = bankDao.transferMoney(
                amount,
                sourceAccountId,
                destinationAccount.getBankAccountId(),
                currentCustomerId
            );

            System.out.printf(
                "Successfully transferred $%.2f to %s's %s.%n",
                dollars,
                destinationAccount.getCustomerName(),
                destinationAccount.getBankName()
            );

            System.out.printf(
                "%s's %s New Balance: $%.2f%n",
                customer.get().getFirstName(),
                bank.getName(),
                result.getSourceNewBalance() / 100.0
            );

            System.out.printf(
                "%s's %s New Balance: $%.2f%n",
                destinationAccount.getCustomerName(),
                destinationAccount.getBankName(),
                result.getDestinationNewBalance() / 100.0
            );

        } catch (InvalidAmountException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.printf(
                "Database error, failed to transfer: %s%n",
                e.getMessage()
            );
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

            long newBalance = bankDao.withdraw(bankAccountId, amount, customer.get().getId());
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
// (8/7/2026) Deposit $100 to Timmy's Bank of America
// - Timmy's new balance: $200
// (8/4/2026) Withdraw $50 from Timmy's Bank of America 
// - New balance: $150
// (8/3/2026) Transfer $10 from Timmy's Bank of America to Jojo's Chase
// - New Bank of America balance: $100
// - New chase balance: $50
// (8/3/2026) Transfer $15 to Jojo's Chase

// Transaction

// customerId: Int
// createdAt: LocalDateTime (date and time) TIMESTAMP on postgres
// action: Enum (DEPOSIT, WITHDRAW, TRANSFER)
// sourceAmount: Long
// sourceBankId: Int
// sourceResultBalance: Long
// sourceCustomerId: Int

// destinationAmount: Long
// destinationBankId: Int
// destinationResultBalance: Long
// destinationCustomerId: Int

// (<createdAt date> <action | DEPOSIT, WITHDRAW, TRANSFER>)

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
