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

public abstract class PreorderSemanticAnalyser extends PreorderJmmVisitor<SymbolTableImpl, Integer> implements SemanticAnalyser{

    private final List<Report> reports;

    protected PreorderSemanticAnalyser() {
        this.reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    public void addReport(Report report){
        reports.add(report);
    }

    protected Type getIdType(JmmNode node, SymbolTableImpl symbolTable){

        Method parent_method = null;

        var parent_method_node = node.getAncestor(AstNode.METHOD_DECL);

        List<Symbol> symbols = new ArrayList<>();

        if (parent_method_node.isPresent()){
            var method_header = parent_method_node.get().getJmmChild(0);
            parent_method = symbolTable.findMethod(method_header.get("name"));
        } else if (node.getAncestor(AstNode.MAIN_DECL).isPresent()){
            parent_method = symbolTable.findMethod("main");
        }
        if (parent_method != null){
            symbols.addAll(parent_method.getLocalVariables());
            symbols.addAll(parent_method.getParameters());
        }
        symbols.addAll(symbolTable.getFields());

        for (var symbol : symbols){
            if(Objects.equals(symbol.getName(), node.get("name"))){
                return symbol.getType();
            }
        }

        for (String imp : symbolTable.getImports()){
            List<String> split_imports = Arrays.asList(imp.trim().split("\\."));
            if (Objects.equals(node.get("name"), split_imports.get(split_imports.size() - 1))){
                //TODO should we ignore?
                return new Type("ignore", false);
            }
        }

        return new Type("invalid", false);
    }

    protected Type getJmmNodeType(JmmNode node, SymbolTableImpl symbolTable){
        return switch (node.getKind()) {
            case "Id" -> this.getIdType(node, symbolTable);
            case "BinOp" -> this.evaluateExpressionType(node, symbolTable);
            case "ArrayAccess", "IntLiteral" -> new Type("integer", false);
            case "BooleanLiteral" -> new Type("boolean", false);
            case "Bool" -> new Type("boolean", false);
            case "ExpressionDot" -> new Type("ignore", false); //TODO not always, change this
            case "ExpressionNew" -> new Type(node.get("name"), false);
            default -> new Type("invalid", false);
        };
    }

    private String expectedTypeForOp(String op){
        return switch (op) {
            case "and" -> "boolean";
            case "lessThan", "division", "multiplication", "addition", "subtraction" -> "integer";
            default -> "invalid";
        };
    }

    private String resultTypeFromOp(String op){
        return switch (op) {
            case "and", "lessThan" -> "bool";
            case "division", "multiplication", "addition", "subtraction" -> "integer";
            default -> "invalid";
        };
    }

    private Type typeOfOperation(String op, Type leftType, Type rightType){

        boolean valid = (
                (Objects.equals(leftType.getName(), expectedTypeForOp(op)) ||
                        Objects.equals(leftType.getName(), "ignore")) &&
                        (Objects.equals(rightType.getName(), expectedTypeForOp(op)) ||
                                Objects.equals(rightType.getName(), "ignore")));

        if (valid){
            return new Type(resultTypeFromOp(op), false);
        }

        return new Type("invalid", false);
    }

    protected Type evaluateExpressionType(JmmNode expression, SymbolTableImpl symbolTable) {

        String operation = expression.get("op");

        JmmNode leftChild = expression.getJmmChild(0);
        JmmNode rightChild = expression.getJmmChild(1);

        Type leftChildType = this.getJmmNodeType(leftChild, symbolTable);
        Type rightChildType = this.getJmmNodeType(rightChild, symbolTable);

        if (Objects.equals(leftChildType.getName(), "invalid") ||
                Objects.equals(rightChildType.getName(), "invalid")){
            return new Type("invalid", false); // Return invalid
        }

        Type ret = this.typeOfOperation(operation, leftChildType, rightChildType);

        if (Objects.equals(ret.getName(), "invalid")){

            // TODO error message sometimes gives ignore as type
            // TODO array type coming out as int (check the isArray param)
            // TODO this error should not be thrown here
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
        return ret;
    }

}
