package org.example.testmal;

import JavaLogik.Demos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

public class BenutzerAdd extends Stage {

    private TextField nameField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField rolleField;
    private Label errorLbl;

    private final Consumer<String> onCreated;

    public BenutzerAdd(Stage owner, Consumer<String> onCreated) {
        this.onCreated = onCreated;
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        initFields();
        buildScene("Benutzer hinzufügen");
    }

    private void initFields() {
        nameField = new TextField();
        nameField.setPromptText("Name");

        emailField = new TextField();
        emailField.setPromptText("E-Mail");

        passwordField = new PasswordField();
        passwordField.setPromptText("Passwort");

        rolleField = new TextField("user");
        rolleField.setPromptText("Rolle (z.B. user)");

        errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF8888; -fx-font-size: 12px;");
    }

    private void buildScene(String titleText) {
        setTitle(titleText);

        Label title = new Label(titleText);
        title.setFont(Font.font(16));
        title.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 0, 0));

        // --- NEU: ColumnConstraints damit Eingabefelder wachsen ---
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(120); // Label-Spalte
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS); // Eingabefeld-Spalte wächst
        col1.setPercentWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(0); // Reserve-Spalte (falls bisher span 2 genutzt wird)
        grid.getColumnConstraints().addAll(col0, col1, col2);

        Label lblName = new Label("Name:");
        lblName.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblName, 0, 0);
        grid.add(nameField, 1, 0, 2, 1);

        Label lblEmail = new Label("E-Mail:");
        lblEmail.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblEmail, 0, 1);
        grid.add(emailField, 1, 1, 2, 1);

        Label lblPwd = new Label("Passwort:");
        lblPwd.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblPwd, 0, 2);
        grid.add(passwordField, 1, 2, 2, 1);

        Label lblRolle = new Label("Rolle:");
        lblRolle.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblRolle, 0, 3);
        grid.add(rolleField, 1, 3, 2, 1);

        grid.add(errorLbl, 0, 4, 3, 1);

        // --- NEU: Eingabefelder so einstellen, dass sie die verfügbare Breite nutzen ---
        nameField.setMaxWidth(Double.MAX_VALUE);
        emailField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        rolleField.setMaxWidth(Double.MAX_VALUE);
        // TextArea (Beschreibung) ist nicht vorhanden hier, aber falls später hinzugefügt:
        // beschreibung.setMaxWidth(Double.MAX_VALUE);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        GridPane.setHgrow(rolleField, Priority.ALWAYS);

        Button cancelBtn = new Button("\u2716  Abbrechen");
        Button saveBtn = new Button("\u2714  Erstellen");

        cancelBtn.setPrefWidth(120);
        saveBtn.setPrefWidth(120);

        String baseBtn = "-fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-padding: 8 10 8 10; -fx-font-size: 13px;";

        cancelBtn.setStyle(
                baseBtn +
                        "-fx-background-color: #2A2A2A; " +
                        "-fx-text-fill: #F6F6F6; " +
                        "-fx-border-color: rgba(255,255,255,0.04);"
        );
        saveBtn.setStyle(
                baseBtn +
                        "-fx-background-color: linear-gradient(#3A6DFF, #2A56D6); " +
                        "-fx-text-fill: white;"
        );

        applyHover(cancelBtn, "#3D3D3D", "#2A2A2A", true);
        applyHover(saveBtn, "#4B7BFF", "#3A6DFF", true);

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, title, grid, buttons);
        root.setPadding(new Insets(14));
        // NEU: Fenster etwas breiter, damit Felder mehr Platz haben (mehr Abstand zum Fensterrand)
        root.setPrefWidth(600);

        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");
        root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.45), 14, 0.15, 0, 6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });

        setScene(scene);

        // Fenster in die Mitte des Owners setzen
        if (getOwner() != null) {
            double w = getOwner().getWidth();
            double h = getOwner().getHeight();
            double x = getOwner().getX();
            double y = getOwner().getY();
            setX(x + w / 2 - 240);
            setY(y + h / 2 - 120);
        }

        cancelBtn.setOnAction(e -> close());
        saveBtn.setOnAction(e -> handleSave());
    }

    private void handleSave() {
        errorLbl.setText("");

        String name = (nameField.getText() != null) ? nameField.getText().trim() : "";
        String email = (emailField.getText() != null) ? emailField.getText().trim() : "";
        String pwd = (passwordField.getText() != null) ? passwordField.getText() : "";
        String rolle = (rolleField.getText() != null && !rolleField.getText().isBlank()) ? rolleField.getText().trim() : "user";

        if (name.isEmpty()) {
            errorLbl.setText("Name ist erforderlich.");
            return;
        }
        if (email.isEmpty()) {
            errorLbl.setText("E-Mail ist erforderlich.");
            return;
        }
        if (pwd.isEmpty()) {
            errorLbl.setText("Passwort ist erforderlich.");
            return;
        }

        boolean ok = Demos.getDemoFamilie().erstelleBenutzer(name, email, pwd, rolle);
        if (!ok) {
            errorLbl.setText("Benutzer konnte nicht erstellt werden (Name evtl. bereits vorhanden).");
            return;
        }

        if (onCreated != null) {
            onCreated.accept(name);
        }
        close();
    }

    // Hover-Helfer (wie in TerminAdd)
    private void applyHover(Button b, String hoverBg, String normalBg, boolean useTranslate) {
        b.setOnMouseEntered(e -> {
            b.setCursor(Cursor.HAND);
            b.setScaleX(1.03);
            b.setScaleY(1.03);
            String style = b.getStyle();
            if (style.contains(normalBg)) {
                b.setStyle(style.replace(normalBg, hoverBg));
            }
            if (useTranslate) b.setTranslateY(-2);
        });
        b.setOnMouseExited(e -> {
            b.setCursor(Cursor.DEFAULT);
            b.setScaleX(1.0);
            b.setScaleY(1.0);
            String style = b.getStyle();
            if (style.contains(hoverBg)) {
                b.setStyle(style.replace(hoverBg, normalBg));
            }
            if (useTranslate) b.setTranslateY(0);
        });
    }

    public static void show(Stage owner, Consumer<String> onCreated) {
        BenutzerAdd d = new BenutzerAdd(owner, onCreated);
        d.showAndWait();
    }
}
