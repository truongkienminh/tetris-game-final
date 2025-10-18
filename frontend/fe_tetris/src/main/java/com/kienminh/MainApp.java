package com.kienminh;

import com.kienminh.util.SceneUtil;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneUtil.setStage(stage);
        SceneUtil.switchScene("login.fxml");
        stage.setTitle("Tetris Game");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
