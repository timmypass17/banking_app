package com.example.services;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.models.Bank;
import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Transaction;
import com.example.models.TransactionAction;
import com.example.models.TransferResult;
import com.example.models.helpers.BankAccountSummary;

public class MongoBankDAO implements BankDAO {

    @Override
    public List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(String customerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllBankAccountsByCustomerId'");
    }

    @Override
    public Optional<BankAccount> getBankAccountById(String bankAccountId) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBankAccountById'");
    }

    @Override
    public Optional<Bank> getBankById(String bankId) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBankById'");
    }

    @Override
    public long deposit(String customerId, String bankAccountId, long amount) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deposit'");
    }

    @Override
    public long withdraw(String bankAccountId, long amount, String customerId) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdraw'");
    }

    @Override
    public TransferResult transferMoney(long amount, String sourceAccountId, String destinationAccountId,
            String customerId) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transferMoney'");
    }

    @Override
    public List<BankAccountSummary> getAllBankAccountsSummary() throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllBankAccountsSummary'");
    }

    @Override
    public List<Bank> getAllBanks() throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllBanks'");
    }

    @Override
    public Optional<BankAccount> createBankAccount(BankAccount bankAccount) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBankAccount'");
    }

    @Override
    public List<Transaction> getTransactionHistory(String customerId, TransactionAction action, LocalDateTime startDate,
            LocalDateTime endDate) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransactionHistory'");
    }

    @Override
    public boolean closeAccount(String bankAccountId) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeAccount'");
    }

    @Override
    public boolean reactivateAccount(String bankAccountId) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reactivateAccount'");
    }


    
}
