package graphical.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    public void start(Stage stage) {
        VBox mainWindow = new VBox();
        Scene scene = new Scene(mainWindow);
        stage.setScene(scene);
        stage.setTitle("Graphical Editor App");
        stage.show();
    }

    public static void main(String[] args) {
    }
}
