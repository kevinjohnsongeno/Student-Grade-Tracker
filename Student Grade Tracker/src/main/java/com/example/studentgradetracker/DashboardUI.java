import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.sql.*;

public class DashboardUI extends Application {

    // ====== DATABASE CONFIG ======
    private static final String URL = "jdbc:mysql://localhost:3306/grade_tracker";
    private static final String USER = "root";
    private static final String PASS = "password"; // change this

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // ====== FETCH DATA METHODS ======
    private int getTotalStudents() {
        String sql = "SELECT COUNT(DISTINCT student_id) FROM subjects";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private int getActiveSemesters() {
        String sql = "SELECT COUNT(DISTINCT semester) FROM subjects";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private int getTotalSubjects() {
        String sql = "SELECT COUNT(DISTINCT subject_code) FROM subjects";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private double getAverageCGPA() {
        String sql = """
            SELECT AVG(student_gpa) AS avg_cgpa
            FROM (
                SELECT student_id,
                       SUM(credits * 
                           CASE grade
                               WHEN 'A+' THEN 10
                               WHEN 'A'  THEN 9
                               WHEN 'B+' THEN 8
                               WHEN 'B'  THEN 7
                               WHEN 'C+' THEN 6
                               WHEN 'C'  THEN 5
                               ELSE 0
                           END
                       ) / SUM(credits) AS student_gpa
                FROM subjects
                GROUP BY student_id
            ) t
        """;
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("avg_cgpa");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ====== UI START ======
    private BorderPane root;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        HBox header = createHeader();
        root.setTop(header);

        refreshDashboard();

        Scene scene = new Scene(root, 1440, 800);
        stage.setTitle("Grade Tracker - Admin Portal");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1e293b;");
        sidebar.setPadding(new Insets(20));

        Label appTitle = new Label("📘 Grade Tracker");
        appTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox navBox = new VBox(10,
                createSidebarBtn("Dashboard", true), // mark Dashboard as active
                createSidebarBtn("Students", false),
                createSidebarBtn("Semesters & Subjects", false),
                createSidebarBtn("Grades", false),
                createSidebarBtn("Reports", false),
                createSidebarBtn("Settings", false)
        );

        sidebar.getChildren().addAll(appTitle, navBox);
        return sidebar;
    }

    private Button createSidebarBtn(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(
                active
                        ? "-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-width: 0 0 0 4; -fx-border-color: #3b82f6;"
                        : "-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-font-size: 14px;"
        );
        return btn;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setSpacing(10);
        header.setStyle("-fx-background-color: #1e293b;");

        Label title = new Label("Dashboard");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button liveDataBtn = new Button("Live Data");
        styleHeaderBtn(liveDataBtn);
        liveDataBtn.setOnAction(e -> refreshDashboard());

        Button reportBtn = new Button("Generate Report");
        styleHeaderBtn(reportBtn);
        reportBtn.setOnAction(e -> generateReport());

        header.getChildren().addAll(title, spacer, liveDataBtn, reportBtn);
        return header;
    }

    private void styleHeaderBtn(Button btn) {
        btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12;"));
    }

    private void refreshDashboard() {
        GridPane dashboard = createDashboardContent();
        root.setCenter(dashboard);
    }

    private GridPane createDashboardContent() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(20);
        grid.setVgap(20);

        // Info cards
        grid.add(createInfoCard(String.valueOf(getTotalStudents()), "Total Students", ""), 0, 0);
        grid.add(createInfoCard(String.valueOf(getActiveSemesters()), "Active Semesters", ""), 1, 0);
        grid.add(createInfoCard(String.valueOf(getTotalSubjects()), "Total Subjects", ""), 2, 0);
        grid.add(createInfoCard(String.format("%.2f", getAverageCGPA()), "Average CGPA", ""), 3, 0);

        // Charts
        grid.add(createLineChart(), 0, 1, 2, 1);
        grid.add(createBarChart(), 2, 1, 2, 1);

        // Bottom row cards
        grid.add(createTopPerformersCard(), 0, 2, 2, 1);
        grid.add(createRecentActivityCard(), 2, 2, 2, 1);

        return grid;
    }

    private VBox createInfoCard(String value, String title, String trend) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10; -fx-padding: 20;");
        Label val = new Label(value);
        val.setFont(Font.font(22));
        val.setStyle("-fx-text-fill: white;");

        Label ttl = new Label(title);
        ttl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        Label tr = new Label(trend);
        tr.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 12px;");

        card.getChildren().addAll(val, ttl, tr);
        return card;
    }

    private LineChart<String, Number> createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Student Enrollment Trend");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String sql = "SELECT semester, COUNT(DISTINCT student_id) AS total_students FROM subjects GROUP BY semester ORDER BY semester";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>("Sem " + rs.getInt("semester"), rs.getInt("total_students")));
            }
        } catch (Exception e) { e.printStackTrace(); }

        lineChart.getData().add(series);
        lineChart.lookup(".chart-title").setStyle("-fx-text-fill: white;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);

        return lineChart;
    }

    private BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Grade Distribution");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String sql = "SELECT grade, COUNT(*) AS count FROM subjects GROUP BY grade";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("grade"), rs.getInt("count")));
            }
        } catch (Exception e) { e.printStackTrace(); }

        barChart.getData().add(series);
        barChart.lookup(".chart-title").setStyle("-fx-text-fill: white;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);
        yAxis.setTickLabelFill(javafx.scene.paint.Color.WHITE);

        return barChart;
    }

    private VBox createTopPerformersCard() {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10; -fx-padding: 20;");

        Label title = new Label("🏆 Top Performers");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox list = new VBox(8);

        String sql = """
            SELECT student_id,
                   SUM(credits *
                       CASE grade
                           WHEN 'A+' THEN 10
                           WHEN 'A'  THEN 9
                           WHEN 'B+' THEN 8
                           WHEN 'B'  THEN 7
                           WHEN 'C+' THEN 6
                           WHEN 'C'  THEN 5
                           ELSE 0
                       END) / SUM(credits) AS cgpa
            FROM subjects
            GROUP BY student_id
            ORDER BY cgpa DESC
            LIMIT 5
        """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int rank = 1;
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                double cgpa = rs.getDouble("cgpa");

                Label lbl = new Label(
                        "#" + rank + "  Student " + studentId + "   CGPA: " + String.format("%.2f", cgpa)
                );
                lbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
                list.getChildren().add(lbl);
                rank++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        card.getChildren().addAll(title, list);
        return card;
    }

    private VBox createRecentActivityCard() {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10; -fx-padding: 20;");

        Label title = new Label("🕒 Recent Activity");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox list = new VBox(8);

        String sql = """
            SELECT student_id, semester, subject_code, grade
            FROM subjects
            ORDER BY semester DESC, subject_code DESC
            LIMIT 5
        """;

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String activity = "Student " + rs.getString("student_id")
                        + " | Sem " + rs.getInt("semester")
                        + " | " + rs.getString("subject_code")
                        + " → Grade " + rs.getString("grade");

                Label lbl = new Label(activity);
                lbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
                list.getChildren().add(lbl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        card.getChildren().addAll(title, list);
        return card;
    }

    private void generateReport() {
        String sql = "SELECT student_id, semester, subject_code, subject_name, credits, grade FROM subjects";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);
             FileWriter writer = new FileWriter("StudentReport.csv")) {

            writer.write("StudentID,Semester,SubjectCode,SubjectName,Credits,Grade\n");
            while (rs.next()) {
                writer.write(rs.getString("student_id") + "," +
                        rs.getInt("semester") + "," +
                        rs.getString("subject_code") + "," +
                        rs.getString("subject_name") + "," +
                        rs.getInt("credits") + "," +
                        rs.getString("grade") + "\n");
            }
            System.out.println("✅ Report generated: StudentReport.csv");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
