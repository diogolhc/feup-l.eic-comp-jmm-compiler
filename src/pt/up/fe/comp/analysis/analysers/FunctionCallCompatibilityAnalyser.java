package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.PreorderSemanticAnalyser;
import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.Method;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class FunctionCallCompatibilityAnalyser extends PreorderSemanticAnalyser {

    public FunctionCallCompatibilityAnalyser() {
        super();
        addVisit(AstNode.THIS, this::visitThis);
    }


    public Integer visitThis(JmmNode node, SymbolTableImpl symbolTable) {

        Method method = symbolTable.findMethod(node.getJmmParent().getJmmChild(1).get("name"));

        if (method == null) return 0; // Handled in another analyser

        List<Symbol> method_args = method.getParameters();
        JmmNode call_args = node.getJmmParent().getJmmChild(2);

        if (method_args.size() != call_args.getChildren().size()) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("col")),
                    "Method " + node.getJmmParent().getJmmChild(1).get("name") +
                            " call doesn't match the number of parameter required by the method."));
        }

        for (int i = 0; i < method_args.size(); i++) {
            Type cur_arg_type = this.getJmmNodeType(call_args.getJmmChild(i).getJmmChild(0), symbolTable);
            if (!method_args.get(i).getType().equals(cur_arg_type)) {
                addReport(new Report(
                        ReportType.ERROR, Stage.SEMANTIC,
                        Integer.parseInt(node.get("line")),
                        Integer.parseInt(node.get("col")),
                        "Method " + node.getJmmParent().getJmmChild(1).get("name") + " call argument " +
                                i + " of type " + cur_arg_type.getName() + (cur_arg_type.isArray() ? "[]" : "") +
                                " doesn't match method parameter " + method_args.get(i).getType().getName() +
                                (method_args.get(i).getType().isArray() ? "[]" : "")));
                return 0;
            }
        }

        return 0;
    }


}
