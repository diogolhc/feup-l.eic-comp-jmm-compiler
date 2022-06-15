package pt.up.fe.comp.ollir.optimization.optimizers;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Map;
import java.util.stream.Collectors;

public class ConstPropagationVisitor extends AJmmVisitor<ConstPropagationParam, Boolean> {

    public ConstPropagationVisitor() {
        setDefaultVisit(this::defaultVisit);

        addVisit(AstNode.START, this::iterateVisit);
        addVisit(AstNode.PROGRAM, this::iterateVisit);
        addVisit(AstNode.CLASS_DECL, this::iterateVisit);
        addVisit(AstNode.SCOPE, this::iterateVisit);
        addVisit(AstNode.BIN_OP, this::iterateVisit);
        addVisit(AstNode.NOT, this::iterateVisit);
        addVisit(AstNode.RETURN, this::iterateVisit);
        addVisit(AstNode.METHOD_BODY_NO_FINAL_RETURN, this::iterateVisit);
        addVisit(AstNode.CONDITION, this::iterateVisit);

        addVisit(AstNode.METHOD_DECL, this::methodAndMainDeclarationVisit);
        addVisit(AstNode.MAIN_DECL, this::methodAndMainDeclarationVisit);

        addVisit(AstNode.EXPRESSION_DOT, this::expressionDotVisit);
        addVisit(AstNode.ASSIGNMENT, this::assignmentVisit);
        addVisit(AstNode.ID, this::idVisit);
        addVisit(AstNode.EXPRESSION_NEW, this::expressionNewVisit);
        addVisit(AstNode.LENGTH, this::lengthVisit);
        addVisit(AstNode.ARRAY_ACCESS, this::arrayAccessVisit);
        addVisit(AstNode.IF_STATEMENT, this::ifStatementVisit);
        addVisit(AstNode.WHILE, this::whileVisit);
    }

    public Boolean defaultVisit(JmmNode jmmNode, ConstPropagationParam constPropagationParam) {
        return false;
    }

    public Boolean iterateVisit(JmmNode jmmNode, ConstPropagationParam constPropagationParam) {
        boolean changes = false;

        for (JmmNode child : jmmNode.getChildren()) {
            changes = visit(child, constPropagationParam) || changes;
        }

        return changes;
    }

    private Boolean methodAndMainDeclarationVisit(JmmNode methodAndMainDeclarationNode, ConstPropagationParam constPropagationParam) {
        constPropagationParam.getConstants().clear();
        return iterateVisit(methodAndMainDeclarationNode, constPropagationParam);
    }

    private Boolean idVisit(JmmNode idNode, ConstPropagationParam constPropagationParam) {
        if (constPropagationParam.isToJustRemoveAssigned()) {
            return false;
        }

        String name = idNode.get("name");

        Map<String, String> constants = constPropagationParam.getConstants();
        if (constants.containsKey(name)) {

            JmmNode literalNode;
            switch (constants.get(name)) {
                case "true", "false" -> literalNode = new JmmNodeImpl(AstNode.BOOL);
                default -> literalNode = new JmmNodeImpl(AstNode.INT_LITERAL);
            }

            literalNode.put("value", constants.get(name));

            literalNode.put("col", idNode.get("col"));
            literalNode.put("line", idNode.get("line"));

            idNode.replace(literalNode);

            return true;
        }

        return false;
    }

    private Boolean assignmentVisit(JmmNode assignmentNode, ConstPropagationParam constPropagationParam) {
        boolean changes = false;

        boolean assignmentToArrayAtIndex = assignmentNode.getJmmChild(0).getNumChildren() > 0;

        if (!constPropagationParam.isToJustRemoveAssigned()) {
            changes = visit(assignmentNode.getJmmChild(1).getJmmChild(0), constPropagationParam) || changes;

            if (assignmentToArrayAtIndex) {
                changes = visit(assignmentNode.getJmmChild(0).getJmmChild(0), constPropagationParam) || changes;
                return changes;
            }
        }

        if (assignmentToArrayAtIndex) {
            return false;
        }

        String assigneeName = assignmentNode.getJmmChild(0).get("name");
        constPropagationParam.getConstants().remove(assigneeName);

        if (!constPropagationParam.isToJustRemoveAssigned()) {
            JmmNode assignedNode = assignmentNode.getJmmChild(1).getJmmChild(0);
            switch (assignedNode.getKind()) {
                case AstNode.INT_LITERAL, AstNode.BOOL -> constPropagationParam.getConstants().put(assigneeName, assignedNode.get("value"));
            }
        }

        return changes;
    }

    private Boolean whileVisit(JmmNode whileNode, ConstPropagationParam constPropagationParam) {
        boolean change = false;

        JmmNode extraCondNode = whileNode.getJmmChild(whileNode.getNumChildren() - 3); // added by WhileConditionDuplicatorVisitor
        JmmNode condNode = whileNode.getJmmChild(whileNode.getNumChildren() - 2);
        JmmNode scopeNode = whileNode.getJmmChild(whileNode.getNumChildren() - 1);

        if (constPropagationParam.isToJustRemoveAssigned()) {
            change = visit(scopeNode, constPropagationParam) || change;

        } else {
            // visit extra node to detect do_whiles at compile time
            change = visit(extraCondNode, constPropagationParam) || change;

            // 1st remove all that are assigned inside the scope
            constPropagationParam.setToJustRemoveAssigned(true);
            change = visit(scopeNode, constPropagationParam) || change;
            constPropagationParam.setToJustRemoveAssigned(false);

            change = visit(scopeNode, constPropagationParam) || change;

            change = visit(condNode, constPropagationParam) || change;

        }

        return change;
    }

    private Boolean ifStatementVisit(JmmNode ifStatementNode, ConstPropagationParam constPropagationParam) {
        boolean change = false;

        JmmNode ifNode = ifStatementNode.getJmmChild(0);
        JmmNode elseNode = ifStatementNode.getJmmChild(1);

        JmmNode ifCondNode = ifNode.getJmmChild(ifNode.getNumChildren() - 2);
        JmmNode ifScopeNode = ifNode.getJmmChild(ifNode.getNumChildren() - 1);
        JmmNode elseScopeNode = elseNode.getJmmChild(0);

        if (!constPropagationParam.isToJustRemoveAssigned()) {
            change = visit(ifCondNode, constPropagationParam) || change;
        }

        ConstPropagationParam constPropagationParam1 = new ConstPropagationParam(constPropagationParam);
        change = visit(ifScopeNode, constPropagationParam1) || change;

        ConstPropagationParam constPropagationParam2 = new ConstPropagationParam(constPropagationParam);
        change = visit(elseScopeNode, constPropagationParam2) || change;

        ConstPropagationVisitor.intersectMaps(
                constPropagationParam.getConstants(),
                constPropagationParam1.getConstants(),
                constPropagationParam2.getConstants()
        );

        return change;
    }

    private Boolean expressionNewVisit(JmmNode expressionNewNode, ConstPropagationParam constPropagationParam) {
        if (constPropagationParam.isToJustRemoveAssigned()) {
            return false;
        }

        boolean isArrayNew = expressionNewNode.getNumChildren() > 0;
        if (isArrayNew) {
            return visit(expressionNewNode.getJmmChild(0), constPropagationParam);
        }

        return false;
    }

    private Boolean arrayAccessVisit(JmmNode arrayAccessNode, ConstPropagationParam constPropagationParam) {
        if (constPropagationParam.isToJustRemoveAssigned()) {
            return false;
        }

        JmmNode array = arrayAccessNode.getJmmChild(0);
        JmmNode index = arrayAccessNode.getJmmChild(1);

        boolean changes = false;
        // NOTE: if index is already an id, it should not be promoted to const because OLLIR doesn't accept immediate index values
        if (!index.getKind().equals(AstNode.ID)) {
            changes = visit(index, constPropagationParam) || changes;
        }

        if (array.getKind().equals(AstNode.EXPRESSION_DOT)) {
            changes = visit(array, constPropagationParam) || changes;
        }

        return changes;
    }

    private Boolean lengthVisit(JmmNode lengthNode, ConstPropagationParam constPropagationParam) {
        JmmNode child = lengthNode.getJmmChild(0);
        if (child.getKind().equals(AstNode.EXPRESSION_DOT)) {
            return visit(child, constPropagationParam);
        }
        return false;
    }

    private Boolean expressionDotVisit(JmmNode expressionDotNode, ConstPropagationParam constPropagationParam) {
        boolean change = false;

        JmmNode argsNode = expressionDotNode.getJmmChild(2);
        for (JmmNode argNode : argsNode.getChildren()) {
            change = visit(argNode.getJmmChild(0), constPropagationParam) || change;
        }

        return change;
    }


    private static void intersectMaps(Map<String, String> res, Map<String, String> map1, Map<String, String> map2) {
        Map<String, String> mapFiltered = map2.entrySet().stream().filter(map -> {
            if (map1.containsKey(map.getKey())) {
                String val = map1.get(map.getKey());
                return val.equals(map.getValue());
            } else {
                return false;
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        res.clear();
        res.putAll(mapFiltered);
    }

}
