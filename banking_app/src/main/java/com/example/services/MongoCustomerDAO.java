package com.example.services;

import java.sql.SQLException;
import java.util.Optional;

import org.bson.Document;

import com.example.models.Customer;
import com.example.utils.MongoConnectionManager;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class MongoCustomerDAO implements CustomerDAO {

    @Override
    public Optional<Customer> register(Customer customer) {
        MongoCollection<Customer> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("customers", Customer.class);

        String hashedPassword =
                BCrypt.withDefaults()
                        .hashToString(12, customer.getPassword().toCharArray());

        customer.setPassword(hashedPassword);

        try {
            collection.insertOne(customer);

            System.out.println(
                    "MongoCustomerDAO: Successfully registered user!"
            );

            return Optional.of(customer);

        } catch (MongoException me) {
            System.out.println(
                    "MongoCustomerDAO: Failed to register user!"
            );
            me.printStackTrace();

            return Optional.empty();
        }
    }

    @Override
    public Optional<Customer> login(String email, String password) {
        MongoCollection<Customer> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("customers", Customer.class);

        Customer customer = collection
                .find(Filters.eq("email", email))
                .first();

        if (customer == null) {
            System.out.println("Email does not exist");
            return Optional.empty();
        }

        BCrypt.Result result =
                BCrypt.verifyer()
                        .verify(password.toCharArray(), customer.getPassword());

        if (result.verified) {
            System.out.println("Login successfully!");
            return Optional.of(customer);
        }

        System.out.println("Password does not match.");
        return Optional.empty();
    }

    @Override
    public Optional<Customer> updateProfile(String customerId, Customer customer) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProfile'");
    }

    @Override
    public Optional<Customer> updatePassword(String customerId, Customer customer) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePassword'");
    }
    
}
