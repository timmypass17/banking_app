package com.example.services;

import java.util.List;
import java.util.Optional;

import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.Customer;

public interface CustomerDAO {
    Optional<Customer> register(Customer customer);
    Optional<Customer> login(String email, String password);
}