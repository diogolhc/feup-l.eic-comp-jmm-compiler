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

import java.util.*;

public class VariableAnalyser extends PreorderSemanticAnalyser {

    public VariableAnalyser() {
        super();
        addVisit(AstNode.ID, this::visitId);
    }

    private Integer visitId(JmmNode variable, SymbolTableImpl symbolTable){
        // TODO might be out of any method? (this is probably syntax)
        // TODO should we check if ExpressionDot is from own class?

        // ignore chained ExpressionDot
        if ((Objects.equals(variable.getJmmParent().getKind(), "ExpressionDot") &&
                variable.getJmmParent().getJmmChild(1) == variable)){
            return 0;
        }

        // TODO PreorderSemanticAnalyser uses this code too
        Method parent_method = null;
        var parent_method_node = variable.getAncestor(AstNode.METHOD_DECL);
        List<Symbol> symbols = new ArrayList<>();
        if (parent_method_node.isPresent()){
            var method_header = parent_method_node.get().getJmmChild(0);
            parent_method = symbolTable.findMethod(method_header.get("name"));
        } else if (variable.getAncestor(AstNode.MAIN_DECL).isPresent()){
            parent_method = symbolTable.findMethod("main");
        }
        if (parent_method != null){
            symbols.addAll(parent_method.getParameters());
            symbols.addAll(parent_method.getLocalVariables());
        }
        symbols.addAll(symbolTable.getFields());

        boolean found = false;

        for (var symbol : symbols){
            if (Objects.equals(symbol.getName(), variable.get("name"))){
                found = true;
                break;
            }
        }

        // Searching on imports
        if (!found){
            for (String imp : symbolTable.getImports()){

                List<String> split_imports = Arrays.asList(imp.trim().split("\\."));

                if (Objects.equals(variable.get("name"), split_imports.get(split_imports.size() - 1))){
                    found = true;
                    break;
                }
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
