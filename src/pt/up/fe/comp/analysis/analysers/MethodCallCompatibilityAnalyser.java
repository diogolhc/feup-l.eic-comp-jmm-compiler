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

public class MethodCallCompatibilityAnalyser extends PreorderSemanticAnalyser {

    public MethodCallCompatibilityAnalyser() {
        super();
        addVisit(AstNode.EXPRESSION_DOT, this::visitExpressionDot);
    }

    private Integer visitExpressionDot(JmmNode node, SymbolTableImpl symbolTable) {

        if (!(node.getJmmChild(0).getKind().equals(AstNode.THIS) || //not this nor call to this class
                this.getJmmNodeType(node.getJmmChild(0), symbolTable)
                        .equals(new Type(symbolTable.getClassName(), false)))){
            return 0;
        }

        Method method = symbolTable.findMethod(node.getJmmChild(1).get("name"));

        if (method == null) return 0; // Handled in another analyser

        List<Symbol> method_args = method.getParameters();

        JmmNode call_args = node.getJmmChild(2);

        if (method_args.size() != call_args.getChildren().size()) {
            addReport(new Report(
                    ReportType.ERROR, Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("col")),
                    "Method " + node.getJmmChild(1).get("name") +
                            " call doesn't match the number of parameter required by the method."));
        } else {
            for (int i = 0; i < method_args.size(); i++) {
                Type cur_arg_type = this.getJmmNodeType(call_args.getJmmChild(i).getJmmChild(0), symbolTable);
                if (!this.compatibleType(method_args.get(i).getType(), cur_arg_type, symbolTable)) {
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
        }

        return 0;
    }
}
