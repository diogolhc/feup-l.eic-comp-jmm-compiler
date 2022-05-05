package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.JmmAnalyser;
import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.Method;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OperandCompatibilityAnalyser extends PreorderSemanticAnalyser {

    public OperandCompatibilityAnalyser(){
        super();
        addVisit(AstNode.BIN_OP, this::visitOp);
    }

    private Type getIdType(JmmNode id, SymbolTableImpl symbolTable){

        // TODO this will probably be used in other stages

        Method parent_method = null;

        var parent_method_node = id.getAncestor(AstNode.METHOD_DECL);

        List<Symbol> symbols = new ArrayList<>();

        if (parent_method_node.isPresent()){
            var method_header = parent_method_node.get().getJmmChild(0);
            parent_method = symbolTable.findMethod(method_header.get("name"));
        } else if (id.getAncestor(AstNode.MAIN_DECL).isPresent()){
            parent_method = symbolTable.findMethod("main");
        }
        if (parent_method != null){
            symbols.addAll(parent_method.getLocalVariables());
            symbols.addAll(parent_method.getParameters());
        }
        symbols.addAll(symbolTable.getFields());

        for (var symbol : symbols){
            if(Objects.equals(symbol.getName(), id.get("name"))){
                return symbol.getType();
            }
        }

        return null;
    }

    // TODO implement this somewhere else to be used by other methods
    private Type getChildType(JmmNode child, SymbolTableImpl symbolTable){
        return switch (child.getKind()) {
            case "Id" -> this.getIdType(child, symbolTable);
            case "BinOp" -> this.evaluateExpressionType(child, symbolTable);
            case "ArrayAccess", "IntLiteral" -> new Type("integer", false);
            case "Bool" -> new Type("boolean", false);
            default -> new Type("ignore", false);
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
            case "and", "lessThan" -> "boolean";
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

    private Type evaluateExpressionType(JmmNode expression, SymbolTableImpl symbolTable) {

        String operation = expression.get("op");

        JmmNode leftChild = expression.getJmmChild(0);
        JmmNode rightChild = expression.getJmmChild(1);

        Type leftChildType = this.getChildType(leftChild, symbolTable);
        Type rightChildType = this.getChildType(rightChild, symbolTable);

        if (Objects.equals(leftChildType.getName(), "invalid") ||
                Objects.equals(rightChildType.getName(), "invalid")){
            return new Type("invalid", false); // Return invalid
        }

        Type ret = this.typeOfOperation(operation, leftChildType, rightChildType);

        if (Objects.equals(ret.getName(), "invalid")){

            String error_message = "Invalid types for operation. ";
            error_message += "Expected " + expectedTypeForOp(operation) + " types for operation " + operation + ". ";
            error_message += "Found " + getChildType(leftChild, symbolTable).getName() + " and "
                    + getChildType(rightChild, symbolTable).getName() + ".";

            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(expression.get("line")),
                    Integer.parseInt(expression.get("col")),
                    error_message));
        }

        return ret;
    }

    private Integer visitOp(JmmNode expression, SymbolTableImpl symbolTable) {
        this.evaluateExpressionType(expression, symbolTable);
        return 0;
    }

}
