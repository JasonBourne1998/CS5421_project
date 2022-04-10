package graphical.editor;

import graphical.editor.LinAlgTree.MainTree;
import graphical.editor.LinAlgTree.Parser;
import graphical.editor.LinAlgTree.UnaryOperation;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.Optional;

public class LayerBox extends HBox {

    private MainTree tree;
    private int level;

    public LayerBox(MainTree tree, int level) {
        super();
        setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        this.tree = tree;
        this.level = level;
        setAlignment(Pos.CENTER);
        init();
    }

    private void init() {
        setOnDragOver(event -> {
            if (event.getSource() != this && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getSource() != this && event.getDragboard().hasString()) {
                setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
            }
            event.consume();
        });

        setOnDragExited(event -> {
            setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            event.consume();
        });

        setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = true;
            if (dragboard.hasString()) {
                String symbol = dragboard.getString();
                switch (symbol) {
                case Operator.SIGMA_SYMBOL:
                    sigmaEvent();
                    break;
                case Operator.PI_SYMBOL:
                    piEvent();
                    break;
                case Operator.RHO_SYMBOL:
                    rhoEvent();
                    break;
                case Operator.TAU_SYMBOL:
                    tauEvent();
                    break;
                case Operator.DELTA_SYMBOL:
                    deltaEvent();
                    break;
                case Operator.GAMMA_SYMBOL:
                    gammaEvent();
                    break;
                case Operator.CROSS_SYMBOL:
                    crossEvent();
                    break;
                case Operator.DIFF_SYMBOL:
                    diffEvent();
                    break;
                case Operator.UNION_SYMBOL:
                    unionEvent();
                    break;
                case Operator.INTERSECT_SYMBOL:
                    intersectEvent();
                    break;
                case Operator.INNER_JOIN_SYMBOL:
                    innerJoinEvent();
                    break;
                default:
                    success = false;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    protected void sigmaEvent() {
        TextInputDialog inputDialog = new TextInputDialog("e.g. attr1 [=|<=|>=|<|>|<>|IS_(NOT_)NULL] [attr2|val]");
        inputDialog.getDialogPane().setHeaderText("Enter selection condition(s), joined by '&' (and) or '|' (or)");
        inputDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Optional<String> input = inputDialog.showAndWait();
        if (input.isPresent()) {
            String[] params = input.get().split("\\w*[&|]\\w*");
            try {
                Parser.parsePredicates(params);
            } catch (InputException e) {
                MainApp.showErrorDialog(e.getMessage());
            }
            UnaryOperation sigma = MainTree.createSigmaOperation(input.get(), params);
            try {
                tree.addParent(sigma, level);
            } catch (Exception e) {
                MainApp.showErrorDialog(e.getMessage());
                return;
            }
            addNewNode();
        }
    }

    protected boolean piEvent() {
        TextInputDialog inputDialog = new TextInputDialog("e.g. attr[,attr2]");
        inputDialog.getDialogPane().setHeaderText("Attributes to project to\n"
                + "(must be scalar--can include arithmetic operations or aggregate functions)");
        Optional<String> input = inputDialog.showAndWait();
        if (input.isPresent()) {
            String[] params = input.get().split(",");
            UnaryOperation pi = MainTree.createPiOperation(input.get(), params);
            try {
                tree.addParent(pi, level);
            } catch (Exception e) {
                MainApp.showErrorDialog(e.getMessage());
                return false;
            }
        } else {
            return false;
        }

        // prompt user to rename the relation, otherwise abort and rollback
        VBox parent = (VBox) getParent();
        ObservableList<Node> children = parent.getChildren();
        boolean isAddNewLayer = false;
        if (children.size() <= level + 1) {
            LayerBox newLayer = new LayerBox(tree, level + 1);
            children.add(newLayer);
            newLayer.toBack();
            isAddNewLayer = true;
        }
        int precedingIndex = children.size() - level - 2;
        LayerBox precedingLayer = (LayerBox) children.get(precedingIndex);
        if (!precedingLayer.rhoEvent()) {
            if (isAddNewLayer) {
                children.remove(0);
            }
            try {
                tree.removeParent(level);
            } catch (Exception e) {
                MainApp.showErrorDialog(e.getMessage());
                return false;
            }
            return false;

        } else {
            addNewNode();
            return true;
        }
    }

    protected boolean rhoEvent() {
        TextInputDialog inputDialog = new TextInputDialog("e.g. new_tbl_name[:attr,attr2,...]");
        inputDialog.setHeaderText("Input the new table name, and optionally the new names of all the columns (must be distinct)");
        Optional<String> input = inputDialog.showAndWait();
        if (input.isPresent()){
            String str = input.get();
            boolean onlyTable = true;
            String[] tableAndAttributes = str.split(":");
            if (str.contains(":")) {
                String attributes = tableAndAttributes[1];
                if (!attributes.isBlank()) {
                    onlyTable = false;
                }
            }
            String arg = tableAndAttributes[0];
            String[] args = new String[]{arg};
            if (!onlyTable) {
                args = Arrays.stream(tableAndAttributes[1].split(","))
                        .map(String::strip)
                        .map(x -> String.format("%s.%s", tableAndAttributes[0], x))
                        .toArray(String[]::new);
                arg = String.join(",", args);
            }
            UnaryOperation rho = MainTree.createSigmaOperation(arg, args);
            try {
                tree.addParent(rho, level);
            } catch (Exception e) {
                MainApp.showErrorDialog(e.getMessage());
                return false;
            }
            addNewNode();
            return true;
        }
        return false;
    }

    protected void tauEvent() {
        return;
    }

    protected void deltaEvent() {
        return;
    }

    protected void gammaEvent() {
        return;
    }

    protected void crossEvent() {
        return;
    }

    protected void diffEvent() {
        return;
    }

    protected void unionEvent() {
        return;
    }

    protected void intersectEvent() {
        return;
    }

    protected void innerJoinEvent() {
        return;
    }

    protected String getLastChildString() {
        int index = getParent().getChildrenUnmodifiable().size() - 1;
        return tree.getNode(level, index).toGraphString();
    }

    private void addNewNode() {
        String node = getLastChildString();
        Text text = new Text();
        text.setText(node);
        StackPane newNode = new StackPane();
        newNode.getChildren().add(text);
        getChildren().add(newNode);
    }

    public void incrementLayer() {
        level++;
    }

    public void decrementLayer() {
        level--;
    }
}
