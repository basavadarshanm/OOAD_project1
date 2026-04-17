package com.onlinebanking;

import com.onlinebanking.config.ApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the JavaFX desktop app.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ApplicationContext context = ApplicationContext.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(context.getControllerFactory());
        Scene scene = new Scene(loader.load(), 480, 320);
        stage.setTitle("Online Banking");
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(300);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.toFront();
        stage.setAlwaysOnTop(true); // force window to surface on launch
        stage.requestFocus();
        stage.show();
        stage.setAlwaysOnTop(false); // return to normal stacking
    }

    @Override
    public void stop() throws Exception {
        ApplicationContext.getInstance().shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
