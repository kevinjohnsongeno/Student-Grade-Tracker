package com.example.demo;

import com.example.demo.DatabaseHandler;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Grade Tracker");

        // ====== Title ======
        Label titleLabel = new Label("Grade Tracker");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.web("#ffffff"));

        Label subtitleLabel = new Label("Access your academic portal");
        subtitleLabel.setFont(Font.font("Segoe UI", 20));
        subtitleLabel.setTextFill(Color.web("#cfcfcf"));

        VBox titleBox = new VBox(10, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20, 0, 20, 0));

        // ====== Toggle Buttons ======
        ToggleGroup toggleGroup = new ToggleGroup();
        ToggleButton studentBtn = new ToggleButton("Student Portal");
        ToggleButton adminBtn = new ToggleButton("Admin Portal");
        studentBtn.setToggleGroup(toggleGroup);
        adminBtn.setToggleGroup(toggleGroup);
        studentBtn.setSelected(true);

        styleToggle(studentBtn, true);
        styleToggle(adminBtn, false);

        HBox toggleBox = new HBox(20, studentBtn, adminBtn);
        toggleBox.setAlignment(Pos.CENTER);

        // ====== Student Login Form ======
        Label studentLabel = new Label("Student Login");
        studentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        studentLabel.setTextFill(Color.WHITE);

        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Enter your student ID");
        PasswordField studentPassField = new PasswordField();
        studentPassField.setPromptText("Enter your password");
        Button studentLoginBtn = new Button("Login as Student");

        styleInput(studentIdField);
        styleInput(studentPassField);
        styleButton(studentLoginBtn);

        VBox studentBox = new VBox(15, studentLabel, studentIdField, studentPassField, studentLoginBtn);
        studentBox.setAlignment(Pos.CENTER);
        studentBox.setPadding(new Insets(40));
        studentBox.setBackground(new Background(new BackgroundFill(Color.web("#1F2937"), new CornerRadii(15), Insets.EMPTY)));
        studentBox.setMaxWidth(400);

        // ====== Admin Login Form ======
        Label adminLabel = new Label("Admin Login");
        adminLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        adminLabel.setTextFill(Color.WHITE);

        TextField adminUserField = new TextField();
        adminUserField.setPromptText("Enter admin username");
        PasswordField adminPassField = new PasswordField();
        adminPassField.setPromptText("Enter admin password");
        Button adminLoginBtn = new Button("Login as Admin");

        styleInput(adminUserField);
        styleInput(adminPassField);
        styleButton(adminLoginBtn);

        VBox adminBox = new VBox(15, adminLabel, adminUserField, adminPassField, adminLoginBtn);
        adminBox.setAlignment(Pos.CENTER);
        adminBox.setPadding(new Insets(40));
        adminBox.setBackground(new Background(new BackgroundFill(Color.web("#1F2937"), new CornerRadii(15), Insets.EMPTY)));
        adminBox.setMaxWidth(400);
        adminBox.setVisible(false);

        // ====== Stack forms so they're centered ======
        StackPane formStack = new StackPane(studentBox, adminBox);
        formStack.setAlignment(Pos.CENTER);

        // ====== Toggle Visibility ======
        studentBtn.setOnAction(e -> {
            studentBox.setVisible(true);
            adminBox.setVisible(false);

            // Clear admin fields when switching
            adminUserField.clear();
            adminPassField.clear();

            styleToggle(studentBtn, true);
            styleToggle(adminBtn, false);
        });

        adminBtn.setOnAction(e -> {
            studentBox.setVisible(false);
            adminBox.setVisible(true);

            // Clear student fields when switching
            studentIdField.clear();
            studentPassField.clear();

            styleToggle(studentBtn, false);
            styleToggle(adminBtn, true);
        });



        // ====== Main Layout ======
        VBox layout = new VBox(30, titleBox, toggleBox, formStack);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(50));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#111827"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(layout, 1920, 1080);

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Fullscreen
        primaryStage.show();

        // ====== Handle Student Login ======
        studentLoginBtn.setOnAction(e -> {
            String id = studentIdField.getText();
            String pass = studentPassField.getText();
            if (DatabaseHandler.validateStudent(id, pass)) {
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, Student " + id);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Student ID or Password");
            }
        });

        // ====== Handle Admin Login ======
        adminLoginBtn.setOnAction(e -> {
            String user = adminUserField.getText();
            String pass = adminPassField.getText();
            if (DatabaseHandler.validateAdmin(user, pass)) {
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, Admin " + user);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Username or Password");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void styleInput(TextField tf) {
        tf.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 8;");
        tf.setMaxWidth(300);
    }

    private void styleButton(Button btn) {
        btn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 10;");
        btn.setMaxWidth(300);
    }

    private void styleToggle(ToggleButton btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 25; -fx-background-radius: 12;");
        } else {
            btn.setStyle("-fx-background-color: #2D3748; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 25; -fx-background-radius: 12;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}