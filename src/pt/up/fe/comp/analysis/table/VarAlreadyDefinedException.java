package pt.up.fe.comp.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

public class VarAlreadyDefinedException extends Exception {

    public VarAlreadyDefinedException(Symbol var, String signature) {
        super("error: variable " + var.getName() + " is already defined in method " + signature);
    }

    public static void main(String[] args) {
        VarAlreadyDefinedException a = new VarAlreadyDefinedException(new Symbol(null, "b"), "f()");
        System.out.println(a.getMessage());
    }
}
