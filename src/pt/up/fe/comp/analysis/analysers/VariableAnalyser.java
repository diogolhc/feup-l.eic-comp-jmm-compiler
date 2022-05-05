package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.SemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.Method;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VariableAnalyser extends PreorderSemanticAnalyser {

    public VariableAnalyser() {
        super();

        addVisit(AstNode.VAR, this::visitVariable);
    }

    private Integer visitVariable(JmmNode variable, SymbolTableImpl symbolTable) {
        // TODO also handle imports
        // TODO might be out of any method? (this is probs syntax)

        boolean found = false;

        System.out.println(variable);

        Method parent_method = null;

        var parent_method_node = variable.getAncestor(AstNode.METHOD_DECL);

        if (parent_method_node.isPresent()){
            var method_header = parent_method_node.get().getJmmChild(0);
            parent_method = symbolTable.findMethod(method_header.get("name"));
        } else {
            parent_method = symbolTable.findMethod("main");
        }

        List<Symbol> symbols = new ArrayList<>();
        symbols.addAll(parent_method.getParameters());
        symbols.addAll(parent_method.getLocalVariables());

        for (var symbol : symbols){
            if (Objects.equals(symbol.getName(), variable.get("name"))){
                found = true;
                break;
            }
        }

        if (!found) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(variable.get("line")),
                    Integer.parseInt(variable.get("col")),
                    "Variable " + variable.get("name") + " was not declared."));
        }

        return 0;
    }
}
