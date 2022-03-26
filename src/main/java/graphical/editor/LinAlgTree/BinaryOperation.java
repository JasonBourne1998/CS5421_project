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
        leftOperand = leftNode.evaluate();
        rightOperand = rightNode.evaluate();
        operator = op;
        params = args;
    }

    public Node evaluate() throws Exception {
        switch (operator) {
        case CROSS:
            return cross(leftOperand, rightOperand);
        case INNER_JOIN:
            return innerJoin(leftOperand, rightOperand, params);
        case UNION:
            // fallthrough
        case INTERSECTION:
            // fallthrough
        case DIFFERENCE:
            return checkMatchingAttributes(leftOperand, rightOperand);
        }
        return null;
    }

    private Node cross(Node leftNode, Node rightNode) {
        String[] leftAttributes = leftNode.getAttributes();
        String[] rightAttributes = rightNode.getAttributes();
        int combinedLength = leftAttributes.length + rightAttributes.length;
        String[] combinedAttributes = Arrays.copyOf(leftAttributes, combinedLength);
        System.arraycopy(rightAttributes, 0, combinedAttributes, leftAttributes.length, rightAttributes.length);
        leftNode.setAttributes(combinedAttributes);
        return leftNode;
    }

    private Node innerJoin(Node leftNode, Node rightNode, String[] args) throws Exception {
        String[][] joinPredicates = parsePredicates(args);
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
        leftNode.setAttributes(combinedAttributes);
        return leftNode;
    }

    private Node checkMatchingAttributes(Node leftNode, Node rightNode) throws Exception {
        String[] leftAttributes = leftNode.getAttributes();
        String[] rightAttributes = rightNode.getAttributes();
        int l = leftAttributes.length;
        int r = rightAttributes.length;
        if (l != r) {
            throw new Exception("Unable to UNION: Number of attributes of each relation does not match.");
        }
        //TODO enforce check of matching column types
        return leftNode;
    }

    private String[][] parsePredicates(String[] args) throws InputException {
        String[][] output = Arrays.stream(args)
                .map(this::parsePredicate)
                .toArray(String[][]::new);

        return output;
    }

    private String[] parsePredicate(String arg) throws InputException {
        // split parameters by whitespace
        String[] predicate = arg.split("[ \t\r\n]+");
        if (predicate.length != 3) {
            throw new InputException(String.format("Unable to parse join attributes %s", arg));
        }
        return predicate;

    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attrs) {
        attributes = attrs;
    }

    @Override
    public String toString() {
        if (params.length > 0) {
            return String.format("(%s %s_{%s} %s)", leftOperand.toString(), operator.toString(),
                    String.join("\u028C", params), // caret symbol (^)--assume all conditions are conjunctive
                    rightOperand.toString());
        }
        return String.format("(%s %s %s)", leftOperand.toString(), operator.toString(), rightOperand.toString());
    }
}
