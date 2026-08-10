package com.example.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Customer;

public interface CustomerDAO {
    Optional<Customer> register(Customer customer);
    Optional<Customer> login(String email, String password);
    Optional<Customer> updateProfile(String customerId, Customer customer)  throws SQLException;
    // separte to prevent rehashing pass on each update
    Optional<Customer> updatePassword(String customerId, Customer customer) throws SQLException;
}