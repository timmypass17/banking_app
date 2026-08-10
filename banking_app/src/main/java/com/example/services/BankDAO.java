package com.example.services;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Transaction;
import com.example.models.TransactionAction;
import com.example.models.TransferResult;
import com.example.models.helpers.BankAccountSummary;
import com.example.models.Bank;

public interface BankDAO {
    long deposit(String customerId, String bankAccountId, long amount) throws SQLException;
    long withdraw(String bankAccountId, long amount, String customerId) throws SQLException;
    TransferResult transferMoney(long amount, String sourceAccountId, String destinationAccountId, String customerId) throws SQLException;
    List<Bank> getAllBanks() throws SQLException;
    List<Transaction> getTransactionHistory(String customerId, TransactionAction action, LocalDateTime startDate, LocalDateTime endDate) throws SQLException; 
    Optional<BankAccount> getBankAccountById(String bankAccountId) throws SQLException;
    Optional<Bank> getBankById(String bankId) throws SQLException;
    
    Optional<BankAccount> createBankAccount(BankAccount bankAccount) throws SQLException;
    boolean closeAccount(String bankAccountId) throws SQLException;
    boolean reactivateAccount(String bankAccountId) throws SQLException;

    List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(String customerId);
    List<BankAccountSummary> getAllBankAccountsSummary() throws SQLException;
}