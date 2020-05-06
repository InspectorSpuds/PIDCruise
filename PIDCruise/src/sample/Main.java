package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.FXMLControllers.GraphScene;

import java.io.IOException;

public class Main extends Application {
    static Sub subway;
    FXMLLoader loader = new FXMLLoader();
    public Main() throws IOException {
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent mainPage = loader.load(getClass().getResourceAsStream("FXML/GraphScene.fxml"));
        GraphScene scene  = (GraphScene)loader.getController();
        scene.setListeners(primaryStage);
        primaryStage.setScene(new Scene(mainPage, 600, 600));
        primaryStage.setTitle("Car PID Simulator");
        primaryStage.show();
    }
}
class Sub {
    static Stage primary;
    public Sub(Stage primary){
        Sub.primary = primary;
    }
}