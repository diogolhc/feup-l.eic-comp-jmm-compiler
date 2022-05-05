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
import java.util.Optional;

public class ArrayAccessAnalyser extends PreorderSemanticAnalyser {

    public ArrayAccessAnalyser(){
        super();
        addVisit(AstNode.ARRAY_ACCESS, this::visitArrayAccess);
    }

    public Integer visitArrayAccess(JmmNode array_access, SymbolTableImpl symbolTable){

        Optional<JmmNode> ancestor_id = Optional.ofNullable(array_access.getJmmChild(0));

        if (ancestor_id.isPresent()){
            Type ancestor_type =  this.getIdType(ancestor_id.get(), symbolTable);
            if (!ancestor_type.isArray()){
                addReport(new Report(
                        ReportType.ERROR, Stage.SEMANTIC,
                        Integer.parseInt(array_access.get("line")),
                        Integer.parseInt(array_access.get("col")),
                        "Array access only allowed on arrays."));
            } else if (!Objects.equals(ancestor_type.getName(), "int")){
                addReport(new Report(
                        ReportType.ERROR, Stage.SEMANTIC,
                        Integer.parseInt(array_access.get("line")),
                        Integer.parseInt(array_access.get("col")),
                        "Array must be of time int"));
            }
        } else {
            // TODO throw error?
            System.out.println("ERROR ----------");
            return -1;
        }

        return 0;
    }

}
