package graphical.editor;

public enum Operator {
    IDENTITY(""),
    SIGMA("\u03C3"), // lowercase sigma
    PI("\u03C0"), // lowercase pi
    RHO("\u03C1"), // lowercase rho
    TAU("\u03C4"), // lowercase tau
    DELTA("\u03B4"), // lowercase delta
    GAMMA("\u03B3"), // lowercase gamma
    CROSS("\uFF38"), // fullwidth capital X
    DIFFERENCE("\u2014"), // em dash
    UNION("\u222A"), // union symbol
    INTERSECTION("\u2229"), // intersection symbol
    INNER_JOIN("\u2A1D"); // large bowtie

    private final String label;

    Operator(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
