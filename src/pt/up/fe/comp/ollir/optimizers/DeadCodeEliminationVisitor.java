package pt.up.fe.comp.ollir.optimizers;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class DeadCodeEliminationVisitor extends AJmmVisitor<String, Boolean> {

    public DeadCodeEliminationVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit(AstNode.WHILE, this::whileVisit);
        addVisit(AstNode.IF_STATEMENT, this::ifStatementVisit);
    }

    public Boolean defaultVisit(JmmNode jmmNode, String dummy) {
        boolean changes = false;

        for (JmmNode child : jmmNode.getChildren()) {
            changes = visit(child) || changes;
        }

        return changes;
    }

    private Boolean whileVisit(JmmNode whileNode, String dummy) {
        JmmNode conditionNode = whileNode.getJmmChild(whileNode.getNumChildren() - 2);
        JmmNode properConditionNode = conditionNode.getJmmChild(0);

        if (properConditionNode.getKind().equals(AstNode.BOOL)) {
            if (properConditionNode.get("value").equals("false")) {
                whileNode.delete();
                return true;
            }
        }

        return false;
    }

    private Boolean ifStatementVisit(JmmNode ifStatementNode, String dummy) {
        JmmNode ifNode = ifStatementNode.getJmmChild(0);
        JmmNode conditionNode = ifNode.getJmmChild(0);
        JmmNode properConditionNode = conditionNode.getJmmChild(0);
        JmmNode ifScopeNode = ifNode.getJmmChild(1);
        JmmNode elseNode = ifStatementNode.getJmmChild(1);
        JmmNode elseScopeNode = elseNode.getJmmChild(0);

        if (properConditionNode.getKind().equals(AstNode.BOOL)) {
            JmmNode toMaintain = null;

            String value = properConditionNode.get("value");
            if (value.equals("true")) {
                toMaintain = ifScopeNode;
            } else if (value.equals("false")) {
                toMaintain = elseScopeNode;
            }

            if (toMaintain != null) {
                JmmNode ifStatementParentNode = ifStatementNode.getJmmParent();
                int index = ifStatementNode.getIndexOfSelf();

                int i = 1;
                for (JmmNode child : toMaintain.getChildren()) {
                    ifStatementParentNode.add(child, index + i);
                    i += 1;
                }

                ifStatementNode.delete();

                return true;
            }
        }

        return false;
    }

}
