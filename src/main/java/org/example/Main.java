package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        int width = 600;
        int height = 500;
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/main.fxml")));
        stage.getIcons().add(new Image("/icon_apk.png"));
//        stage.setAlwaysOnTop(true);//一直悬浮
        stage.setResizable(true);//不准拖动改变大小
        stage.setTitle("Android12 签名工具v1.0");
        stage.setScene(new Scene(root, width, height));
        Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
        stage.setX(rectangle2D.getWidth() / 2.0 - (width / 2.0));
        stage.setY(rectangle2D.getHeight() / 2.0 - (height / 2.0));
        stage.show();
    }
}
