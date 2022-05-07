package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class OllirUtils {
    public static String getCode(Symbol symbol) {
        return symbol.getName() + getCode(symbol.getType());
    }

    public static String getCode(Type type) {
        StringBuilder code = new StringBuilder();

        if (type.isArray()) {
            code.append(".array");
        }

        code.append(getOllirType(type.getName()));

        return code.toString();
    }

    public static String getOllirType(String jmmType) {
        return switch (jmmType) {
            case "void" -> ".V";
            case "integer" -> ".i32";
            case "boolean" -> ".bool";
            default -> "." + jmmType;
        };
    }

    public static String getInvokeType(String invokee, SymbolTable symbolTable) {
        if (invokee.equals("this")) {
            return "invokevirtual";
        }

        List<String> imports = symbolTable.getImports();

        for (String _import : imports) {
            String[] tokens = _import.split("\\.");
            if (tokens[tokens.length - 1].equals(invokee)) {
                return "invokestatic";
            }
        }

        return "invokevirtual";
    }

    public static String getOperator(String javaCCOperator) {
        return switch (javaCCOperator) {
            case "addition" -> "+.i32";
            case "subtraction" -> "-.i32";
            case "multiplication" -> "*.i32";
            case "division" -> "/.i32";
            case "and" -> "&&.bool";
            case "lessThan" -> "<.i32";
            default -> "// ERROR: invalid javaCCOperator\n";
        };
    }

    public static String getReturnType(String javaCCOperator) {
        return switch (javaCCOperator) {
            case "addition" -> ".i32";
            case "subtraction" -> ".i32";
            case "multiplication" -> ".i32";
            case "division" -> ".i32";
            case "and" -> ".bool";
            case "lessThan" -> ".bool";
            default -> "// ERROR: invalid javaCCOperator\n";
        };
    }

//    // returns Pair<Code, t>
//    public static Pair<String, String> getCode(JmmNode binOp) {
//
//
//    }

}
