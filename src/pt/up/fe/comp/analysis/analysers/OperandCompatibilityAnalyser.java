package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.JmmAnalyser;
import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.Method;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

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

    private Type getChildType(JmmNode child, SymbolTableImpl symbolTable){

        Type childType = null;

        if (Objects.equals(child.getKind(), "Id")){
            String leftChildName = child.get("name");
            childType = this.getIdType(child, symbolTable);
        } else if (Objects.equals(child.getKind(), "BinOp")){
            childType = this.evaluateExpressionType(child, symbolTable);
        } else if (Objects.equals(child.getKind(), "ArrayAccess")){
            //TODO is it correct to assume this? since it will be analysed somewhere else
            childType = new Type("int", false);
        } else {
            // Dummy value
            // TODO is this "legal" ?
            childType = new Type("ignore", false);
        }

        return childType;
    }

    // TODO implement
    private Type typeOfOperation(String op, Type leftType, Type rightType){
        return null;
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

        return this.typeOfOperation(operation, leftChildType, rightChildType);
    }

    private Integer visitOp(JmmNode expression, SymbolTableImpl symbolTable) {

        return 0;
    }



}
