package graphical.editor.LinAlgTree;

/***
 * Interface that represents a node in the tree.
 */
public interface Node {
    Node evaluate() throws Exception;
    String[] getAttributes();
    void setAttributes(String[] attributes);
}
