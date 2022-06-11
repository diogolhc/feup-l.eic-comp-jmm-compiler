package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.Method;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class PreorderSemanticAnalyser extends PreorderJmmVisitor<SymbolTableImpl, Integer> implements SemanticAnalyser {

    private final List<Report> reports;

    protected PreorderSemanticAnalyser() {
        this.reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    public void addReport(Report report) {
        reports.add(report);
    }

    public boolean isImport(String id, SymbolTableImpl symbolTable) {
        for (String imp : symbolTable.getImports()) {
            List<String> split_imports = Arrays.asList(imp.trim().split("\\."));
            if (Objects.equals(id, split_imports.get(split_imports.size() - 1))) {
                return true;
            }
        }
        return false;
    }

    public boolean isLiteral(String id, SymbolTableImpl symbolTable) {
        return (id.equals("integer") || id.equals("boolean") || id.equals("array"));
    }

    protected boolean compatibleType(Type type1, Type type2, SymbolTableImpl symbolTable) {
        // Literals
        Type super_type = new Type(symbolTable.getSuper(), false);
        Type class_type = new Type(symbolTable.getClassName(), false);

        if (type1.equals(type2)) {
            return true;
        } else if (type1.equals(new Type("ignore", false)) ||
                type2.equals(new Type("ignore", false))) {
            return true;
        } else if (type1.equals(super_type)) {
            if (type2.equals(class_type) ||
                    this.isImport(type2.getName(), symbolTable)) { //import might extend super
                return true;
            }
        } else if (this.isImport(type1.getName(), symbolTable)) {
            if (this.isImport(type2.getName(), symbolTable)) {
                return true;
            }
        }
        return false;
    }

    protected Type getIdType(JmmNode node, SymbolTableImpl symbolTable) {

        Method parent_method = null;
        Boolean parent_method_is_main = false;

        var parent_method_node = node.getAncestor(AstNode.METHOD_DECL);

        List<Symbol> symbols = new ArrayList<>();

        if (parent_method_node.isPresent()) {
            var method_header = parent_method_node.get().getJmmChild(0);
            parent_method = symbolTable.findMethod(method_header.get("name"));
        } else if (node.getAncestor(AstNode.MAIN_DECL).isPresent()) {
            parent_method = symbolTable.findMethod("main");
            parent_method_is_main = true;
        }
        if (parent_method != null) {
            symbols.addAll(parent_method.getLocalVariables());
            symbols.addAll(parent_method.getParameters());
        }
        if (!parent_method_is_main) {
            symbols.addAll(symbolTable.getFields());
        }

        Type ret = new Type("invalid", false);

        for (var symbol : symbols) {
            if (Objects.equals(symbol.getName(), node.get("name"))) {
                ret = symbol.getType();
                break;
            }
        }

        if (node.get("name").equals(symbolTable.getClassName())) ret = new Type(symbolTable.getClassName(), false);
        if (node.get("name").equals(symbolTable.getSuper())) ret = new Type(symbolTable.getSuper(), false);

        return ret;
    }

    private Type getExpressionNewType(JmmNode node, SymbolTableImpl symbolTable) {
        if (node.getChildren().size() > 0) {
            if (this.getJmmNodeType(node.getJmmChild(0), symbolTable).equals(new Type("integer", false))) {
                return new Type("int", true);
            } else {
                return new Type("invalid", false);
            }
        }
        return new Type(node.get("name"), false);
    }

    private Type getExpressionDotType(JmmNode node, SymbolTableImpl symbolTable) {

        Method method = symbolTable.findMethod(node.getJmmChild(1).get("name"));
        if (node.getJmmChild(0).getKind().equals(AstNode.THIS) ||
                this.getJmmNodeType(node.getJmmChild(0), symbolTable).equals(new Type(symbolTable.getClassName(), false))) {
            if (method != null) {
                return method.getReturnType();
            } else if (symbolTable.getSuper() != null) {
                return new Type("ignore", false);
            } else {
                return new Type("invalid", false);
            }
        }
        return new Type("ignore", false);
    }

    protected Type getJmmNodeType(JmmNode node, SymbolTableImpl symbolTable) {
        return switch (node.getKind()) {
            case "Id" -> this.getIdType(node, symbolTable);
            case "BinOp", "Not" -> this.evaluateExpressionType(node, symbolTable);
            case "ArrayAccess", "IntLiteral", "Length" -> new Type("integer", false);
            case "BooleanLiteral", "Bool" -> new Type("boolean", false);
            case "ExpressionDot" -> this.getExpressionDotType(node, symbolTable);
            case "ExpressionNew" -> this.getExpressionNewType(node, symbolTable);
            case "This" -> new Type(symbolTable.getClassName(), false);
            default -> new Type("invalid", false);
        };
    }

    private String expectedTypeForOp(String op) {
        return switch (op) {
            case "and" -> "boolean";
            case "lessThan", "division", "multiplication", "addition", "subtraction" -> "integer";
            default -> "invalid";
        };
    }

    private String resultTypeFromOp(String op) {
        return switch (op) {
            case "and", "lessThan" -> "boolean";
            case "division", "multiplication", "addition", "subtraction" -> "integer";
            default -> "invalid";
        };
    }

    private Type typeOfOperation(String op, Type leftType, Type rightType) {

        boolean valid = (
                (Objects.equals(leftType.getName(), expectedTypeForOp(op)) ||
                        Objects.equals(leftType.getName(), "ignore")) &&
                        (Objects.equals(rightType.getName(), expectedTypeForOp(op)) ||
                                Objects.equals(rightType.getName(), "ignore")));

        if (valid) {
            return new Type(resultTypeFromOp(op), false);
        }

        return new Type("invalid", false);
    }

    protected Type evaluateExpressionType(JmmNode expression, SymbolTableImpl symbolTable) {

        Type ret = null;

        if (expression.getKind().equals("Not")) {

            ret = this.getJmmNodeType(expression.getJmmChild(0), symbolTable);

            if (!ret.equals(new Type("boolean", false))) {
                addReport(new Report(
                        ReportType.ERROR, Stage.SEMANTIC,
                        Integer.parseInt(expression.get("line")),
                        Integer.parseInt(expression.get("col")),
                        "Expected boolean for not operator"));
            }

        } else {
            String operation = expression.get("op");

            JmmNode leftChild = expression.getJmmChild(0);
            JmmNode rightChild = expression.getJmmChild(1);

            Type leftChildType = this.getJmmNodeType(leftChild, symbolTable);
            Type rightChildType = this.getJmmNodeType(rightChild, symbolTable);

            if (Objects.equals(leftChildType.getName(), "invalid") ||
                    Objects.equals(rightChildType.getName(), "invalid")) {
                return new Type("invalid", false); // Return invalid
            }

            ret = this.typeOfOperation(operation, leftChildType, rightChildType);

            if (Objects.equals(ret.getName(), "invalid")) {

                String error_message = "Invalid types for operation. ";
                error_message += "Expected " + expectedTypeForOp(operation) + " types for operation " + operation + ". ";
                error_message += "Found " + getJmmNodeType(leftChild, symbolTable).getName() + " and "
                        + getJmmNodeType(rightChild, symbolTable).getName() + ".";

                addReport(new Report(
                        ReportType.ERROR, Stage.SEMANTIC,
                        Integer.parseInt(expression.get("line")),
                        Integer.parseInt(expression.get("col")),
                        error_message));
            }
        }

        return ret;
    }

}
