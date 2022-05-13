package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class LengthAnalyser extends PreorderSemanticAnalyser {
    public LengthAnalyser() {
        super();
        addVisit(AstNode.LENGTH, this::visitLength);
    }

    private Integer visitLength(JmmNode node, SymbolTableImpl symbolTable) {

        if (!this.getJmmNodeType(node.getJmmChild(0), symbolTable).equals(new Type("int", true))) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("col")),
                    "Length only works for arrays"));
        }

        return 0;
    }
}
