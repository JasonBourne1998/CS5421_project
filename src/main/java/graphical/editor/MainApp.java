package graphical.editor;

import graphical.editor.LinAlgTree.MainTree;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    private MainTree mainTree;
    private FXMLLoader fxmlLoader;
    private URL location;

    @FXML
    private VBox graphArea;

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem newButton;

    @FXML
    private MenuItem sqlButton;

    @FXML
    private MenuItem closeButton;

    @FXML
    private MenuItem addLayerButton;

    @FXML
    private MenuItem addBaseLayerButton;

    @FXML
    private MenuItem deleteLayerButton;

    @FXML
    private MenuItem deleteBaseLayerButton;

    @FXML
    private AnchorPane operatorsContainer;

    @FXML
    private VBox feedbackBox;

    @FXML
    private Text sqlText;

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
        OperatorsToolbar operatorsToolbar = new OperatorsToolbar();
        operatorsToolbar.init();
        operatorsContainer.getChildren().add(operatorsToolbar);

        newButton = menuBar.getMenus().get(0).getItems().get(0);
        sqlButton = menuBar.getMenus().get(0).getItems().get(1);
        closeButton = menuBar.getMenus().get(0).getItems().get(2);

        addLayerButton = menuBar.getMenus().get(1).getItems().get(0);
        addBaseLayerButton = menuBar.getMenus().get(1).getItems().get(1);
        deleteLayerButton = menuBar.getMenus().get(1).getItems().get(2);
        deleteBaseLayerButton = menuBar.getMenus().get(1).getItems().get(3);

        sqlText = (Text) feedbackBox.getChildren().get(1);

        initNewButton();
        initSqlButton();
        initCloseButton(stage);
        initAddLayerButton();
        initAddBaseLayerButton();
        graphArea.setBackground(new Background(new BackgroundFill(Color.AZURE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void initNewButton() {
        newButton.setOnAction(event -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContentText("You are about to reset the environment. Are you sure?");
            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    mainTree = new MainTree();
                    graphArea = new VBox();
                    sqlText.setText("");
                    dialog.close();
                    event.consume();
                } else if (response == ButtonType.CANCEL) {
                    dialog.close();
                    event.consume();
                }
            });
        });
    }

    private void initSqlButton() {
        sqlButton.setOnAction(event -> {
            try {
                sqlText.setText(mainTree.convertToString());
            } catch (Exception e) {
                showErrorDialog(e.getMessage());
            }
        });
    }

    private void initCloseButton(Stage stage) {
        closeButton.setOnAction(event -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.CLOSE);
            dialog.getDialogPane().setContentText("Are you sure you want to close the application?");
            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    dialog.close();
                    stage.hide();
                    event.consume();
                } else if (response == ButtonType.CLOSE) {
                    dialog.close();
                    event.consume();
                }
            });
        });
    }

    private void initAddLayerButton() {
        addLayerButton.setOnAction(event -> {
            boolean success = addLayer();
            if (success) {
                sqlText.setText("Layer added successfully.");
            }
            event.consume();
        });
    }

    private boolean addLayer() {
        try {
            mainTree.addLayer();
            LayerBox newLayer = new LayerBox(mainTree, graphArea.getChildrenUnmodifiable().size());
            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(newLayer);
            graphArea.getChildren().add(stackPane);
            stackPane.toBack();
            stackPane.getChildren().add(new Text("Drag here"));
        } catch (Exception e) {
            showErrorDialog(e.getMessage());
            return false;
        }
        return true;
    }

    private void initAddBaseLayerButton() {
        addBaseLayerButton.setOnAction(event -> {
            boolean success = addBaseLayer();
            if (success) {
                sqlText.setText("Base layer added successfully.");
            }
            event.consume();
        });
    }

    private boolean addBaseLayer() {
        try {
            mainTree.addFirstLayer();
            LayerBox newLayer = new LayerBox(mainTree, 0);
            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(newLayer);
            graphArea.getChildren().add(stackPane);
            stackPane.getChildren().add(new Text("Drag here"));
        } catch (Exception e) {
            showErrorDialog(e.getMessage());
            return false;
        }
        return true;
    }

    public static void showErrorDialog(String errorMessage) {
        Dialog<ButtonType> errorDialog = new Dialog<>();
        errorDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        errorDialog.setContentText(errorMessage);
        errorDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.CLOSE) {
                errorDialog.close();
            }
        });
    }

    public static void main(String[] args) {
    }
}
