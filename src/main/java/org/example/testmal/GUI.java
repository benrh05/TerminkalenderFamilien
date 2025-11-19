// Java
package org.example.testmal;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.Cursor;

public class GUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Root
        BorderPane root = new BorderPane();
        // leicht aufgehellter Dark-Mode-Hintergrund (dezent höherer Helligkeit, hoher Kontrast bleibt)
        root.setStyle("-fx-background-color: linear-gradient(#1b1b1e 0%, #262629 100%);");

        // --- LEFT SIDEBAR ---
        VBox leftBar = new VBox(16);
        leftBar.setPadding(new Insets(18));
        leftBar.setPrefWidth(220);
        leftBar.setStyle("-fx-background-color: transparent; -fx-border-width:0 1 0 0; -fx-border-color: rgba(0,0,0,0.04);");

        // Date/Time header with navigation and add button
        Label dateLabel = new Label("Date/Time");
        dateLabel.setFont(Font.font(18));
        // hellere Schrift für Dark Mode
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

        // Sidebar action buttons
        Button kategorienBtn = new Button("Kategorien \u25BE"); // dropdown arrow
        kategorienBtn.setPrefWidth(Double.MAX_VALUE);
        Button benutzerWechselnBtn = new Button("Benutzer wechseln \u21C5");
        benutzerWechselnBtn.setPrefWidth(Double.MAX_VALUE);
        Button heuteBtn = new Button("Heute \u2B06");
        heuteBtn.setPrefWidth(Double.MAX_VALUE);

        // Style sidebar buttons (freundlicher)
        // leicht aufgehellte Dark-Mode-Stile (Buttons)
        String sideBtnStyle = "-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10 8 10; -fx-text-fill: #F0F0F0;";
        kategorienBtn.setStyle(sideBtnStyle);
        benutzerWechselnBtn.setStyle(sideBtnStyle);
        heuteBtn.setStyle(sideBtnStyle);

        leftBar.getChildren().addAll(dateLabel, nav, new Separator(), kategorienBtn, benutzerWechselnBtn, heuteBtn);

        // --- TOP BAR ---
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(12, 18, 12, 18));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setStyle("-fx-background-color: transparent; -fx-border-width:0 0 1 0; -fx-border-color: rgba(0,0,0,0.04);");

        // Left side of top (can be empty or month name)
        Region topLeftSpacer = new Region();
        HBox.setHgrow(topLeftSpacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Suche...");
        searchField.setPrefWidth(360);
        Button searchBtn = new Button("\uD83D\uDD0D"); // magnifier (may render as emoji)
        Button profileBtn = new Button("\uD83D\uDC64"); // user icon
        profileBtn.setPrefSize(44, 36);
        Button mtBtn = new Button("M/T");
        mtBtn.setPrefSize(54, 36);

        // leicht aufgehellte Topbar-Elemente (erhöhter Kontrast bleibt)
        searchField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: #232324; -fx-border-color: rgba(255,255,255,0.07); -fx-padding: 8; -fx-text-fill: #F2F2F2;");
        searchBtn.setStyle("-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; -fx-text-fill: #F2F2F2;");
        mtBtn.setStyle("-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6; -fx-text-fill: #F2F2F2;");
        // Profil-Button: Hintergrundradius setzen, damit keine eckigen Reste sichtbar sind
        profileBtn.setStyle("-fx-background-color: #2A2A2A; -fx-border-color: rgba(255,255,255,0.07); -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 8 6 8; -fx-text-fill: #F2F2F2;");

        topBar.getChildren().addAll(topLeftSpacer, searchField, searchBtn, profileBtn, mtBtn);

        // --- CENTER: CALENDAR GRID ---
        GridPane calendarGrid = new GridPane();
        // weniger Abstand oberhalb der Tabelle, damit sie näher am Header sitzt
        calendarGrid.setPadding(new Insets(8, 18, 8, 18));
        calendarGrid.setHgap(6);
        calendarGrid.setVgap(8);
        calendarGrid.setAlignment(Pos.TOP_CENTER);

        // Create 7 columns (days) and 6 rows (weeks)
        for (int c = 0; c < 7; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7.0);
            cc.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(cc);
        }
        // Weniger Reihen anzeigen (5 statt 6) — entfernt überflüssige Tage
        for (int r = 0; r < 5; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 5.0);
            rc.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rc);
        }

        // Optional: day-of-week header
        String[] days = {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
        HBox headerRow = new HBox();
        // weniger Abstand unten, damit die Tabelle näher rückt
        headerRow.setPadding(new Insets(8, 18, 2, 18));
        headerRow.setSpacing(6);
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setPrefHeight(28);
            d.setPrefWidth(120);
            d.setAlignment(Pos.CENTER);
            // größere, aber nicht dickere Schrift, leicht helleres Text für besseren Kontrast
            d.setStyle("-fx-font-weight: normal; -fx-font-size: 15px; -fx-text-fill: #F0F0F0;");
            HBox.setHgrow(d, Priority.ALWAYS);
            headerRow.getChildren().add(d);
        }

        // Fill calendar cells
        // Style-Vorlage für Zellen
        // leicht aufgehellte Kartenfarbe (höherer Lesekomfort, Kontrast bleibt)
        final String cellBaseStyle = "-fx-background-color: #222225; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1;";
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                StackPane cell = new StackPane();
                cell.setMinHeight(100);
                cell.setStyle(cellBaseStyle);
                // sanfter Schatten
                cell.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.10), 6, 0.15, 0, 2));
                // Hover-Effekt
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
                // placeholder date label in top-left of cell
                Label dayNumber = new Label("");
                // Nummerierung 1..35 (row*7 + col + 1)
                int num = row * 7 + col + 1;
                if (num <= 35) {
                    dayNumber.setText(String.valueOf(num));
                } else {
                    dayNumber.setText("");
                }
                // Zahl oben rechts positionieren mit kleinem Abstand zum Rand
                StackPane.setAlignment(dayNumber, Pos.TOP_RIGHT);
                dayNumber.setPadding(new Insets(6, 10, 0, 0)); // oben, rechts Abstand
                dayNumber.setStyle("-fx-text-fill: #F5F5F5; -fx-font-size: 14px; -fx-font-weight: 600;");
                dayNumber.setTranslateY(0);
                cell.getChildren().add(dayNumber);
                calendarGrid.add(cell, col, row);
            }
        }

        // Center wrapper to keep grid visually aligned and responsive
        VBox centerBox = new VBox(2);
         centerBox.getChildren().addAll(headerRow, calendarGrid);
         VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        // Put pieces into root
        root.setLeft(leftBar);
        root.setTop(topBar);
        root.setCenter(centerBox);

        // Scene and Stage
        // Scene-Fill etwas aufgehellt, passend zum Root-Gradient
        Scene scene = new Scene(root, 1100, 720, Color.web("#19191A"));
        primaryStage.setTitle("Kalender");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Wende Hover auf Buttons an (wie die Tage)
        applyHover(prevBtn);
        applyHover(nextBtn);
        applyHover(addBtn);
        applyHover(kategorienBtn);
        applyHover(benutzerWechselnBtn);
        applyHover(heuteBtn);
        applyHover(searchBtn);
        applyHover(profileBtn);
        applyHover(mtBtn);
    }

    // kleine Hilfsmethode: Buttons beim Hover leicht skalieren (freundlicher Effekt)
    private void applyHover(Button b) {
        b.setOnMouseEntered(e -> {
            b.setScaleX(1.04);
            b.setScaleY(1.04);
            b.setCursor(Cursor.HAND);
            // optional leichter Anheb-Effekt
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
