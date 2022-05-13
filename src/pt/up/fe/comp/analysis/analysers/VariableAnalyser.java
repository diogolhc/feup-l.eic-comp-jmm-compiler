package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class VariableAnalyser extends PreorderSemanticAnalyser {

    public VariableAnalyser() {
        super();
        addVisit(AstNode.ID, this::visitId);
    }

    private Integer visitId(JmmNode variable, SymbolTableImpl symbolTable) {

        // Ignore chained ExpressionDot
        if ((Objects.equals(variable.getJmmParent().getKind(), AstNode.EXPRESSION_DOT) &&
                variable.getJmmParent().getJmmChild(1) == variable)) {
            return 0;
        }

        if (this.getIdType(variable, symbolTable).equals(new Type("invalid", false)) &&
                !this.isImport(variable.get("name"), symbolTable)) {
            addReport(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(variable.get("line")),
                    Integer.parseInt(variable.get("col")), "Identifier " + variable.get("name") +
                    " was not recognized."));
        }

        return 0;
    }

}
