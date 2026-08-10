package com.example.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Transaction;
import com.example.models.TransferResult;
import com.example.models.helpers.BankAccountSummary;
import com.example.models.Bank;

public interface BankDAO {
    List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(int customerId);
    Optional<BankAccount> getBankAccountById(int bankAccountId) throws SQLException;
    Optional<Bank> getBankById(int bankId) throws SQLException; // optional, bank may not exist
    long deposit(int customerId, int bankAccountId, long amount) throws SQLException;
    long withdraw(int bankAccountId, long amount, int customerId) throws SQLException;
    TransferResult transferMoney(long amount, int sourceAccountId, int destinationAccountId, int customerId) throws SQLException;

    List<BankAccountSummary> getAllBankAccountsSummary() throws SQLException;

    List<Bank> getAllBanks() throws SQLException;

    Optional<BankAccount> createBankAccount(BankAccount bankAccount) throws SQLException;

    List<Transaction> getTransactionHistory(int customerId) throws SQLException; 

    boolean closeAccount(int bankAccountId) throws SQLException;
    boolean reactivateAccount(int bankAccountId) throws SQLException;
}