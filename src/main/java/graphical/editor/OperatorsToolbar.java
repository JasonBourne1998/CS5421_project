package graphical.editor;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class OperatorsToolbar extends VBox {

    private FXMLLoader fxmlLoader;
    private URL location;

    @FXML
    Button sigmaButton;

    @FXML
    Button piButton;

    @FXML
    Button rhoButton;

    @FXML
    Button tauButton;

    @FXML
    Button deltaButton;

    @FXML
    Button gammaButton;

    @FXML
    Button crossButton;

    @FXML
    Button diffButton;

    @FXML
    Button unionButton;

    @FXML
    Button intersectButton;

    @FXML
    Button innerjoinButton;

    public void init() {
        fxmlLoader = new FXMLLoader();
        location = OperatorsToolbar.class.getResource("/view/operators_toolbar.fxml");
        fxmlLoader.setLocation(location);
        fxmlLoader.setController(null);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Label operatorsToolbarLabel = (Label) getChildren().get(0);
        sigmaButton = (Button) ((StackPane) getChildren().get(1)).getChildren().get(0);
        piButton = (Button) ((StackPane) getChildren().get(2)).getChildren().get(0);
        rhoButton = (Button) ((StackPane) getChildren().get(3)).getChildren().get(0);
        tauButton = (Button) ((StackPane) getChildren().get(4)).getChildren().get(0);
        deltaButton = (Button) ((StackPane) getChildren().get(5)).getChildren().get(0);
        gammaButton = (Button) ((StackPane) getChildren().get(6)).getChildren().get(0);
        crossButton = (Button) ((StackPane) getChildren().get(7)).getChildren().get(0);
        diffButton = (Button) ((StackPane) getChildren().get(8)).getChildren().get(0);
        unionButton = (Button) ((StackPane) getChildren().get(9)).getChildren().get(0);
        intersectButton = (Button) ((StackPane) getChildren().get(10)).getChildren().get(0);
        innerjoinButton = (Button) ((StackPane) getChildren().get(11)).getChildren().get(0);
        prepare();
    }

    private void addStackPanes() {
        StackPane sigmaBox = new StackPane();
        StackPane piBox = new StackPane();
        StackPane rhoBox = new StackPane();
        StackPane tauBox = new StackPane();
        StackPane deltaBox = new StackPane();
        StackPane gammaBox = new StackPane();
        StackPane crossBox = new StackPane();
        StackPane diffBox = new StackPane();
        StackPane unionBox = new StackPane();
        StackPane intersectBox = new StackPane();
        StackPane innerjoinBox = new StackPane();
        sigmaBox.getChildren().add(sigmaButton);
        piBox.getChildren().add(piButton);
        rhoBox.getChildren().add(rhoButton);
        tauBox.getChildren().add(tauButton);
        deltaBox.getChildren().add(deltaButton);
        gammaBox.getChildren().add(gammaButton);
        crossBox.getChildren().add(crossButton);
        diffBox.getChildren().add(diffButton);
        unionBox.getChildren().add(unionButton);
        intersectBox.getChildren().add(intersectButton);
        innerjoinBox.getChildren().add(innerjoinButton);
        getChildren().addAll(sigmaBox, piBox, rhoBox, tauBox, deltaBox, gammaBox,
                crossBox, diffBox, unionBox, intersectBox, innerjoinBox);
    }

    private void createTooltips() {
        sigmaButton.setTooltip(new Tooltip("selection"));
        piButton.setTooltip(new Tooltip("projection"));
        rhoButton.setTooltip(new Tooltip("renaming"));
        tauButton.setTooltip(new Tooltip("order by"));
        deltaButton.setTooltip(new Tooltip("duplicate elimination (distinct)"));
        gammaButton.setTooltip(new Tooltip("aggregation (group by)"));
        crossButton.setTooltip(new Tooltip("cross join"));
        diffButton.setTooltip(new Tooltip("set difference"));
        unionButton.setTooltip(new Tooltip("union"));
        intersectButton.setTooltip(new Tooltip("intersection"));
        innerjoinButton.setTooltip(new Tooltip("inner join"));
    }

    public void prepare() {
        initButton(sigmaButton, Operator.SIGMA);
        initButton(piButton, Operator.PI);
        initButton(rhoButton, Operator.RHO);
        initButton(tauButton, Operator.TAU);
        initButton(deltaButton, Operator.DELTA);
        initButton(gammaButton, Operator.GAMMA);
        initButton(crossButton, Operator.CROSS);
        initButton(diffButton, Operator.DIFFERENCE);
        initButton(unionButton, Operator.UNION);
        initButton(intersectButton, Operator.INTERSECTION);
        initButton(innerjoinButton, Operator.INNER_JOIN);

    }

    private void initButton(Button button, Operator op) {
        button.setText(op.toSymbol());
        button.setOnDragDetected(event -> {
            Dragboard dragboard = button.startDragAndDrop(TransferMode.COPY);
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(op.toSymbol());
            dragboard.setContent(clipboardContent);

            event.consume();
        });
        button.setOnDragDone(Event::consume);
    }
}
