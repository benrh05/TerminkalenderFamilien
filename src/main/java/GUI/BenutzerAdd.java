package GUI;

import JavaLogik.MainLogik;
import JavaLogik.Benutzer;
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

public class BenutzerAdd extends Stage {

    private TextField nameField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField rolleField;
    private Label errorLbl;
    private final java.util.function.Consumer<Benutzer> onCreated;

    // Konstruktor: liefert das erstellte Benutzer-Objekt an den Caller
    public BenutzerAdd(Stage owner, java.util.function.Consumer<Benutzer> onCreated) {
        this.onCreated = onCreated;
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        felderInitialisieren();
        erstelleSzene("Benutzer hinzufügen");
    }

    private void felderInitialisieren() {
        // Name-Eingabefeld
        nameField = new TextField();
        nameField.setPromptText("Name");

        // E-Mail-Eingabefeld
        emailField = new TextField();
        emailField.setPromptText("E-Mail");

        // Passwort-Eingabefeld
        passwordField = new PasswordField();
        passwordField.setPromptText("Passwort");

        // Rollenfeld -- irrelevant
        rolleField = new TextField("user");
        rolleField.setPromptText("Rolle (z.B. user)");

        // Fehler-Label
        errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF8888; -fx-font-size: 12px;");
    }

    private void erstelleSzene(String titleText) {
        setTitle(titleText);

        Label title = new Label(titleText);
        title.setFont(Font.font(16));
        title.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        // Anordnung der Felder in zwei Spalten
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 0, 0));

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(120);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(0);
        grid.getColumnConstraints().addAll(col0, col1, col2);

        // Label für Name
        Label lblName = new Label("Name:");
        lblName.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblName, 0, 0);
        grid.add(nameField, 1, 0, 2, 1);

        // Label für E-Mail
        Label lblEmail = new Label("E-Mail:");
        lblEmail.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblEmail, 0, 1);
        grid.add(emailField, 1, 1, 2, 1);

        // Label für Passwort
        Label lblPwd = new Label("Passwort:");
        lblPwd.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblPwd, 0, 2);
        grid.add(passwordField, 1, 2, 2, 1);

        // Label für Rolle
        Label lblRolle = new Label("Rolle:");
        lblRolle.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblRolle, 0, 3);
        grid.add(rolleField, 1, 3, 2, 1);

        // Fehler-Label
        grid.add(errorLbl, 0, 4, 3, 1);

        nameField.setMaxWidth(Double.MAX_VALUE);
        emailField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        rolleField.setMaxWidth(Double.MAX_VALUE);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        GridPane.setHgrow(rolleField, Priority.ALWAYS);

        // Abbrechen-Button
        Button cancelBtn = new Button("\u2716  Abbrechen");
        // Erstellen-Button
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

        // Layout
        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, title, grid, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(600);

        // Hintergrund
        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");
        root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.45), 14, 0.15, 0, 6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // ESC schließt den Dialog
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });

        setScene(scene);
        centerOnScreen();

        // Abbrechen schließt, Erstellen führt handleSave aus
        cancelBtn.setOnAction(e -> close());
        saveBtn.setOnAction(e -> handleSave());
    }

    private void handleSave() {
        // liest Felder, validiert und ruft MainLogik auf
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

        boolean created = false;
        try {
            created = MainLogik.benutzerErstellen(name, email, pwd, rolle);
        } catch (Throwable ex) {
            created = false;
            System.err.println("Fehler beim Anlegen des Benutzers: " + ex.getMessage());
        }

        if (!created) {
            errorLbl.setText("Benutzer konnte nicht angelegt werden (Name evtl. bereits vergeben).");
            return;
        }

        try {
            // Callback
            Benutzer createdObj = MainLogik.getBenutzerPerName(name);
            if (this.onCreated != null) {
                this.onCreated.accept(createdObj);
            }
        } catch (Throwable ex) {
            System.err.println("Benutzer angelegt, aber Abruf schlug fehl: " + ex.getMessage());
            if (this.onCreated != null) this.onCreated.accept(null);
        }
        close();
    }

    private void applyHover(Button b, String hoverBg, String normalBg, boolean useTranslate) {  // Hover wie sonst auch
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

    public static void show(Stage owner, java.util.function.Consumer<Benutzer> onCreated) {
        BenutzerAdd d = new BenutzerAdd(owner, onCreated);
        d.showAndWait();
    }
}
