// java
package GUI;

import JavaLogik.Termin;
import JavaLogik.MainLogik;
import JavaLogik.Demos;
import JavaLogik.Benutzer;
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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

// zusätzliche Importe für die Uhr
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.application.Platform;

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

    // NEU: userPanel als Instanzfeld, damit es aus Lambdas genutzt werden kann
    private VBox userPanel;

    // NEU: categoryPanel als Instanzfeld
    private VBox categoryPanel;

    // NEU: aktuell gewählte Kategorie (null = alle)
    private String selectedCategoryName = null;

    // Notification timeline + shown keys to avoid duplicates
    private Timeline notificationTimeline;
    private final Set<String> shownNotifications = new HashSet<>();

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
        clockTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), ev -> {
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

        // --- WICHTIG: userPanel FRÜHZEITIG initialisieren (vermeidet NPE in addBtn handler) ---
        this.userPanel = new VBox(6);
        this.userPanel.setPadding(new Insets(8));
        this.userPanel.setStyle("-fx-background-color: #242428; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(255,255,255,0.03);");
        this.userPanel.setVisible(false);
        this.userPanel.setManaged(false);
        this.userPanel.setMaxWidth(Double.MAX_VALUE);
        // initial befüllen
        refreshUserPanel();

        // Neuer: + Button öffnet TerminAdd und fügt Termin über MainLogik hinzu
        addBtn.setOnAction(e -> {
            ContextMenu addMenu = new ContextMenu();
            MenuItem miTermin = new MenuItem("Termin hinzufügen");
            MenuItem miBenutzer = new MenuItem("Benutzer hinzufügen");

            miTermin.setOnAction(ae -> {
                TerminAdd.show(primaryStageRef, LocalDate.now(), (Termin neu) -> {
                    if (neu == null) return;
                    try {
                        boolean conflict = MainLogik.hasConflictForCurrentUser(neu);
                        if (conflict) {
                            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.CONFIRMATION,
                                    "Der Termin scheint sich mit bestehenden Terminen zu überschneiden. Trotzdem erstellen?",
                                    javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO
                            );
                            confirm.setHeaderText(null);
                            confirm.initOwner(primaryStageRef);
                            java.util.Optional<javafx.scene.control.ButtonType> resp = confirm.showAndWait();
                            if (!resp.isPresent() || resp.get() != javafx.scene.control.ButtonType.YES) {
                                // Abbruch durch den Nutzer -> nicht anlegen
                                return;
                            }
                        }

                        // Entweder kein Konflikt oder Nutzer hat bestätigt
                        MainLogik.addTermin(neu);
                    } catch (Throwable ex) {
                        System.out.println("Konnte Termin nicht an MainLogik übergeben: " + ex.getMessage());
                        javafx.scene.control.Alert err = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Termin konnte nicht erstellt werden.");
                        err.initOwner(primaryStageRef);
                        err.showAndWait();
                    }

                    if (isDayView) {
                        showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                    } else {
                        renderCalendar(); // neu rendern falls Monatsansicht sichtbar
                    }
                });
            });

            miBenutzer.setOnAction(ae -> {
                // Öffne Benutzer-Dialog; bei Erfolg echten Benutzer-Objekt erhalten und UI updaten
                java.util.function.Consumer<JavaLogik.Benutzer> onSaved = (JavaLogik.Benutzer neu) -> {
                    if (neu == null) return;
                    try {
                        MainLogik.setCurrentUserName(neu.getName());
                        currentUserLabel.setText(neu.getName());
                        refreshUserPanel();
                        if (isDayView) {
                            showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                        } else {
                            renderCalendar();
                        }
                    } catch (Throwable ex) {
                        System.err.println("Fehler beim Verarbeiten des neuen Benutzers: " + ex.getMessage());
                        Alert a = new Alert(Alert.AlertType.ERROR, "Benutzer erstellt, UI konnte nicht aktualisiert werden.");
                        a.initOwner(primaryStageRef);
                        a.showAndWait();
                    }
                };
                BenutzerAdd.show(primaryStageRef, onSaved);
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

       // Tooltip und initialer visueller Zustand entsprechend MainLogik-Flag
        updateProfileButtonVisual(profileBtn);

        // Toggle: zeigt entweder nur Termine des aktuellen Benutzers oder alle Termine der Familie
        profileBtn.setOnAction(e -> {
            boolean newState = !MainLogik.isShowAllFamilyTermine();
            MainLogik.setShowAllFamilyTermine(newState);
            updateProfileButtonVisual(profileBtn);
            // Neu rendern der aktuell sichtbaren Ansicht
           try {
                if (isDayView) {
                    showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
               } else {
                    renderCalendar();
                }
            } catch (Throwable ex) {
                System.err.println("Fehler beim Aktualisieren der Ansicht nach Toggle All-Users: " + ex.getMessage());
            }
        });

        // Topbar jetzt mit monthLabel vor dem searchField
        topBar.getChildren().addAll(topLeftSpacer, monthLabel, searchField, searchBtn, currentUserLabel, profileBtn, mtBtn);

        // --- USER PANEL (ausklappbar unter "Benutzer wechseln") ---
        // this.userPanel wurde bereits initialisiert weiter oben; Inhalte kommen aus refreshUserPanel()

        int insertIndex = leftBar.getChildren().indexOf(benutzerWechselnBtn);
        if (insertIndex >= 0) {
            leftBar.getChildren().add(insertIndex + 1, this.userPanel);
        } else {
            leftBar.getChildren().add(this.userPanel);
        }

        benutzerWechselnBtn.setOnAction(e -> {
            boolean showing = this.userPanel.isVisible();
            this.userPanel.setVisible(!showing);
            this.userPanel.setManaged(!showing);
        });

        // --- CATEGORY PANEL (ausklappbar unter "Kategorien") ---
        // Instanz-Panel und initiale Befüllung via helper
        this.categoryPanel = new VBox(6);
        this.categoryPanel.setPadding(new Insets(8));
        this.categoryPanel.setStyle("-fx-background-color: #242428; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: rgba(255,255,255,0.03);");
        this.categoryPanel.setVisible(false);
        this.categoryPanel.setManaged(false);
        this.categoryPanel.setMaxWidth(Double.MAX_VALUE);
        refreshCategoryPanel();

        int catInsertIndex = leftBar.getChildren().indexOf(kategorienBtn);
        if (catInsertIndex >= 0) {
            leftBar.getChildren().add(catInsertIndex + 1, this.categoryPanel);
        } else {
            leftBar.getChildren().add(this.categoryPanel);
        }

        kategorienBtn.setOnAction(e -> {
            boolean showing = this.categoryPanel.isVisible();
            this.categoryPanel.setVisible(!showing);
            this.categoryPanel.setManaged(!showing);
        });

        // Register UI listener, damit Kategorien, die z.B. aus KategorieAdd erstellt werden,
        // sofort in der UI sichtbar werden.
        MainLogik.setCategoryAddedListener((String newName) -> {
            Platform.runLater(() -> {
                try {
                    refreshCategoryPanel();
                } catch (Throwable ex) {
                    System.err.println("Fehler beim Aktualisieren der Kategorien: " + ex.getMessage());
                }
            });
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
            if (notificationTimeline != null) notificationTimeline.stop();
        });

        // --- NEU: Benachrichtigungs-Timeline starten (prüft alle 30 Sekunden) ---
        notificationTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(30), ev -> {
            try {
                checkNotifications();
            } catch (Throwable ex) {
                System.err.println("Fehler in Notification-Check: " + ex.getMessage());
            }
        }));
        notificationTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationTimeline.play();

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

                // ----- Balken nur für Termine, die (falls gesetzt) zur selectedCategoryName gehören -----
                if (dayNum >= 1 && dayNum <= daysInMonth) {
                    LocalDate thisDate = currentMonth.withDayOfMonth(dayNum);
                    List<Termin> termine = MainLogik.getTermineForDate(thisDate);
                    List<Termin> visibleTermine = new ArrayList<>();
                    if (termine != null) {
                        if (selectedCategoryName == null) {
                            visibleTermine.addAll(termine);
                        } else {
                            for (Termin t : termine) {
                                try {
                                    if (t.getKategorie() != null && selectedCategoryName.equals(t.getKategorie().getName())) {
                                        visibleTermine.add(t);
                                    }
                                } catch (Throwable ignore) {}
                            }
                        }
                    }

                    if (visibleTermine != null && !visibleTermine.isEmpty()) {
                        VBox barsContainer = new VBox(6);
                        // Platz unten und seitlich freihalten
                        StackPane.setAlignment(barsContainer, Pos.BOTTOM_CENTER);
                        // oben mehr Abstand, damit die Balken nicht in die Tageszahl ragen
                        StackPane.setMargin(barsContainer, new Insets(32, 6, 8, 6)); // erhöhten Abstand oben
                        // Damit die Bars die Breite der Kachel nutzen
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

                            // Ersetze:
                            // Label tlabel = new Label(t.getTitel());
                            String displayTitle = t.getTitel() == null ? "" : t.getTitel();
                            if (MainLogik.isShowAllFamilyTermine()) {
                                String owner = findOwnerNameForTermin(t);
                                if (owner != null && !owner.isBlank()) {
                                    displayTitle = owner + ": " + displayTitle;
                                }
                            }
                            Label tlabel = new Label(displayTitle);
                            tlabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
                            tlabel.setPadding(new Insets(0, 8, 0, 8));
                            bar.getChildren().add(tlabel);

                            barsContainer.getChildren().add(bar);
                        }

                        // Wenn es mehr Termine gibt als angezeigt werden, ein kleines "…" unten links
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
        this.currentDisplayedDate = date;

        List<Termin> termine = MainLogik.getTermineForDate(date);
        List<Termin> visible = new ArrayList<>();
        if (termine != null) {
            if (selectedCategoryName == null) {
                visible.addAll(termine);
            } else {
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

    // Aktualisiert Inhalte des userPanel aus MainLogik (sicher + defensiv)
    private void refreshUserPanel() {
        if (this.userPanel == null) return;
        this.userPanel.getChildren().clear();
        try {
            List<String> names = MainLogik.getBenutzerNamen();
            if (names == null) return;
            for (String n : names) {
                Button b = new Button(n);
                b.setMaxWidth(Double.MAX_VALUE);
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
                b.setOnMouseEntered(ev -> {
                    b.setCursor(Cursor.HAND);
                    b.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
                });
                b.setOnMouseExited(ev -> {
                    b.setCursor(Cursor.DEFAULT);
                    b.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
                });
                b.setOnAction(ev -> {
                    try {
                        MainLogik.setCurrentUserName(n);
                        currentUserLabel.setText(n);
                        this.userPanel.setVisible(false);
                        this.userPanel.setManaged(false);
                        if (isDayView) {
                            showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                        } else {
                            renderCalendar();
                        }
                    } catch (Throwable ex) {
                        System.err.println("Fehler beim Wechseln des Benutzers: " + ex.getMessage());
                        Alert a = new Alert(Alert.AlertType.ERROR, "Benutzer konnte nicht gewechselt werden.");
                        a.initOwner(primaryStageRef);
                        a.showAndWait();
                    }
                });
                this.userPanel.getChildren().add(b);
            }
        } catch (Throwable ex) {
            System.err.println("refreshUserPanel failed: " + ex.getMessage());
        }
    }

    // NEU: refresh der Kategorie-Liste im Panel (angepasst: Auswahl setzt Filter)
    private void refreshCategoryPanel() {
        if (this.categoryPanel == null) return;
        this.categoryPanel.getChildren().clear();
        try {
            // "Alle" Button - setzt Filter zurück
            Button allBtn = new Button("Alle");
            allBtn.setPrefWidth(Double.MAX_VALUE);
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
            allBtn.setOnAction(ev -> {
                selectedCategoryName = null;
                this.categoryPanel.setVisible(false);
                this.categoryPanel.setManaged(false);
                // neu rendern
                if (isDayView) {
                    showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                } else {
                    renderCalendar();
                }
            });
            this.categoryPanel.getChildren().add(allBtn);

            List<String> kategorien = MainLogik.getKategorienNamen();
            if (kategorien == null) return;
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
                    // restore selection style or base
                    boolean sel = (selectedCategoryName != null && selectedCategoryName.equals(k));
                    kb.setStyle(sel ? "-fx-background-color: rgba(75,123,255,0.18); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;" :
                                      "-fx-background-color: transparent; -fx-text-fill: #E8E8E8; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
                });
                kb.setOnAction(ev -> {
                    // Toggle selection: bei erneutem Klick entfernen
                    if (k.equals(selectedCategoryName)) {
                        selectedCategoryName = null;
                    } else {
                        selectedCategoryName = k;
                    }
                    this.categoryPanel.setVisible(false);
                    this.categoryPanel.setManaged(false);

                    // neu rendern mit Filter
                    if (isDayView) {
                        showDayView(currentDisplayedDate != null ? currentDisplayedDate : LocalDate.now());
                    } else {
                        renderCalendar();
                    }
                });
                // visuelle Markierung, falls ausgewählt
                if (selectedCategoryName != null && selectedCategoryName.equals(k)) {
                    kb.setStyle("-fx-background-color: rgba(75,123,255,0.18); -fx-text-fill: #FFFFFF; -fx-alignment: center-left; -fx-padding: 6 10 6 10;");
                }
                this.categoryPanel.getChildren().add(kb);
            }
        } catch (Throwable ex) {
            System.err.println("refreshCategoryPanel failed: " + ex.getMessage());
        }
    }

    // helper: passt Profil-Button-Style und Tooltip an den aktuellen Toggle-Zustand an
    private void updateProfileButtonVisual(Button profileBtn) {
        if (MainLogik.isShowAllFamilyTermine()) {
            profileBtn.setStyle(
                    "-fx-background-color: linear-gradient(#3A6DFF, #2A56D6); " +
                    "-fx-border-color: rgba(255,255,255,0.12); " +
                    "-fx-background-radius: 8; -fx-border-radius: 8; -fx-text-fill: white;"
            );
            profileBtn.setTooltip(new javafx.scene.control.Tooltip("Alle Termine anzeigen (An/Aus)"));
        } else {
            profileBtn.setStyle(
                    "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); " +
                    "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 8 6 8; -fx-text-fill: #F2F2F2;"
            );
            profileBtn.setTooltip(new javafx.scene.control.Tooltip("Nur Termine des aktuellen Benutzers anzeigen"));
        }
    }

    // Prüft, ob Termine in genau 30 Minuten beginnen (toleranz 60 Sekunden) und zeigt Popup einmalig an
    private void checkNotifications() {
        try {
            // Zielzeit: jetzt + 30 Minuten (Toleranzfenster: [target, target + 59s])
            Instant now = Instant.now();
            Instant target = now.plus(30, ChronoUnit.MINUTES);
            Instant windowEnd = target.plus(59, ChronoUnit.SECONDS);

            // LocalDate des target (für Kalender-Abfragen)
            ZoneId zone = ZoneId.systemDefault();
            LocalDate targetDate = ZonedDateTime.ofInstant(target, zone).toLocalDate();

            // Hole Demo-Familie-Mitglieder sicher
            var fam = Demos.getDemoFamilie();
            if (fam == null) return;
            var members = fam.getMitglieder();
            if (members == null || members.isEmpty()) return;

            for (Benutzer b : members) {
                if (b == null || b.getKalender() == null) continue;
                List<Termin> terms = b.getKalender().getTermineForDate(targetDate);
                if (terms == null || terms.isEmpty()) continue;
                for (Termin t : terms) {
                    if (t == null || t.getStart() == null) continue;
                    Instant s = t.getStart();
                    // prüfen ob Start innerhalb des Fensters liegt
                    if (!s.isBefore(target) && !s.isAfter(windowEnd)) {
                        // Erzeuge eindeutigen Key: user|startEpoch|titel
                        String key = b.getName() + "|" + s.toEpochMilli() + "|" + (t.getTitel() == null ? "" : t.getTitel());
                        if (shownNotifications.contains(key)) continue; // schon angezeigt
                        // Merken und anzeigen
                        shownNotifications.add(key);
                        // UI-Thread: Benachrichtigung anzeigen
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Benachrichtigung.showReminder(primaryStageRef, t, b);
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

    // Helfer: findet den Benutzernamen, dem dieser Termin gehört (oder null)
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
}
