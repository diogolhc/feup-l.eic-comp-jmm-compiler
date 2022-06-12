package pt.up.fe.comp.ollir.optimizers;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class DoWhileAnnotatorVisitor extends AJmmVisitor<String, Boolean> {

    public DoWhileAnnotatorVisitor() {
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
        if (whileNode.getNumChildren() == 2)
            return false;

        JmmNode extraNode = whileNode.getJmmChild(0);
        if (extraNode.getKind().equals(AstNode.BOOL) && extraNode.get("value").equals("true")) {
            whileNode.put("do_while", "true");
        } else {
            whileNode.put("do_while", "false");
        }

        extraNode.delete();

        visit(whileNode.getJmmChild(1)); // scope

        return true;
    }

}
