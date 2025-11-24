// java
package org.example.testmal;

import JavaLogik.Termin;
import JavaLogik.MainLogik;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * WICHTIG für AI-Refactoring (Copilot / JetBrains AI):
 * - Das UI-Layout und das Styling (Dark Mode, Grid, Hover-Effekte, Sidebar, Topbar) sollen erhalten bleiben.
 * - Bitte NUR die Java-Logik vereinfachen, ohne das visuelle Ergebnis zu verändern.
 * - Die Klasse darf ruhig "anfängerhaft" aussehen: einfache if/else, klare Felder, wenig Abstraktion.
 * - KEINE Layouts, Styles oder Nodes löschen, solange sie sichtbar fürs UI gebraucht werden.
 */
public class StandardAnsicht extends Application {

    // Zustände und Haupt-Container als normale Instanzfelder
    private boolean isDayView = false;
    private VBox monthViewHolder;   // enthält die Monatsansicht (Header + Grid)
    private DayView dayView;        // eine Tagesansicht
    private BorderPane root;        // Root-Layout

    // Referenz auf das Primary Stage (wird in start() gesetzt)
    private Stage primaryStageRef;

    // aktuelles in der Tagesansicht angezeigtes Datum
    private LocalDate currentDisplayedDate = LocalDate.now();

    // aktueller Benutzer (anzeigen)
    private Label currentUserLabel;

    // Label für aktuellen Monat (sichtbar in der Topbar, links vom Suchfeld)
    private Label monthLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // store primary stage reference
        this.primaryStageRef = primaryStage;

        // Root
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(#1b1b1e 0%, #262629 100%);");

        // DayView initialisieren (eine Instanz)
        dayView = new DayView();
        // Zeige initialen leeren Tag (heute)
        dayView.setOnRequestBack(this::showMonthView);
        dayView.show(LocalDate.now(), new ArrayList<>());
        this.currentDisplayedDate = LocalDate.now();

        // --- LEFT SIDEBAR ---
        VBox leftBar = new VBox(16);
        leftBar.setPadding(new Insets(18));
        leftBar.setPrefWidth(220);
        leftBar.setStyle("-fx-background-color: transparent; -fx-border-width:0 1 0 0; -fx-border-color: rgba(0,0,0,0.04);");

        Label dateLabel = new Label("Date/Time");
        dateLabel.setFont(Font.font(18));
        dateLabel.setStyle("-fx-text-fill: #E6E6E6;");

        HBox nav = new HBox(8);
        nav.setAlignment(Pos.CENTER_LEFT);
        Button prevBtn = new Button("\u25C0"); // left
        Button nextBtn = new Button("\u25B6"); // right
        Button addBtn = new Button("+");
        prevBtn.setPrefSize(36, 28);
        nextBtn.setPrefSize(36, 28);
        addBtn.setPrefSize(36, 28);
        nav.getChildren().addAll(prevBtn, nextBtn, addBtn);

        // Neuer: + Button öffnet TerminAdd und fügt Termin über MainLogik hinzu
        addBtn.setOnAction(e -> {
            // defaultDate: heute (kann angepasst werden)
            TerminAdd.show(primaryStageRef, LocalDate.now(), (Termin neu) -> {
                // versuche Termin in zentrale MainLogik zu speichern
                try {
                    MainLogik.addTermin(neu); // erwartet, dass MainLogik eine addTermin-Methode bietet
                } catch (Throwable ex) {
                    // Falls MainLogik.addTermin nicht vorhanden ist, nur Log-Ausgabe (Test-Fall)
                    System.out.println("Konnte Termin nicht an MainLogik übergeben: " + ex.getMessage());
                }

                // Wenn gerade Tagesansicht sichtbar ist, neu laden damit der Termin sofort erscheint
                if (isDayView) {
                    // showDayView lädt Termine erneut aus MainLogik
                    showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                }
            });
        });

        Button kategorienBtn = new Button("Kategorien \u25BE"); // dropdown arrow
        kategorienBtn.setPrefWidth(Double.MAX_VALUE);
        Button benutzerWechselnBtn = new Button("Benutzer wechseln \u21C5");
        benutzerWechselnBtn.setPrefWidth(Double.MAX_VALUE);
        Button heuteBtn = new Button("Heute \u2B06");
        heuteBtn.setPrefWidth(Double.MAX_VALUE);

        String sideBtnStyle = "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10 8 10; -fx-text-fill: #F0F0F0;";
        kategorienBtn.setStyle(sideBtnStyle);
        benutzerWechselnBtn.setStyle(sideBtnStyle);
        heuteBtn.setStyle(sideBtnStyle);

        leftBar.getChildren().addAll(dateLabel, nav, new Separator(), kategorienBtn, benutzerWechselnBtn, heuteBtn);

        // --- TOP BAR ---
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(12, 18, 12, 18));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setStyle("-fx-background-color: transparent; -fx-border-width:0 0 1 0; -fx-border-color: rgba(0,0,0,0.04);");

        Region topLeftSpacer = new Region();
        HBox.setHgrow(topLeftSpacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Suche...");
        searchField.setPrefWidth(360);
        Button searchBtn = new Button("\uD83D\uDD0D"); // 🔍
        Button profileBtn = new Button("\uD83D\uDC64"); // 👤
        profileBtn.setPrefSize(44, 36);
        Button mtBtn = new Button("M/T");
        mtBtn.setPrefSize(54, 36);
        mtBtn.setTooltip(new javafx.scene.control.Tooltip("Zwischen Monats- und Tagesansicht wechseln"));

        // Neuer: Label für aktuellen Benutzer (neben Profil-Icon)
        currentUserLabel = new Label("Gast");
        currentUserLabel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        currentUserLabel.setPadding(new Insets(0, 6, 0, 0));

        // Neuer: Monat-Label, links vom Suchfeld (Instanzfeld damit später aktualisierbar)
        monthLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthLabel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 16px;");

        searchField.setStyle(
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: #232324; " +
                        "-fx-border-color: rgba(255,255,255,0.07); -fx-padding: 8; -fx-text-fill: #F2F2F2;");
        searchBtn.setStyle(
                "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; -fx-text-fill: #F2F2F2;");
        mtBtn.setStyle(
                "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; -fx-text-fill: #F2F2F2;");
        profileBtn.setStyle(
                "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 8 6 8; -fx-text-fill: #F2F2F2;");

        // Topbar jetzt mit monthLabel vor dem searchField
        topBar.getChildren().addAll(topLeftSpacer, monthLabel, searchField, searchBtn, currentUserLabel, profileBtn, mtBtn);

        // --- USER PANEL (ausklappbar unter "Benutzer wechseln") ---
        VBox userPanel = new VBox(6);
        userPanel.setPadding(new Insets(8));
        userPanel.setStyle("-fx-background-color: #242428; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(255,255,255,0.03);");
        userPanel.setVisible(false);
        userPanel.setManaged(false);
        userPanel.setMaxWidth(Double.MAX_VALUE);

        // Befülle Panel mit Benutzern aus MainLogik (Familie.getBenutzerNamen())
        List<String> users = MainLogik.getBenutzerNamen();
        for (String u : users) {
            Button ub = new Button(u);
            ub.setPrefWidth(Double.MAX_VALUE);
            ub.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            // Hover
            ub.setOnMouseEntered(ev -> {
                ub.setCursor(Cursor.HAND);
                ub.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            ub.setOnMouseExited(ev -> {
                ub.setCursor(Cursor.DEFAULT);
                ub.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            ub.setOnAction(ev -> {
                currentUserLabel.setText(u);      // setze aktuellen Benutzer
                userPanel.setVisible(false);     // Panel zuklappen
                userPanel.setManaged(false);
            });
            userPanel.getChildren().add(ub);
        }

        // Füge userPanel direkt unter dem benutzerWechselnBtn in der leftBar ein
        int insertIndex = leftBar.getChildren().indexOf(benutzerWechselnBtn);
        if (insertIndex >= 0) {
            leftBar.getChildren().add(insertIndex + 1, userPanel);
        } else {
            leftBar.getChildren().add(userPanel);
        }

        // Toggle-Logik für den Button
        benutzerWechselnBtn.setOnAction(e -> {
            boolean showing = userPanel.isVisible();
            userPanel.setVisible(!showing);
            userPanel.setManaged(!showing);
        });

        // --- CATEGORY PANEL (ausklappbar unter "Kategorien") ---
        VBox categoryPanel = new VBox(6);
        categoryPanel.setPadding(new Insets(8));
        categoryPanel.setStyle("-fx-background-color: #242428; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(255,255,255,0.03);");
        categoryPanel.setVisible(false);
        categoryPanel.setManaged(false);
        categoryPanel.setMaxWidth(Double.MAX_VALUE);

        // Befülle Panel mit Kategorienamen aus MainLogik (Wrapper)
        List<String> kategorien = MainLogik.getKategorienNamen();
        for (String k : kategorien) {
            Button kb = new Button(k);
            kb.setPrefWidth(Double.MAX_VALUE);
            kb.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            // Hover
            kb.setOnMouseEntered(ev -> {
                kb.setCursor(Cursor.HAND);
                kb.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            kb.setOnMouseExited(ev -> {
                kb.setCursor(Cursor.DEFAULT);
                kb.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            kb.setOnAction(ev -> {
                // Setze Button-Text auf die gewählte Kategorie und klappe zu
                kategorienBtn.setText(k + " \u25BE");
                categoryPanel.setVisible(false);
                categoryPanel.setManaged(false);
                // optional: hier Filter-Logik einbauen (z.B. Anzeige von Terminen nach Kategorie)
            });
            categoryPanel.getChildren().add(kb);
        }

        // Füge categoryPanel direkt unter dem kategorienBtn in der leftBar ein
        int catInsertIndex = leftBar.getChildren().indexOf(kategorienBtn);
        if (catInsertIndex >= 0) {
            leftBar.getChildren().add(catInsertIndex + 1, categoryPanel);
        } else {
            leftBar.getChildren().add(categoryPanel);
        }

        // Toggle-Logik für den Kategorien-Button
        kategorienBtn.setOnAction(e -> {
            boolean showing = categoryPanel.isVisible();
            categoryPanel.setVisible(!showing);
            categoryPanel.setManaged(!showing);
        });

        // --- CENTER: CALENDAR GRID (Monatsansicht) ---
        GridPane calendarGrid = new GridPane();
        calendarGrid.setPadding(new Insets(8, 18, 8, 18));
        calendarGrid.setHgap(6);
        calendarGrid.setVgap(8);
        calendarGrid.setAlignment(Pos.TOP_CENTER);

        for (int c = 0; c < 7; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7.0);
            cc.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 5; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 5.0);
            rc.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rc);
        }

        String[] days = {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
        GridPane headerGrid = new GridPane();
        headerGrid.setPadding(new Insets(8, 18, 2, 18));
        headerGrid.setHgap(6);
        headerGrid.setVgap(0);
        headerGrid.setAlignment(Pos.CENTER);

        for (ColumnConstraints cc : calendarGrid.getColumnConstraints()) {
            ColumnConstraints hcc = new ColumnConstraints();
            hcc.setPercentWidth(cc.getPercentWidth());
            hcc.setHgrow(Priority.ALWAYS);
            headerGrid.getColumnConstraints().add(hcc);
        }
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setPrefHeight(28);
            d.setMaxWidth(Double.MAX_VALUE);
            d.setAlignment(Pos.CENTER);
            d.setStyle("-fx-font-weight: normal; -fx-font-size: 15px; -fx-text-fill: #F0F0F0;");
            GridPane.setHgrow(d, Priority.ALWAYS);
            GridPane.setHalignment(d, javafx.geometry.HPos.CENTER);
            headerGrid.add(d, i, 0);
        }

        final String cellBaseStyle =
                "-fx-background-color: #222225; -fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1;";

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                StackPane cell = new StackPane();
                cell.setMinHeight(100);
                cell.setStyle(cellBaseStyle);
                cell.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.10),
                        6, 0.15, 0, 2));
                cell.setOnMouseEntered(e -> {
                    cell.setCursor(Cursor.HAND);
                    cell.setTranslateY(-4);
                    cell.setStyle(cellBaseStyle + "-fx-border-color: rgba(255,255,255,0.10);");
                });
                cell.setOnMouseExited(e -> {
                    cell.setCursor(Cursor.DEFAULT);
                    cell.setTranslateY(0);
                    cell.setStyle(cellBaseStyle);
                });

                Label dayNumber = new Label("");
                int num = row * 7 + col + 1; // Nummerierung 1..35
                if (num <= 35) {
                    dayNumber.setText(String.valueOf(num));
                }
                StackPane.setAlignment(dayNumber, Pos.TOP_RIGHT);
                dayNumber.setPadding(new Insets(6, 10, 0, 0));
                dayNumber.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 14px; -fx-font-weight: 600;");
                cell.getChildren().add(dayNumber);

                // Neuer Klick-Handler: öffnet die Tagesansicht für die entsprechende Zahl
                final int dayNum = num; // benötigt für Lambda
                cell.setOnMouseClicked(ev -> {
                    // Erstelle ein Datum: erster Tag des aktuellen Monats + (dayNum - 1)
                    // (Bei dayNum > Anzahl der Tage im Monat springt es in den nächsten Monat — später anpassbar)
                    LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
                    LocalDate date = firstOfMonth.plusDays(dayNum - 1);
                    showDayView(date);
                });

                calendarGrid.add(cell, col, row);
            }
        }

        VBox centerBox = new VBox(2);
        centerBox.getChildren().addAll(headerGrid, calendarGrid);
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        monthViewHolder = centerBox;

        root.setLeft(leftBar);
        root.setTop(topBar);
        root.setCenter(monthViewHolder); // Start: Monatsansicht

        Scene scene = new Scene(root, 1100, 720, Color.web("#19191A"));
        primaryStage.setTitle("Kalender");
        primaryStage.setScene(scene);
        primaryStage.show();

        applyHover(prevBtn);
        applyHover(nextBtn);
        applyHover(addBtn);
        applyHover(kategorienBtn);
        applyHover(benutzerWechselnBtn);
        applyHover(heuteBtn);
        applyHover(searchBtn);
        applyHover(profileBtn);
        applyHover(mtBtn);

        // Toggle Monats- / Tagesansicht
        mtBtn.setOnAction(e -> {
            if (!isDayView) {
                showDayView(LocalDate.now());
                mtBtn.setText("T/M");
            } else {
                showMonthView();
                mtBtn.setText("M/T");
            }
        });
    }

    private void showMonthView() {
        root.setCenter(monthViewHolder);
        isDayView = false;
    }

    /**
     * Zeigt die Tagesansicht für ein bestimmtes Datum.
     * Termine werden nicht mehr hier erzeugt, sondern aus MainLogik geholt.
     */
    private void showDayView(LocalDate date) {
        // merke aktuell angezeigtes Datum
        this.currentDisplayedDate = date;

        // Termine vom zentralen MainLogik/Kalender holen
        List<Termin> termine = MainLogik.getTermineForDate(date);

        // Neue API: direkt anzeigen
        dayView.show(date, termine);

        root.setCenter(dayView);
        isDayView = true;
    }

    private void applyHover(Button b) {
        b.setOnMouseEntered(e -> {
            b.setScaleX(1.04);
            b.setScaleY(1.04);
            b.setCursor(Cursor.HAND);
            b.setTranslateY(-2);
        });
        b.setOnMouseExited(e -> {
            b.setScaleX(1.0);
            b.setScaleY(1.0);
            b.setCursor(Cursor.DEFAULT);
            b.setTranslateY(0);
        });
    }
}
