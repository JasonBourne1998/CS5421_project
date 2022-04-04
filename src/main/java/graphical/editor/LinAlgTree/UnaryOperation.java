package graphical.editor.LinAlgTree;

import graphical.editor.DuplicateColumnException;
import graphical.editor.Operator;

import java.util.Arrays;

public class UnaryOperation implements Node {
    private Node operand;
    private final Operator operator;
    private final String[] params;
    private String[] attributes;

    public UnaryOperation(Operator op, String[] args) {
        operator = op;
        params = args;
    }

    public UnaryOperation(Operator op, Node node, String[] args) throws Exception {
        operator = op;
        operand = node;
        params = args;
        evaluate();
    }

    public void evaluate() throws Exception {
        switch (operator) {
        case PI:
            if (Parser.detectAggregateFunctions(params)) {
                Node groupBy = locateGroupBy();
            } else {
                project(operand, params);
            }
            break;
        case RHO:
            if (params.length == 1 && params[0].matches("[A-Za-z0-9_]+")) {
                renameTable(operand, params[0]);
            }
            renameEach(operand, params);
            break;
        case DELTA:
            duplicateElimination(operand);
            break;
        case SIGMA:
            if (Parser.detectAggregateFunctions(params)) {
                Node groupBy = locateGroupBy();
                //TODO HAVING clause
            } else {
                //TODO WHERE clause
            }
            break;
        case TAU:
            orderBy(operand, params);
            break;
        //TODO case SIGMA, case GAMMA
        }
        throw new Exception("Invalid operator.");
    }


    private boolean checkAttributesPresent(Node operand, String[] params) {
        String[] relAttrs = operand.getAttributes();
        return Arrays.stream(params)
                .allMatch(param -> Arrays.stream(relAttrs).anyMatch(relAttr -> relAttr.equalsIgnoreCase(param)));
    }

    private void project(Node operand, String[] params) throws Exception {
        if (!checkAttributesPresent(operand, params)) {
            throw new Exception("Attributes not present in the schema.");
        }
        attributes = params;
    }

    private void orderBy(Node operand, String[] params) throws Exception {
        if (!checkAttributesPresent(operand, params)) {
            throw new Exception("Attributes not present in the schema.");
        }
    }

    private void renameTable(Node operand, String tableName) throws DuplicateColumnException {
        String[] originalAttributes = operand.getAttributes();
        String[] newAttributes = Arrays.stream(originalAttributes)
                .map(attr -> String.format("%s.%s", tableName, attr.split("\\.")[1]))
                .distinct()
                .toArray(String[]::new);
        if (originalAttributes.length > newAttributes.length) {
            throw new DuplicateColumnException();
        }
        setAttributes(newAttributes);
    }

    private void renameEach(Node operand, String[] params) throws Exception {
        String[] relAttrs = operand.getAttributes();
        if (relAttrs.length != params.length) {
            throw new Exception("Number of attributes to rename does not match original number of attributes.");
        }
        setAttributes(params);
    }

    private void duplicateElimination(Node operand) throws Exception {
        if (operand.getOperator() != Operator.PI) {
            throw new Exception("Can only eliminate duplicates after projection.");
        }
    }

    public int getNumDescendants() {
        return 1 + operand.getNumDescendants();
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

    private void setAttributes(String[] newAttrs) {
        attributes = newAttrs;
    }

    //TODO enforce stricter checks on GROUP BY i.e. not part of a separate subquery, satisfying grouping condition
    public Node locateGroupBy() {
        if (operator.equals(Operator.GAMMA)) {
            return this;
        }
        return operand.locateGroupBy();
    }

    @Override
    public String toString() {
        if (params.length > 0) {
            return String.format("%s#%s#%s", operator.toString(), String.join(",", params), operand.toString());
        }
        return String.format("%s#%s", operator.toString(), operand.toString());
    }
}
