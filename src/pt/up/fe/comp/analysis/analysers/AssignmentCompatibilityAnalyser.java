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

public class AssignmentCompatibilityAnalyser extends PreorderSemanticAnalyser {

    public AssignmentCompatibilityAnalyser() {
        super();
        addVisit(AstNode.ASSIGNMENT, this::visitAssignment);
    }

    public Integer visitAssignment(JmmNode assignment, SymbolTableImpl symbolTable) {

        // TODO don't allow imports as assignee?

        Type assignee_type = this.getIdType(assignment.getJmmChild(0), symbolTable);

        JmmNode assignment_val;
        if (Objects.equals(assignment.getJmmChild(1).getJmmChild(0).getKind(), "ExpressionNew")) {
            assignment_val = assignment.getJmmChild(1).getJmmChild(0).getJmmChild(0);
        } else {
            assignment_val = assignment.getJmmChild(1).getJmmChild(0);
        }

        Type assignment_type = this.getJmmNodeType(assignment_val, symbolTable);

        if (!(Objects.equals(assignee_type, assignment_type) ||
                assignment_type.equals(new Type("ignore", false)))) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(assignment.get("line")),
                    Integer.parseInt(assignment.get("col")),
                    "Assignee ( " + assignee_type.getName() + (assignee_type.isArray() ? "[]" : "")
                            + " ) must be of the same type as assignment (" + assignment_type.getName() +
                            (assignment_type.isArray() ? "[]" : "") + ")."));
        }

        return 0;
    }

}
