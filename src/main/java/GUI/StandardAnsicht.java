package GUI;

import JavaLogik.Termin;
import JavaLogik.MainLogik;
import JavaLogik.Demos;
import JavaLogik.Benutzer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

public class StandardAnsicht extends Application {

    private boolean isDayView = false;                 // true = Tagesansicht, false = Monatsansicht
    private VBox monthViewHolder;                      // für Monatsansicht
    private DayView dayView;                           // Tagesansicht-UI
    private BorderPane root;
    private Stage primaereStageReferenz;
    private LocalDate currentDisplayedDate = LocalDate.now(); // Datum, das aktuell im DayView angezeigt wird
    private LocalDate currentMonth = LocalDate.now().withDayOfMonth(1); // aktuell angezeigter Monat
    private Label currentUserLabel;                    // aktueller Benutzer
    private Label monthLabel;                           // Monat/Jahr
    private Label dateTimeLabel;
    private Timeline clockTimeline;
    private GridPane kalenderGrid;
    private final int MAX_BADGES_ON_TILE = 3;          // maximale Termine pro Tag in der Monatsansicht
    private Button mtBtn;                              // Button zum Wechsel Monat/Tag
    private VBox benutzerPanel;                             // die Panels links
    private VBox kategoriePanel;
    private String selectedCategoryName = null;        // Filter: null = alle Kategorien
    private Timeline benachrichtigungsZeit;         // Benachrichtigungs-Check
    private final Set<String> shownNotifications = new HashSet<>(); // damit Benachrichtigungen nicht doppelt kommen

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        this.primaereStageReferenz = primaryStage;

        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(#1b1b1e 0%, #262629 100%);");

        dayView = new DayView();
        dayView.setOnRequestBack(this::showMonthView);           // Zurück-Callback
        dayView.show(LocalDate.now(), new ArrayList<>());
        this.currentDisplayedDate = LocalDate.now();

        VBox leftBar = new VBox(16);   // linke Seitenleiste
        leftBar.setPadding(new Insets(18));
        leftBar.setPrefWidth(220);
        leftBar.setStyle("-fx-background-color: transparent; -fx-border-width:0 1 0 0; -fx-border-color: rgba(0,0,0,0.04);");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");  // Uhrzeit
        dateTimeLabel = new Label(LocalDateTime.now().format(dtf));
        dateTimeLabel.setFont(Font.font(18));
        dateTimeLabel.setStyle("-fx-text-fill: #E6E6E6;");

        clockTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), ev -> {    // Updater für Uhrzeit
            dateTimeLabel.setText(LocalDateTime.now().format(dtf));
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        HBox nav = new HBox(8);
        nav.setAlignment(Pos.CENTER_LEFT);
        Button prevBtn = new Button("\u25C0");    // Pfeile
        Button nextBtn = new Button("\u25B6");
        Button addBtn = new Button("+");
        prevBtn.setPrefSize(36, 28);
        nextBtn.setPrefSize(36, 28);
        addBtn.setPrefSize(36, 28);
        nav.getChildren().addAll(prevBtn, nextBtn, addBtn);

        // UserPanel
        this.benutzerPanel = new VBox(6);
        this.benutzerPanel.setPadding(new Insets(8));
        this.benutzerPanel.setStyle("-fx-background-color: #242428; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(255,255,255,0.03);");
        this.benutzerPanel.setVisible(false);
        this.benutzerPanel.setManaged(false);
        this.benutzerPanel.setMaxWidth(Double.MAX_VALUE);
        refreshUserPanel();

        // Termin hinzufügen / Benutzer hinzufügen -- vllt noch schöner machen
        addBtn.setOnAction(e -> {
            ContextMenu addMenu = new ContextMenu();
            MenuItem miTermin = new MenuItem("Termin hinzufügen");
            MenuItem miBenutzer = new MenuItem("Benutzer hinzufügen");

            // Termin hinzufügen
            miTermin.setOnAction(ae -> {
                TerminAdd.show(primaereStageReferenz, LocalDate.now(),
                        (Termin neu) -> {
                            try {
                                boolean conflict = MainLogik.hatKonflikt(neu);
                                if (conflict) {
                                    Alert confirm = new Alert(
                                            Alert.AlertType.CONFIRMATION,
                                            "Der Termin scheint sich mit bestehenden Terminen zu überschneiden. Trotzdem erstellen?",
                                            ButtonType.YES, ButtonType.NO
                                    );
                                    confirm.setHeaderText(null);
                                    confirm.initOwner(primaereStageReferenz);
                                    Optional<ButtonType> resp = confirm.showAndWait();
                                    if (!resp.isPresent() || resp.get() != ButtonType.YES) return;
                                }
                                MainLogik.terminHinzufuegen(neu);

                            } catch (Throwable ex) {
                                System.out.println("Konnte Termin nicht an MainLogik übergeben: " + ex.getMessage());
                                Alert err = new Alert(Alert.AlertType.ERROR, "Termin konnte nicht erstellt werden.");
                                err.initOwner(primaereStageReferenz);
                                err.showAndWait();
                                return;
                            }

                            // UI refreshen
                            if (isDayView) showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                            else renderCalendar();
                        },
                        () -> javafx.application.Platform.runLater(this::refreshCategoryPanel) // Kategorien neu laden
                );
            });

            // Benutzer hinzufügen
            miBenutzer.setOnAction(ae -> {
                java.util.function.Consumer<JavaLogik.Benutzer> onSaved = (JavaLogik.Benutzer neu) -> {
                    try {
                        MainLogik.setCurrentUserName(neu.getName());
                        currentUserLabel.setText(neu.getName());
                        refreshUserPanel();

                        if (isDayView) showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                        else renderCalendar();

                    } catch (Throwable ex) {
                        System.err.println("Fehler beim Verarbeiten des neuen Benutzers: " + ex.getMessage());
                        Alert a = new Alert(Alert.AlertType.ERROR, "Benutzer erstellt, UI konnte nicht aktualisiert werden.");
                        a.initOwner(primaereStageReferenz);
                        a.showAndWait();
                    }
                };
                BenutzerAdd.show(primaereStageReferenz, onSaved);
            });
            addMenu.getItems().addAll(miTermin, miBenutzer);
            addMenu.show(addBtn, Side.BOTTOM, 0, 6);
        });

        // Buttons links
        Button kategorienBtn = new Button("Kategorien \u25BE");
        Button benutzerWechselnBtn = new Button("Benutzer wechseln \u21C5");
        Button heuteBtn = new Button("Aktueller Monat \u2B06");

        kategorienBtn.setPrefWidth(Double.MAX_VALUE);
        benutzerWechselnBtn.setPrefWidth(Double.MAX_VALUE);
        heuteBtn.setPrefWidth(Double.MAX_VALUE);

        final String sideBtnStyle =
                "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10 8 10; -fx-text-fill: #F0F0F0;";
        kategorienBtn.setStyle(sideBtnStyle);
        benutzerWechselnBtn.setStyle(sideBtnStyle);
        heuteBtn.setStyle(sideBtnStyle);

        leftBar.getChildren().addAll(dateTimeLabel, nav, new Separator(), kategorienBtn, benutzerWechselnBtn, heuteBtn);

        // Bar oben
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(12, 18, 12, 18));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setStyle("-fx-background-color: transparent; -fx-border-width:0 0 1 0; -fx-border-color: rgba(0,0,0,0.04);");

        Region topLeftSpacer = new Region();                // schiebt alles nach rechts
        HBox.setHgrow(topLeftSpacer, Priority.ALWAYS);

        // Suche -- noch ohne Funktion -> nicht mehr machen
        TextField searchField = new TextField();
        searchField.setPromptText("Suche...");
        searchField.setPrefWidth(360);
        Button searchBtn = new Button("\uD83D\uDD0D");

        // Ich / Alle Ansicht
        Button profilBtn = new Button();
        profilBtn.setPrefHeight(36);
        profilBtn.setPrefWidth(110);

        // Monat / Tag Ansicht
        this.mtBtn = new Button("Heute");
        mtBtn.setPrefSize(54, 36);
        mtBtn.setTooltip(new Tooltip("Zwischen Monats- und Tagesansicht wechseln"));

        // aktueller Benutzer
        currentUserLabel = new Label(MainLogik.getCurrentUserName());
        currentUserLabel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 13px;");
        currentUserLabel.setPadding(new Insets(0, 6, 0, 0));

        // Monatslabel
        monthLabel = new Label(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthLabel.setStyle("-fx-text-fill: #E6E6E6; -fx-font-size: 16px;");

        final String topBoxStyle =
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: #232324; " +
                        "-fx-border-color: rgba(255,255,255,0.07); -fx-padding: 8; -fx-text-fill: #F2F2F2;";
        final String topBtnStyle =
                "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; -fx-text-fill: #F2F2F2;";
        searchField.setStyle(topBoxStyle);
        searchBtn.setStyle(topBtnStyle);
        mtBtn.setStyle(topBtnStyle);

        refreshProfileBtn(profilBtn);

        profilBtn.setOnAction(e -> {
            boolean newState = !MainLogik.getZeigeAlleTermine();
            MainLogik.setAlleTermineAnzeigen(newState);
            refreshProfileBtn(profilBtn);

            try {
                if (isDayView) showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                else renderCalendar();
            } catch (Throwable ex) {
                System.err.println("Fehler beim Aktualisieren der Ansicht: " + ex.getMessage());
            }
        });

        topBar.getChildren().addAll(topLeftSpacer, monthLabel, searchField, searchBtn, currentUserLabel, profilBtn, mtBtn);

        int insertIndex = leftBar.getChildren().indexOf(benutzerWechselnBtn);
        if (insertIndex >= 0) leftBar.getChildren().add(insertIndex + 1, this.benutzerPanel);
        else leftBar.getChildren().add(this.benutzerPanel);

        benutzerWechselnBtn.setOnAction(e -> {
            boolean showing = this.benutzerPanel.isVisible();
            this.benutzerPanel.setVisible(!showing);
            this.benutzerPanel.setManaged(!showing);
        });

        // Kategorie-Panel
        this.kategoriePanel = new VBox(6);
        this.kategoriePanel.setPadding(new Insets(8));
        this.kategoriePanel.setStyle("-fx-background-color: #242428; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(255,255,255,0.03);");
        this.kategoriePanel.setVisible(false);
        this.kategoriePanel.setManaged(false);
        this.kategoriePanel.setMaxWidth(Double.MAX_VALUE);
        refreshCategoryPanel();

        int catInsertIndex = leftBar.getChildren().indexOf(kategorienBtn);
        if (catInsertIndex >= 0) leftBar.getChildren().add(catInsertIndex + 1, this.kategoriePanel);
        else leftBar.getChildren().add(this.kategoriePanel);

        kategorienBtn.setOnAction(e -> {
            boolean showing = this.kategoriePanel.isVisible();
            this.kategoriePanel.setVisible(!showing);
            this.kategoriePanel.setManaged(!showing);
        });

        // Monatsraster
        kalenderGrid = new GridPane();
        kalenderGrid.setPadding(new Insets(8, 18, 8, 18));
        kalenderGrid.setHgap(6);
        kalenderGrid.setVgap(8);
        kalenderGrid.setAlignment(Pos.TOP_CENTER);

        // 7 Spalten
        for (int c = 0; c < 7; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7.0);
            cc.setHgrow(Priority.ALWAYS);
            kalenderGrid.getColumnConstraints().add(cc);
        }

        // 5 Reihen
        for (int r = 0; r < 5; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 5.0);
            rc.setVgrow(Priority.ALWAYS);
            kalenderGrid.getRowConstraints().add(rc);
        }

        // Wochentag über dem Raster
        String[] days = {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
        GridPane headerGrid = new GridPane();
        headerGrid.setPadding(new Insets(8, 18, 2, 18));
        headerGrid.setHgap(6);
        headerGrid.setVgap(0);
        headerGrid.setAlignment(Pos.CENTER);

        for (ColumnConstraints cc : kalenderGrid.getColumnConstraints()) {
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
        renderCalendar(); // Kalender zeigen

        VBox centerBox = new VBox(2);
        centerBox.getChildren().addAll(headerGrid, kalenderGrid);
        VBox.setVgrow(kalenderGrid, Priority.ALWAYS);

        monthViewHolder = centerBox;

        root.setLeft(leftBar);
        root.setTop(topBar);
        root.setCenter(monthViewHolder);

        // Fenster anzeigen
        Scene scene = new Scene(root, 1100, 720, Color.web("#19191A"));
        primaryStage.setTitle("Kalender");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(ev -> {
            if (clockTimeline != null) clockTimeline.stop();
            if (benachrichtigungsZeit != null) benachrichtigungsZeit.stop();
        });

        // Benachrichtigungs-Check alle 30 Sekunden
        benachrichtigungsZeit = new Timeline(new KeyFrame(javafx.util.Duration.seconds(30), ev -> {
            try {
                checkNotifications();
            } catch (Throwable ex) {
                System.err.println("Fehler bei Benachrichtigungen: " + ex.getMessage());
            }
        }));
        benachrichtigungsZeit.setCycleCount(Timeline.INDEFINITE);
        benachrichtigungsZeit.play();

        // Hover wie immer
        applyHover(prevBtn);
        applyHover(nextBtn);
        applyHover(addBtn);
        applyHover(kategorienBtn);
        applyHover(benutzerWechselnBtn);
        applyHover(heuteBtn);
        applyHover(searchBtn);
        applyHover(profilBtn);
        applyHover(mtBtn);

        // Navigation
        prevBtn.setOnAction(e -> {
            if (isDayView) showDayView(currentDisplayedDate.minusDays(1));
            else {
                currentMonth = currentMonth.minusMonths(1);
                renderCalendar();
            }
        });

        nextBtn.setOnAction(e -> {
            if (isDayView) showDayView(currentDisplayedDate.plusDays(1));
            else {
                currentMonth = currentMonth.plusMonths(1);
                renderCalendar();
            }
        });

        // Aktueller Monat
        heuteBtn.setOnAction(e -> {
            currentMonth = LocalDate.now().withDayOfMonth(1);
            showMonthView();
        });

        // Monat/Tag
        mtBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            if (isDayView && currentDisplayedDate != null && currentDisplayedDate.equals(today)) showMonthView();
            else showDayView(today);
        });
    }

    // Monatsraster neu (mit Terminen)
    private void renderCalendar() {
        kalenderGrid.getChildren().clear();

        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate firstOfMonth = currentMonth;
        int firstColumn = firstOfMonth.getDayOfWeek().getValue() - 1; // Mo=0

        final String cellBaseStyle =
                "-fx-background-color: #222225; -fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1;";

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {

                int cellIndex = row * 7 + col;
                int dayNum = cellIndex - firstColumn + 1;

                // Tageskachel
                StackPane cell = new StackPane();
                cell.setMinHeight(100);
                cell.setStyle(cellBaseStyle);
                cell.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.10), 6, 0.15, 0, 2));
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

                // Tagesnummer oben rechts
                Label dayNumber = new Label("");
                if (dayNum >= 1 && dayNum <= daysInMonth) dayNumber.setText(String.valueOf(dayNum));
                StackPane.setAlignment(dayNumber, Pos.TOP_RIGHT);
                dayNumber.setPadding(new Insets(6, 10, 0, 0));
                dayNumber.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 14px; -fx-font-weight: 600;");
                cell.getChildren().add(dayNumber);

                // Termine für Tag
                if (dayNum >= 1 && dayNum <= daysInMonth) {
                    LocalDate thisDate = currentMonth.withDayOfMonth(dayNum);

                    List<Termin> termine = MainLogik.getTermineFuerDatum(thisDate);
                    List<Termin> visibleTermine = new ArrayList<>();

                    if (termine != null) {
                        if (selectedCategoryName == null) visibleTermine.addAll(termine);
                        else {
                            for (Termin t : termine) {
                                try {
                                    if (t.getKategorie() != null && selectedCategoryName.equals(t.getKategorie().getName())) {
                                        visibleTermine.add(t);
                                    }
                                } catch (Throwable ignore) {}
                            }
                        }
                    }

                    if (!visibleTermine.isEmpty()) {
                        VBox barsContainer = new VBox(6);
                        StackPane.setAlignment(barsContainer, Pos.BOTTOM_CENTER);
                        StackPane.setMargin(barsContainer, new Insets(32, 6, 8, 6));
                        barsContainer.maxWidthProperty().bind(cell.widthProperty().subtract(12));

                        int count = Math.min(visibleTermine.size(), MAX_BADGES_ON_TILE);
                        for (int i = 0; i < count; i++) {
                            Termin t = visibleTermine.get(i);

                            HBox bar = new HBox();
                            bar.setPrefHeight(16);
                            bar.setMinHeight(16);
                            bar.setMaxHeight(16);
                            bar.maxWidthProperty().bind(cell.widthProperty().subtract(12));
                            bar.setAlignment(Pos.CENTER_LEFT);

                            // Farbe des Termins
                            String color = "#4A90E2";
                            try {
                                if (t.getKategorie() != null && t.getKategorie().getFarbe() != null && !t.getKategorie().getFarbe().isBlank()) {
                                    color = t.getKategorie().getFarbe();
                                }
                            } catch (Throwable ex) {}

                            bar.setStyle(
                                    "-fx-background-color: " + color + ";" +
                                            "-fx-background-radius: 6;" +
                                            "-fx-border-radius: 6;" +
                                            "-fx-border-color: rgba(0,0,0,0.12);"
                            );

                            // Titel von Termin
                            String displayTitle = t.getTitel() == null ? "" : t.getTitel();
                            if (MainLogik.getZeigeAlleTermine()) {
                                String owner = MainLogik.getBenutzernameFuerTermin(t);
                                if (owner != null && !owner.isBlank()) displayTitle = owner + ": " + displayTitle;
                            }

                            Label tlabel = new Label(displayTitle);
                            tlabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
                            tlabel.setPadding(new Insets(0, 8, 0, 8));
                            bar.getChildren().add(tlabel);

                            barsContainer.getChildren().add(bar);
                        }

                        // wenn viele Termine, dann "..."
                        if (visibleTermine.size() > MAX_BADGES_ON_TILE) {
                            Label more = new Label("…");
                            more.setStyle("-fx-text-fill: #D0D0D0; -fx-font-size: 12px;");
                            StackPane.setAlignment(more, Pos.BOTTOM_LEFT);
                            StackPane.setMargin(more, new Insets(0, 0, 6, 8));
                            cell.getChildren().add(more);
                        }

                        cell.getChildren().add(barsContainer);
                    }
                }

                // DayView öffnen
                final int dni = dayNum;
                if (dayNum >= 1 && dayNum <= daysInMonth) {
                    cell.setOnMouseClicked(ev -> {
                        LocalDate date = currentMonth.withDayOfMonth(dni);
                        showDayView(date);
                    });
                } else {
                    cell.setOnMouseClicked(null);
                }

                kalenderGrid.add(cell, col, row);
            }
        }

        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        if (mtBtn != null) mtBtn.setText("Heute");
    }

    // zurück zur Monatsansicht
    private void showMonthView() {
        root.setCenter(monthViewHolder);
        isDayView = false;
        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        renderCalendar();
        if (mtBtn != null) mtBtn.setText("Heute");
    }

    // Tagesansicht zeigen
    private void showDayView(LocalDate date) {
        this.currentDisplayedDate = date;

        List<Termin> termine = MainLogik.getTermineFuerDatum(date);
        List<Termin> visible = new ArrayList<>();

        if (termine != null) {
            if (selectedCategoryName == null) visible.addAll(termine);
            else {
                for (Termin t : termine) {
                    try {
                        if (t.getKategorie() != null && selectedCategoryName.equals(t.getKategorie().getName())) {
                            visible.add(t);
                        }
                    } catch (Throwable ignore) {}
                }
            }
        }

        dayView.show(date, visible);
        root.setCenter(dayView);
        isDayView = true;

        monthLabel.setText(date.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        if (mtBtn != null) {
            if (date.equals(LocalDate.now())) mtBtn.setText("Monat");
            else mtBtn.setText("Heute");
        }
    }

    // Hover wie immer
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

    // aktualisiert Benutzer-Panel
    private void refreshUserPanel() {
        if (this.benutzerPanel == null) return;
        this.benutzerPanel.getChildren().clear();
        try {
            List<String> names = MainLogik.getBenutzerNamen();
            if (names == null) return;

            for (String n : names) {
                Button b = new Button(n);
                b.setMaxWidth(Double.MAX_VALUE);
                final String normal =
                        "-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;";
                final String hover =
                        "-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;";

                b.setStyle(normal);
                b.setOnMouseEntered(ev -> {
                    b.setCursor(Cursor.HAND);
                    b.setStyle(hover);
                });
                b.setOnMouseExited(ev -> {
                    b.setCursor(Cursor.DEFAULT);
                    b.setStyle(normal);
                });

                // Benutzer wechseln
                b.setOnAction(ev -> {
                    try {
                        MainLogik.setCurrentUserName(n);
                        currentUserLabel.setText(n);

                        this.benutzerPanel.setVisible(false);
                        this.benutzerPanel.setManaged(false);

                        if (isDayView) showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                        else renderCalendar();

                    } catch (Throwable ex) {
                        System.err.println("Fehler beim Wechseln des Benutzers: " + ex.getMessage());
                        Alert a = new Alert(Alert.AlertType.ERROR, "Benutzer konnte nicht gewechselt werden.");
                        a.initOwner(primaereStageReferenz);
                        a.showAndWait();
                    }
                });

                this.benutzerPanel.getChildren().add(b);
            }
        } catch (Throwable ex) {
            System.err.println("refreshUserPanel failed: " + ex.getMessage());
        }
    }

    // aktualisiert Kategorie-Panel
    private void refreshCategoryPanel() {
        if (this.kategoriePanel == null) return;
        this.kategoriePanel.getChildren().clear();
        try {
            Button allBtn = new Button("Alle");
            allBtn.setPrefWidth(Double.MAX_VALUE);
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            allBtn.setOnAction(ev -> {
                selectedCategoryName = null;
                this.kategoriePanel.setVisible(false);
                this.kategoriePanel.setManaged(false);

                if (isDayView) showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                else renderCalendar();
            });
            this.kategoriePanel.getChildren().add(allBtn);

            List<String> kategorien = MainLogik.getKategorienNamen();
            if (kategorien != null) {
                for (String k : kategorien) {
                    Button kb = new Button(k);
                    kb.setPrefWidth(Double.MAX_VALUE);

                    boolean sel = (selectedCategoryName != null && selectedCategoryName.equals(k));

                    final String selectedStyle =
                            "-fx-background-color: rgba(75,123,255,0.18); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;";
                    final String normalStyle =
                            "-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;";
                    final String hoverStyle =
                            "-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;";

                    kb.setStyle(sel ? selectedStyle : normalStyle);

                    kb.setOnMouseEntered(ev -> {
                        kb.setCursor(Cursor.HAND);
                        kb.setStyle(hoverStyle);
                    });

                    kb.setOnMouseExited(ev -> {
                        kb.setCursor(Cursor.DEFAULT);
                        boolean selected = (selectedCategoryName != null && selectedCategoryName.equals(k));
                        kb.setStyle(selected ? selectedStyle : normalStyle);
                    });

                    // Kategorie auswählen
                    kb.setOnAction(ev -> {
                        if (k.equals(selectedCategoryName)) selectedCategoryName = null;
                        else selectedCategoryName = k;

                        this.kategoriePanel.setVisible(false);
                        this.kategoriePanel.setManaged(false);

                        if (isDayView) showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                        else renderCalendar();
                    });

                    this.kategoriePanel.getChildren().add(kb);
                }
            }

            // neue Kategorie erstellen
            Button addCatBtn = new Button("+ Neue Kategorie");
            addCatBtn.setPrefWidth(Double.MAX_VALUE);

            final String addNormal =
                    "-fx-background-color: #333336; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10; -fx-background-radius:6;";
            final String addHover =
                    "-fx-background-color: #3A3A3D; -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10; -fx-background-radius:6;";

            addCatBtn.setStyle(addNormal);
            addCatBtn.setOnMouseEntered(ev -> {
                addCatBtn.setCursor(Cursor.HAND);
                addCatBtn.setStyle(addHover);
            });
            addCatBtn.setOnMouseExited(ev -> {
                addCatBtn.setCursor(Cursor.DEFAULT);
                addCatBtn.setStyle(addNormal);
            });

            addCatBtn.setOnAction(ev -> {
                KategorieAdd.show(primaereStageReferenz, (String createdName) -> {
                    try {
                        refreshCategoryPanel();
                    } catch (Throwable ex) {
                        System.err.println("Fehler beim Aktualisieren der Kategorien nach Erstellung: " + ex.getMessage());
                    }
                });
            });

            this.kategoriePanel.getChildren().add(addCatBtn);

        } catch (Throwable ex) {
            System.err.println("refreshCategoryPanel failed: " + ex.getMessage());
        }
    }

    // aktualisiert Ich/Alle Button
    private void refreshProfileBtn(Button profileBtn) {
        if (profileBtn == null) return;

        if (MainLogik.getZeigeAlleTermine()) {
            profileBtn.setText("\uD83D\uDC65  Alle");
            profileBtn.setStyle(
                    "-fx-background-color: linear-gradient(#3A6DFF, #2A56D6); " +
                            "-fx-border-color: rgba(255,255,255,0.12); " +
                            "-fx-background-radius: 8; -fx-border-radius: 8; -fx-text-fill: white;"
            );
            profileBtn.setTooltip(new Tooltip("Alle Termine anzeigen (An/Aus)"));
        } else {
            profileBtn.setText("\uD83D\uDC64  Ich");
            profileBtn.setStyle(
                    "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                            "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 8 6 8; -fx-text-fill: #F2F2F2;"
            );
            profileBtn.setTooltip(new Tooltip("Nur Termine des aktuellen Benutzers anzeigen"));
        }
    }

    // prüft auf anstehende Termine und zeigt Benachrichtigungen an
    private void checkNotifications() {
        try {
            Instant now = Instant.now();
            Instant target = now.plus(30, ChronoUnit.MINUTES);
            Instant windowEnd = target.plus(59, ChronoUnit.SECONDS);

            ZoneId zone = ZoneId.systemDefault();
            LocalDate targetDate = ZonedDateTime.ofInstant(target, zone).toLocalDate();

            var fam = Demos.getDemoFamilie();
            if (fam == null) return;
            var members = fam.getMitglieder();
            if (members == null || members.isEmpty()) return;

            for (Benutzer b : members) {
                if (b == null || b.getKalender() == null) continue;

                List<Termin> terms = b.getKalender().getTermineDatum(targetDate);
                if (terms == null || terms.isEmpty()) continue;

                for (Termin t : terms) {
                    if (t == null || t.getStart() == null) continue;

                    Instant s = t.getStart();

                    // Startzeit liegt innerhalb von 30 Minuten
                    if (!s.isBefore(target) && !s.isAfter(windowEnd)) {

                        // nichts doppelt zeigen
                        String key = b.getName() + "|" + s.toEpochMilli() + "|" + (t.getTitel() == null ? "" : t.getTitel());
                        if (shownNotifications.contains(key)) continue;
                        shownNotifications.add(key);

                        javafx.application.Platform.runLater(() -> {
                            try {
                                Benachrichtigung.showReminder(primaereStageReferenz, t, b);
                            } catch (Throwable ex) {
                                System.err.println("Fehler beim Anzeigen der Benachrichtigung: " + ex.getMessage());
                            }
                        });
                    }
                }
            }
        } catch (Throwable ex) {
            System.err.println("checkNotifications failed: " + ex.getMessage());
        }
    }
}
