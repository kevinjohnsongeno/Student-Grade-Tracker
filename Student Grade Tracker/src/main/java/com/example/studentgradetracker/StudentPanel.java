package com.example.demo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;

public class StudentPanel extends Application {

    public static class Subject {
        private final String code;
        private final String name;
        private final int credits;
        private final String grade;

        public Subject(String code, String name, int credits, String grade) {
            this.code = code;
            this.name = name;
            this.credits = credits;
            this.grade = grade;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public int getCredits() { return credits; }
        public String getGrade() { return grade; }
    }

    public static class DatabaseHandler {
        private static final String URL = "jdbc:mysql://localhost:3306/grade_tracker";
        private static final String USER = "root";
        private static final String PASSWORD = "password"; // Change if needed

        private Connection connect() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }

        public ObservableList<Subject> getSubjects(String studentId, int semester) {
            ObservableList<Subject> subjects = FXCollections.observableArrayList();
            String query = "SELECT subject_code, subject_name, credits, grade FROM subjects WHERE student_id=? AND semester=?";
            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, studentId);
                stmt.setInt(2, semester);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    subjects.add(new Subject(
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            rs.getInt("credits"),
                            rs.getString("grade")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return subjects;
        }

        public int getTotalCredits(String studentId) {
            String query = "SELECT SUM(credits) AS total FROM subjects WHERE student_id=?";
            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("total");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public int getTotalSemesters(String studentId) {
            String query = "SELECT COUNT(DISTINCT semester) AS sems FROM subjects WHERE student_id=?";
            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("sems");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public double calculateSGPA(String studentId, int semester) {
            String query = "SELECT credits, grade FROM subjects WHERE student_id=? AND semester=?";
            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, studentId);
                stmt.setInt(2, semester);
                ResultSet rs = stmt.executeQuery();

                int totalCredits = 0;
                int weightedPoints = 0;

                while (rs.next()) {
                    int credits = rs.getInt("credits");
                    String grade = rs.getString("grade");
                    int points = gradeToPoints(grade);
                    weightedPoints += credits * points;
                    totalCredits += credits;
                }
                if (totalCredits == 0) return 0;
                return (double) weightedPoints / totalCredits;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public double calculateCGPA(String studentId) {
            String query = "SELECT credits, grade FROM subjects WHERE student_id=?";
            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();

                int totalCredits = 0;
                int weightedPoints = 0;

                while (rs.next()) {
                    int credits = rs.getInt("credits");
                    String grade = rs.getString("grade");
                    int points = gradeToPoints(grade);
                    weightedPoints += credits * points;
                    totalCredits += credits;
                }
                if (totalCredits == 0) return 0;
                return (double) weightedPoints / totalCredits;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        private int gradeToPoints(String grade) {
            return switch (grade) {
                case "A+" -> 10;
                case "A" -> 9;
                case "B+" -> 8;
                case "B" -> 7;
                case "C" -> 6;
                case "D" -> 5;
                default -> 0;
            };
        }
    }

    @Override
    public void start(Stage primaryStage) {
        String studentId = "S1001"; // Example student ID
        DatabaseHandler db = new DatabaseHandler();

        primaryStage.setTitle("Grade Tracker - Student Panel");

        // TOP BAR
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: #1E293B;");

        Label title = new Label("Grade Tracker");
        title.setFont(Font.font(20));
        title.setTextFill(Color.WHITE);

        Button downloadBtn = new Button("Download Report");
        downloadBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label studentLabel = new Label("Student ID: " + studentId);
        studentLabel.setTextFill(Color.WHITE);

        topBar.getChildren().addAll(title, spacer, downloadBtn, studentLabel);

        // STUDENT INFO
        VBox studentInfo = new VBox(5);
        studentInfo.setPadding(new Insets(20));
        studentInfo.setStyle("-fx-background-color: #334155; -fx-background-radius: 12;");

        int totalCredits = db.getTotalCredits(studentId);
        int totalSemesters = db.getTotalSemesters(studentId);
        double cgpa = db.calculateCGPA(studentId);

        Label cgpaLbl = new Label("CGPA: " + String.format("%.2f", cgpa) +
                "   |   Credits: " + totalCredits +
                "   |   Semesters: " + totalSemesters);
        cgpaLbl.setTextFill(Color.LIGHTGREEN);

        studentInfo.getChildren().addAll(cgpaLbl);

        // SEMESTER SELECTOR
        HBox semesterBox = new HBox(10);
        semesterBox.setAlignment(Pos.CENTER_LEFT);
        semesterBox.setPadding(new Insets(10));

        Label semLabel = new Label("Semester: ");
        semLabel.setTextFill(Color.WHITE);

        ComboBox<Integer> semSelector = new ComboBox<>();
        for (int i = 1; i <= totalSemesters; i++) semSelector.getItems().add(i);
        if (!semSelector.getItems().isEmpty()) semSelector.setValue(1);

        Label sgpaLbl = new Label("SGPA: " + String.format("%.2f", db.calculateSGPA(studentId, 1)));
        sgpaLbl.setTextFill(Color.LIGHTGREEN);

        semesterBox.getChildren().addAll(semLabel, semSelector, sgpaLbl);

        // SUBJECT TABLE
        TableView<Subject> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 10;");

        TableColumn<Subject, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));

        TableColumn<Subject, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Subject, String> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCredits())));

        TableColumn<Subject, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrade()));

        table.getColumns().addAll(codeCol, nameCol, creditsCol, gradeCol);

        table.setItems(db.getSubjects(studentId, 1));

        semSelector.setOnAction(e -> {
            int sem = semSelector.getValue();
            table.setItems(db.getSubjects(studentId, sem));
            sgpaLbl.setText("SGPA: " + String.format("%.2f", db.calculateSGPA(studentId, sem)));
        });

        VBox semesterSection = new VBox(10, semesterBox, table);
        semesterSection.setPadding(new Insets(15));

        // DOWNLOAD REPORT BUTTON ACTION
        downloadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report As");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("StudentReport.csv");

            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                try (Connection conn = DriverManager.getConnection(DatabaseHandler.URL, DatabaseHandler.USER, DatabaseHandler.PASSWORD);
                     PreparedStatement stmt = conn.prepareStatement("SELECT semester, subject_code, subject_name, credits, grade FROM subjects WHERE student_id = ?")) {

                    stmt.setString(1, studentId);
                    ResultSet rs = stmt.executeQuery();

                    try (FileWriter writer = new FileWriter(file)) {
                        // Write student details header
                        writer.write("Student ID:," + studentId + "\n");
                        writer.write("Total Credits:," + totalCredits + "\n");
                        writer.write("Total Semesters:," + totalSemesters + "\n");
                        writer.write("CGPA:," + String.format("%.2f", cgpa) + "\n");
                        writer.write("\n");

                        // Column headers
                        writer.write("Semester,Subject Code,Subject Name,Credits,Grade\n");

                        while (rs.next()) {
                            writer.write(rs.getInt("semester") + "," +
                                    rs.getString("subject_code") + "," +
                                    rs.getString("subject_name") + "," +
                                    rs.getInt("credits") + "," +
                                    rs.getString("grade") + "\n");
                        }
                    }

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Report Saved");
                    alert.setHeaderText("Report successfully generated!");
                    alert.setContentText("Saved at:\n" + file.getAbsolutePath());
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    alert.showAndWait();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to generate report");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        // ROOT LAYOUT
        VBox root = new VBox(15, topBar, studentInfo, semesterSection);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #0F172A;");

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
