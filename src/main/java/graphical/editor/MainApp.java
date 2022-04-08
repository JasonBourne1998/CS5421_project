package graphical.editor;

import graphical.editor.LinAlgTree.MainTree;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    private MainTree mainTree;
    private FXMLLoader fxmlLoader;
    private URL location;

    @FXML
    private AnchorPane mainWindow;

    @FXML
    private SplitPane splitWindow;

    @FXML
    private AnchorPane topFrame;

    @FXML
    private StackPane menu;

    @Override
    public void init() {
        mainTree = new MainTree();
        fxmlLoader = new FXMLLoader();
        location = MainApp.class.getResource("/view/graphical_editor_v0.1.fxml");
    }

    public void start(Stage stage) {
        fxmlLoader.setLocation(location);
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(stage);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        stage.show();

        mainWindow = new AnchorPane();
        splitWindow = new SplitPane();
        topFrame = new AnchorPane();
        topFrame.getChildren().add(new AppMenu());

        topFrame.setVisible(true);

        //Scene scene = new Scene(mainWindow);
        //stage.setScene(scene);
        //stage.setTitle("Graphical Editor App");
    }

    public static void main(String[] args) {
    }
}
