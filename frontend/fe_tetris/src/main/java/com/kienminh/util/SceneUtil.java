package com.kienminh.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneUtil {

    private static Stage primaryStage; // Lưu stage chính

    // Phương thức setStage để MainApp gọi
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxml) {
        if (primaryStage == null) {
            System.err.println("Primary stage is not set!");
            return;
        }
        try {
            Scene scene = new Scene(FXMLLoader.load(SceneUtil.class.getResource("/view/" + fxml)));
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

