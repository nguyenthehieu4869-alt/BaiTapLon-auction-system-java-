package com.auction;

import com.auction.util.FxmlUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuctionApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = FxmlUtil.createLoader(getClass(), "/com/auction/view/login.fxml");

        Scene scene = new Scene(loader.load());
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setTitle("Hệ thống đấu giá trực tuyến");
        stage.show();
    }
}
