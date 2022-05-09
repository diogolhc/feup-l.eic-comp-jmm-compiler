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

public class ArrayAccessAnalyser extends PreorderSemanticAnalyser {

    public ArrayAccessAnalyser() {
        super();
        addVisit(AstNode.ARRAY_ACCESS, this::visitArrayAccess);
    }

    public Integer visitArrayAccess(JmmNode array_access, SymbolTableImpl symbolTable) {

        JmmNode ancestor_id = array_access.getJmmChild(0);

        JmmNode access_node = array_access.getJmmChild(1);

        Type ancestor_type = this.getIdType(ancestor_id, symbolTable);
        if (!ancestor_type.isArray()) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(array_access.get("line")),
                    Integer.parseInt(array_access.get("col")),
                    "Array access only allowed on arrays."));
        }
        if (!Objects.equals(ancestor_type.getName(), "int")) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(array_access.get("line")),
                    Integer.parseInt(array_access.get("col")),
                    "Array must be of type int"));
        }
        // TODO getJmmNodeType should be able to handle IntLiteral by itself
        if (!(Objects.equals(access_node.getKind(), "IntLiteral") ||
                Objects.equals(this.getJmmNodeType(access_node, symbolTable).getName(), "integer"))) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(array_access.get("line")),
                    Integer.parseInt(array_access.get("col")),
                    "Array access index must be of type integer."));
        }

        return 0;
    }

}
