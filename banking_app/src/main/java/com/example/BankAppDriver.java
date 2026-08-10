package com.example;

import java.util.Scanner;

import com.example.services.BankDAO;
import com.example.services.BankService;
import com.example.services.CustomerDAO;
import com.example.services.CustomerService;
import com.example.services.MongoBankDAO;
import com.example.services.MongoCustomerDAO;
import com.example.services.PostgresBankDAO;
import com.example.services.PostgresCustomerDAO;
/**
 * Hello world!
 *
 */
public class BankAppDriver 
{
    public static void main( String[] args )
    {

        boolean usePostgres = true;
        CustomerDAO customerDao;
        BankDAO bankDao;
        
        if (usePostgres) {
            customerDao = new PostgresCustomerDAO();
            bankDao = new PostgresBankDAO();
        } else {
            customerDao = new MongoCustomerDAO();
            bankDao = new MongoBankDAO();
        }

        CustomerService customerService = new CustomerService(customerDao);
        BankService bankService = new BankService(bankDao);
        Scanner scanner = new Scanner(System.in);

        BankApp bankApp = new BankApp(scanner, customerService, bankService);
        bankApp.start();
    }
}