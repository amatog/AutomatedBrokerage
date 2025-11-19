package com.mybroker.dbtest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresTest {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mybrokerdb";
        String user = "broker_user"; // oder "postgres"
        String password = "brokerPwd";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Verbindung erfolgreich! " + conn);
        } catch (SQLException e) {
            System.out.println("Fehler bei der DB-Verbindung:");
            e.printStackTrace();
        }
    }
}
