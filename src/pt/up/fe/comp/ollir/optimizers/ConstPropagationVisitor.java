package pt.up.fe.comp.ollir.optimizers;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Map;

public class ConstPropagationVisitor extends AJmmVisitor<Map<String, String>, Boolean> {

    public ConstPropagationVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit(AstNode.METHOD_DECL, this::methodAndMainDeclarationVisit);
        addVisit(AstNode.MAIN_DECL, this::methodAndMainDeclarationVisit);
        addVisit(AstNode.ID, this::idVisit);
        addVisit(AstNode.ASSIGNMENT, this::assignmentVisit);
        addVisit(AstNode.IF_STATEMENT, this::ifStatementVisit);
        addVisit(AstNode.WHILE, this::whileVisit);

        /// TODO be careful to not change ids to bool/intLiteral if they are identifiers of methods, imports... maybe use symbol table to be simpler
        // or visit explicitly instead of using a default visitor

    }

    public Boolean defaultVisit(JmmNode jmmNode, Map<String, String> constants) {
        boolean changes = false;
/*
        for (JmmNode child : jmmNode.getChildren()) {
            changes = visit(child, constants) || changes;
        }
*/
        return changes;
    }

    private Boolean methodAndMainDeclarationVisit(JmmNode methodAndMainDeclarationNode, Map<String, String> constants) {
        constants.clear();
        return defaultVisit(methodAndMainDeclarationNode, constants);
    }

    private Boolean idVisit(JmmNode idNode, Map<String, String> constants) {
        String name = idNode.get("name");

        if (constants.containsKey(name)) {

            JmmNode literalNode;
            switch (constants.get(name)) {
                case "true", "false" -> literalNode = new JmmNodeImpl("Bool");
                default -> literalNode = new JmmNodeImpl("IntLiteral");
            }

            literalNode.put("value", constants.get(name));

            literalNode.put("col", idNode.get("col"));
            literalNode.put("line", idNode.get("line"));

            idNode.replace(literalNode);

            return true;
        }

        return false;
    }

    private Boolean assignmentVisit(JmmNode assignmentNode, Map<String, String> constants) {
        boolean changes = visit(assignmentNode.getJmmChild(1), constants);

        String assigneeName = assignmentNode.getJmmChild(0).get("name");
        constants.remove(assigneeName);

        JmmNode assignedNode = assignmentNode.getJmmChild(1).getJmmChild(0);
        switch (assignedNode.getKind()) {
            case "IntLiteral", "Bool" -> constants.put(assigneeName, assignedNode.get("value"));
        }

        return changes;
    }

    private Boolean whileVisit(JmmNode whileNode, Map<String, String> constants) {
        // TODO
        return false;
    }

    private Boolean ifStatementVisit(JmmNode ifStatementNode, Map<String, String> constants) {
        // TODO
        return false;
    }

}
