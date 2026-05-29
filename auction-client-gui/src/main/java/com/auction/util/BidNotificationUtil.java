package com.auction.util;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import org.example.network.protocol.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BidNotificationUtil {
    private static final double NOTIFICATION_WIDTH = 360;
    private static final double RIGHT_MARGIN = 24;
    private static final double TOP_MARGIN = 24;
    private static final double GAP = 12;
    private static final int MAX_VISIBLE_NOTIFICATIONS = 4;
    private static final List<Popup> activeNotifications = new ArrayList<>();

    private BidNotificationUtil() {
    }

    public static void showBidNotification(Window owner, String currentUsername, Message message) {
        if (owner == null || !owner.isShowing() || message == null || !(message.getData() instanceof Map<?, ?> data)) {
            return;
        }

        String bidderUsername = getString(data, "bidderUsername");
        Double bidPrice = getDouble(data, "currentPrice");

        if (bidderUsername == null || bidderUsername.isBlank() || bidPrice == null) {
            return;
        }

        if (isSameUser(bidderUsername, currentUsername)) {
            return;
        }

        String productName = getProductName(data);
        show(owner, bidderUsername + " vừa đặt " + PriceFormatter.formatVND(bidPrice) + " cho " + productName + ".");
    }

    private static void show(Window owner, String message) {
        while (activeNotifications.size() >= MAX_VISIBLE_NOTIFICATIONS) {
            activeNotifications.get(0).hide();
        }

        Popup popup = new Popup();
        Node content = createContent(message, popup);

        popup.setAutoFix(true);
        popup.setAutoHide(false);
        popup.getContent().add(content);
        popup.setOnHidden(event -> {
            activeNotifications.remove(popup);
            repositionNotifications(owner);
        });

        activeNotifications.add(popup);
        popup.show(owner);
        repositionNotifications(owner);

        PauseTransition dismissTimer = new PauseTransition(Duration.seconds(8));
        dismissTimer.setOnFinished(event -> popup.hide());
        content.getProperties().put("dismissTimer", dismissTimer);
        dismissTimer.play();
    }

    private static Node createContent(String message, Popup popup) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(NOTIFICATION_WIDTH - 64);
        messageLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 13px; -fx-font-weight: bold; -fx-line-spacing: 2px;");

        Button closeButton = new Button("x");
        closeButton.setFocusTraversable(false);
        closeButton.setMinSize(24, 24);
        closeButton.setPrefSize(24, 24);
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #f5d76e; -fx-font-size: 14px; -fx-font-weight: 900; -fx-padding: 0; -fx-cursor: hand;");
        closeButton.setOnAction(event -> popup.hide());

        HBox root = new HBox(10, messageLabel, closeButton);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(12, 12, 12, 14));
        root.setPrefWidth(NOTIFICATION_WIDTH);
        root.setMaxWidth(NOTIFICATION_WIDTH);
        root.setStyle("-fx-background-color: rgba(8,36,51,0.96); -fx-background-radius: 8; -fx-border-color: #d4af37; -fx-border-radius: 8; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.32), 14, 0.2, 0, 4);");
        HBox.setHgrow(messageLabel, Priority.ALWAYS);

        return root;
    }

    private static void repositionNotifications(Window owner) {
        if (owner == null || !owner.isShowing()) {
            return;
        }

        double x = Math.max(
                owner.getX() + RIGHT_MARGIN,
                owner.getX() + owner.getWidth() - NOTIFICATION_WIDTH - RIGHT_MARGIN
        );
        double y = owner.getY() + TOP_MARGIN;

        for (Popup popup : new ArrayList<>(activeNotifications)) {
            if (!popup.isShowing() || popup.getContent().isEmpty()) {
                continue;
            }

            Node content = popup.getContent().get(0);
            content.applyCss();
            content.autosize();

            popup.setX(x);
            popup.setY(y);

            double height = content.getLayoutBounds().getHeight();
            y += Math.max(60, height) + GAP;
        }
    }

    private static String getProductName(Map<?, ?> data) {
        String productName = getString(data, "productName");

        if (productName != null && !productName.isBlank()) {
            return productName;
        }

        Object productId = data.get("productId");
        if (productId instanceof Number number) {
            return "sản phẩm #" + number.intValue();
        }

        return "sản phẩm";
    }

    private static String getString(Map<?, ?> data, String key) {
        Object value = data.get(key);
        return value == null ? null : value.toString();
    }

    private static Double getDouble(Map<?, ?> data, String key) {
        Object value = data.get(key);

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private static boolean isSameUser(String firstUsername, String secondUsername) {
        return firstUsername != null
                && secondUsername != null
                && firstUsername.trim().equalsIgnoreCase(secondUsername.trim());
    }
}
