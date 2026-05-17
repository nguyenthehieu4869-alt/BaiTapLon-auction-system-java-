package com.auction.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Optional;

public class AlertUtil {

    private static final String BACKGROUND = "#062f2d";
    private static final String PANEL = "#0b3f3c";
    private static final String GOLD = "#d4af37";
    private static final String TEXT = "#f8fafc";
    private static final String MUTED = "#cbd5e1";
    private static final String ERROR = "#dc5b57";
    private static final String INFO = "#3b82f6";
    private static final String WARNING = "#f59e0b";

    public static void showInfo(String message) {
        showMessage("Thông báo", message, "i", INFO, "Đã hiểu");
    }

    public static void showError(String message) {
        showMessage("Lỗi", message, "!", ERROR, "Đóng");
    }

    public static void showWarning(String message) {
        showMessage("Cảnh báo", message, "!", WARNING, "Đã hiểu");
    }

    public static boolean showConfirm(String title, String message) {
        Dialog<ButtonType> dialog = createBaseDialog(title);

        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirmButton = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelButton, confirmButton);
        dialog.getDialogPane().setContent(createContent(title, message, "?", WARNING));

        styleButton(dialog.getDialogPane(), cancelButton, false);
        styleButton(dialog.getDialogPane(), confirmButton, true);

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }

    private static void showMessage(String title, String message, String icon, String accent, String buttonText) {
        Dialog<ButtonType> dialog = createBaseDialog(title);

        ButtonType okButton = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);
        dialog.getDialogPane().setContent(createContent(title, message, icon, accent));

        styleButton(dialog.getDialogPane(), okButton, true);
        dialog.showAndWait();
    }

    private static Dialog<ButtonType> createBaseDialog(String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        Window owner = getActiveWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }

        DialogPane pane = dialog.getDialogPane();
        pane.setMinWidth(440);
        pane.setMaxWidth(560);
        pane.setPadding(new Insets(0));
        pane.setStyle(
                "-fx-background-color: " + BACKGROUND + ";" +
                "-fx-border-color: " + GOLD + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.42), 24, 0.28, 0, 8);"
        );

        return dialog;
    }

    private static Node createContent(String title, String message, String iconText, String accent) {
        Label icon = new Label(iconText);
        icon.setAlignment(Pos.CENTER);
        icon.setMinSize(58, 58);
        icon.setPrefSize(58, 58);
        icon.setMaxSize(58, 58);
        icon.setStyle(
                "-fx-background-color: " + accent + ";" +
                "-fx-background-radius: 29;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 28;" +
                "-fx-font-weight: 800;" +
                "-fx-border-color: rgba(255,255,255,0.42);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 29;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: " + GOLD + ";" +
                "-fx-font-size: 22;" +
                "-fx-font-weight: 800;"
        );

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                "-fx-font-size: 15;" +
                "-fx-line-spacing: 4;"
        );

        VBox textBox = new VBox(8, titleLabel, messageLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox content = new HBox(18, icon, textBox);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(28, 30, 22, 30));
        content.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                "-fx-background-radius: 16 16 0 0;"
        );

        return content;
    }

    private static void styleButton(DialogPane pane, ButtonType buttonType, boolean primary) {
        Node buttonNode = pane.lookupButton(buttonType);
        if (!(buttonNode instanceof Button button)) {
            return;
        }

        button.setMinWidth(112);
        button.setMinHeight(38);
        button.setCursor(javafx.scene.Cursor.HAND);

        if (primary) {
            button.setStyle(
                    "-fx-background-color: " + GOLD + ";" +
                    "-fx-text-fill: #062f2d;" +
                    "-fx-font-size: 14;" +
                    "-fx-font-weight: 800;" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-radius: 20;"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + TEXT + ";" +
                    "-fx-font-size: 14;" +
                    "-fx-font-weight: 700;" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-radius: 20;" +
                    "-fx-border-color: rgba(255,255,255,0.35);" +
                    "-fx-border-width: 1;"
            );
        }
    }

    private static Window getActiveWindow() {
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window.isFocused()) {
                return window;
            }
        }

        for (Window window : Window.getWindows()) {
            if (window.isShowing()) {
                return window;
            }
        }

        return null;
    }
}
