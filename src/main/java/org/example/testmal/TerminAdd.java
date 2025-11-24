package org.example.testmal;

import JavaLogik.Termin;
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class TerminAdd extends Stage {

    public TerminAdd(Stage owner, LocalDate defaultDate, Consumer<Termin> onSaved) {
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Neuen Termin erstellen");

        // --- Titel ---
        Label title = new Label("Neuen Termin anlegen");
        title.setFont(Font.font(16));
        title.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        // --- Felder ---
        TextField titelField = new TextField();
        titelField.setPromptText("Titel");

        DatePicker startDate = new DatePicker(defaultDate != null ? defaultDate : LocalDate.now());
        TextField startTime = new TextField("09:00");
        startTime.setPromptText("HH:mm");

        DatePicker endDate = new DatePicker(defaultDate != null ? defaultDate : LocalDate.now());
        TextField endTime = new TextField("10:00");
        endTime.setPromptText("HH:mm");

        TextArea beschreibung = new TextArea();
        beschreibung.setPromptText("Beschreibung (optional)");
        beschreibung.setWrapText(true);
        beschreibung.setPrefRowCount(4);

        // Kategorie als einfache String-Liste (falls Kategorie-API vorhanden, kann hier angepasst werden)
        ComboBox<String> kategorieCb = new ComboBox<>();
        kategorieCb.setPromptText("Kategorie (optional)");
        kategorieCb.getItems().addAll("Allgemein", "Arbeit", "Privat", "Wichtig");

        // Fehlermeldung
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF8888; -fx-font-size: 12px;");

        // Grid für Eingaben
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 0, 0));

        grid.add(new Label("Titel:"), 0, 0);
        grid.add(titelField, 1, 0, 2, 1);

        grid.add(new Label("Start Datum:"), 0, 1);
        grid.add(startDate, 1, 1);
        grid.add(startTime, 2, 1);

        grid.add(new Label("Ende Datum:"), 0, 2);
        grid.add(endDate, 1, 2);
        grid.add(endTime, 2, 2);

        grid.add(new Label("Kategorie:"), 0, 3);
        grid.add(kategorieCb, 1, 3, 2, 1);

        grid.add(new Label("Beschreibung:"), 0, 4);
        grid.add(beschreibung, 1, 4, 2, 1);

        grid.add(errorLbl, 0, 5, 3, 1);

        // Buttons
        Button cancelBtn = new Button("\u2716  Abbrechen");
        Button saveBtn = new Button("\u2714  Speichern");

        cancelBtn.setPrefWidth(120);
        saveBtn.setPrefWidth(120);

        String baseBtn = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 10 8 10; -fx-font-size: 13px;";
        cancelBtn.setStyle(baseBtn + "-fx-background-color: #2A2A2A; -fx-text-fill: #F6F6F6; -fx-border-color: rgba(255,255,255,0.04);");
        saveBtn.setStyle(baseBtn + "-fx-background-color: linear-gradient(#3A6DFF, #2A56D6); -fx-text-fill: white;");

        applyHover(cancelBtn, "#3D3D3D", "#2A2A2A", true);
        applyHover(saveBtn, "#4B7BFF", "#3A6DFF", true);

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // Root container
        VBox root = new VBox(12, title, grid, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");
        root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.45), 14, 0.15, 0, 6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // ESC schließt
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) close();
        });

        setScene(scene);

        // Positionierung: in der Mitte des Owners
        if (owner != null) {
            setX(owner.getX() + owner.getWidth() / 2 - 240);
            setY(owner.getY() + owner.getHeight() / 2 - 120);
        }

        cancelBtn.setOnAction(e -> close());

        saveBtn.setOnAction(e -> {
            errorLbl.setText("");
            String titel = titelField.getText() != null ? titelField.getText().trim() : "";
            if (titel.isEmpty()) {
                errorLbl.setText("Titel ist erforderlich.");
                return;
            }

            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
            LocalDate sd = startDate.getValue();
            LocalDate ed = endDate.getValue();
            if (sd == null || ed == null) {
                errorLbl.setText("Start- und Enddatum müssen gesetzt sein.");
                return;
            }

            LocalTime st;
            LocalTime et;
            try {
                st = LocalTime.parse(startTime.getText().trim(), tf);
                et = LocalTime.parse(endTime.getText().trim(), tf);
            } catch (DateTimeParseException ex) {
                errorLbl.setText("Zeitformat muss HH:mm sein.");
                return;
            }

            ZonedDateTime zStart = ZonedDateTime.of(sd, st, ZoneId.systemDefault());
            ZonedDateTime zEnd = ZonedDateTime.of(ed, et, ZoneId.systemDefault());

            if (zEnd.isBefore(zStart) || zEnd.isEqual(zStart)) {
                errorLbl.setText("Ende muss nach Start liegen.");
                return;
            }

            Instant iStart = zStart.toInstant();
            Instant iEnd = zEnd.toInstant();
            String beschr = beschreibung.getText();
            // Kategorie-Objekt nicht verfügbar / API unknown -> übergebe null (kann später ersetzt werden)
            Object selectedCat = kategorieCb.getValue();
            // JavaLogik.Termin erwartet Kategorie als Typ; wir übergeben null, da Kategorie-Klasse nicht verändert werden soll.
            Termin neu = new Termin(titel, iStart, iEnd, beschr, null);

            if (onSaved != null) {
                onSaved.accept(neu);
            }
            close();
        });
    }

    public static void show(Stage owner, LocalDate defaultDate, Consumer<Termin> onSaved) {
        TerminAdd d = new TerminAdd(owner, defaultDate, onSaved);
        d.showAndWait();
    }

    // kleiner Hover-Helper (kopiert/angepasst von FehlendeRechte)
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
}
