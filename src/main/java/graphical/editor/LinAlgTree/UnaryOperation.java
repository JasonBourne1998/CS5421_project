package graphical.editor.LinAlgTree;

import graphical.editor.Operator;

public class UnaryOperation implements Node {
    private Node operand;
    private final Operator operator;
    private final String[] params;
    private String[] attributes;

    public UnaryOperation(Operator op, String[] args) {
        this.operator = op;
        params = args;
    }

    public UnaryOperation(Operator op, Node node, String[] args) throws Exception {
        this.operator = op;
        operand = node.evaluate();
        params = args;
    }

    public Node evaluate() throws Exception {
        switch (operator) {
        case IDENTITY:
            return operand;
        case PI:
            return project(operand, params);
        case RHO:
            return rename(operand, params);
        //TODO case SIGMA, case TAU, case GAMMA, case DELTA
        }
        throw new Exception("Invalid operator.");
    }

    private Node project(Node operand, String[] params) throws Exception {
        String[] relAttrs = operand.getAttributes();
        boolean isPresent;
        for (String attr : params) {
            isPresent = false;
            for (String attr2 : relAttrs) {
                if (attr.compareToIgnoreCase(attr2) == 0) {
                    isPresent = true;
                    break;
                }
            }
            if (!isPresent) {
                throw new Exception("Projected attribute(s) not present in operand relation.");
            }
        }
        operand.setAttributes(params);
        return operand;
    }

    private Node rename(Node operand, String[] params) throws Exception {
        String[] relAttrs = operand.getAttributes();
        if (relAttrs.length != params.length) {
            throw new Exception("Number of attributes to rename does not match original number of attributes.");
        }
        operand.setAttributes(params);
        return operand;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] newAttrs) {
        attributes = newAttrs;
    }

    @Override
    public String toString() {
        if (operator == Operator.IDENTITY) {
            return String.join(",", attributes);
        }
        if (params.length > 0) {
            return String.format("%s_%s(%s)", operator.toString(), String.join(",", params), operand.toString());
        }
        return String.format("%s(%s)", operator.toString(), operand.toString());
    }
}
