package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FehlendeRechte extends Stage {        // nicht verwendet -- wäre für später relevant geworden

    public FehlendeRechte(Stage owner, String message, Runnable onAccept, Runnable onMore) {
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);

        initStyle(StageStyle.TRANSPARENT);
        setTitle("Fehlende Rechte");

        Label title = new Label("Fehlende Berechtigungen");
        title.setFont(Font.font(16));
        title.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: 600;");

        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-text-fill: #E8E8E8; -fx-font-size: 13px;");

        Button acceptBtn = new Button("\u2716  Schließen");
        Button moreBtn = new Button("\uD83D\uDD04  Benutzer wechseln");

        acceptBtn.setPrefWidth(120);
        moreBtn.setPrefWidth(160);

        Tooltip.install(acceptBtn, new Tooltip("Dialog schließen"));
        Tooltip.install(moreBtn, new Tooltip("Zum anderen Benutzer wechseln"));

        String baseBtn = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 10 8 10; -fx-font-size: 13px;";
        acceptBtn.setStyle(baseBtn + "-fx-background-color: #2A2A2A; -fx-text-fill: #F6F6F6; -fx-border-color: rgba(255,255,255,0.04);");
        moreBtn.setStyle(baseBtn + "-fx-background-color: linear-gradient(#3A6DFF, #2A56D6); -fx-text-fill: white;");

        applyHover(acceptBtn, "#3D3D3D", "#2A2A2A", true);
        applyHover(moreBtn, "#4B7BFF", "#3A6DFF", true);

        acceptBtn.setOnAction(e -> {
            close();
            if (onAccept != null) onAccept.run();
        });

        moreBtn.setOnAction(e -> {
            close();
            if (onMore != null) onMore.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(10, spacer, acceptBtn, moreBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, title, lbl, buttons);
        root.setPadding(new Insets(14));
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: #2a2a2d; -fx-background-radius: 10;");

        root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.45), 14, 0.15, 0, 6));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // ESC schließt
        scene.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ESCAPE) {
                close();
                if (onAccept != null) onAccept.run();
            }
        });

        setScene(scene);
        centerOnScreen();
    }

    public static void show(Stage owner, String message, Runnable onAccept, Runnable onMore) {
        FehlendeRechte dialog = new FehlendeRechte(owner, message, onAccept, onMore);
        dialog.showAndWait();
    }

    // Hover wie immer
    private void applyHover(Button b, String hoverBg, String normalBg, boolean useTranslate) {
        b.setOnMouseEntered(e -> {
            b.setCursor(Cursor.HAND);
            b.setScaleX(1.03);
            b.setScaleY(1.03);
            b.setStyle(b.getStyle().replace(normalBg, hoverBg));
            if (useTranslate) b.setTranslateY(-2);
        });
        b.setOnMouseExited(e -> {
            b.setCursor(Cursor.DEFAULT);
            b.setScaleX(1.0);
            b.setScaleY(1.0);
            b.setStyle(b.getStyle().replace(hoverBg, normalBg));
            if (useTranslate) b.setTranslateY(0);
        });
    }
}
