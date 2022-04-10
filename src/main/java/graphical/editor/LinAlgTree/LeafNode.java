package graphical.editor.LinAlgTree;

import graphical.editor.DuplicateColumnException;
import graphical.editor.Operator;

import java.util.Arrays;

public class LeafNode implements Node {
    private String[] attributes;
    private final String name;

    public LeafNode(String relation) throws DuplicateColumnException {
        String[] nameAndAttributes = relation.strip().split("\\)")[0].split("\\(");
        String tableName = nameAndAttributes[0];
        attributes = nameAndAttributes[1].split(",");
        if (Arrays.stream(attributes).distinct().count() != attributes.length) {
            throw new DuplicateColumnException();
        }
        attributes = Arrays.stream(attributes)
                .map(attr -> String.format("%s.%s", tableName, attr))
                .toArray(String[]::new);
        name = relation;
    }

    public void evaluate() {
        // nothing to evaluate
    }

    public int getNumDescendants() {
        return 0;
    }

    public Node locateGroupBy() {
        return null;
    }

    public Operator getOperator() {
        return Operator.IDENTITY;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public String[] getParams() {
        return new String[0];
    }

    public String getParamString() {
        return name;
    }

    public String toGraphString() {
        return name;
    }

    @Override
    public String toString() {
        return String.join(",", attributes);
    }
}
