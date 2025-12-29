// java
package GUI;

import JavaLogik.MainLogik;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    private ColorPicker farbenAuswahl;
    private TextField farbenHexFeld;
    private Label errorLbl;
    private final Consumer<String> onCreated;
    private final Runnable onChanged;

    public KategorieAdd(Stage owner, Consumer<String> onCreated, Runnable onChanged) {
        this.onCreated = onCreated;
        this.onChanged = onChanged;
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        felderInitialisieren();
        erstelleSzene("Kategorie erstellen");
    }

    // UI

    private void felderInitialisieren() {
        // Eingabefeld für Kategoriename
        nameField = new TextField();
        nameField.setPromptText("Kategoriename");

        // ColorPicker zur Farbauswahl
        farbenAuswahl = new ColorPicker(Color.web("#4A90E2"));

        // Textfeld für Farb-Hex (#RRGGBB)
        farbenHexFeld = new TextField("#4A90E2");
        farbenHexFeld.setPrefWidth(100);
        farbenHexFeld.setPromptText("#RRGGBB");

        farbenAuswahl.setOnAction(e -> farbenHexFeld.setText(toHex(farbenAuswahl.getValue())));

        farbenHexFeld.setOnKeyReleased(e -> {
            String txt = sicherTrimmen(farbenHexFeld.getText());
            if (txt.isEmpty()) return;
            try {
                farbenAuswahl.setValue(Color.web(txt));
            } catch (Exception ignored) {
                // einfach ignorieren
            }
        });

        // Label für Fehlermeldungen
        errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF8888; -fx-font-size: 12px;");
    }

    private void erstelleSzene(String titleText) {
        setTitle(titleText);

        // Titel-Label
        Label title = new Label(titleText);
        title.setFont(Font.font(16));
        title.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        // Grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 0, 0));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(120);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        // Label für Name
        Label lblName = new Label("Name:");
        lblName.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblName, 0, 0);
        grid.add(nameField, 1, 0);

        // Label für Farbe
        Label lblColor = new Label("Farbe:");
        lblColor.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        HBox colorBox = new HBox(8, farbenAuswahl, farbenHexFeld);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(lblColor, 0, 1);
        grid.add(colorBox, 1, 1);

        // Error-Label in Grid
        grid.add(errorLbl, 0, 2, 2, 1);

        // Buttons: Abbrechen
        Button cancelBtn = new Button("\u2716  Abbrechen");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: #F6F6F6;");
        applyHover(cancelBtn, "#3D3D3D", "#2A2A2A", true);

        // Buttons: Erstellen
        Button saveBtn = new Button("\u2714  Erstellen");
        saveBtn.setPrefWidth(120);
        saveBtn.setStyle("-fx-background-color: linear-gradient(#3A6DFF, #2A56D6); -fx-text-fill: white;");
        applyHover(saveBtn, "#4B7BFF", "#3A6DFF", true);

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, title, grid, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");
        root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.45), 12, 0.12, 0, 6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // ESC schließt
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) close();
        });

        setScene(scene);
        centerOnScreen();

        cancelBtn.setOnAction(e -> close());
        saveBtn.setOnAction(e -> onSave());
    }

    private void onSave() {
        errorLbl.setText("");

        String name = sicherTrimmen(nameField.getText());
        if (name.isEmpty()) {
            errorLbl.setText("Name ist erforderlich.");
            return;
        }

        String hex = sicherTrimmen(farbenHexFeld.getText());
        if (hex.isEmpty()) {
            hex = toHex(farbenAuswahl.getValue());
        }
        if (!hex.startsWith("#")) hex = "#" + hex;

        if (!valideFarbe(hex)) {
            errorLbl.setText("Ungültiger Farbwert.");
            return;
        }

        boolean korrekt;
        try {
            korrekt = MainLogik.kategorieErstellen(name, hex);
        } catch (Throwable ex) {
            System.err.println("Fehler beim Erstellen der Kategorie: " + ex.getMessage());
            korrekt = false;
        }

        if (!korrekt) {
            errorLbl.setText("Kategorie konnte nicht angelegt werden (evtl. Name bereits vorhanden).");
            return;
        }

        // Callbacks nach erfolgreichem Speichern
        safeAccept(onCreated, name);
        safeRun(onChanged);

        close();
    }

    private boolean valideFarbe(String hex) {
        try {
            Color.web(hex);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static String sicherTrimmen(String s) {  // null-sicher trimmen
        return (s == null) ? "" : s.trim();
    }

    private static void safeAccept(Consumer<String> c, String value) {  // null-sicher accept
        if (c == null) return;
        try {
            c.accept(value);
        } catch (Exception ignored) {
        }
    }

    private static void safeRun(Runnable r) {  // null-sicher run
        if (r == null) return;
        try {
            r.run();
        } catch (Exception ignored) {
        }
    }

    private String toHex(Color c) {     // Farbe in #RRGGBB
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private void applyHover(Button b, String hoverBg, String normalBg, boolean useTranslate) {   // Hover wie sonst auch
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
    // show für StandardAnsicht: nur Rückgabe des neu erstellten Kategorienamens
    public static void show(Stage owner, Consumer<String> onCreated) {
        new KategorieAdd(owner, onCreated, null).showAndWait();
    }

    // show für TerminAdd: Rückgabe + zusätzliches akualisieren
    public static void show(Stage owner, Consumer<String> onCreated, Runnable onChanged) {
        new KategorieAdd(owner, onCreated, onChanged).showAndWait();
    }
}
