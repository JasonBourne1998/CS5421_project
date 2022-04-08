package graphical.editor;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class AppMenu extends MenuBar {

    private final Menu file;
    private final Menu edit;
    private final Menu help;
    private MenuItem reset;
    private MenuItem generateSql;
    private MenuItem closeApp;
    private MenuItem addLayer;
    private MenuItem addBaseLayer;
    private MenuItem deleteLayer;
    private MenuItem deleteBaseLayer;
    private MenuItem about;

    public AppMenu() {
        file = new Menu("File");
        reset = new MenuItem("New");
        generateSql = new MenuItem("Generate SQL Query");
        closeApp = new MenuItem("Close");
        file.getItems().addAll(reset, generateSql, closeApp);

        edit = new Menu("Edit");
        addLayer = new MenuItem("Add Layer");
        addBaseLayer = new MenuItem("Add Base Layer");
        deleteLayer = new MenuItem("Delete Layer");
        deleteBaseLayer = new MenuItem("Delete Base Layer");
        edit.getItems().addAll(addLayer, addBaseLayer, deleteLayer, deleteBaseLayer);

        help = new Menu("Help");
        about = new MenuItem("About");
        help.getItems().add(about);

        getMenus().addAll(file, edit, help);
    }
}
