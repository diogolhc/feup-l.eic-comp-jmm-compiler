package pt.up.fe.comp.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Method {
    private final Map<Type, List<Symbol>> parameters;
    private final Map<Type, List<Symbol>> localVariables;
    private final Type returnType;

    protected Method(Type returnType) {
        this.parameters = new HashMap<>();
        this.localVariables = new HashMap<>();
        this.returnType = returnType;
    }

    public static void main(String[] args) {
        Method m = new Method(null);
        var s1 = new Symbol(new Type("int", false), "v1");
        var s2 = new Symbol(new Type("int", false), "v1");
    }

    protected Type getReturnType() {
        return returnType;
    }

    protected List<Symbol> getParameters() {
        List<Symbol> params = new ArrayList<>();

        for (Type type : parameters.keySet()) {
            List<Symbol> symbols = parameters.get(type);
            params.addAll(symbols);
        }

        return params;
    }

    protected List<Symbol> getLocalVariables() {
        List<Symbol> locals = new ArrayList<>();

        for (Type type : localVariables.keySet()) {
            List<Symbol> symbols = localVariables.get(type);
            locals.addAll(symbols);
        }

        return locals;
    }

    protected Symbol findParameter(String name) {
        for (Symbol symbol : getParameters()) {
            if (symbol.getName().equals(name)) {
                return symbol;
            }
        }

        return null;
    }

    protected Symbol findLocalVariable(String name) {
        for (Symbol symbol : getLocalVariables()) {
            if (symbol.getName().equals(name)) {
                return symbol;
            }
        }

        return null;
    }

    protected boolean addParameter(Symbol parameter) {
        var params = parameters.get(parameter.getType());
        if (params == null) {
            parameters.put(parameter.getType(), new ArrayList<>());
            parameters.get(parameter.getType()).add(parameter);
            return true;
        }

        if (params.contains(parameter)) {
            return false;
        }

        params.add(parameter);

        return true;
    }

    protected boolean addLocalVariable(Symbol localVariable) {
        var locals = localVariables.get(localVariable.getType());
        if (locals == null) {
            localVariables.put(localVariable.getType(), new ArrayList<>());
            localVariables.get(localVariable.getType()).add(localVariable);
            return true;
        }

        if (locals.contains(localVariable)) {
            return false;
        }

        var params = parameters.get(localVariable.getType());
        if (params != null) {
            if (params.contains(localVariable)) {
                return false;
            }
        }

        locals.add(localVariable);

        return true;
    }
}
