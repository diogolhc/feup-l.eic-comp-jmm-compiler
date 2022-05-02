package pt.up.fe.comp.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class SymbolTableImpl implements SymbolTable {
    private String className;
    private String superclassName;

    private Map<Type, List<Symbol>> fields;
    private List<String> imports;
    private Map<String, Method> methods;

    public SymbolTableImpl() {
        this.imports = new ArrayList<>();
        this.className = null;
        this.superclassName = null;
        this.methods = new HashMap<>();
        this.fields = new HashMap<>();
    }

    public SymbolTableImpl(String className, String superclassName, List<String> imports,
                           List<Symbol> fields, Map<String, List<Symbol>> parameters,
                           Map<String, List<Symbol>> localVariables, Map<String, Type> methodTypes)
                           throws VarAlreadyDefinedException {
        this.className = className;
        this.superclassName = superclassName;
        this.imports = imports;
        this.fields = new HashMap<>();
        this.methods = new HashMap<>();

        for (Symbol field : fields) {
            if (!this.fields.containsKey(field.getType())) {
                this.fields.put(field.getType(), new ArrayList<>());
            }
            this.fields.get(field.getType()).add(field);
        }

        for (String signature : methodTypes.keySet()) {
            methods.put(signature, new Method(methodTypes.get(signature)));
        }

        for (String signature : methods.keySet()) {
            for (Symbol parameter : parameters.get(signature)) {
                if (!methods.get(signature).addParameter(parameter)) {
                    throw new VarAlreadyDefinedException(parameter, signature);
                }
            }
        }

        for (String signature : methods.keySet()) {
            for (Symbol localVariable : localVariables.get(signature)) {
                if (!methods.get(signature).addLocalVariable(localVariable)) {
                    throw new VarAlreadyDefinedException(localVariable, signature);
                }
            }
        }
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return superclassName;
    }

    @Override
    public List<Symbol> getFields() {
        List<Symbol> fields_ = new ArrayList<>();

        for (Type type : fields.keySet()) {
            List<Symbol> symbols = fields.get(type);
            fields_.addAll(symbols);
        }

        return fields_;
    }

    @Override
    public List<String> getMethods() {
        return new ArrayList<>(methods.keySet());
    }

    @Override
    public Type getReturnType(String methodSignature) {
        Method method = methods.get(methodSignature);

        return method == null ? null : method.getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        Method method = methods.get(methodSignature);

        return method == null ? null : method.getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        Method method = methods.get(methodSignature);
        return method == null ? null : method.getLocalVariables();
    }

/*    public static void main(String[] args) {
        List<String> p = new ArrayList<>();
        p.add("String[]");
        p.add("int");
        p.add("ArrayList");
        System.out.println(SymbolTableImpl.toMethodSignature("f", p));
    }*/

    private static String toMethodSignature(String methodName, List<String> parameterTypes) {
        var builder = new StringBuilder();
        builder.append(methodName);
        builder.append("(");
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (i != 0) {
                builder.append(" ");
            }
            builder.append(parameterTypes.get(i));

            if (i != parameterTypes.size()-1) {
                builder.append(",");
            }
        }
        builder.append(")");

        return builder.toString();
    }

    public Symbol findVariable(String methodSignature, String variableName) throws VarNotInScopeException {
        Method method = methods.get(methodSignature);

        Symbol asLocalVariable = method.findLocalVariable(variableName);
        if (asLocalVariable != null) {
            return asLocalVariable;
        }

        Symbol asParameter = method.findParameter(variableName);
        if (asParameter != null) {
            return asParameter;
        }

        Symbol asField = findField(variableName);
        if (asField != null) {
            return asField;
        }

        throw new VarNotInScopeException(variableName, methodSignature);
    }

    private Symbol findField(String name) {
        for (Symbol symbol : getFields()) {
            if (symbol.getName().equals(name)) {
                return symbol;
            }
        }

        return null;
    }

    public Method findMethod(String methodName) {
        return methods.get(methodName);
    }

    public void addImport(String importResult) {
        imports.add(importResult);
    }

    public boolean addField(Symbol field) {
        if (findField(field.getName()) != null) {
            return false;
        }

        if (!fields.containsKey(field.getType())) {
            List<Symbol> f = new ArrayList<>();
            f.add(field);

            fields.put(field.getType(), f);
        } else {
            fields.get(field.getType()).add(field);
        }

        return true;
    }

    public void setClassName(String name) {
        this.className = name;
    }

    public void setSuperclassName(String superclassName) {
        this.superclassName = superclassName;
    }

    public boolean hasMethod(String methodSignature) {
        // TODO: METHOD OVERLOADING
        return methods.containsKey(methodSignature);
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params) throws VarAlreadyDefinedException {
        Method method = new Method(returnType);

        for (Symbol param : params) {
            if (!method.addParameter(param)) {
                throw new VarAlreadyDefinedException(param, methodSignature);
            }
        }

        methods.put(methodSignature, method);
    }
}
