package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class DeclarationAnalyser extends PreorderSemanticAnalyser {

    public DeclarationAnalyser(){
        super();
        addVisit(AstNode.VAR_DECL, this::visitDeclaration);
    }

    private Integer visitDeclaration(JmmNode node, SymbolTableImpl symbolTable) {

        String var_type = node.get("varType");

        System.out.println("YOOOO " + var_type);

        if (var_type == null) return 0;

        if(!(   this.isImport(var_type, symbolTable) ||
                (symbolTable.getSuper() != null && symbolTable.getSuper().equals(var_type)) ||
                symbolTable.getClassName().equals(var_type))){
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("col")),
                    "Declaration must be of known type."));
        }

        return 0;
    }

}
