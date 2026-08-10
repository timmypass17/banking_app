package com.example.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.exceptions.InvalidAmountException;
import com.example.models.Bank;
import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.utils.ConnectionUtil;
import com.example.models.BankAccountType;
import com.example.models.Transaction;
import com.example.models.TransactionAction;
import com.example.models.TransferResult;
import com.example.models.helpers.BankAccountSummary;

public class PostgresBankDAO implements BankDAO {
    @Override
    public List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(int customerId) {
        /*
         * This implementation showcases how a Simple Statement
         * Object works - this is used because there is no
         * fear of SQL injection (the query takes no user input)
         */
        List<BankAccountByCustomerResult> bankAccounts = new ArrayList<BankAccountByCustomerResult>();
        String query = "SELECT ba.id AS bank_account_id, b.name AS bank_name \n" +
                        "FROM bank_accounts AS ba\n" +
                        "JOIN banks AS b ON ba.bank_id = b.id\n" +
                        "WHERE ba.customer_id = ?;";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ){
            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    bankAccounts.add(
                            new BankAccountByCustomerResult(
                                rs.getInt("bank_account_id"),
                                rs.getString("bank_name")
                            )
                    );
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return bankAccounts;
    }
    
    @Override
    public Optional<BankAccount> getBankAccountById(int bankAccountId) throws SQLException {
        String query = "SELECT * FROM bank_accounts WHERE id = ?";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, bankAccountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                        new BankAccount(
                            rs.getInt("id"),
                            rs.getInt("customer_id"),
                            rs.getInt("bank_id"),
                            BankAccountType.valueOf(rs.getString("bank_account_type")),
                            rs.getLong("balance"),
                            rs.getBoolean("is_active")
                        )
                    );
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Bank> getBankById(int bankId) throws SQLException {
        String query = "SELECT * FROM banks WHERE id = ?";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, bankId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                        new Bank(
                            rs.getInt("id"),
                            rs.getString("name")
                        )
                    );
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public long deposit(int customerId, int bankAccountId, long amount) throws SQLException {
        try (Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long balance = deposit(conn, bankAccountId, amount);
                createDepositTransaction(conn, bankAccountId, amount, balance);
                conn.commit();
                return balance;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void createDepositTransaction(Connection conn, int accountId, long amount, long resultBalance) throws SQLException {
        String query =
            "INSERT INTO transactions (" +
                "action, " +
                "destination_amount, " +
                "destination_account_id, " +
                "destination_result_balance " +
            ") VALUES (?::transaction_action, ?, ?, ?)";

        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, TransactionAction.DEPOSIT.name());
            ps.setLong(2, amount);
            ps.setInt(3, accountId);
            ps.setLong(4, resultBalance);

            ps.executeUpdate();
        }
    }

    public void createWithdrawTransaction(Connection conn, int accountId, long amount, long resultBalance) throws SQLException {
        String query =
            "INSERT INTO transactions (" +
                "action, " +
                "source_amount, " +
                "source_account_id, " +
                "source_result_balance " +
            ") VALUES (?::transaction_action, ?, ?, ?)";

        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, TransactionAction.WITHDRAW.name());
            ps.setLong(2, amount);
            ps.setInt(3, accountId);
            ps.setLong(4, resultBalance);

            ps.executeUpdate();
        }
    }

    public void createTransferTransaction(
        Connection conn,
        long amount,
        int sourceAccountId,
        long sourceResultBalance,
        int destinationAccountId,
        long destinationResultBalance
    ) throws SQLException {

        String query =
            "INSERT INTO transactions (" +
                "action, " +
                "source_amount, " +
                "source_account_id, " +
                "source_result_balance, " +
                "destination_amount, " +
                "destination_account_id, " +
                "destination_result_balance " +
            ") VALUES (?::transaction_action, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, TransactionAction.TRANSFER.name());

            // Source
            ps.setLong(2, amount);
            ps.setInt(3, sourceAccountId);
            ps.setLong(4, sourceResultBalance);

            // Destination
            ps.setLong(5, amount);
            ps.setInt(6, destinationAccountId);
            ps.setLong(7, destinationResultBalance);

            ps.executeUpdate();
        }
    }

    @Override
    public long withdraw(int bankAccountId, long amount, int customerId) throws SQLException {
        try (Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                long balance = withdraw(conn, bankAccountId, amount, customerId);
                createWithdrawTransaction(conn, bankAccountId, amount, balance);
                conn.commit();
                return balance;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private long deposit(Connection conn, int bankAccountId, long amount) throws SQLException {

        String depositQuery = "UPDATE bank_accounts SET balance = balance + ? WHERE id = ?";
        String balanceQuery = "SELECT balance FROM bank_accounts WHERE id = ?";

        try (
            PreparedStatement depositPs = conn.prepareStatement(depositQuery);
            PreparedStatement balancePs = conn.prepareStatement(balanceQuery)
        ) {
            depositPs.setLong(1, amount);
            depositPs.setInt(2, bankAccountId);

            int rowsAffected = depositPs.executeUpdate();

            if (rowsAffected == 0) {
                throw new IllegalArgumentException(
                    "Bank account not found: " + bankAccountId
                );
            }

            balancePs.setInt(1, bankAccountId);

            try (ResultSet rs = balancePs.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("balance");
                }
            }

            throw new SQLException("Failed to retrieve updated balance");
        }
    }

    // Only source account needs ownership check for withdraw
    private long withdraw(Connection conn, int bankAccountId, long amount, int customerId) throws SQLException {

        String withdrawQuery =
            "UPDATE bank_accounts " +
            "SET balance = balance - ? " +
            "WHERE id = ? " +
            "AND customer_id = ? " +
            "AND balance >= ?";
        
        String balanceQuery =
            "SELECT balance FROM bank_accounts WHERE id = ?";

        try (
            PreparedStatement withdrawPs = conn.prepareStatement(withdrawQuery);
            PreparedStatement balancePs = conn.prepareStatement(balanceQuery)
        ) {
            withdrawPs.setLong(1, amount);
            withdrawPs.setInt(2, bankAccountId);
            withdrawPs.setInt(3, customerId);
            withdrawPs.setLong(4, amount);

            int rowsAffected = withdrawPs.executeUpdate();

            if (rowsAffected == 0) {
                throw new InvalidAmountException(
                    "Bank account not found or insufficient funds or account does not belong to current user: "
                        + bankAccountId
                );
            }

            balancePs.setInt(1, bankAccountId);

            try (ResultSet rs = balancePs.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("balance");
                }
            }

            throw new SQLException("Failed to retrieve updated balance");
        }
    }

    @Override
    public TransferResult transferMoney(long amount, int sourceAccountId, int destinationAccountId, int customerId) throws SQLException {

        try (Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long sourceBalance = withdraw(conn, sourceAccountId, amount, customerId);
                long destinationBalance = deposit(conn, destinationAccountId, amount);
                createTransferTransaction(conn, amount, sourceAccountId, sourceBalance, destinationAccountId, destinationBalance);
                conn.commit();

                return new TransferResult(sourceBalance, destinationBalance);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<BankAccountSummary> getAllBankAccountsSummary() throws SQLException {
        List<BankAccountSummary> bankAccounts = new ArrayList<BankAccountSummary>();
        String query =
            "SELECT ba.id AS bank_account_id, " +
                "b.name AS bank_name, " +
                "c.id AS customer_id, " +
                "c.first_name AS customer_name " +
            "FROM bank_accounts AS ba " +
            "JOIN banks AS b ON ba.bank_id = b.id " +
            "JOIN customers AS c ON ba.customer_id = c.id";
                                
        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ){
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    bankAccounts.add(
                        new BankAccountSummary(
                            rs.getInt("bank_account_id"),
                            rs.getString("bank_name"),
                            rs.getInt("customer_id"),
                            rs.getString("customer_name")
                        )
                    );
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return bankAccounts;
    }
    
    @Override
    public List<Bank> getAllBanks() throws SQLException {
        List<Bank> banks = new ArrayList<Bank>();
        String query = "SELECT * FROM banks";
                                
        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ){
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    banks.add(
                        new Bank(
                            rs.getInt("id"),
                            rs.getString("name")
                        )
                    );
                }
            }
        }

        return banks;
    }

    @Override
    public Optional<BankAccount> createBankAccount(BankAccount bankAccount)
            throws SQLException {

        String query =
            "INSERT INTO bank_accounts " +
            "(customer_id, bank_id, bank_account_type, balance) " +
            "VALUES (?, ?, ?::bank_account_type, ?)";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setInt(1, bankAccount.getCustomerId());
            ps.setInt(2, bankAccount.getBankId());
            ps.setString(3, bankAccount.getAccountType().name());
            ps.setLong(4, bankAccount.getBalance());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                return Optional.empty();
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);

                    return Optional.of(
                        new BankAccount(
                            generatedId,
                            bankAccount.getCustomerId(),
                            bankAccount.getBankId(),
                            bankAccount.getAccountType(),
                            bankAccount.getBalance(),
                            bankAccount.getIsActive()
                        )
                    );
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Transaction> getTransactionHistory(int customerId) throws SQLException {
        List<Transaction> transactions = new ArrayList<Transaction>();

        String query =
            "SELECT " +
                "t.*, " +

                // Source
                "sc.id AS source_customer_id, " +
                "sb.id AS source_bank_id, " +
                "sc.first_name AS source_customer_name, " +
                "sb.name AS source_bank_name, " +

                // Destination
                "dc.id AS destination_customer_id, " +
                "db.id AS destination_bank_id, " +
                "dc.first_name AS destination_customer_name, " +
                "db.name AS destination_bank_name " +

            "FROM transactions AS t " +

            "LEFT JOIN bank_accounts AS sa " +
                "ON t.source_account_id = sa.id " +

            "LEFT JOIN bank_accounts AS da " +
                "ON t.destination_account_id = da.id " +

            "LEFT JOIN customers AS sc " +
                "ON sa.customer_id = sc.id " +

            "LEFT JOIN customers AS dc " +
                "ON da.customer_id = dc.id " +

            "LEFT JOIN banks AS sb " +
                "ON sa.bank_id = sb.id " +

            "LEFT JOIN banks AS db " +
                "ON da.bank_id = db.id " +

            "WHERE sc.id = ? OR dc.id = ? " +

            "ORDER BY t.created_at DESC";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, customerId);
            ps.setInt(2, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        TransactionAction.valueOf(
                            rs.getString("action")
                        ),

                        rs.getObject("source_amount") != null
                            ? rs.getLong("source_amount")
                            : null,

                        rs.getObject("source_bank_id") != null
                            ? rs.getInt("source_bank_id")
                            : null,

                        rs.getObject("source_result_balance") != null
                            ? rs.getLong("source_result_balance")
                            : null,

                        rs.getObject("destination_amount") != null
                            ? rs.getLong("destination_amount")
                            : null,

                        rs.getObject("destination_bank_id") != null
                            ? rs.getInt("destination_bank_id")
                            : null,

                        rs.getObject("destination_result_balance") != null
                            ? rs.getLong("destination_result_balance")
                            : null
                    );

                    transaction.setSourceCustomerId(
                        rs.getInt("source_customer_id")
                    );

                    transaction.setSourceCustomerName(
                        rs.getString("source_customer_name")
                    );

                    transaction.setSourceCustomerId(
                        rs.getInt("source_bank_id")
                    );

                    transaction.setSourceBankName(
                        rs.getString("source_bank_name")
                    );

                    transaction.setDestinationCustomerId(
                        rs.getInt("destination_customer_id")
                    );
                    
                    transaction.setDestinationCustomerName(
                        rs.getString("destination_customer_name")
                    );

                    transaction.setDestinationBankId(
                        rs.getInt("destination_bank_id")
                    );

                    transaction.setDestinationBankName(
                        rs.getString("destination_bank_name")
                    );

                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }
    
    @Override
    public boolean closeAccount(int bankAccountId) throws SQLException {
        String query =
            "UPDATE bank_accounts " +
            "SET is_active = FALSE " +
            "WHERE id = ? " +
            "AND is_active = TRUE";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, bankAccountId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean reactivateAccount(int bankAccountId) throws SQLException {
        String query =
            "UPDATE bank_accounts " +
            "SET is_active = TRUE " +
            "WHERE id = ? " +
            "AND is_active = FALSE";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, bankAccountId);

            return ps.executeUpdate() > 0;
        }
    }
}
