package graphical.editor.LinAlgTree;

import graphical.editor.DuplicateColumnException;
import graphical.editor.Operator;

import java.util.Arrays;
import java.util.ArrayList;

public class UnaryOperation implements Node {
    private Node operand;
    private final Operator operator;
    private final String[] params;
    private final String paramString;
    private String[] attributes;

    public UnaryOperation(Operator op, String argString, String[] args) {
        operator = op;
        params = args;
        paramString = argString;
    }

    public UnaryOperation(Operator op, Node node, String argString, String[] args) throws Exception {
        operator = op;
        operand = node;
        params = args;
        paramString = argString;
        evaluate();
    }

    public void evaluate() throws Exception {
        switch (operator) {
        case PI:
            project(operand, params);
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
            selectWhere(operand, params);
            break;
        case TAU:
            orderBy(operand, params);
            break;
        case GAMMA:
            groupBy(operand, params);
            break;
        default:
            throw new Exception("Invalid operator.");
        }
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

    private void selectWhere(Node operand, String[] params) throws Exception {
        String[] leftHandSide = Parser.parsePredicates(params).stream()
                .map(x -> x.get(0))
                .toArray(String[]::new);
        if (!checkAttributesPresent(operand, leftHandSide)) {
            throw new Exception("Attributes not present in the schema.");
        }
        attributes = params;
    }

    private void groupBy(Node operand, String[] params) throws Exception {
        if (!checkAttributesPresent(operand, params)) {
            throw new Exception("Attributes not present in the schema.");
        }

        // Loop through params, find following 3 kinds of attributes, split them
        ArrayList<String> groupBy = new ArrayList<String>();
        ArrayList<String> select = new ArrayList<String>();
        ArrayList<String> having = new ArrayList<String>();

        // Check for HAVING first, then SELECT, because aggregate functions can both be in HAVING and SELECT, but only HAVING params have condition
        for(String i : params){
            if (i.contains("<") || i.contains(">") || i.contains("=")){
                having.add(i);
            } else if (Parser.detectAggregateFunctions(i)) { //TODO case SELECT without aggregate functions
                select.add(i);
            } else {
                groupBy.add(i);
            }
        }

        // Add separator, expected output format: GAMMA # groupBy @ select @ having
        groupBy.add("@");
        select.add("@");
        ArrayList<String> combined = new ArrayList<String>();
        combined.addAll(groupBy);
        combined.addAll(select);
        combined.addAll(having);

        // convert to attributes array
        String[] newAttributes = combined.toArray(new String[combined.size()]);
        setAttributes(newAttributes);
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

    public String getParamString() {
        return paramString;
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

    @Override
    public String toGraphString() {
        if (params.length > 0) {
            return String.format("%s\n%s", operator.toSymbol(), String.join(",", params));
        }
        return operator.toSymbol();
    }
}
