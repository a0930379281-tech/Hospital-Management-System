
package ca.learncodegap.hospital;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Function;

public class HospitalManagementApp extends Application {

    private final PatientDAO dao = new PatientDAO();

    private final TextField id = new TextField();
    private final TextField name = new TextField();
    private final TextField age = new TextField();
    private final TextField phone = new TextField();
    private final TextField diagnosis = new TextField();
    private final TextField search = new TextField();

    private final ComboBox<String> gender = new ComboBox<>();
    private final DatePicker date = new DatePicker();

    private final TableView<Patient> table = new TableView<>();
    private final Label status = new Label("Ready");

    @Override
    public void start(Stage stage) {

        // Patient ID is generated automatically
        id.setEditable(false);
        id.setPromptText("Auto-generated");

        // Gender options
        gender.getItems().addAll(
                "Female",
                "Male",
                "Non-binary",
                "Prefer not to say"
        );

        date.setValue(LocalDate.now());

        // Application heading
        VBox heading = new VBox(
                4,
                new Label("Hospital Management System"),
                new Label("Patient Registration • JavaFX + JDBC + MySQL")
        );
        heading.setPadding(new Insets(12));

        // Patient registration form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        String[] labels = {
                "Patient ID",
                "Full name",
                "Age",
                "Gender",
                "Phone",
                "Diagnosis / reason for visit",
                "Admission date"
        };

        Control[] controls = {
                id,
                name,
                age,
                gender,
                phone,
                diagnosis,
                date
        };

        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(controls[i], 1, i);
        }

        // CRUD buttons
        Button add = new Button("Add Patient");
        Button update = new Button("Update");
        Button delete = new Button("Delete");
        Button clear = new Button("Clear");

        HBox actions = new HBox(
                8, add, update, delete, clear
        );
        actions.setPadding(new Insets(10, 0, 0, 0));

        VBox left = new VBox(
                8,
                new Label("Patient Details"),
                form,
                actions
        );

        left.setPadding(new Insets(12));
        left.setMinWidth(390);

        // CREATE - Add patient
        add.setOnAction(_ -> perform(
                "Patient added",
                () -> {
                    dao.add(readForm(0));
                    refresh();
                    clearForm();
                }
        ));

        // UPDATE - Update selected patient
        update.setOnAction(_ -> {

            Patient selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                status.setText("Select a patient to update.");
                return;
            }

            perform("Patient updated", () -> {
                dao.update(readForm(selected.id()));
                refresh();
                clearForm();
            });
        });

        // DELETE - Delete selected patient
        delete.setOnAction(_ -> {

            Patient selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                status.setText("Select a patient to delete.");
                return;
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Delete patient #" + selected.id() + "?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            confirm.setHeaderText("Confirm deletion");

            if (confirm.showAndWait().orElse(ButtonType.NO)
                    == ButtonType.YES) {

                perform("Patient deleted", () -> {
                    dao.delete(selected.id());
                    refresh();
                    clearForm();
                });
            }
        });

        // CLEAR - Reset registration form
        clear.setOnAction(_ -> {
            clearForm();
            status.setText("Form cleared");
        });

        // Table columns
        addColumn("ID",
                p -> Integer.toString(p.id()));

        addColumn("Name", Patient::name);

        addColumn("Age",
                p -> Integer.toString(p.age()));

        addColumn("Gender", Patient::gender);

        addColumn("Phone", Patient::phone);

        addColumn("Diagnosis", Patient::diagnosis);

        addColumn("Admission date",
                p -> p.admissionDate().toString());

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        // Populate form when a patient is selected
        table.getSelectionModel()
                .selectedItemProperty()
                .addListener((_, _, patient) -> {

                    if (patient != null) {
                        fill(patient);
                    }
                });

        // SEARCH and SHOW ALL
        Button find = new Button("Search");
        Button all = new Button("Show All");

        find.setOnAction(_ -> perform(
                "Search complete",
                () -> table.getItems().setAll(
                        dao.find(search.getText())
                )
        ));

        all.setOnAction(_ -> perform(
                "All records loaded",
                () -> {
                    search.clear();
                    refresh();
                }
        ));

        search.setPromptText("Name or phone");

        HBox searchBar = new HBox(
                8, search, find, all
        );

        // Patient records panel
        VBox right = new VBox(
                10,
                new Label("Patient Records"),
                searchBar,
                table
        );

        right.setPadding(new Insets(12));

        HBox.setHgrow(right, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Main layout
        HBox center = new HBox(8, left, right);

        BorderPane root = new BorderPane();
        root.setTop(heading);
        root.setCenter(center);
        root.setBottom(status);

        BorderPane.setMargin(status, new Insets(12));

        // Display application
        stage.setScene(new Scene(root, 1150, 600));
        stage.setTitle("Hospital Management System");
        stage.show();

        // Load patient records
        perform("Patient records loaded", this::refresh);
    }

    // Create a table column
    private void addColumn(
            String title,
            Function<Patient, String> value) {

        TableColumn<Patient, String> col =
                new TableColumn<>(title);

        col.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(
                        value.apply(c.getValue())
                )
        );

        col.setPrefWidth(125);
        table.getColumns().add(col);
    }

    // Read and validate patient information
    private Patient readForm(int patientId) {

        String n = name.getText().trim();
        String ph = phone.getText().trim();
        String d = diagnosis.getText().trim();

        if (n.isEmpty()
                || ph.isEmpty()
                || d.isEmpty()
                || gender.getValue() == null
                || date.getValue() == null) {

            throw new IllegalArgumentException(
                    "Complete all required fields."
            );
        }

        int a;

        try {
            a = Integer.parseInt(age.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Age must be a whole number between 0 and 125."
            );
        }

        if (a < 0 || a > 125) {
            throw new IllegalArgumentException(
                    "Age must be between 0 and 125."
            );
        }

        if (!ph.matches("[+0-9() .-]{7,20}")
                || ph.replaceAll("\\D", "").length() < 7) {

            throw new IllegalArgumentException(
                    "Enter a valid phone number (at least 7 digits)."
            );
        }

        if (date.getValue().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Admission date cannot be in the future."
            );
        }

        return new Patient(
                patientId,
                n,
                a,
                gender.getValue(),
                ph,
                d,
                date.getValue()
        );
    }

    // Fill form with selected patient data
    private void fill(Patient p) {

        id.setText(Integer.toString(p.id()));
        name.setText(p.name());
        age.setText(Integer.toString(p.age()));
        gender.setValue(p.gender());
        phone.setText(p.phone());
        diagnosis.setText(p.diagnosis());
        date.setValue(p.admissionDate());
    }

    // Clear registration form
    private void clearForm() {

        table.getSelectionModel().clearSelection();

        id.clear();
        name.clear();
        age.clear();
        gender.setValue(null);
        phone.clear();
        diagnosis.clear();

        date.setValue(LocalDate.now());
    }

    // Reload patient records from database
    private void refresh() throws SQLException {

        table.getItems().setAll(
                dao.find("")
        );
    }

    // Execute database operations safely
    private void perform(String success, Action action) {

        try {
            action.run();
            status.setText(success);

        } catch (IllegalArgumentException | SQLException ex) {

            status.setText(
                    "Error: " + ex.getMessage()
            );
        }
    }

    @FunctionalInterface
    private interface Action {
        void run() throws SQLException;
    }

    // Application entry point
    public static void main(String[] args) {
        launch(args);
    }
}
