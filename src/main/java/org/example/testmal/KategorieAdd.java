package org.example.testmal;

import JavaLogik.Kategorie;
import JavaLogik.Benutzer;
import JavaLogik.MainLogik;
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


public class KategorieAdd extends Stage {

    private TextField nameField;
    private ColorPicker colorPicker;
    private TextField colorHexField;
    private Label errorLbl;
    private final Consumer<String> onCreated;

    public KategorieAdd(Stage owner, Consumer<String> onCreated) {
        this.onCreated = onCreated;
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        initFields();
        buildScene("Kategorie erstellen");
    }

    private void initFields() {
        nameField = new TextField();
        nameField.setPromptText("Kategoriename");

        colorPicker = new ColorPicker(Color.web("#4A90E2"));
        colorHexField = new TextField("#4A90E2");
        colorHexField.setPrefWidth(100);
        colorHexField.setPromptText("#RRGGBB");
        // synchronize picker <-> hex field
        colorPicker.setOnAction(e -> {
            String hex = toHex(colorPicker.getValue());
            colorHexField.setText(hex);
        });
        colorHexField.setOnKeyReleased(e -> {
            String txt = colorHexField.getText().trim();
            try {
                if (!txt.isEmpty()) {
                    Color c = Color.web(txt);
                    colorPicker.setValue(c);
                }
            } catch (Exception ignored) {}
        });

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

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(120);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        Label lblName = new Label("Name:");
        lblName.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblName, 0, 0);
        grid.add(nameField, 1, 0);

        Label lblColor = new Label("Farbe:");
        lblColor.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        HBox colorBox = new HBox(8, colorPicker, colorHexField);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(lblColor, 0, 1);
        grid.add(colorBox, 1, 1);

        grid.add(errorLbl, 0, 2, 2, 1);

        Button cancelBtn = new Button("\u2716  Abbrechen");
        Button saveBtn = new Button("\u2714  Erstellen");
        cancelBtn.setPrefWidth(120);
        saveBtn.setPrefWidth(120);

        String baseBtn = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 10 8 10; -fx-font-size: 13px;";
        cancelBtn.setStyle(baseBtn + "-fx-background-color: #2A2A2A; -fx-text-fill: #F6F6F6;");
        saveBtn.setStyle(baseBtn + "-fx-background-color: linear-gradient(#3A6DFF, #2A56D6); -fx-text-fill: white;");

        applyHover(cancelBtn, "#3D3D3D", "#2A2A2A", true);
        applyHover(saveBtn, "#4B7BFF", "#3A6DFF", true);

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, title, grid, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");
        root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.45), 12, 0.12, 0, 6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) close();
        });

        setScene(scene);

        if (getOwner() != null) {
            double w = getOwner().getWidth();
            double h = getOwner().getHeight();
            double x = getOwner().getX();
            double y = getOwner().getY();
            setX(x + w / 2 - 210);
            setY(y + h / 2 - 120);
        }

        cancelBtn.setOnAction(e -> close());
        saveBtn.setOnAction(e -> onSave());
    }

    private void onSave() {
        errorLbl.setText("");
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        if (name.isEmpty()) {
            errorLbl.setText("Name ist erforderlich.");
            return;
        }

        // check duplicate via MainLogik (current user context)
        try {
            if (MainLogik.getKategorieByName(name) != null) {
                errorLbl.setText("Kategorie existiert bereits.");
                return;
            }
        } catch (Throwable ex) {
            // ignore, let creation attempt fail later if necessary
        }

        String hex = colorHexField.getText() != null ? colorHexField.getText().trim() : toHex(colorPicker.getValue());
        if (!hex.startsWith("#")) hex = "#" + hex;
        // simple normalization: ensure it's a valid color
        try {
            Color.web(hex);
        } catch (Exception ex) {
            errorLbl.setText("Ungültiger Farbwert.");
            return;
        }

        // create and add to current user's calendar
        try {
            String currentUser = MainLogik.getCurrentUserName();
            if (currentUser == null) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Kein aktueller Benutzer angemeldet.");
                a.initOwner(getOwner());
                a.showAndWait();
                return;
            }
            Benutzer b = MainLogik.getBenutzerByName(currentUser);
            if (b == null) {
                Alert a = new Alert(Alert.AlertType.ERROR, "Aktueller Benutzer nicht gefunden.");
                a.initOwner(getOwner());
                a.showAndWait();
                return;
            }

            Kategorie k = new Kategorie(name, hex);
            b.getKalender().kategorieHinzufuegen(k);

            // Callback informieren
            if (this.onCreated != null) {
                try { this.onCreated.accept(name); } catch (Exception ignored) {}
            }
            close();
        } catch (Throwable ex) {
            System.err.println("Kategorie konnte nicht angelegt werden: " + ex.getMessage());
            Alert a = new Alert(Alert.AlertType.ERROR, "Kategorie konnte nicht erstellt werden.");
            a.initOwner(getOwner());
            a.showAndWait();
        }
    }

    private String toHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    // Hover-Helfer (wie in anderen Dialogen)
    private void applyHover(Button b, String hoverBg, String normalBg, boolean useTranslate) {
        b.setOnMouseEntered(e -> {
            b.setCursor(Cursor.HAND);
            b.setScaleX(1.03);
            b.setScaleY(1.03);
            String style = b.getStyle();
            if (style.contains(normalBg)) b.setStyle(style.replace(normalBg, hoverBg));
            if (useTranslate) b.setTranslateY(-2);
        });
        b.setOnMouseExited(e -> {
            b.setCursor(Cursor.DEFAULT);
            b.setScaleX(1.0);
            b.setScaleY(1.0);
            String style = b.getStyle();
            if (style.contains(hoverBg)) b.setStyle(style.replace(hoverBg, normalBg));
            if (useTranslate) b.setTranslateY(0);
        });
    }

    // Static show convenience
    public static void show(Stage owner, Consumer<String> onCreated) {
        KategorieAdd d = new KategorieAdd(owner, onCreated);
        d.showAndWait();
    }
}
