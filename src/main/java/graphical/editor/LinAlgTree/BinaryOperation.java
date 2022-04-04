package graphical.editor.LinAlgTree;

import graphical.editor.InputException;
import graphical.editor.Operator;

import java.util.Arrays;
import java.util.stream.Stream;

public class BinaryOperation implements Node {
    private Node leftOperand;
    private Node rightOperand;
    private final Operator operator;
    private final String[] params;
    private String[] attributes;

    public BinaryOperation(Operator op, String[] args) {
        operator = op;
        params = args;
    }

    public BinaryOperation(Operator op, Node leftNode, Node rightNode, String[] args) throws Exception {
        leftOperand = leftNode;
        rightOperand = rightNode;
        operator = op;
        params = args;
        evaluate();
    }

    public void evaluate() throws Exception {
        switch (operator) {
        case CROSS:
            cross(leftOperand, rightOperand);
            break;
        case INNER_JOIN:
            innerJoin(leftOperand, rightOperand, params);
            break;
        case UNION:
            // fallthrough
        case INTERSECTION:
            // fallthrough
        case DIFFERENCE:
            checkMatchingAttributes(leftOperand, rightOperand);
            break;
        }
        throw new Exception("Invalid operator.");
    }

    private void cross(Node leftNode, Node rightNode) {
        String[] leftAttributes = leftNode.getAttributes();
        String[] rightAttributes = rightNode.getAttributes();
        int combinedLength = leftAttributes.length + rightAttributes.length;
        String[] combinedAttributes = Arrays.copyOf(leftAttributes, combinedLength);
        System.arraycopy(rightAttributes, 0, combinedAttributes, leftAttributes.length, rightAttributes.length);
        setAttributes(combinedAttributes);
    }

    private void innerJoin(Node leftNode, Node rightNode, String[] args) throws Exception {
        String[][] joinPredicates = Parser.parsePredicates(args);
        String[] leftAttributes = leftNode.getAttributes();
        String[] rightAttributes = rightNode.getAttributes();
        for (String[] p : joinPredicates) {
            assert(p.length == 3);
            if (Arrays.stream(leftAttributes).noneMatch(x -> x.equalsIgnoreCase(p[0]))) {
                throw new Exception(String.format("Attribute %s is not present in left relation.", p[0]));
            }
            if (Arrays.stream(rightAttributes).noneMatch(y -> y.equalsIgnoreCase(p[1]))) {
                throw new Exception(String.format("Attribute %s is not present in right relation.", p[2]));
            }
        }
        String[] filterAttributes = Arrays.stream(joinPredicates)
                .map(p -> p[1])
                .toArray(String[]::new);
        String[] combinedAttributes = Stream.concat(Arrays.stream(leftAttributes), Arrays.stream(rightAttributes))
                .filter(attr -> Arrays.stream(filterAttributes)
                        .noneMatch(f -> f.equalsIgnoreCase(attr)))
                .toArray(String[]::new);
        setAttributes(combinedAttributes);
    }

    private void checkMatchingAttributes(Node leftNode, Node rightNode) throws Exception {
        String[] leftAttributes = leftNode.getAttributes();
        String[] rightAttributes = rightNode.getAttributes();
        int l = leftAttributes.length;
        int r = rightAttributes.length;
        if (l != r) {
            throw new Exception("Unable to UNION: Number of attributes of each relation does not match.");
        }
        //TODO enforce check of matching column types
    }

    public int getNumDescendants() {
        return 2 + leftOperand.getNumDescendants() + rightOperand.getNumDescendants();
    }

    public Operator getOperator() {
        return operator;
    }

    public String[] getParams() {
        return params;
    }

    public String[] getAttributes() {
        return attributes;
    }

    private void setAttributes(String[] attrs) {
        attributes = attrs;
    }

    public Node locateGroupBy() {
        Node groupBy = leftOperand.locateGroupBy();
        if (groupBy == null) {
            groupBy = rightOperand.locateGroupBy();
        }
        return groupBy;
    }

    @Override
    public String toString() {
        if (params.length > 0) {
            return String.format("%s#%s#%s#%s)", operator.toString(),
                    String.join("\u028C", params), // caret symbol (^)--assume all conditions are conjunctive
                    leftOperand.toString(), rightOperand.toString());
        }
        return String.format("%s#%s#%s)", operator.toString(), leftOperand.toString(), rightOperand.toString());
    }
}
