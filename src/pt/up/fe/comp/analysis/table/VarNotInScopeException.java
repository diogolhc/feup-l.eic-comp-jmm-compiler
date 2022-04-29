package pt.up.fe.comp.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class VarNotInScopeException extends Exception {
    public VarNotInScopeException(String varName, String signature) {
        //super("error: cannot find symbol\nsymbol:   variable " + var.getName() + "\nlocation: " + "method "+ signature);
        super("error: cannot find symbol" + "\nsymbol:   variable " + varName + "\nlocation: " + "method "+ signature);
    }

    /*
    public static void main(String[] args) {
        VarNotInScopeException a = new VarNotInScopeException(new Symbol(null, "b"), "f()");
        System.out.println(a.getMessage());
    }*/
}
