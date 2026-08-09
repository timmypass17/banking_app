package com.example.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.ZoneId;

import com.example.models.BankAccount;
import com.example.models.BankAccountByCustomerResult;
import com.example.models.BankAccountType;
import com.example.models.Customer;
import com.example.utils.ConnectionUtil;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PostgresCustomerDAO implements CustomerDAO {

    @Override
    public Optional<Customer> register(Customer customer) {
        // Create Customer 
        String query = "INSERT INTO customers (first_name, last_name, date_of_birth, email, password) VALUES (? , ?, ?, ?, ?)";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setObject(3, customer.getDateOfBirth());
            ps.setString(4, customer.getEmail());
            String hashedPassword = BCrypt.withDefaults().hashToString(12, customer.getPassword().toCharArray());
            ps.setString(5, hashedPassword);

            ps.executeUpdate();
            System.out.println("PostgresCustomerDAO: Successfully registered user!");
            customer.setPassword(hashedPassword);
            return Optional.of(customer);
        } catch (SQLException sqle) {
            System.out.println("PostgresCustomerDAO: Failed to register user!");
            sqle.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Customer> login(String email, String password) {
        String query = "SELECT * FROM customers WHERE email = ? LIMIT 1";
        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, email); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    LocalDate dateOfBirth = rs.getObject("date_of_birth", LocalDate.class);
                    String emailRes = rs.getString("email");
                    String passwordRes = rs.getString("password");
                    
                    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), passwordRes);
                    
                    if (result.verified) {
                        System.out.println("Login successfully!");
                        Customer customer = new Customer(id, firstName, lastName, dateOfBirth, emailRes, passwordRes);
                        return Optional.of(customer);
                    } else {
                        System.out.println("Password does not match.");
                        return Optional.empty();
                    }
                } else {
                    System.out.println("Email does not exist");
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Customer> updateProfile(
            int customerId,
            Customer customer
    ) throws SQLException {

        String query =
            "UPDATE customers " +
            "SET first_name = ?, " +
                "last_name = ?, " +
                "date_of_birth = ?, " +
                "email = ? " +
            "WHERE id = ?";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setDate(
                3,
                java.sql.Date.valueOf(customer.getDateOfBirth())
            );
            ps.setString(4, customer.getEmail());
            ps.setInt(5, customerId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                return Optional.empty();
            }

            return Optional.of(customer);
        }
    }

    @Override
    public Optional<Customer> updatePassword(int customerId, Customer customer) throws SQLException {

        String query =
            "UPDATE customers " +
            "SET password = ? " +
            "WHERE id = ?";

        try (
            Connection conn = ConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            String hashedPassword = BCrypt.withDefaults().hashToString(12, customer.getPassword().toCharArray());
            ps.setString(1, hashedPassword);
            ps.setInt(2, customerId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                return Optional.empty();
            }

            customer.setPassword(hashedPassword);

            return Optional.of(customer);
        }
    }
}