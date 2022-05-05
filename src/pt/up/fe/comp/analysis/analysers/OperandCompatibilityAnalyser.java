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






    private Integer visitOp(JmmNode expression, SymbolTableImpl symbolTable) {
        this.evaluateExpressionType(expression, symbolTable);
        return 0;
    }

}
