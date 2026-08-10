package com.example.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(String customerId) {
        List<BankAccountByCustomerResult> bankAccounts = new ArrayList<BankAccountByCustomerResult>();
        String query = "SELECT ba.id AS bank_account_id, b.name AS bank_name \n" +
                        "FROM bank_accounts AS ba\n" +
                        "JOIN banks AS b ON ba.bank_id = b.id\n" +
                        "WHERE ba.customer_id = ?;";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ){
            ps.setString(1, customerId);

            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    bankAccounts.add(
                            new BankAccountByCustomerResult(
                                rs.getString("bank_account_id"),
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
    public Optional<BankAccount> getBankAccountById(String bankAccountId) throws SQLException {
        String query = "SELECT * FROM bank_accounts WHERE id = ?";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, bankAccountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                        new BankAccount(
                            rs.getString("id"),
                            rs.getString("customer_id"),
                            rs.getString("bank_id"),
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
    public Optional<Bank> getBankById(String bankId) throws SQLException {
        String query = "SELECT * FROM banks WHERE id = ?";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, bankId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                        new Bank(
                            rs.getString("id"),
                            rs.getString("name")
                        )
                    );
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public long deposit(String customerId, String bankAccountId, long amount) throws SQLException {
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

    public void createDepositTransaction(Connection conn, String accountId, long amount, long resultBalance) throws SQLException {
        String query =
            "INSERT INTO transactions (" +
                "id, " +
                "action, " +
                "destination_amount, " +
                "destination_account_id, " +
                "destination_result_balance " +
            ") VALUES (?, ?::transaction_action, ?, ?, ?)";

        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, TransactionAction.DEPOSIT.name());
            ps.setLong(3, amount);
            ps.setString(4, accountId);
            ps.setLong(5, resultBalance);

            ps.executeUpdate();
        }
    }

    public void createWithdrawTransaction(Connection conn, String accountId, long amount, long resultBalance) throws SQLException {
        String query =
            "INSERT INTO transactions (" +
                "id, " +
                "action, " +
                "source_amount, " +
                "source_account_id, " +
                "source_result_balance " +
            ") VALUES (?, ?::transaction_action, ?, ?, ?)";

        try (
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, TransactionAction.WITHDRAW.name());
            ps.setLong(3, amount);
            ps.setString(4, accountId);
            ps.setLong(5, resultBalance);

            ps.executeUpdate();
        }
    }

    public void createTransferTransaction(
        Connection conn,
        long amount,
        String sourceAccountId,
        long sourceResultBalance,
        String destinationAccountId,
        long destinationResultBalance
    ) throws SQLException {

        String query =
            "INSERT INTO transactions (" +
                "id, " +
                "action, " +
                "source_amount, " +
                "source_account_id, " +
                "source_result_balance, " +
                "destination_amount, " +
                "destination_account_id, " +
                "destination_result_balance " +
            ") VALUES (?, ?::transaction_action, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, TransactionAction.TRANSFER.name());

            // Source
            ps.setLong(3, amount);
            ps.setString(4, sourceAccountId);
            ps.setLong(5, sourceResultBalance);

            // Destination
            ps.setLong(6, amount);
            ps.setString(7, destinationAccountId);
            ps.setLong(8, destinationResultBalance);

            ps.executeUpdate();
        }
    }

    @Override
    public long withdraw(String bankAccountId, long amount, String customerId) throws SQLException {
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

    private long deposit(Connection conn, String bankAccountId, long amount) throws SQLException {

        String depositQuery = "UPDATE bank_accounts SET balance = balance + ? WHERE id = ?";
        String balanceQuery = "SELECT balance FROM bank_accounts WHERE id = ?";

        try (
            PreparedStatement depositPs = conn.prepareStatement(depositQuery);
            PreparedStatement balancePs = conn.prepareStatement(balanceQuery)
        ) {
            depositPs.setLong(1, amount);
            depositPs.setString(2, bankAccountId);

            int rowsAffected = depositPs.executeUpdate();

            if (rowsAffected == 0) {
                throw new IllegalArgumentException(
                    "Bank account not found: " + bankAccountId
                );
            }

            balancePs.setString(1, bankAccountId);

            try (ResultSet rs = balancePs.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("balance");
                }
            }

            throw new SQLException("Failed to retrieve updated balance");
        }
    }

    // Only source account needs ownership check for withdraw
    private long withdraw(Connection conn, String bankAccountId, long amount, String customerId) throws SQLException {

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
            withdrawPs.setString(2, bankAccountId);
            withdrawPs.setString(3, customerId);
            withdrawPs.setLong(4, amount);

            int rowsAffected = withdrawPs.executeUpdate();

            if (rowsAffected == 0) {
                throw new InvalidAmountException(
                    "Bank account not found or insufficient funds or account does not belong to current user: "
                        + bankAccountId
                );
            }

            balancePs.setString(1, bankAccountId);

            try (ResultSet rs = balancePs.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("balance");
                }
            }

            throw new SQLException("Failed to retrieve updated balance");
        }
    }

    @Override
    public TransferResult transferMoney(long amount, String sourceAccountId, String destinationAccountId, String customerId) throws SQLException {

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
                            rs.getString("bank_account_id"),
                            rs.getString("bank_name"),
                            rs.getString("customer_id"),
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
                            rs.getString("id"),
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
            "(id, customer_id, bank_id, bank_account_type, balance) " +
            "VALUES (?, ?, ?, ?::bank_account_type, ?)";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            String id = UUID.randomUUID().toString();
            ps.setString(1, id);
            ps.setString(2, bankAccount.getCustomerId());
            ps.setString(3, bankAccount.getBankId());
            ps.setString(4, bankAccount.getAccountType().name());
            ps.setLong(5, bankAccount.getBalance());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                return Optional.empty();
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return Optional.of(
                        new BankAccount(
                            id,
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
    public List<Transaction> getTransactionHistory(
        String customerId,
        TransactionAction action,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) throws SQLException {

        List<Transaction> transactions = new ArrayList<Transaction>();

        StringBuilder query = new StringBuilder(
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

            "WHERE (sc.id = ? OR dc.id = ?)"
        );

        List<Object> params = new ArrayList<Object>();

        // Customer ID
        params.add(customerId);
        params.add(customerId);

        // Filter by transaction type
        if (action != null) {
            query.append(" AND t.action = ?::transaction_action");
            params.add(action.name());
        }

        // Filter by start date
        if (startDate != null) {
            query.append(" AND t.created_at >= ?");
            params.add(Timestamp.valueOf(startDate));
        }

        // Filter by end date
        if (endDate != null) {
            query.append(" AND t.created_at <= ?");
            params.add(Timestamp.valueOf(endDate));
        }

        query.append(" ORDER BY t.created_at DESC");

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query.toString())
        ) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Transaction transaction = new Transaction(
                        rs.getString("id"),

                        rs.getTimestamp("created_at")
                            .toLocalDateTime(),

                        TransactionAction.valueOf(
                            rs.getString("action")
                        ),

                        // Source amount
                        rs.getObject("source_amount") != null
                            ? rs.getLong("source_amount")
                            : null,

                        // Source account ID
                        rs.getObject("source_account_id") != null
                            ? rs.getString("source_account_id")
                            : null,

                        // Source result balance
                        rs.getObject("source_result_balance") != null
                            ? rs.getLong("source_result_balance")
                            : null,

                        // Destination amount
                        rs.getObject("destination_amount") != null
                            ? rs.getLong("destination_amount")
                            : null,

                        // Destination account ID
                        rs.getObject("destination_account_id") != null
                            ? rs.getString("destination_account_id")
                            : null,

                        // Destination result balance
                        rs.getObject("destination_result_balance") != null
                            ? rs.getLong("destination_result_balance")
                            : null
                    );

                    // Source customer
                    if (rs.getObject("source_customer_id") != null) {
                        transaction.setSourceCustomerId(
                            rs.getString("source_customer_id")
                        );
                    }

                    transaction.setSourceCustomerName(
                        rs.getString("source_customer_name")
                    );

                    // Source bank
                    if (rs.getObject("source_bank_id") != null) {
                        transaction.setSourceBankId(
                            rs.getString("source_bank_id")
                        );
                    }

                    transaction.setSourceBankName(
                        rs.getString("source_bank_name")
                    );

                    // Destination customer
                    if (rs.getObject("destination_customer_id") != null) {
                        transaction.setDestinationCustomerId(
                            rs.getString("destination_customer_id")
                        );
                    }

                    transaction.setDestinationCustomerName(
                        rs.getString("destination_customer_name")
                    );

                    // Destination bank
                    if (rs.getObject("destination_bank_id") != null) {
                        transaction.setDestinationBankId(
                            rs.getString("destination_bank_id")
                        );
                    }

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
    public boolean closeAccount(String bankAccountId) throws SQLException {
        String query =
            "UPDATE bank_accounts " +
            "SET is_active = FALSE " +
            "WHERE id = ? " +
            "AND is_active = TRUE";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, bankAccountId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean reactivateAccount(String bankAccountId) throws SQLException {
        String query =
            "UPDATE bank_accounts " +
            "SET is_active = TRUE " +
            "WHERE id = ? " +
            "AND is_active = FALSE";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, bankAccountId);

            return ps.executeUpdate() > 0;
        }
    }
}
