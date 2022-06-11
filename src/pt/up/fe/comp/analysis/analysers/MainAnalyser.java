package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class MainAnalyser extends PreorderSemanticAnalyser {

    public MainAnalyser() {
        super();
        addVisit(AstNode.THIS, this::visitThis);
    }

    private Integer visitThis(JmmNode node, SymbolTableImpl symbolTable) {

        if (node.getAncestor(AstNode.MAIN_DECL).isPresent()){
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("col")),
                    "This can't be used in main"));
        }

        return 0;
    }

}
