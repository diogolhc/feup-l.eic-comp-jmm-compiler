package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Arrays;
import java.util.List;

public class OllirUtils {
    public static String getCode(Symbol symbol) {
        return symbol.getName() + getOllirType(symbol.getType());
    }

    public static String getOllirType(Type jmmType) {
        // TODO uncomment this switch and delete the other after the refactor of the ast/symbol table filler
        /*
        switch (jmmType.getName()) {
            case "void" -> {
                return ".V";
            }
            case "boolean" -> {
                return ".bool";
            }
            case "int" -> {
                if (jmmType.isArray()) {
                    return ".array.i32";
                } else {
                    return ".i32";
                }
            }
            case "String" -> {
                return ".array.String";
            }
            default -> {
                return "." + jmmType.getName();
            }
        }
        */

        return switch (jmmType.getName()) {
            case "void" -> ".V";
            case "integer" -> ".i32";
            case "boolean" -> ".bool";
            case "int" -> ".array.i32";
            case "String" -> ".array.String";
            default -> "." + jmmType.getName();
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
            case "lessThan" -> "<.bool";
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

    public static boolean isIntegerString(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getArrayIdWithoutType(String child) {
        String[] childSplit = child.split("\\.");
        return String.join(".", Arrays.copyOf(childSplit, childSplit.length == 3 ? 1 : 2));
    }

    public static String getOllirIdWithoutTypeAndParamNum(String ollirId) {
        String[] ollirIdSplit = ollirId.split("\\.");
        int n = ollirId.charAt(0) == '$' ? 1 : 0;
        return ollirIdSplit[n];
    }

    public static String getOllirIdWithoutParamNum(String ollirId) {
        String[] ollirIdSplit = ollirId.split("\\.");
        int n = ollirId.charAt(0) == '$' ? 2 : 1;
        return String.join(".", Arrays.copyOfRange(ollirIdSplit, n, ollirIdSplit.length));
    }

    public static boolean isImmediateValueIndex(String index) {
        if (index.charAt(0) == '-') {
            return isIntegerString(index.substring(0, 2));
        }
        return isIntegerString(index.substring(0, 1));
    }
}
