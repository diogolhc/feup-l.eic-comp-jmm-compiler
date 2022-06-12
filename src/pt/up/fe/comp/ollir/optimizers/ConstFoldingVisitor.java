package pt.up.fe.comp.ollir.optimizers;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

public class ConstFoldingVisitor extends AJmmVisitor<String, Boolean> {

    public ConstFoldingVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit(AstNode.BIN_OP, this::binOpVisit);
        addVisit(AstNode.NOT, this::notVisit);
    }

    public Boolean defaultVisit(JmmNode jmmNode, String dummy) {
        boolean changes = false;

        for (JmmNode child : jmmNode.getChildren()) {
            changes = visit(child) || changes;
        }

        return changes;
    }

    private Boolean notVisit(JmmNode notNode, String dummy) {
        boolean changes = visit(notNode.getJmmChild(0));

        JmmNode child = notNode.getJmmChild(0);
        if (child.getKind().equals(AstNode.BOOL)) {
            String value = child.get("value");

            JmmNode literalNode = new JmmNodeImpl(AstNode.BOOL);
            literalNode.put("col", notNode.get("col"));
            literalNode.put("line", notNode.get("line"));

            if (value.equals("true")) {
                literalNode.put("value", "false");
            } else {
                literalNode.put("value", "true");
            }

            notNode.replace(literalNode);

            return true;
        }

        return changes;
    }

    private Boolean binOpVisit(JmmNode binOpNode, String dummy) {
        JmmNode leftNode = binOpNode.getJmmChild(0);
        JmmNode rightNode = binOpNode.getJmmChild(1);

        boolean changes = visit(leftNode);
        changes = visit(rightNode) || changes;

        // NOTE: these might be different than the previous ones
        leftNode = binOpNode.getJmmChild(0);
        rightNode = binOpNode.getJmmChild(1);

        boolean hasBoolOperands = leftNode.getKind().equals(AstNode.BOOL) && rightNode.getKind().equals(AstNode.BOOL);
        boolean hasIntOperands = leftNode.getKind().equals(AstNode.INT_LITERAL) && rightNode.getKind().equals(AstNode.INT_LITERAL);

        // shortcut evaluation on '&&'
        if (binOpNode.get("op").equals("and") && leftNode.getKind().equals(AstNode.BOOL)) {
            // true && x => x
            if (leftNode.get("value").equals("true")) {
                binOpNode.replace(rightNode);
                return true;

                // false && x => false
            } else if (leftNode.get("value").equals("false")) {
                binOpNode.replace(leftNode);
                return true;
            }

        // bin op folding
        } else if (hasBoolOperands || hasIntOperands) {
            String value = ConstFoldingVisitor.parseBinOp(binOpNode.get("op"), leftNode.get("value"), rightNode.get("value"));

            JmmNode literalNode;

            if (value.equals("true") || value.equals("false")) {
                literalNode = new JmmNodeImpl(AstNode.BOOL);
            } else {
                literalNode = new JmmNodeImpl(AstNode.INT_LITERAL);
            }

            literalNode.put("value", value);

            literalNode.put("col", binOpNode.get("col"));
            literalNode.put("line", binOpNode.get("line"));

            binOpNode.replace(literalNode);

            return true;
        }

        return changes;
    }

    public static String parseBinOp(String op, String lhs, String rhs) {
        if (op.equals("and")) {
            if (lhs.equals("true") && rhs.equals("true")) {
                return "true";
            } else {
                return "false";
            }

        } else {
            int lhsInt = Integer.parseInt(lhs);
            int rhsInt = Integer.parseInt(rhs);

            return switch (op) {
                case "addition" -> String.valueOf(lhsInt + rhsInt);
                case "subtraction" -> String.valueOf(lhsInt - rhsInt);
                case "multiplication" -> String.valueOf(lhsInt * rhsInt);
                case "division" -> String.valueOf(lhsInt / rhsInt);
                case "lessThan" -> (lhsInt < rhsInt) ? "true" : "false";
                default -> throw new IllegalStateException("Unexpected value: " + op); // TODO remove this (?)
            };
        }
    }

}
