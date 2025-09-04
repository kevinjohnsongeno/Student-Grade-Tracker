package com.example.demo;

import java.sql.*;

public class DatabaseHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/grade_tracker";
    private static final String USER = "root"; // change if needed
    private static final String PASSWORD = "password"; // change if needed

    public static boolean validateStudent(String studentId, String password) {
        String query = "SELECT * FROM students WHERE student_id=? AND password=?";
        return checkLogin(query, studentId, password);
    }

    public static boolean validateAdmin(String username, String password) {
        String query = "SELECT * FROM admins WHERE username=? AND password=?";
        return checkLogin(query, username, password);
    }

    private static boolean checkLogin(String query, String user, String pass) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            return rs.next(); // true if a record exists
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}