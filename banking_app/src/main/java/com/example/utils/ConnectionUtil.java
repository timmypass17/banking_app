package com.example.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtil {
    public static Connection getConnection() throws SQLException {
        String url = System.getenv("CONN_URL");
        String user = System.getenv("CONN_NAME");
        String pass = System.getenv("CONN_PASS");
        // System.out.println("CONN_URL: " + url);
        // System.out.println("CONN_NAME: " + user);
        // System.out.println("CONN_PASS: " + pass);
        return DriverManager.getConnection(url, user, pass);
    }
}
