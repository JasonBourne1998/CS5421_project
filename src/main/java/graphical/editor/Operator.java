package graphical.editor;

public enum Operator {
    IDENTITY("", ""),
    SIGMA("sigma", "\u03C3"), // lowercase sigma
    PI("pi", "\u03C0"), // lowercase pi
    RHO("rho", "\u03C1"), // lowercase rho
    TAU("tau", "\u03C4"), // lowercase tau
    DELTA("delta", "\u03B4"), // lowercase delta
    GAMMA("gamma", "\u03B3"), // lowercase gamma
    CROSS("cross", "\uFF38"), // fullwidth capital X
    DIFFERENCE("diff", "\u2014"), // em dash
    UNION("union", "\u222A"), // union symbol
    INTERSECTION("intersection", "\u2229"), // intersection symbol
    INNER_JOIN("innerjoin", "\u2A1D"); // large bowtie

    public static final String SIGMA_SYMBOL = "\u03C3";
    public static final String PI_SYMBOL = "\u03C0";
    public static final String RHO_SYMBOL = "\u03C1";
    public static final String TAU_SYMBOL = "\u03C4";
    public static final String DELTA_SYMBOL = "\u03B4";
    public static final String GAMMA_SYMBOL = "\u03B3";
    public static final String CROSS_SYMBOL = "\uFF38";
    public static final String DIFF_SYMBOL = "\u2014";
    public static final String UNION_SYMBOL = "\u222A";
    public static final String INTERSECT_SYMBOL = "\u2229";
    public static final String INNER_JOIN_SYMBOL = "\u2A1D";

    private final String label;
    private final String symbol;

    Operator(String label, String symbol) {
        this.label = label;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return label;
    }

    public String toSymbol() {
        return symbol;
    }
}
