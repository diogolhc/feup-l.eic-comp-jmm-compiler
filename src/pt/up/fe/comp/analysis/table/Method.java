package pt.up.fe.comp.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;

public class Method {
    private final List<Symbol> parameters;
    private final List<Symbol> localVariables;
    private final Type returnType;

    protected Method(Type returnType) {
        this.parameters = new ArrayList<>();
        this.localVariables = new ArrayList<>();
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Symbol> getParameters() {
        return this.parameters;
    }

    public List<Symbol> getLocalVariables() {
        return this.localVariables;
    }

    protected Symbol findParameter(String name) {
        for (Symbol symbol : this.parameters) {
            if (symbol.getName().equals(name)) {
                return symbol;
            }
        }

        return null;
    }

    protected Symbol findLocalVariable(String name) {
        for (Symbol symbol : this.localVariables) {
            if (symbol.getName().equals(name)) {
                return symbol;
            }
        }

        return null;
    }

    protected boolean addParameter(Symbol parameter) {
        for (var s : this.parameters) {
            if (s.getName().equals(parameter.getName())) {
                return false;
            }
        }

        this.parameters.add(parameter);

        return true;
    }

    public boolean addLocalVariable(Symbol localVariable) {
        for (var s : this.localVariables) {
            if (s.getName().equals(localVariable.getName())) {
                return false;
            }
        }

        // Checks if local variable name already exists as parameter
        for (var s : parameters) {
            if (s.getName().equals(localVariable.getName())) {
                return false;
            }
        }

        this.localVariables.add(localVariable);

        return true;
    }
}
