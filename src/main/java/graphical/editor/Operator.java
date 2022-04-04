package graphical.editor;

public enum Operator {
    IDENTITY(""),
    SIGMA("sigma"), // lowercase sigma
    PI("pi"), // lowercase pi
    RHO("rho"), // lowercase rho
    TAU("tau"), // lowercase tau
    DELTA("delta"), // lowercase delta
    GAMMA("gamma"), // lowercase gamma
    CROSS("cross"), // fullwidth capital X
    DIFFERENCE("diff"), // em dash
    UNION("union"), // union symbol
    INTERSECTION("intersection"), // intersection symbol
    INNER_JOIN("innerjoin"); // large bowtie

    private final String label;

    Operator(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
