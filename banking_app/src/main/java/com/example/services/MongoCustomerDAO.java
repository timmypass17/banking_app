package com.example.services;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.bson.Document;

import com.example.models.Customer;
import com.example.utils.MongoConnectionManager;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

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

        customer.setId(UUID.randomUUID().toString());
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
            System.out.println("Login successfully!\n");
            return Optional.of(customer);
        }

        System.out.println("Password does not match.");
        return Optional.empty();
    }

    @Override
    public Optional<Customer> updateProfile(
            String customerId,
            Customer customer) {

        MongoCollection<Customer> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("customers", Customer.class);

        UpdateResult result = collection.updateOne(
                Filters.eq("_id", customerId),

                Updates.combine(
                        Updates.set("firstName", customer.getFirstName()),
                        Updates.set("lastName", customer.getLastName()),
                        Updates.set("dateOfBirth", customer.getDateOfBirth()),
                        Updates.set("email", customer.getEmail())
                )
        );

        if (result.getMatchedCount() == 0) {
            return Optional.empty();
        }

        return Optional.of(customer);
    }

    @Override
    public Optional<Customer> updatePassword(
            String customerId,
            Customer customer) {

        MongoCollection<Customer> collection =
                MongoConnectionManager
                        .getDatabase()
                        .getCollection("customers", Customer.class);

        String hashedPassword =
                BCrypt.withDefaults()
                        .hashToString(
                                12,
                                customer.getPassword().toCharArray()
                        );

        Customer updatedCustomer = collection.findOneAndUpdate(
                Filters.eq("_id", customerId),

                Updates.set("password", hashedPassword),

                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updatedCustomer);
    }
    
}
