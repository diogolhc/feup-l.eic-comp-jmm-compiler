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

        // must be getId
        Type assignee_type = this.getIdType(assignment.getJmmChild(0), symbolTable);

        Type assignment_type = this.getJmmNodeType(assignment.getJmmChild(1).getJmmChild(0), symbolTable);

        System.out.println("DEBUG1 " + assignment.getJmmChild(0) + " " + assignment.getJmmChild(1).getJmmChild(0));
        System.out.println("DEBUG2 " + assignee_type + " " + assignment_type);

        if (Objects.equals(assignment.getJmmChild(1).getJmmChild(0).getKind(), "ExpressionNew") &&
                assignment_type.equals(new Type("invalid", false))){
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(assignment.get("line")),
                    Integer.parseInt(assignment.get("col")),
                    "Array size must be of type integer."));
        }
        else if (!this.compatibleType(assignee_type, assignment_type, symbolTable)){
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(assignment.get("line")),
                    Integer.parseInt(assignment.get("col")),
                    "Assignee type must be compatible with assignment type." + assignee_type + assignment_type));
        } //TODO remove this is for debug

        return 0;
    }

}
