package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Objects;

public class MethodLiteralCallAnalyser extends PreorderSemanticAnalyser {

    public MethodLiteralCallAnalyser(){
        super();
        addVisit(AstNode.EXPRESSION_DOT, this::visitExpressionDot);
    }

    public Integer visitExpressionDot(JmmNode node, SymbolTableImpl symbolTable){

        if(Objects.equals(node.getJmmParent().getKind(), AstNode.EXPRESSION_DOT)) return 0;

        if(this.getJmmNodeType(node.getJmmChild(0), symbolTable).equals(new Type("integer", false)) ||
                this.getJmmNodeType(node.getJmmChild(0), symbolTable).equals(new Type("bool", false)) ||
                this.getJmmNodeType(node.getJmmChild(0), symbolTable).equals(new Type("int", true))){
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("col")),
                    "Literals can't call methods"));
        }

        return 0;
    }
}
