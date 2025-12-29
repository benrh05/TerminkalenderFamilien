package GUI;

import JavaLogik.Termin;
import JavaLogik.MainLogik;
import JavaLogik.Demos;
import JavaLogik.Benutzer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.control.CustomMenuItem;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DayView extends VBox {

    private final Label header;
    private final Label infoLabel;

    private final VBox timeColumn;         // linke Spalte mit Uhrzeiten
    private final VBox timelineBackground; // Stundenraster
    private final Pane eventLayer;         // Ebene mit Terminblöcken

    private Runnable onRequestBack;

    private LocalDate currentDate;

    // Dynamische Skala (wird in show(...) gesetzt)
    private int startHour = 8;
    private int endHour = 20;
    private double minuteHeight = 1.0; // 1 px pro Minute => 60px pro Stunde

    // für sauberen Gleichlauf zwischen Zeitspalte / Raster / Terminen
    private static final double TOP_BOTTOM_OFFSET = 4.0; // entspricht dem Padding der timeColumn (oben/unten)

    public DayView() {
        setSpacing(12);
        setPadding(new Insets(14));
        setStyle("-fx-background-color: transparent;");

        // --- Header ---
        header = new Label();
        header.setFont(Font.font(20));
        header.setStyle("-fx-text-fill: #EFEFEF; -fx-font-weight: 500;");

        infoLabel = new Label("Keine Termine");
        infoLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 12px;");

        Button backBtn = new Button("\u2190 Zurück");
        backBtn.setStyle("-fx-background-color: #2A2A2A; -fx-background-radius: 8; -fx-text-fill: #F2F2F2;");
        backBtn.setOnAction(e -> {
            if (onRequestBack != null) onRequestBack.run();
        });
        backBtn.setOnMouseEntered(e -> {
            backBtn.setScaleX(1.03);
            backBtn.setScaleY(1.03);
            backBtn.setCursor(Cursor.HAND);
        });
        backBtn.setOnMouseExited(e -> {
            backBtn.setScaleX(1.0);
            backBtn.setScaleY(1.0);
            backBtn.setCursor(Cursor.DEFAULT);
        });

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox headerBar = new HBox(10, backBtn, header, headerSpacer, infoLabel);
        headerBar.setAlignment(Pos.CENTER_LEFT);

        // --- Zeitachse + Timeline ---
        timeColumn = new VBox();
        timeColumn.setSpacing(0);
        timeColumn.setPrefWidth(60);
        timeColumn.setPadding(new Insets(TOP_BOTTOM_OFFSET, 8, TOP_BOTTOM_OFFSET, 0));

        timelineBackground = new VBox();
        timelineBackground.setSpacing(0);
        timelineBackground.setFillWidth(true);

        // Gleicher vertikaler Offset wie timeColumn
        timelineBackground.setPadding(new Insets(TOP_BOTTOM_OFFSET, 0, TOP_BOTTOM_OFFSET, 0));

        eventLayer = new Pane();
        eventLayer.setPickOnBounds(false);

        // Termine starten auf derselben Höhe wie Raster/TimeColumn
        eventLayer.setTranslateY(TOP_BOTTOM_OFFSET);

        // initiale Timeline (wird später durch show(...) überschrieben)
        rebuildTimeline(startHour, endHour);

        StackPane timelineStack = new StackPane();
        timelineStack.getChildren().addAll(timelineBackground, eventLayer);
        StackPane.setAlignment(timelineBackground, Pos.TOP_LEFT);
        StackPane.setAlignment(eventLayer, Pos.TOP_LEFT);

        // WICHTIG: TimelineStack soll seine Höhe aus timelineBackground übernehmen (sonst kann unten abgeschnitten werden)
        timelineStack.minHeightProperty().bind(timelineBackground.prefHeightProperty());
        timelineStack.prefHeightProperty().bind(timelineBackground.prefHeightProperty());
        timelineStack.setMaxHeight(Region.USE_PREF_SIZE);

        HBox content = new HBox(12, timeColumn, timelineStack);
        content.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(timelineStack, Priority.ALWAYS);

        timelineBackground.prefWidthProperty().bind(timelineStack.widthProperty());
        eventLayer.prefWidthProperty().bind(timelineStack.widthProperty());

        // --- ScrollPane, damit auch sehr späte Termine sichtbar bleiben ---
        ScrollPane scroll = new ScrollPane(content);

        // WICHTIG: fitToHeight = false, sonst wird der Content auf Viewport-Höhe "zusammengequetscht"
        // und die unteren Bereiche können visuell abgeschnitten werden.
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);

        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Optisch: transparent, damit es wie vorher aussieht
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        content.setStyle("-fx-background-color: transparent;");

        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(headerBar, scroll);
        setAlignment(Pos.TOP_LEFT);
    }

    // baut Stundenlabels neu basierend auf startHour/endHour
    private void buildTimeColumn() {
        timeColumn.getChildren().clear();
        // Hinweis: wir listen Stunden von startHour bis endHour-1 (konsistent mit background rows)
        for (int h = startHour; h < endHour; h++) {
            Label timeLabel = new Label(String.format("%02d:00", h));
            timeLabel.setStyle("-fx-text-fill: #CFCFCF; -fx-font-size: 11px;");
            timeLabel.setPrefWidth(60);

            // Fixe Höhe, damit VBox nichts "zusammenquetscht"
            double rowHeight = 60 * minuteHeight;
            timeLabel.setMinHeight(rowHeight);
            timeLabel.setPrefHeight(rowHeight);
            timeLabel.setMaxHeight(rowHeight);

            timeLabel.setAlignment(Pos.TOP_RIGHT);
            timeColumn.getChildren().add(timeLabel);
        }
    }

    private void buildBackgroundRows() {
        timelineBackground.getChildren().clear();
        for (int h = startHour; h < endHour; h++) {
            Region row = new Region();
            row.setMinHeight(60 * minuteHeight);
            row.setPrefHeight(60 * minuteHeight);
            row.setMaxHeight(60 * minuteHeight);
            row.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.02); " +
                            "-fx-border-color: rgba(255,255,255,0.06); " +
                            "-fx-border-width: 0 0 1 0;"
            );
            timelineBackground.getChildren().add(row);
        }
    }

    private void rebuildTimeline(int sHour, int eHour) {
        this.startHour = sHour;
        this.endHour = eHour;

        double totalMinutes = (endHour - startHour) * 60.0;
        double totalHeight = totalMinutes * minuteHeight;

        // timeColumn + timelineBackground haben oben+unten Padding, eventLayer ist nach unten verschoben
        double extra = TOP_BOTTOM_OFFSET * 2;

        timeColumn.setPrefHeight(totalHeight + extra);
        timelineBackground.setPrefHeight(totalHeight + extra);
        eventLayer.setPrefHeight(totalHeight + extra);

        buildTimeColumn();
        buildBackgroundRows();
    }

    // öffentliche API: zeigt ein Datum und die Liste von JavaLogik.Termin an
    public void show(LocalDate date, List<Termin> termine) {
        this.currentDate = date;
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy");
        header.setText(date.format(f));

        ZoneId zone = ZoneId.systemDefault();

        // dynamische Timeline-Grenzen bestimmen (min 8 Std., cap 0..24)
        int minHour = termine == null || termine.isEmpty() ? 8 :
                termine.stream()
                        .map(t -> ZonedDateTime.ofInstant(t.getStart(), zone).getHour())
                        .min(Integer::compare)
                        .orElse(8);
        int maxHour = termine == null || termine.isEmpty() ? (minHour + 8) :
                termine.stream()
                        .map(t -> ZonedDateTime.ofInstant(t.getEnde(), zone).getHour())
                        .max(Integer::compare)
                        .orElse(minHour + 8);

        int sHour = Math.max(0, minHour);
        int eHour = Math.min(24, Math.max(minHour + 8, maxHour));

        rebuildTimeline(sHour, eHour);

        // Events rendern
        eventLayer.getChildren().clear();

        if (termine == null || termine.isEmpty()) {
            infoLabel.setText("Keine Termine");
            return;
        } else {
            infoLabel.setText(termine.size() + " Termin(e)");
        }

        LocalTime dayStartTime = LocalTime.of(startHour, 0);
        // LocalTime.of(24,0) wäre ungültig -> bei 24 als Grenze 23:59 benutzen (als oberes Limit)
        LocalTime dayEndTime = (endHour == 24) ? LocalTime.of(23, 59) : LocalTime.of(endHour, 0);

        for (Termin t : termine) {
            ZonedDateTime zs = ZonedDateTime.ofInstant(t.getStart(), zone);
            ZonedDateTime ze = ZonedDateTime.ofInstant(t.getEnde(), zone);
            LocalTime startTime = zs.toLocalTime();
            LocalTime endTime = ze.toLocalTime();

            if (endTime.isBefore(startTime)) continue;

            // Clip auf Timeline
            LocalTime clippedStart = startTime.isBefore(dayStartTime) ? dayStartTime : startTime;
            LocalTime clippedEnd = endTime.isAfter(dayEndTime) ? dayEndTime : endTime;

            long minutesFromStart = Duration.between(dayStartTime, clippedStart).toMinutes();
            long durationMinutes = Duration.between(clippedStart, clippedEnd).toMinutes();
            if (durationMinutes <= 0) continue;

            double layoutY = minutesFromStart * minuteHeight;
            double blockHeight = durationMinutes * minuteHeight;

            VBox block = new VBox(2);
            block.setLayoutY(layoutY);
            block.setPadding(new Insets(6));
            block.setMinHeight(blockHeight);
            block.setPrefHeight(blockHeight);

            // neue Logik: benutze Kategorie-Farbe falls vorhanden, sonst Default
            String baseColor = "#3A6DFF"; // Standard-Farbe
            try {
                if (t.getKategorie() != null && t.getKategorie().getFarbe() != null && !t.getKategorie().getFarbe().isBlank()) {
                    baseColor = t.getKategorie().getFarbe().trim();
                }
            } catch (Throwable ignore) {}

            String darker = darkenHex(baseColor, 0.85); // leicht dunkler für Gradient
            block.setStyle(
                    "-fx-background-color: linear-gradient(" + baseColor + ", " + darker + "); " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-radius: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 8, 0.2, 0, 2);"
            );

            double horizontalPadding = 18;
            block.setTranslateX(horizontalPadding);
            block.prefWidthProperty().bind(eventLayer.widthProperty().subtract(horizontalPadding * 2));
            block.setMaxWidth(Double.MAX_VALUE);

            String timeRange = String.format("%02d:%02d - %02d:%02d",
                    clippedStart.getHour(), clippedStart.getMinute(),
                    clippedEnd.getHour(), clippedEnd.getMinute()
            );

            String displayTitle = t.getTitel() == null ? "" : t.getTitel();
            if (MainLogik.getZeigeAlleTermine()) {
                String owner = findOwnerNameForTermin(t);
                if (owner != null && !owner.isBlank()) {
                    displayTitle = owner + ": " + displayTitle;
                }
            }
            Label titleLabel = new Label(displayTitle);
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

            Label timeLabel = new Label(timeRange);
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 11px;");

            Label descLabel = null;
            if (t.getBeschreibung() != null && !t.getBeschreibung().isBlank()) {
                descLabel = new Label(t.getBeschreibung());
                descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 11px;");
                descLabel.setWrapText(true);
            }

            block.getChildren().addAll(titleLabel, timeLabel);
            if (descLabel != null) block.getChildren().add(descLabel);

            block.setOnMouseEntered(e -> {
                block.setScaleX(1.02);
                block.setScaleY(1.02);
                block.setCursor(Cursor.HAND);
            });
            block.setOnMouseExited(e -> {
                block.setScaleX(1.0);
                block.setScaleY(1.0);
                block.setCursor(Cursor.DEFAULT);
            });

            // --- ContextMenu mit "Löschen" (ohne Bestätigungsdialog, dezenter Hover, kein blaues Focus) ---
            ContextMenu cm = new ContextMenu();
            cm.setStyle(
                    "-fx-background-color: #2a2a2d; " +
                            "-fx-border-color: rgba(255,255,255,0.04); " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 6;"
            );

            Button menuBtn = new Button("Löschen");
            Label bin = new Label("\uD83D\uDDD1");
            bin.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 14px;");
            menuBtn.setGraphic(bin);

            menuBtn.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #E6E6E6; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 6 10 6 10; " +
                            "-fx-alignment: center-left; " +
                            "-fx-focus-color: transparent; " +
                            "-fx-faint-focus-color: transparent;"
            );
            menuBtn.setFocusTraversable(false);

            menuBtn.setOnMouseEntered(ev -> {
                menuBtn.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.03); " +
                                "-fx-text-fill: #E6E6E6; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 6 10 6 10; " +
                                "-fx-alignment: center-left; " +
                                "-fx-focus-color: transparent; " +
                                "-fx-faint-focus-color: transparent;"
                );
                menuBtn.setCursor(Cursor.HAND);
            });
            menuBtn.setOnMouseExited(ev -> {
                menuBtn.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #E6E6E6; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 6 10 6 10; " +
                                "-fx-alignment: center-left; " +
                                "-fx-focus-color: transparent; " +
                                "-fx-faint-focus-color: transparent;"
                );
                menuBtn.setCursor(Cursor.DEFAULT);
            });

            menuBtn.setOnAction(evt -> {
                boolean ok = MainLogik.terminLoeschen(t);
                if (ok) {
                    List<Termin> newList = MainLogik.getTermineFuerDatum(this.currentDate);
                    this.show(this.currentDate, newList);
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Termin konnte nicht gelöscht werden.");
                    err.setHeaderText(null);
                    err.initOwner(getScene() == null ? null : (Stage) getScene().getWindow());
                    err.showAndWait();
                }
            });

            CustomMenuItem cmi = new CustomMenuItem(menuBtn, false);
            cmi.setHideOnClick(true);
            cm.getItems().add(cmi);

            block.setOnContextMenuRequested(cmEvent -> {
                cm.show(block, cmEvent.getScreenX(), cmEvent.getScreenY());
                cmEvent.consume();
            });

            block.setOnMouseClicked(ev -> {
                if (ev.getButton() == MouseButton.SECONDARY) {
                    cm.show(block, ev.getScreenX(), ev.getScreenY());
                    ev.consume();
                    return;
                }
                if (getScene() == null || getScene().getWindow() == null) return;
                Stage owner = (Stage) getScene().getWindow();
                java.util.function.Consumer<Termin> onSaved = (Termin ignored) -> {
                    List<Termin> newList = MainLogik.getTermineFuerDatum(this.currentDate);
                    this.show(this.currentDate, newList);
                };
                TerminAdd.show(owner, this.currentDate, t, onSaved);
            });

            eventLayer.getChildren().add(block);
        }
    }

    public void setOnRequestBack(Runnable r) {
        this.onRequestBack = r;
    }

    private String findOwnerNameForTermin(Termin t) {
        try {
            var fam = Demos.getDemoFamilie();
            if (fam == null) return null;
            for (Benutzer b : fam.getMitglieder()) {
                if (b == null || b.getKalender() == null) continue;
                if (b.getKalender().getTermine().contains(t)) return b.getName();
            }
        } catch (Throwable ignore) {}
        return null;
    }

    // Helfer: dunkelt einen #RRGGBB-String ab; bei Fehlern gibt original zurück.
    private String darkenHex(String hex, double factor) {
        if (hex == null) return "#000000";
        String h = hex.trim();
        if (h.startsWith("#")) h = h.substring(1);
        if (h.length() != 6) return hex;
        try {
            int r = Integer.parseInt(h.substring(0,2), 16);
            int g = Integer.parseInt(h.substring(2,4), 16);
            int b = Integer.parseInt(h.substring(4,6), 16);
            r = (int) Math.max(0, Math.min(255, Math.round(r * factor)));
            g = (int) Math.max(0, Math.min(255, Math.round(g * factor)));
            b = (int) Math.max(0, Math.min(255, Math.round(b * factor)));
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception ex) {
            return hex;
        }
    }
}
