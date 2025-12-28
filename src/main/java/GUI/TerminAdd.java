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

    // --- Felder für das Formular ---
    private TextField titelField;
    private DatePicker startDate;
    private TextField startTime;
    private DatePicker endDate;
    private TextField endTime;
    private TextArea beschreibung;
    private ComboBox<String> kategorieCb;
    private Label errorLbl;

    // --- Logik-Felder ---
    private final Consumer<Termin> onSaved;
    private final Termin existingTermin;
    private final boolean editMode;

    // Formatter für die Zeit-Eingabe
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // --- Konstruktor für "Neuen Termin anlegen" ---
    public TerminAdd(Stage owner, LocalDate defaultDate, Consumer<Termin> onSaved) {
        this.onSaved = onSaved;
        this.existingTermin = null;
        this.editMode = false;

        initWindow(owner);
        initFields(defaultDate);
        // Kein bestehender Termin → nichts zum Vorbefüllen
        buildScene("Neuen Termin anlegen");
    }

    // --- Konstruktor für "Termin bearbeiten" ---
    public TerminAdd(Stage owner, LocalDate defaultDate, Termin existing, Consumer<Termin> onSaved) {
        this.onSaved = onSaved;
        this.existingTermin = existing;
        this.editMode = (existing != null);

        initWindow(owner);
        initFields(defaultDate);
        fillFieldsFromExisting();
        buildScene("Termin bearbeiten");
    }

    // --- Fenster-Grundeinstellungen ---
    private void initWindow(Stage owner) {
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
    }

    // --- Formular-Felder anlegen und Kategorien laden ---
    private void initFields(LocalDate defaultDate) {
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
        try {
            java.util.List<String> katNamen = MainLogik.getKategorienNamen();
            if (katNamen != null && !katNamen.isEmpty()) {
                kategorieCb.getItems().addAll(katNamen);
            }
        } catch (Exception ex) {
            System.out.println("Kategorien konnten nicht geladen werden: " + ex.getMessage());
        }

        errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF8888; -fx-font-size: 12px;");
    }

    // --- Felder aus bestehendem Termin vorbefüllen (für Edit-Modus) ---
    private void fillFieldsFromExisting() {
        if (existingTermin == null) {
            return;
        }

        try {
            if (existingTermin.getTitel() != null) {
                titelField.setText(existingTermin.getTitel());
            }
        } catch (Exception ignored) {}

        try {
            ZoneId zone = ZoneId.systemDefault();
            ZonedDateTime zs = ZonedDateTime.ofInstant(existingTermin.getStart(), zone);
            ZonedDateTime ze = ZonedDateTime.ofInstant(existingTermin.getEnde(), zone);

            startDate.setValue(zs.toLocalDate());
            startTime.setText(String.format("%02d:%02d", zs.getHour(), zs.getMinute()));

            endDate.setValue(ze.toLocalDate());
            endTime.setText(String.format("%02d:%02d", ze.getHour(), ze.getMinute()));
        } catch (Exception ignored) {}

        try {
            if (existingTermin.getBeschreibung() != null) {
                beschreibung.setText(existingTermin.getBeschreibung());
            }
        } catch (Exception ignored) {}

        try {
            if (existingTermin.getKategorie() != null && existingTermin.getKategorie().getName() != null) {
                String kn = existingTermin.getKategorie().getName();
                if (!kategorieCb.getItems().contains(kn)) {
                    kategorieCb.getItems().add(kn);
                }
                kategorieCb.setValue(kn);
            }
        } catch (Exception ignored) {}
    }

    // --- Szene / Layout bauen ---
    private void buildScene(String windowTitle) {
        setTitle(windowTitle);

        Label titleLabel = new Label(windowTitle);
        titleLabel.setFont(Font.font(16));
        titleLabel.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 0, 0));

        Label lblTitel = new Label("Titel:");
        lblTitel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblTitel, 0, 0);
        grid.add(titelField, 1, 0, 2, 1);

        Label lblStart = new Label("Start Datum:");
        lblStart.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblStart, 0, 1);
        grid.add(startDate, 1, 1);
        grid.add(startTime, 2, 1);

        Label lblEnde = new Label("Ende Datum:");
        lblEnde.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblEnde, 0, 2);
        grid.add(endDate, 1, 2);
        grid.add(endTime, 2, 2);

        Label lblKat = new Label("Kategorie:");
        lblKat.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblKat, 0, 3);

        // --- CHANGED: Kategorie-ComboBox + kleiner "+" Button zum Erstellen neuer Kategorien ---
        Button addCategoryBtn = new Button("+");
        addCategoryBtn.setPrefSize(28, 28);
        addCategoryBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.06); " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #E6E6E6;"
        );
        // Dezenter Hover-Effekt (ähnlich wie andere Buttons)
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

        // Wenn auf "+" geklickt wird: KategorieAdd öffnen und Ergebnis in Combo übernehmen
        addCategoryBtn.setOnAction(ev -> {
            Stage owner = (getOwner() instanceof Stage) ? (Stage) getOwner() : null;
            KategorieAdd.show(owner, (String newName) -> {
                if (newName == null || newName.isBlank()) return;
                // UI-Update im JavaFX-Thread
                javafx.application.Platform.runLater(() -> {
                    if (!kategorieCb.getItems().contains(newName)) {
                        kategorieCb.getItems().add(newName);
                    }
                    kategorieCb.setValue(newName);
                });
            });
        });

        HBox katBox = new HBox(8, kategorieCb, addCategoryBtn);
        katBox.setAlignment(Pos.CENTER_LEFT);
        // ensure combo grows, button keeps fixed size
        HBox.setHgrow(kategorieCb, Priority.ALWAYS);
        kategorieCb.setMaxWidth(Double.MAX_VALUE);

        grid.add(katBox, 1, 3, 2, 1);

        Label lblBesch = new Label("Beschreibung:");
        lblBesch.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        grid.add(lblBesch, 0, 4);
        grid.add(beschreibung, 1, 4, 2, 1);

        grid.add(errorLbl, 0, 5, 3, 1);

        Button cancelBtn = new Button("\u2716  Abbrechen");
        Button saveBtn = new Button("\u2714  Speichern");

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

        VBox root = new VBox(12, titleLabel, grid, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");
        root.setEffect(new DropShadow(
                BlurType.GAUSSIAN,
                Color.rgb(0, 0, 0, 0.45),
                14, 0.15, 0, 6
        ));

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

    // --- Speichern-Logik (für Neu & Edit) ---
    private void handleSave() {
        errorLbl.setText("");

        String titel = (titelField.getText() != null) ? titelField.getText().trim() : "";
        if (titel.isEmpty()) {
            errorLbl.setText("Titel ist erforderlich.");
            return;
        }

        LocalDate sd = startDate.getValue();
        LocalDate ed = endDate.getValue();
        if (sd == null || ed == null) {
            errorLbl.setText("Start- und Enddatum müssen gesetzt sein.");
            return;
        }

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

        if (!zEnd.isAfter(zStart)) {
            errorLbl.setText("Ende muss nach Start liegen.");
            return;
        }

        Instant iStart = zStart.toInstant();
        Instant iEnd = zEnd.toInstant();
        String beschr = (beschreibung.getText() != null) ? beschreibung.getText() : "";

        String selectedKatName = kategorieCb.getValue();
        Kategorie chosenKat = null;
        if (selectedKatName != null && !selectedKatName.isBlank()) {
            chosenKat = MainLogik.getKategorieByName(selectedKatName);
        }

        if (!editMode) {
            // Neuen Termin anlegen
            Termin neu = new Termin(titel, iStart, iEnd, beschr, chosenKat);
            if (onSaved != null) {
                onSaved.accept(neu);
            }
            close();
        } else {
            // Bestehenden Termin bearbeiten
            if (existingTermin == null) {
                errorLbl.setText("Kein bestehender Termin zum Bearbeiten.");
                return;
            }

            boolean ok = MainLogik.editTermin(existingTermin, titel, iStart, iEnd, beschr, chosenKat);
            if (!ok) {
                errorLbl.setText("Konnte Termin nicht bearbeiten.");
                return;
            }

            if (onSaved != null) {
                onSaved.accept(existingTermin);
            }
            close();
        }
    }

    // --- Öffnen für "Neu" ---
    public static void show(Stage owner, LocalDate defaultDate, Consumer<Termin> onSaved) {
        TerminAdd d = new TerminAdd(owner, defaultDate, onSaved);
        d.showAndWait();
    }

    // --- Öffnen für "Edit" ---
    public static void show(Stage owner, LocalDate defaultDate, Termin existing, Consumer<Termin> onSaved) {
        TerminAdd d = new TerminAdd(owner, defaultDate, existing, onSaved);
        d.showAndWait();
    }

    // --- Hover-Effekt-Helfer ---
    private void applyHover(Button b, String hoverBg, String normalBg, boolean useTranslate) {
        b.setOnMouseEntered(e -> {
            b.setCursor(Cursor.HAND);
            b.setScaleX(1.03);
            b.setScaleY(1.03);
            String style = b.getStyle();
            if (style.contains(normalBg)) {
                b.setStyle(style.replace(normalBg, hoverBg));
            }
            if (useTranslate) {
                b.setTranslateY(-2);
            }
        });

        b.setOnMouseExited(e -> {
            b.setCursor(Cursor.DEFAULT);
            b.setScaleX(1.0);
            b.setScaleY(1.0);
            String style = b.getStyle();
            if (style.contains(hoverBg)) {
                b.setStyle(style.replace(hoverBg, normalBg));
            }
            if (useTranslate) {
                b.setTranslateY(0);
            }
        });
    }
}
