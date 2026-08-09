package com.example.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Bank;

public interface BankDAO {
    List<BankAccountByCustomerResult> getAllBankAccountsByCustomerId(int customerId);
    Optional<BankAccount> getBankAccountById(int bankAccountId) throws SQLException;
    Optional<Bank> getBankById(int bankId) throws SQLException; // optional, bank may not exist
    long deposit(int bankAccountId, long amount) throws SQLException;
    long withdraw(int bankAccountId, long amount) throws SQLException;
}
