module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Export both packages so JavaFX can access them
    exports com.example.demo;
    exports com.gradetracker;
}
