package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.ast.JmmNode;


public class OperandCompatibilityAnalyser extends PreorderSemanticAnalyser {

    public OperandCompatibilityAnalyser() {
        super();
        addVisit(AstNode.BIN_OP, this::visitOp);
    }

    private Integer visitOp(JmmNode expression, SymbolTableImpl symbolTable) {
        this.evaluateExpressionType(expression, symbolTable);
        return 0;
    }

}
