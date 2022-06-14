package pt.up.fe.comp.ollir.optimization.optimizers;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class WhileConditionDuplicatorVisitor extends AJmmVisitor<String, Boolean> {

    public WhileConditionDuplicatorVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit(AstNode.WHILE, this::whileVisit);
    }

    public Boolean defaultVisit(JmmNode jmmNode, String dummy) {
        boolean changes = false;

        for (JmmNode child : jmmNode.getChildren()) {
            changes = visit(child) || changes;
        }

        return changes;
    }

    private Boolean whileVisit(JmmNode whileNode, String dummy) {
        if (whileNode.getNumChildren() != 2)
            return false;

        JmmNode conditionNode = whileNode.getJmmChild(0);
        JmmNode copyProperCondition = JmmNode.fromJson(conditionNode.getJmmChild(0).toJson());
        whileNode.add(copyProperCondition, 0);

        visit(whileNode.getJmmChild(2));

        return true;
    }

}
