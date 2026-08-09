package com.example.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.exceptions.InvalidAmountException;
import com.example.models.Bank;
import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.utils.ConnectionUtil;
import com.example.models.BankAccountType;
import com.example.models.TransferResult;

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
                            rs.getLong("balance")
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
    public long deposit(int bankAccountId, long amount) throws SQLException {
        try (Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long balance = deposit(conn, bankAccountId, amount);
                conn.commit();
                return balance;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public long withdraw(int bankAccountId, long amount) throws SQLException {
        try (Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long balance = withdraw(conn, bankAccountId, amount);
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

    private long withdraw(Connection conn, int bankAccountId, long amount) throws SQLException {

        String withdrawQuery =
            "UPDATE bank_accounts " +
            "SET balance = balance - ? " +
            "WHERE id = ? AND balance >= ?";

        String balanceQuery =
            "SELECT balance FROM bank_accounts WHERE id = ?";

        try (
            PreparedStatement withdrawPs = conn.prepareStatement(withdrawQuery);
            PreparedStatement balancePs = conn.prepareStatement(balanceQuery)
        ) {
            withdrawPs.setLong(1, amount);
            withdrawPs.setInt(2, bankAccountId);
            withdrawPs.setLong(3, amount);

            int rowsAffected = withdrawPs.executeUpdate();

            if (rowsAffected == 0) {
                throw new InvalidAmountException(
                    "Bank account not found or insufficient funds: "
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
    public TransferResult transferMoney(long amount, int sourceAccountId, int destinationAccountId) throws SQLException {

        try (Connection conn = ConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long sourceBalance = withdraw(conn, sourceAccountId, amount);
                long destinationBalance = deposit(conn, destinationAccountId, amount);
                conn.commit();

                return new TransferResult(sourceBalance, destinationBalance);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
 }
