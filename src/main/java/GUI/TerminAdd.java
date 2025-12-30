package GUI;

import JavaLogik.Termin;
import JavaLogik.Kategorie;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class TerminAdd extends Stage {

    private TextField titelField;
    private DatePicker startDate;
    private TextField startTime;
    private DatePicker endDate;
    private TextField endTime;
    private TextArea beschreibung;
    private ComboBox<String> kategorieCb;
    private Label errorLbl;
    private final Consumer<Termin> onSaved;
    private final Runnable onKategorieChanged;
    private final Termin existingTermin;
    private final boolean editMode;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // neuer Termin
    public TerminAdd(Stage owner, LocalDate defaultDate, Consumer<Termin> onSaved, Runnable onKategorieChanged) {
        this.onSaved = onSaved;
        this.onKategorieChanged = onKategorieChanged;
        this.existingTermin = null;
        this.editMode = false;

        fensterInitialisieren(owner);
        felderInitialisieren(defaultDate);
        erstelleSzene("Neuen Termin anlegen");
    }

    // Termin bearbeiten
    public TerminAdd(Stage owner, LocalDate defaultDate, Termin existing, Consumer<Termin> onSaved, Runnable onKategorieChanged) {
        this.onSaved = onSaved;
        this.onKategorieChanged = onKategorieChanged;
        this.existingTermin = existing;
        this.editMode = (existing != null);

        fensterInitialisieren(owner);
        felderInitialisieren(defaultDate);
        felderFuellen();
        erstelleSzene("Termin bearbeiten");
    }

    private void fensterInitialisieren(Stage owner) {
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
    }

    private void felderInitialisieren(LocalDate defaultDate) {
        LocalDate initialDate = (defaultDate != null) ? defaultDate : LocalDate.now();

        titelField = new TextField();
        titelField.setPromptText("Titel");

        startDate = new DatePicker(initialDate);
        startTime = new TextField("09:00");
        startTime.setPromptText("HH:mm");

        endDate = new DatePicker(initialDate);
        endTime = new TextField("10:00");
        endTime.setPromptText("HH:mm");

        beschreibung = new TextArea();
        beschreibung.setPromptText("Beschreibung (optional)");
        beschreibung.setWrapText(true);
        beschreibung.setPrefRowCount(4);

        kategorieCb = new ComboBox<>();
        kategorieCb.setPromptText("Kategorie (optional)");

        // Kategorienamen laden
        try {
            java.util.List<String> katNamen = MainLogik.getKategorienNamen();
            if (katNamen != null && !katNamen.isEmpty()) {
                kategorieCb.getItems().addAll(katNamen);
            }
        } catch (Exception ex) {
            System.out.println("Kategorien konnten nicht geladen werden: " + ex.getMessage());
        }

        // Fehleranzeige
        errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF8888; -fx-font-size: 12px;");
    }

    // füllt die Felder mit den Daten des bestehenden Termins
    private void felderFuellen() {
        if (existingTermin == null) return;

        // Titel setzen
        titelField.setText(existingTermin.getTitel());

        // Start/Ende Datum + Uhrzeit setzen
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zs = ZonedDateTime.ofInstant(existingTermin.getStart(), zone);
        ZonedDateTime ze = ZonedDateTime.ofInstant(existingTermin.getEnde(), zone);

        startDate.setValue(zs.toLocalDate());
        startTime.setText(String.format("%02d:%02d", zs.getHour(), zs.getMinute()));

        endDate.setValue(ze.toLocalDate());
        endTime.setText(String.format("%02d:%02d", ze.getHour(), ze.getMinute()));

        // Beschreibung setzen
        if (existingTermin.getBeschreibung() != null) {
            beschreibung.setText(existingTermin.getBeschreibung());
        }


        // Kategorie setzen
        if (existingTermin.getKategorie() != null && existingTermin.getKategorie().getName() != null) {
            String kn = existingTermin.getKategorie().getName();
            if (!kategorieCb.getItems().contains(kn)) {
                kategorieCb.getItems().add(kn);
            }
            kategorieCb.setValue(kn);
        }

    }

    // baut UI
    private void erstelleSzene(String windowTitle) {
        setTitle(windowTitle);

        // Fenstertitel
        Label titleLabel = new Label(windowTitle);
        titleLabel.setFont(Font.font(16));
        titleLabel.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        // Grid für Label + Eingabefelder
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 0, 0));

        // Name
        Label lblTitel = new Label("Titel:");
        lblTitel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblTitel, 0, 0);
        grid.add(titelField, 1, 0, 2, 1);

        // Start
        Label lblStart = new Label("Start Datum:");
        lblStart.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblStart, 0, 1);
        grid.add(startDate, 1, 1);
        grid.add(startTime, 2, 1);

        // Ende
        Label lblEnde = new Label("Ende Datum:");
        lblEnde.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblEnde, 0, 2);
        grid.add(endDate, 1, 2);
        grid.add(endTime, 2, 2);

        // Kategorie
        Label lblKat = new Label("Kategorie:");
        lblKat.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblKat, 0, 3);

        // Button zum Erstellen einer neuen Kategorie
        Button addCategoryBtn = new Button("+");
        addCategoryBtn.setPrefSize(28, 28);
        addCategoryBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.06); " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #E6E6E6;"
        );

        // Hover auf Kategorie-Button
        addCategoryBtn.setOnMouseEntered(ev -> {
            addCategoryBtn.setCursor(Cursor.HAND);
            addCategoryBtn.setScaleX(1.06);
            addCategoryBtn.setScaleY(1.06);
        });
        addCategoryBtn.setOnMouseExited(ev -> {
            addCategoryBtn.setCursor(Cursor.DEFAULT);
            addCategoryBtn.setScaleX(1.0);
            addCategoryBtn.setScaleY(1.0);
        });

        // fügt neue Kategorie hinzu
        addCategoryBtn.setOnAction(ev -> {
            Stage owner = (getOwner() instanceof Stage) ? (Stage) getOwner() : null;

            KategorieAdd.show(owner,
                    (String newName) -> {
                        if (newName == null || newName.isBlank()) return;
                        javafx.application.Platform.runLater(() -> {
                            if (!kategorieCb.getItems().contains(newName)) {
                                kategorieCb.getItems().add(newName);
                            }
                            kategorieCb.setValue(newName);
                        });
                    },
                    // meldet nach außen: Kategorien wurden geändert
                    this.onKategorieChanged
            );
        });

        // Kategorie-Auswahl + Kategorie hinzufügen nebeneinander
        HBox katBox = new HBox(8, kategorieCb, addCategoryBtn);
        katBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(kategorieCb, Priority.ALWAYS);
        kategorieCb.setMaxWidth(Double.MAX_VALUE);

        grid.add(katBox, 1, 3, 2, 1);

        // Beschreibung
        Label lblBesch = new Label("Beschreibung:");
        lblBesch.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblBesch, 0, 4);
        grid.add(beschreibung, 1, 4, 2, 1);

        // Fehler
        grid.add(errorLbl, 0, 5, 3, 1);

        // Buttons unten (Abbrechen + Speichern)
        Button cancelBtn = new Button("\u2716  Abbrechen");
        Button saveBtn = new Button("\u2714  Speichern");

        cancelBtn.setPrefWidth(120);
        saveBtn.setPrefWidth(120);

        String baseBtn = "-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-padding: 8 10 8 10; -fx-font-size: 13px;";

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

        // Hover
        applyHover(cancelBtn, "#3D3D3D", "#2A2A2A", true);
        applyHover(saveBtn, "#4B7BFF", "#3A6DFF", true);

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, titleLabel, grid, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");

        // Schatten
        root.setEffect(new DropShadow(
                BlurType.GAUSSIAN,
                Color.rgb(0, 0, 0, 0.45),
                14, 0.15, 0, 6
        ));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // schließen mit ESC
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) close();
        });

        setScene(scene);
        centerOnScreen();

        cancelBtn.setOnAction(e -> close());
        saveBtn.setOnAction(e -> speichern());
    }

    // speichert entweder neuen Termin oder bearbeitet bestehenden
    private void speichern() {
        errorLbl.setText("");

        // Titel prüfen
        String titel = (titelField.getText() != null) ? titelField.getText().trim() : "";
        if (titel.isEmpty()) {
            errorLbl.setText("Titel ist erforderlich.");
            return;
        }

        // Datum prüfen
        LocalDate sd = startDate.getValue();
        LocalDate ed = endDate.getValue();
        if (sd == null || ed == null) {
            errorLbl.setText("Start- und Enddatum müssen gesetzt sein.");
            return;
        }

        // Zeit prüfen
        LocalTime st;
        LocalTime et;
        try {
            st = LocalTime.parse(startTime.getText().trim(), timeFormatter);
            et = LocalTime.parse(endTime.getText().trim(), timeFormatter);
        } catch (DateTimeParseException ex) {
            errorLbl.setText("Zeitformat muss HH:mm sein.");
            return;
        }
        ZonedDateTime zStart = ZonedDateTime.of(sd, st, ZoneId.systemDefault());
        ZonedDateTime zEnd = ZonedDateTime.of(ed, et, ZoneId.systemDefault());

        // Reihenfolge prüfen
        if (!zEnd.isAfter(zStart)) {
            errorLbl.setText("Ende muss nach Start liegen.");
            return;
        }

        // Speicherung in Instant
        Instant iStart = zStart.toInstant();
        Instant iEnd = zEnd.toInstant();

        // Beschreibung
        String beschr = (beschreibung.getText() != null) ? beschreibung.getText() : "";

        // Kategorie holen
        String selectedKatName = kategorieCb.getValue();
        Kategorie chosenKat = null;
        if (selectedKatName != null && !selectedKatName.isBlank()) {
            chosenKat = MainLogik.getKategoriePerName(selectedKatName);
        }

        // Neuer Termin
        if (!editMode) {
            Termin neu = new Termin(titel, iStart, iEnd, beschr, chosenKat);
            if (onSaved != null) onSaved.accept(neu);
            close();
        }
        // Bearbeiten
        else {
            boolean ok = MainLogik.terminBearbeiten(existingTermin, titel, iStart, iEnd, beschr, chosenKat);
            if (!ok) {
                errorLbl.setText("Konnte Termin nicht bearbeiten. Möglicher Konflikt mit anderem Termin.");
                return;
            }
            if (onSaved != null) onSaved.accept(existingTermin);
            close();
        }
    }

    // Öffnen
    public static void show(Stage owner, LocalDate defaultDate, Consumer<Termin> onSaved, Runnable onKategorieChanged) {
        TerminAdd d = new TerminAdd(owner, defaultDate, onSaved, onKategorieChanged);
        d.showAndWait();
    }

    // Öffnen: Edit
    public static void show(Stage owner, LocalDate defaultDate, Termin existing, Consumer<Termin> onSaved) {
        TerminAdd d = new TerminAdd(owner, defaultDate, existing, onSaved, null);
        d.showAndWait();
    }

    // Hover wie sonst auch
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
