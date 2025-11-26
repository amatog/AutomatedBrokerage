package com.mybroker.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnectionFactory {

    public static Connection getConnection() {
        try {
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASSWORD");

            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to PostgreSQL", e);
        }
    }
}
