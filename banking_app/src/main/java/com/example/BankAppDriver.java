package com.example;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.text.DateFormatter;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.example.models.Customer;
import com.example.services.BankDAO;
import com.example.services.CustomerDAO;
import com.example.services.MongoBankDAO;
import com.example.services.MongoCustomerDAO;
import com.example.services.PostgresBankDAO;
import com.example.services.PostgresCustomerDAO;
import com.example.utils.ConnectionUtil;
import com.example.utils.MongoConnectionManager;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import at.favre.lib.crypto.bcrypt.BCrypt;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
/**
 * Hello world!
 *
 */
public class BankAppDriver 
{
    public static void main( String[] args )
    {

        boolean usePostgres = true;
        Scanner scanner = new Scanner(System.in);
        CustomerDAO customerDao;
        BankDAO bankDao;

        if (usePostgres) {
            customerDao = new PostgresCustomerDAO();
            bankDao = new PostgresBankDAO();
        } else {
            // MongoClient client = MongoConnectionManager.getMongoClient();

            // CodecRegistry pojoCodecRegistry = fromRegistries(
            //     MongoClientSettings.getDefaultCodecRegistry(),
            //     fromProviders(PojoCodecProvider.builder().automatic(true).build())
            // );

            // Create our database using the registry provider above
            // MongoDatabase database = client.getDatabase("bank_db")
            //         .withCodecRegistry(pojoCodecRegistry);

            customerDao = new MongoCustomerDAO();
            bankDao = new MongoBankDAO();
        }
        BankApp bankApp = new BankApp(scanner, customerDao, bankDao);
        bankApp.start();
    }
}


// TODO: Make mongo version
// TODO: Add tests to test dao (mock?)
