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

// zusätzliche Importe für die Uhr
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.geometry.Side;

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

    // Neues: Label für aktuelles Datum+Uhrzeit und Timeline für die Aktualisierung
    private Label dateTimeLabel;
    private Timeline clockTimeline;

    // Aktuell in der Monatsansicht dargestellter Monat (erster Tag des Monats)
    private LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

    // Grid als Instanzfeld damit es neu gerendert werden kann
    private GridPane calendarGrid;

    // Neues: maximale Anzahl an kleinen Indikator-Punkten pro Tages-Kachel
    private final int MAX_BADGES_ON_TILE = 3;

    // Neues: mtBtn als Instanzfeld, damit andere Methoden die Beschriftung setzen können
    private Button mtBtn;

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

        // dynamisches Datum + Uhrzeit (wird jede Sekunde aktualisiert)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        dateTimeLabel = new Label(LocalDateTime.now().format(dtf));
        dateTimeLabel.setFont(Font.font(18));
        dateTimeLabel.setStyle("-fx-text-fill: #E6E6E6;");

        // Timeline aktualisiert die Anzeige jede Sekunde
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            dateTimeLabel.setText(LocalDateTime.now().format(dtf));
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

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
            ContextMenu addMenu = new ContextMenu();
            MenuItem miTermin = new MenuItem("Termin hinzufügen");
            MenuItem miBenutzer = new MenuItem("Benutzer hinzufügen");

            miTermin.setOnAction(ae -> {
                TerminAdd.show(primaryStageRef, LocalDate.now(), (Termin neu) -> {
                    try {
                        MainLogik.addTermin(neu);
                    } catch (Throwable ex) {
                        System.out.println("Konnte Termin nicht an MainLogik übergeben: " + ex.getMessage());
                    }
                    if (isDayView) {
                        showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                    } else {
                        renderCalendar(); // neu rendern falls Monatsansicht sichtbar
                    }
                });
            });

            // vorerst keine Aktion – Platzhalter für späteren Benutzer-Workflow
            miBenutzer.setOnAction(ae -> {
                // TODO: Benutzer hinzufügen (noch nicht implementiert)
            });

            addMenu.getItems().addAll(miTermin, miBenutzer);
            // Menu direkt am Button unten anzeigen
            addMenu.show(addBtn, Side.BOTTOM, 0, 6);
        });

        Button kategorienBtn = new Button("Kategorien \u25BE"); // dropdown arrow
        kategorienBtn.setPrefWidth(Double.MAX_VALUE);
        Button benutzerWechselnBtn = new Button("Benutzer wechseln \u21C5");
        benutzerWechselnBtn.setPrefWidth(Double.MAX_VALUE);
        Button heuteBtn = new Button("Aktueller Monat \u2B06");
        heuteBtn.setPrefWidth(Double.MAX_VALUE);

        String sideBtnStyle = "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10 8 10; -fx-text-fill: #F0F0F0;";
        kategorienBtn.setStyle(sideBtnStyle);
        benutzerWechselnBtn.setStyle(sideBtnStyle);
        heuteBtn.setStyle(sideBtnStyle);

        leftBar.getChildren().addAll(dateTimeLabel, nav, new Separator(), kategorienBtn, benutzerWechselnBtn, heuteBtn);

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

        // Erstelle mtBtn als Instanz-Button (Text initial "Heute")
        this.mtBtn = new Button("Heute");
        mtBtn.setPrefSize(54, 36);
        mtBtn.setTooltip(new javafx.scene.control.Tooltip("Zwischen Monats- und Tagesansicht wechseln"));

        // Neuer: Label für aktuellen Benutzer (neben Profil-Icon)
        currentUserLabel = new Label(MainLogik.getCurrentUserName());
        currentUserLabel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        currentUserLabel.setPadding(new Insets(0, 6, 0, 0));

        // Neuer: Monat-Label, links vom Suchfeld (Instanzfeld damit später aktualisierbar)
        monthLabel = new Label(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
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

        List<String> users = MainLogik.getBenutzerNamen();
        for (String u : users) {
            Button ub = new Button(u);
            ub.setPrefWidth(Double.MAX_VALUE);
            ub.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            ub.setOnMouseEntered(ev -> {
                ub.setCursor(Cursor.HAND);
                ub.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            ub.setOnMouseExited(ev -> {
                ub.setCursor(Cursor.DEFAULT);
                ub.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            ub.setOnAction(ev -> {
                // setze aktuellen Benutzer in MainLogik
                MainLogik.setCurrentUserName(u);
                // UI-Update
                currentUserLabel.setText(u);
                userPanel.setVisible(false);
                userPanel.setManaged(false);
                // neu rendern: Tagesansicht zeigt nun nur Termine des neuen Benutzers,
                // Monatsansicht bleibt, ggf. mit anderen Terminen (falls dort dargestellt)
                if (isDayView) {
                    showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                } else {
                    renderCalendar();
                }
            });
            userPanel.getChildren().add(ub);
        }

        int insertIndex = leftBar.getChildren().indexOf(benutzerWechselnBtn);
        if (insertIndex >= 0) {
            leftBar.getChildren().add(insertIndex + 1, userPanel);
        } else {
            leftBar.getChildren().add(userPanel);
        }

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

        List<String> kategorien = MainLogik.getKategorienNamen();
        for (String k : kategorien) {
            Button kb = new Button(k);
            kb.setPrefWidth(Double.MAX_VALUE);
            kb.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            kb.setOnMouseEntered(ev -> {
                kb.setCursor(Cursor.HAND);
                kb.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            kb.setOnMouseExited(ev -> {
                kb.setCursor(Cursor.DEFAULT);
                kb.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            });
            kb.setOnAction(ev -> {
                kategorienBtn.setText(k + " \u25BE");
                categoryPanel.setVisible(false);
                categoryPanel.setManaged(false);
            });
            categoryPanel.getChildren().add(kb);
        }

        int catInsertIndex = leftBar.getChildren().indexOf(kategorienBtn);
        if (catInsertIndex >= 0) {
            leftBar.getChildren().add(catInsertIndex + 1, categoryPanel);
        } else {
            leftBar.getChildren().add(categoryPanel);
        }

        kategorienBtn.setOnAction(e -> {
            boolean showing = categoryPanel.isVisible();
            categoryPanel.setVisible(!showing);
            categoryPanel.setManaged(!showing);
        });

        // --- CENTER: CALENDAR GRID (Monatsansicht) ---
        calendarGrid = new GridPane();
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

        renderCalendar(); // initial render

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

        // Timeline beim Schließen stoppen, damit kein Hintergrund-Thread weiterläuft
        primaryStage.setOnCloseRequest(ev -> {
            if (clockTimeline != null) clockTimeline.stop();
        });

        applyHover(prevBtn);
        applyHover(nextBtn);
        applyHover(addBtn);
        applyHover(kategorienBtn);
        applyHover(benutzerWechselnBtn);
        applyHover(heuteBtn);
        applyHover(searchBtn);
        applyHover(profileBtn);
        applyHover(mtBtn);

        // Prev / Next Buttons: Monat in Monatsansicht, Tag in Tagesansicht
        prevBtn.setOnAction(e -> {
            if (isDayView) {
                showDayView(currentDisplayedDate.minusDays(1));
            } else {
                currentMonth = currentMonth.minusMonths(1);
                renderCalendar();
            }
        });
        nextBtn.setOnAction(e -> {
            if (isDayView) {
                showDayView(currentDisplayedDate.plusDays(1));
            } else {
                currentMonth = currentMonth.plusMonths(1);
                renderCalendar();
            }
        });

        // Aktueller-Monat-Button: springt immer in die Monatsansicht des aktuellen Monats
        heuteBtn.setOnAction(e -> {
            currentMonth = LocalDate.now().withDayOfMonth(1);
            showMonthView();
        });

        // mtBtn-Action: Wenn gerade die Tagesansicht DES HEUTIGEN DATUMS angezeigt wird -> zurück zur Monatsansicht.
        // Ansonsten immer zur Tagesansicht von heute springen.
        mtBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            if (isDayView && currentDisplayedDate != null && currentDisplayedDate.equals(today)) {
                // Wir sind bereits in der Tagesansicht von heute -> zurück zur Monatsansicht
                showMonthView();
            } else {
                // In jedem anderen Fall -> zeige die Tagesansicht für HEUTE
                showDayView(today);
            }
        });
    }

    // Rendert die Monatsansicht basierend auf currentMonth
    private void renderCalendar() {
        calendarGrid.getChildren().clear();

        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate firstOfMonth = currentMonth;
        int firstColumn = firstOfMonth.getDayOfWeek().getValue() - 1; // Monday=0 .. Sunday=6

        final String cellBaseStyle =
                "-fx-background-color: #222225; -fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1;";

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                int cellIndex = row * 7 + col;
                int dayNum = cellIndex - firstColumn + 1;

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
                if (dayNum >= 1 && dayNum <= daysInMonth) {
                    dayNumber.setText(String.valueOf(dayNum));
                }
                StackPane.setAlignment(dayNumber, Pos.TOP_RIGHT);
                dayNumber.setPadding(new Insets(6, 10, 0, 0));
                dayNumber.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 14px; -fx-font-weight: 600;");
                cell.getChildren().add(dayNumber);

                // ----- NEU: kleine Balken mit Titel, wenn an diesem Tag Termine vorhanden sind -----
                if (dayNum >= 1 && dayNum <= daysInMonth) {
                    LocalDate thisDate = currentMonth.withDayOfMonth(dayNum);
                    // Hole Termine für den aktuell angemeldeten Benutzer an diesem Datum
                    List<Termin> termine = MainLogik.getTermineForDate(thisDate);

                    if (termine != null && !termine.isEmpty()) {
                        VBox barsContainer = new VBox(6);
                        // Platz unten und seitlich freihalten
                        StackPane.setAlignment(barsContainer, Pos.BOTTOM_CENTER);
                        // oben mehr Abstand, damit die Balken nicht in die Tageszahl ragen
                        StackPane.setMargin(barsContainer, new Insets(32, 6, 8, 6)); // erhöhten Abstand oben
                        // Damit die Bars die Breite der Kachel nutzen
                        barsContainer.maxWidthProperty().bind(cell.widthProperty().subtract(12));

                        int count = Math.min(termine.size(), MAX_BADGES_ON_TILE);
                        for (int i = 0; i < count; i++) {
                            Termin t = termine.get(i);
                            HBox bar = new HBox();
                            bar.setPrefHeight(16);
                            bar.setMinHeight(16);
                            bar.setMaxHeight(16);
                            bar.maxWidthProperty().bind(cell.widthProperty().subtract(12));
                            bar.setAlignment(Pos.CENTER_LEFT);

                            String color = "#4A90E2"; // Default-Farbe
                            try {
                                if (t.getKategorie() != null && t.getKategorie().getFarbe() != null && !t.getKategorie().getFarbe().isBlank()) {
                                    color = t.getKategorie().getFarbe();
                                }
                            } catch (Throwable ex) {
                                // falls Termin keine Kategorie hat / Methode nicht verfügbar → Fallback-Farbe nutzen
                            }

                            bar.setStyle(
                                "-fx-background-color: " + color + ";" +
                                "-fx-background-radius: 6;" +
                                "-fx-border-radius: 6;" +
                                "-fx-border-color: rgba(0,0,0,0.12);"
                            );

                            Label tlabel = new Label(t.getTitel());
                            tlabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
                            tlabel.setPadding(new Insets(0, 8, 0, 8));
                            bar.getChildren().add(tlabel);

                            barsContainer.getChildren().add(bar);
                        }

                        // Wenn es mehr Termine gibt als angezeigt werden, ein kleines "…" unten links
                        if (termine.size() > MAX_BADGES_ON_TILE) {
                            Label more = new Label("…");
                            more.setStyle("-fx-text-fill: #D0D0D0; -fx-font-size: 12px;");
                            StackPane.setAlignment(more, Pos.BOTTOM_LEFT);
                            StackPane.setMargin(more, new Insets(0, 0, 6, 8));
                            cell.getChildren().add(more);
                        }

                        cell.getChildren().add(barsContainer);
                    }
                }

                // Klick nur wenn gültiger Tag
                final int dni = dayNum;
                if (dayNum >= 1 && dayNum <= daysInMonth) {
                    cell.setOnMouseClicked(ev -> {
                        LocalDate date = currentMonth.withDayOfMonth(dni);
                        showDayView(date);
                    });
                } else {
                    // kein klick
                    cell.setOnMouseClicked(null);
                }

                calendarGrid.add(cell, col, row);
            }
        }

        // Update month label
        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        // In Monatsansicht: mtBtn immer "Heute"
        if (mtBtn != null) mtBtn.setText("Heute");
    }

    private void showMonthView() {
        root.setCenter(monthViewHolder);
        isDayView = false;
        // ensure month label shows the month being displayed
        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        renderCalendar();
        // In Monatsansicht: button soll "Heute" anzeigen (springt zur Tagesansicht von heute)
        if (mtBtn != null) mtBtn.setText("Heute");
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

        // setze monthLabel auf den Monat des aktuell angezeigten Tages
        monthLabel.setText(date.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        // Wenn wir die Tagesansicht für HEUTE zeigen -> Button "Monat" (geht zur Monatsansicht).
        // Ansonsten (Tagesansicht eines anderen Datums) -> Button "Heute" (springt zur Tagesansicht HEUTE).
        if (mtBtn != null) {
            if (date.equals(LocalDate.now())) {
                mtBtn.setText("Monat");
            } else {
                mtBtn.setText("Heute");
            }
        }
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
