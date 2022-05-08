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
        return getOllirType(type.getName());
    }

    public static String getOllirType(String jmmType) {
        return switch (jmmType) {
            case "void" -> ".V";
            case "integer" -> ".i32";
            case "boolean" -> ".bool";
            case "int" -> ".array.i32";
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
            case "addition", "subtraction", "multiplication", "division" -> ".i32";
            case "and", "lessThan" -> ".bool";
            default -> "// ERROR: invalid javaCCOperator\n";
        };
    }

    public static String getBoolValue(String value) {
        return switch (value) {
            case "true" -> "1";
            case "false" -> "0";
            default -> "// ERROR: invalid bool value\n";
        };
    }

    public static String getOperandType(String javaCCOperator) {
        return switch (javaCCOperator) {
            case "addition", "lessThan", "subtraction", "multiplication", "division" -> ".i32";
            case "and" -> ".bool";
            default -> "// ERROR: invalid javaCCOperator\n";
        };
    }

}
