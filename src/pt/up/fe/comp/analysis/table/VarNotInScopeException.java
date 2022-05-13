package pt.up.fe.comp.analysis.table;

public class VarNotInScopeException extends Exception {
    public VarNotInScopeException(String varName, String signature) {
        super("error: cannot find symbol" + "\nsymbol:   variable " + varName + "\nlocation: " + "method " + signature);
    }
}
