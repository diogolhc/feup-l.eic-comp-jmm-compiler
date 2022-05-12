package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class ConditionAnalyser extends PreorderSemanticAnalyser {

    public ConditionAnalyser() {
        super();
        addVisit(AstNode.CONDITION, this::visitCondition);
    }

    public Integer visitCondition(JmmNode condition, SymbolTableImpl symbolTable){
        if (!this.compatibleType(this.getJmmNodeType(condition.getJmmChild(0), symbolTable),
                new Type("bool", false), symbolTable)){
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(condition.get("line")),
                    Integer.parseInt(condition.get("col")),
                    "Conditions must be of type bool."));
        }
        return 0;
    }

}
