package com.example;

import java.sql.SQLException;

import com.example.utils.ConnectionUtil;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World! Timmy :)" );
        postgresConnectionSanityTest();
    }

    public static void postgresConnectionSanityTest() {
        // This acts as a sanity test to make sure our connection is working
        try {
            System.out.println("Env Var");
            ConnectionUtil.getConnection_env();
            System.out.println("Connection was successful");
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection was NOT sucessful");
        }
    }
}
