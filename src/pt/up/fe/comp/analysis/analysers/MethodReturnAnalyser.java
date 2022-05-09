package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.Method;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import javax.swing.*;
import java.util.Optional;

public class MethodReturnAnalyser extends PreorderSemanticAnalyser {

    public MethodReturnAnalyser(){
        super();
        addVisit(AstNode.RETURN, this::visitReturn);
    }

    public Integer visitReturn(JmmNode node, SymbolTableImpl symbolTable){

        Optional<JmmNode> opt_method_node = node.getAncestor("ClassDeclaration");
        if(opt_method_node.isEmpty()) return -1; // TODO throw error?

        Method method = symbolTable.findMethod(opt_method_node.get().getJmmChild(0).getJmmChild(0).get("name"));
        if(method == null) return -1; //TODO throw error?

        if(!this.getJmmNodeType(node.getJmmChild(0), symbolTable).equals(
                new Type(opt_method_node.get().getJmmChild(0).getJmmChild(0).get("returnType"), false))){
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("col")),
                    "Return type doesn't match method's return type."));
        }

        return 0;
    }

}
