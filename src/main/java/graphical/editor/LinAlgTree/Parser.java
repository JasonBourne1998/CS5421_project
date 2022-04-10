package graphical.editor.LinAlgTree;

import graphical.editor.ConditionToken;
import graphical.editor.InputException;

import java.util.ArrayList;
import java.util.Arrays;

public class Parser {
    private static final String ATTR_REGEX_MATCHER = "(([a-z0-9_]+\\.)?([a-z0-9_]+))";
    private static final String AGG_REGEX_MATCHER = "(min|max|avg|sum|count)\\((distinct)? "
            + "(" + ATTR_REGEX_MATCHER + "|(\\(" + ATTR_REGEX_MATCHER + "(, " + ATTR_REGEX_MATCHER + ")+\\)))\\)";
    public static ArrayList<ArrayList<String>> parsePredicates(String[] args) throws InputException {
        ArrayList<ArrayList<String>> output = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            output.set(i, parsePredicate(args[i]));
        }

        return output;
    }

    public static ArrayList<String> parsePredicate(String arg) throws InputException {
        // split parameters by whitespace
        String[] predicate = Arrays.stream(arg.strip().split("[ \t\r\n]+"))
                .map(str -> str.replaceAll("[()]", ""))
                .map(String::strip)
                .toArray(String[]::new);
        if (predicate.length < 2 || predicate.length > 3
                || Arrays.stream(ConditionToken.values()).noneMatch(t -> t.toString().equalsIgnoreCase(predicate[1]))) {
            throw new InputException(String.format("Unable to parse condition %s", arg));
        }
        if ((predicate[1].equalsIgnoreCase(ConditionToken.IS_NOT_NULL.toString())
                || predicate[1].equalsIgnoreCase(ConditionToken.IS_NULL.toString())) && predicate.length == 3) {
            throw new InputException(String.format("Unexpected token found after %s: %s", predicate[1], predicate[2]));
        }
        return new ArrayList<>(Arrays.asList(predicate));
    }

    public static boolean detectAggregateFunctions(String[] args) {
        return Arrays.stream(args)
                .anyMatch(arg -> arg.toLowerCase().matches(AGG_REGEX_MATCHER));
    }

    public static boolean detectAggregateFunctions(String s) {
        return s.matches(AGG_REGEX_MATCHER);
    }
}
