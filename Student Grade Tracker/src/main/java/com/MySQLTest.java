package com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLTest {
    public static void main(String[] args) {
        // Connection details
        String url = "jdbc:mysql://localhost:3306/grade_tracker"; // replace testdb with your DB name
        String user = "root"; // replace with your username
        String password = "password"; // replace with your password

        try {
            // Connect to MySQL
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to MySQL: " + conn);

            // Close connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
