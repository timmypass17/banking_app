package com.example.services;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.exceptions.InvalidAmountException;
import com.example.models.Bank;
import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Transaction;
import com.example.models.TransactionAction;
import com.example.models.TransferResult;
import com.example.models.helpers.BankAccountSummary;

// Apply business logic/validation using DAO
public class BankService {
    private BankDAO dao;

    public BankService(BankDAO dao) {
        this.dao = dao;
    }

    public long deposit(String customerId, String bankAccountId, long amount) throws SQLException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("BankService: Deposit amount cannot be zero or negative");
        }
        return dao.deposit(customerId, bankAccountId, amount);
    }


    public long withdraw(String bankAccountId, long amount, String customerId) throws SQLException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("BankService: Withdraw amount cannot be zero or negative");
        }
        return dao.withdraw(bankAccountId, amount, customerId);
    }

    public TransferResult transferMoney(long amount, String sourceAccountId, String destinationAccountId, String customerId) throws SQLException {
        if (amount <= 0) {
            throw new InvalidAmountException("BankService: Transfer amount cannot be zero or negative");
        }

        return dao.transferMoney(amount, sourceAccountId, destinationAccountId, customerId);
    }

    public List<Bank> getAllBanks() throws SQLException {
        return dao.getAllBanks();
    }


    public List<Transaction> getTransactionHistory(String customerId, TransactionAction action, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return dao.getTransactionHistory(customerId, action, startDate, endDate);
    }

    public Optional<BankAccount> getBankAccountById(String bankAccountId) throws SQLException {
        return dao.getBankAccountById(bankAccountId);
    }

    public Optional<Bank> getBankById(String bankId) throws SQLException {
        return dao.getBankById(bankId);
    }

    public Optional<BankAccount> createBankAccount(BankAccount bankAccount) throws SQLException {
        return dao.createBankAccount(bankAccount);
    }

    public boolean closeAccount(String bankAccountId) throws SQLException {
        return dao.closeAccount(bankAccountId);
    }

    public boolean reactivateAccount(String bankAccountId) throws SQLException {
        return dao.reactivateAccount(bankAccountId);
    }

    public List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(String customerId) {
        return dao.getAllBankAccountsByCustomerId(customerId);
    }

    public List<BankAccountSummary> getAllBankAccountsSummary() throws SQLException {
        return dao.getAllBankAccountsSummary();
    }
}