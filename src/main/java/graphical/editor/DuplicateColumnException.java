package graphical.editor;

public class DuplicateColumnException extends Exception {
    public DuplicateColumnException() {
        super("This operation will result in multiple columns having the same name. Please rename the columns.");
    }
}
