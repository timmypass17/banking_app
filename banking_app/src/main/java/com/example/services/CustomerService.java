package com.example.services;

import java.sql.SQLException;
import java.util.Optional;

import com.example.models.Customer;

public class CustomerService {
    private CustomerDAO dao;

    public CustomerService(CustomerDAO dao) {
        this.dao = dao;
    }

    public Optional<Customer> register(Customer customer) {
        return dao.register(customer);
    }

    public Optional<Customer> login(String email, String password) {
        return dao.login(email, password);
    }

    public Optional<Customer> updateProfile(String customerId, Customer customer)  throws SQLException {
        return dao.updateProfile(customerId, customer);
    }

    public Optional<Customer> updatePassword(String customerId, Customer customer) throws SQLException {
        return dao.updatePassword(customerId, customer);
    }
}