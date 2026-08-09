package com.example;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.text.DateFormatter;

import com.example.models.Customer;
import com.example.services.BankDAO;
import com.example.services.CustomerDAO;
import com.example.services.PostgresBankDAO;
import com.example.services.PostgresCustomerDAO;
import com.example.utils.ConnectionUtil;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Hello world!
 *
 */
public class BankAppDriver 
{
    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);
        CustomerDAO customerDao = new PostgresCustomerDAO();
        BankDAO bankDao = new PostgresBankDAO();
        BankApp bankApp = new BankApp(scanner, customerDao, bankDao);
        bankApp.start();
    }
}