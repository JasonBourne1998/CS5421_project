package graphical.editor.LinAlgTree;

import graphical.editor.InputException;

import java.util.Arrays;

public class Parser {
    private static final String ATTR_REGEX_MATCHER = "(([a-z0-9_]+\\.)?([a-z0-9_]+))";
    private static final String AGG_REGEX_MATCHER = "(min|max|avg|sum|count)\\((distinct)? "
            + "(" + ATTR_REGEX_MATCHER + "|(\\(" + ATTR_REGEX_MATCHER + "(, " + ATTR_REGEX_MATCHER + ")+\\)))\\)";
    public static String[][] parsePredicates(String[] args) throws InputException {
        String[][] output = Arrays.stream(args)
                .map(Parser::parsePredicate)
                .toArray(String[][]::new);

        return output;
    }

    public static String[] parsePredicate(String arg) throws InputException {
        // split parameters by whitespace
        String[] predicate = arg.split("[ \t\r\n]+");
        if (predicate.length != 3) {
            throw new InputException(String.format("Unable to parse join attributes %s", arg));
        }
        return predicate;
    }

    public static boolean detectAggregateFunctions(String[] args) {
        return Arrays.stream(args)
                .anyMatch(arg -> arg.toLowerCase().matches(AGG_REGEX_MATCHER));
    }
}
