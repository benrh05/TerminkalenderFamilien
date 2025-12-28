package GUI;

import JavaLogik.Termin;
import JavaLogik.Benutzer;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Kleine Popup-Stage für Erinnerungen (z. B. "Termin in 30 min").
 * Nur Stage-Definition und Anzeige; Scheduling/Timing erfolgt extern.
 */
public class Benachrichtigung extends Stage {

    private final Termin termin;
    private final Benutzer benutzer;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("EEE, d. MMM HH:mm").withZone(ZoneId.systemDefault());

    public Benachrichtigung(Stage owner, Termin termin, Benutzer benutzer) {
        this.termin = termin;
        this.benutzer = benutzer;

        initOwner(owner);
        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);

        buildUI();
    }

    private void buildUI() {
        // Left accent circle (category/user color could be used here)
        Circle accent = new Circle(8, Color.web("#4A90E2"));
        accent.setStroke(Color.rgb(255,255,255,0.06));
        accent.setStrokeWidth(1.0);

        // Header: Hinweisstext (größer)
        Label header = new Label("Erinnerung: Termin in 30 Minuten");
        header.setFont(Font.font(18));
        header.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: 600;");

        // Titel des Termins (etwas größer)
        Label title = new Label(termin != null ? termin.getTitel() : "(kein Titel)");
        title.setFont(Font.font(16));
        title.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        // Zeitspanne (größere Schrift)
        String timeText = "";
        try {
            if (termin != null) {
                ZonedDateTime zs = ZonedDateTime.ofInstant(termin.getStart(), ZoneId.systemDefault());
                ZonedDateTime ze = ZonedDateTime.ofInstant(termin.getEnde(), ZoneId.systemDefault());
                timeText = TIME_FMT.format(zs) + " — " + TIME_FMT.format(ze);
            }
        } catch (Exception ignored) {}

        Label timeLbl = new Label(timeText);
        timeLbl.setStyle("-fx-text-fill: #D6D6D6; -fx-font-size: 13px;");

        // Beschreibung (optional) - größer und breiteres Wrap
        Label desc = new Label();
        if (termin != null && termin.getBeschreibung() != null && !termin.getBeschreibung().isBlank()) {
            desc.setText(termin.getBeschreibung());
            desc.setStyle("-fx-text-fill: #DADADA; -fx-font-size: 13px;");
            desc.setWrapText(true);
            desc.setMaxWidth(420);
        }

        // Benutzer-Angabe (leicht größer)
        Label userLbl = new Label();
        if (benutzer != null) {
            userLbl.setText("Benutzer: " + benutzer.getName());
            userLbl.setStyle("-fx-text-fill: #BFCFEA; -fx-font-size: 12px;");
        }

        VBox textBox = new VBox(6, header, title, timeLbl);
        if (desc.getText() != null && !desc.getText().isBlank()) textBox.getChildren().add(desc);
        textBox.getChildren().add(userLbl);

        // Einziger Button: OK (schließt das Fenster) - etwas größer
        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(84);
        okBtn.setStyle(buttonBaseStyle("#3A6DFF") + "-fx-font-size: 13px;");
        okBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> close());
        okBtn.setOnMouseEntered(e -> {
            okBtn.setCursor(javafx.scene.Cursor.HAND);
            okBtn.setScaleX(1.03);
            okBtn.setScaleY(1.03);
        });
        okBtn.setOnMouseExited(e -> {
            okBtn.setCursor(javafx.scene.Cursor.DEFAULT);
            okBtn.setScaleX(1.0);
            okBtn.setScaleY(1.0);
        });

        HBox btns = new HBox(8, okBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        HBox contentRow = new HBox(14, accent, textBox);
        contentRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        VBox root = new VBox(14, contentRow, btns);
        root.setPadding(new Insets(16));
        root.setPrefWidth(480);
        root.setMaxWidth(480);
        root.setStyle(
                "-fx-background-color: linear-gradient(#2a2a2d, #222225);" +
                "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(255,255,255,0.04); -fx-border-width: 1;"
        );
        root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.45), 12, 0.12, 0, 6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Position bottom-right of owner (or screen if owner null)
        positionBottomRight();

        // subtle appear animation
        FadeTransition ft = new FadeTransition(Duration.millis(260), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private String buttonBaseStyle(String bg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: white; -fx-background-radius: 6; " +
               "-fx-padding: 6 10 6 10; -fx-font-size: 12px;";
    }

    private void positionBottomRight() {
        double margin = 16;
        // compute owner bounds if available
        Stage owner = (Stage) getOwner();
        double sx = Screen.getPrimary().getVisualBounds().getMaxX();
        double sy = Screen.getPrimary().getVisualBounds().getMaxY();

        // compute after layout when the window is shown - avoid showing here to prevent timing issues
        this.setOnShown(ev -> {
            try {
                double w = this.getWidth();
                double h = this.getHeight();
                double targetX = (owner != null) ? owner.getX() + owner.getWidth() - w - margin : sx - w - margin;
                double targetY = (owner != null) ? owner.getY() + owner.getHeight() - h - margin : sy - h - margin;
                setX(targetX);
                setY(targetY);
            } catch (Throwable ex) {
                System.err.println("Positionierung der Benachrichtigung fehlgeschlagen: " + ex.getMessage());
            }
        });
    }

    /**
     * Convenience: zeigt das Popup (ohne Scheduling). Caller kann später eigene Logik ergänzen.
     */
    public static void showReminder(Stage owner, Termin termin, Benutzer benutzer) {
        try {
            Benachrichtigung n = new Benachrichtigung(owner, termin, benutzer);
            // Anzeige jetzt explizit auslösen (Positionierung erfolgt in setOnShown)
            n.show();
        } catch (Exception ex) {
            System.err.println("Benachrichtigung konnte nicht angezeigt werden: " + ex.getMessage());
        }
    }
}
