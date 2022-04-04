package graphical.editor;

public enum ConditionToken {
    NE("<>"),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    EQ("="),
    AND("&"),
    OR("|"),
    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");
    //TODO add IN, NOT IN, EXISTS, NOT EXISTS

    private final String label;

    ConditionToken(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
