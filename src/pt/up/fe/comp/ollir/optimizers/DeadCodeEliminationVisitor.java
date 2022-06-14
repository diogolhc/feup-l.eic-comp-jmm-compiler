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

        // while (false) {}
        if (properConditionNode.getKind().equals(AstNode.BOOL)) {
            if (properConditionNode.get("value").equals("false")) {
                whileNode.delete();
                return true;
            }

        // while (id1 < id1) {}
        } else if (properConditionNode.getKind().equals(AstNode.BIN_OP)) {
            JmmNode leftNode = properConditionNode.getJmmChild(0);
            JmmNode rightNode = properConditionNode.getJmmChild(1);

            if (properConditionNode.get("op") != null && properConditionNode.get("op").equals("lessThan")) {

                if (leftNode.getKind().equals(AstNode.ID) && rightNode.getKind().equals(AstNode.ID)) {
                    if (leftNode.get("name") != null && rightNode.get("name") != null
                            && leftNode.get("name").equals(rightNode.get("name"))) {

                        whileNode.delete();
                        return true;
                    }
                }
            }
        }

        return defaultVisit(whileNode.getJmmChild(whileNode.getNumChildren() - 1), null);
    }

    private Boolean ifStatementVisit(JmmNode ifStatementNode, String dummy) {
        JmmNode ifNode = ifStatementNode.getJmmChild(0);
        JmmNode conditionNode = ifNode.getJmmChild(0);
        JmmNode properConditionNode = conditionNode.getJmmChild(0);
        JmmNode ifScopeNode = ifNode.getJmmChild(1);
        JmmNode elseNode = ifStatementNode.getJmmChild(1);
        JmmNode elseScopeNode = elseNode.getJmmChild(0);

        JmmNode toMaintain = null;

        // if (true)
        // if (false)
        if (properConditionNode.getKind().equals(AstNode.BOOL)) {
            String value = properConditionNode.get("value");
            if (value.equals("true")) {
                toMaintain = ifScopeNode;
            } else if (value.equals("false")) {
                toMaintain = elseScopeNode;
            }

        // if (id1 < id2)
        } else if (properConditionNode.getKind().equals(AstNode.BIN_OP)) {
            JmmNode leftNode = properConditionNode.getJmmChild(0);
            JmmNode rightNode = properConditionNode.getJmmChild(1);

            if (properConditionNode.get("op") != null && properConditionNode.get("op").equals("lessThan")) {

                if (leftNode.getKind().equals(AstNode.ID) && rightNode.getKind().equals(AstNode.ID)) {
                    if (leftNode.get("name") != null && rightNode.get("name") != null
                            && leftNode.get("name").equals(rightNode.get("name"))) {

                        toMaintain = elseScopeNode;
                    }
                }
            }
        }

        if (toMaintain != null) {
            return removeIfButMaintainScopeThatIsAlwaysExecuted(ifStatementNode, toMaintain);
        } else {
            boolean changes = false;

            if (ifScopeNode != null && elseScopeNode != null) {
                changes = defaultVisit(ifScopeNode, null);
                changes = defaultVisit(elseScopeNode, null) || changes;
            }

            return changes;
        }
    }

    private boolean removeIfButMaintainScopeThatIsAlwaysExecuted(JmmNode ifStatementNode, JmmNode toMaintain) {
        if (ifStatementNode != null && toMaintain != null) {
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
        return false;
    }

}
