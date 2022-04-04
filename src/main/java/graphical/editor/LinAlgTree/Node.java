package graphical.editor.LinAlgTree;

import graphical.editor.Operator;

/***
 * Interface that represents a node in the tree.
 */
public interface Node {
    void evaluate() throws Exception;
    int getNumDescendants();
    Operator getOperator();
    String[] getAttributes();
    String[] getParams();
    Node locateGroupBy();
}
