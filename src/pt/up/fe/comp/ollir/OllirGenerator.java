package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.analysis.table.VarNotInScopeException;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<OllirInference, String> {
    private final StringBuilder code;
    private final SymbolTable symbolTable;
    private int indentationLevel;
    private int tempVarNum;
    private int ifThenElseNum;
    private int whileNum;
    private final boolean optimize;

    public OllirGenerator(SymbolTable symbolTable, boolean optimize) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.optimize = optimize;
        this.indentationLevel = 0;
        this.tempVarNum = 0;
        this.ifThenElseNum = 1;
        this.whileNum = 1;

        addVisit(AstNode.START, this::startVisit);
        addVisit(AstNode.PROGRAM, this::programVisit);
        addVisit(AstNode.CLASS_DECL, this::classDeclVisit);
        addVisit(AstNode.METHOD_DECL, this::methodDeclVisit);
        addVisit(AstNode.MAIN_DECL, this::methodDeclVisit);
        addVisit(AstNode.EXPRESSION_DOT, this::expressionDotVisit);
        addVisit(AstNode.BIN_OP, this::binOpVisit);
        addVisit(AstNode.INT_LITERAL, this::intLiteralVisit);
        addVisit(AstNode.BOOL, this::boolVisit);
        addVisit(AstNode.NOT, this::notVisit);
        addVisit(AstNode.ASSIGNMENT, this::assignmentVisit);
        addVisit(AstNode.ID, this::idVisit);
        addVisit(AstNode.EXPRESSION_NEW, this::expressionNewVisit);
        addVisit(AstNode.ASSIGNEE, this::assigneeVisit);
        addVisit(AstNode.LENGTH, this::lengthVisit);
        addVisit(AstNode.ARRAY_ACCESS, this::arrayAccessVisit);
        addVisit(AstNode.IF_STATEMENT, this::ifStatementVisit);
        addVisit(AstNode.WHILE, this::whileVisit);
        addVisit(AstNode.SCOPE, this::scopeVisit);
        addVisit(AstNode.THIS, this::thisVisit);
    }

    private void incrementIndentation() {
        this.indentationLevel++;
    }

    private void decrementIndentation() {
        this.indentationLevel--;
    }

    private String getIndentation() {
        return "\t".repeat(indentationLevel);
    }

    private int getAndAddTempVar(JmmNode currentNode) {
        String currentMethodName = getCurrentMethodName(currentNode);

        while (true) {
            this.tempVarNum++;
            try {
                ((SymbolTableImpl) symbolTable).findVariable(currentMethodName, "t" + tempVarNum);
            } catch (VarNotInScopeException e) {
                return this.tempVarNum;
            }
        }
    }

    private int getAndAddIfThenElseNum() {
        return this.ifThenElseNum++;
    }

    private int getAndAddWhileNum() {
        return this.whileNum++;
    }

    public String getCode() {
        return code.toString();
    }

    private String startVisit(JmmNode start, OllirInference dummy) {
        visit(start.getChildren().get(0));
        return "";
    }

    private String programVisit(JmmNode program, OllirInference dummy) {
        for (var importString : symbolTable.getImports()) {
            code.append("import ").append(importString).append(";\n");
        }

        code.append("\n");

        for (var child : program.getChildren()) {
            visit(child);
        }

        return "";
    }

    private String classDeclVisit(JmmNode classDecl, OllirInference dummy) {
        code.append(getIndentation()).append("public ").append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();

        if (superClass != null) {
            code.append(" extends ").append(superClass);
        }

        code.append(" {\n");

        this.incrementIndentation();

        // fields
        for (var field : symbolTable.getFields()) {
            code.append(getIndentation()).append(".field ").append(field.getName()).append(OllirUtils.getOllirType(field.getType())).append(";\n");
        }

        code.append("\n");

        // default constructor
        code.append(getIndentation()).append(".construct ").append(symbolTable.getClassName()).append("().V {\n");
        this.incrementIndentation();
        code.append(getIndentation()).append("invokespecial(this, \"<init>\").V;\n");
        this.decrementIndentation();
        code.append(getIndentation()).append("}\n");

        // methods
        for (var child : classDecl.getChildren().subList(symbolTable.getFields().size(), classDecl.getNumChildren())) {
            code.append("\n");
            visit(child);
        }

        this.decrementIndentation();

        code.append(getIndentation()).append("}\n");

        return "";
    }

    private String methodDeclVisit(JmmNode methodDecl, OllirInference dummy) {
        var methodName = "";
        List<JmmNode> statements;

        boolean isMain = methodDecl.getKind().equals(AstNode.MAIN_DECL);

        if (isMain) {
            methodName = "main";
            code.append(getIndentation()).append(".method public static ").append(methodName).append("(");

            statements = methodDecl.getJmmChild(0).getChildren();

        } else {
            // First child of MethodDeclaration is MethodHeader
            JmmNode methodHeader = methodDecl.getJmmChild(0);
            methodName = methodHeader.get("name");

            code.append(getIndentation()).append(".method public ").append(methodName).append("(");

            statements = methodDecl.getJmmChild(1).getChildren();
        }

        var params = symbolTable.getParameters(methodName);

        var paramCode = params.stream()
                .map(OllirUtils::getCode).
                collect(Collectors.joining(", "));

        code.append(paramCode).append(")");
        code.append(OllirUtils.getOllirType(symbolTable.getReturnType(methodName)));

        code.append(" {\n");

        this.incrementIndentation();

        for (var child : statements) {
            visit(child);
        }

        // return
        String returnString, returnReg;
        if (isMain) {
            returnString = ".V";
            returnReg = "";
        } else {
            returnString = OllirUtils.getOllirType(symbolTable.getReturnType(methodName)) + " ";
            returnReg = visit(methodDecl.getJmmChild(2).getJmmChild(0), new OllirInference(returnString, true));
        }
        code.append(getIndentation()).append("ret").append(returnString)
                .append(returnReg).append(";\n");

        this.decrementIndentation();

        code.append(getIndentation()).append("}\n");

        return "";
    }

    private String expressionDotVisit(JmmNode expressionDot, OllirInference ollirInference) {
        String firstChildKind = expressionDot.getJmmChild(0).getKind();
        String firstArg;

        if (firstChildKind.equals(AstNode.THIS)) {
            firstArg = "this";
        } else if (firstChildKind.equals(AstNode.EXPRESSION_DOT) || firstChildKind.equals(AstNode.EXPRESSION_NEW)) {
            firstArg = visit(expressionDot.getJmmChild(0));
        } else {
            firstArg = expressionDot.getJmmChild(0).get("name");
            try {
                firstArg += OllirUtils.getOllirType(((SymbolTableImpl) symbolTable).findVariable(getCurrentMethodName(expressionDot), firstArg).getType());
            } catch (VarNotInScopeException ignored) {}
        }

        String invokeType = OllirUtils.getInvokeType(firstArg, symbolTable);
        String method = expressionDot.getJmmChild(1).get("name");

        String returnType;
        if (ollirInference == null || ollirInference.getInferredType() == null) {
            String type;
            if (firstArg.equals("this")) {
                type = symbolTable.getClassName();
            } else if (((SymbolTableImpl) symbolTable).isExternalClass(getCurrentMethodName(expressionDot), firstArg)) {
                type = firstArg;
            } else {
                type = OllirUtils.getOllirIdWithoutParamNum(firstArg);
            }

            if (type.equals(symbolTable.getClassName())) {
                System.out.println("DEBUG: " + type + " " );
                Type retType = symbolTable.getReturnType(method);

                // if inherited method
                if (retType == null) {
                    // always void because if we could infer its type, it would not enter in the outer "if" and go to the "else"
                    returnType = ".V";
                } else {
                    returnType = OllirUtils.getOllirType(retType);
                }

            } else {
                // This is the case where an unknown method is not the last method call on a chain of calls
                // and due to that, it's not possible to know its return type
                returnType = ".V";
            }

        } else {
            returnType = ollirInference.getInferredType();
        }

        List<String> args = new ArrayList<>();
        for (JmmNode arg : expressionDot.getJmmChild(2).getChildren()) {
            args.add(visit(arg.getJmmChild(0), new OllirInference(true)));
        }

        StringBuilder operationString = new StringBuilder(invokeType + "(" + firstArg + ", \"" + method + "\"");
        // args
        for (String arg : args) {
            operationString.append(", ").append(arg);
        }
        operationString.append(")").append(returnType);

        if (ollirInference == null || ollirInference.getIsToAssignToTemp()) {
            code.append(getIndentation());

            int tempVar = getAndAddTempVar(expressionDot);

            if (!returnType.equals(".V")) {
                code.append("t").append(tempVar).append(returnType).append(" :=").append(returnType).append(" ");
            }
            code.append(operationString);
            code.append(";\n");

            return "t" + tempVar + returnType;
        } else {
            return operationString.toString();
        }

    }

    private String binOpVisit(JmmNode binOp, OllirInference ollirInference) {
        String returnType = OllirUtils.getReturnType(binOp.get("op"));

        String assignmentType = OllirUtils.getOperandType(binOp.get("op"));

        String lhs = visit(binOp.getJmmChild(0), new OllirInference(assignmentType, true));
        String rhs = visit(binOp.getJmmChild(1), new OllirInference(assignmentType, true));

        String operationString = lhs + " " + OllirUtils.getOperator(binOp.get("op")) + " " + rhs;

        if (ollirInference == null || ollirInference.getIsToAssignToTemp()) {
            int tempVar = getAndAddTempVar(binOp);

            code.append(getIndentation()).append("t").append(tempVar).append(returnType).append(" :=")
                    .append(returnType).append(" ")
                    .append(operationString)
                    .append(";\n");

            return "t" + tempVar + returnType;
        } else {
            return operationString;
        }
    }

    private String intLiteralVisit(JmmNode intLiteral, OllirInference dummy) {
        return intLiteral.get("value") + ".i32";
    }

    private String boolVisit(JmmNode bool, OllirInference dummy) {
        return OllirUtils.getBoolValue(bool.get("value")) + ".bool";
    }

    private String notVisit(JmmNode not, OllirInference ollirInference) {
        String child = visit(not.getJmmChild(0), new OllirInference(".bool", true));

        String operationString = "!.bool " + child;

        if (ollirInference == null || ollirInference.getIsToAssignToTemp()) {
            int tempVar = getAndAddTempVar(not);

            code.append(getIndentation()).append("t").append(tempVar).append(".bool").append(" :=.bool ").append(operationString).append(";\n");

            return "t" + tempVar + ".bool";
        } else {
            return operationString;
        }

    }

    private String assignmentVisit(JmmNode assignment, OllirInference dummy) {
        String assignee = visit(assignment.getJmmChild(0));
        String assigneeName = OllirUtils.getOllirIdWithoutTypeAndParamNum(assignee);

        String type = "";

        String methodName = getCurrentMethodName(assignment);

        // if assignment to array at index
        if (assignment.getJmmChild(0).getNumChildren() > 0) {
            type = ".i32";
        } else {
            try {
                type = OllirUtils.getOllirType(((SymbolTableImpl) symbolTable).findVariable(methodName, assigneeName).getType());
            } catch (VarNotInScopeException ignored) {}
        }

        if (((SymbolTableImpl) symbolTable).isField(methodName, assigneeName)) {
            String child = visit(assignment.getJmmChild(1).getJmmChild(0), new OllirInference(type, true));

            code.append(getIndentation()).append("putfield(this, ").append(assignee).append(", ").append(child).append(").V;\n");
        } else {
            String child = visit(assignment.getJmmChild(1).getJmmChild(0), new OllirInference(type, false));

            code.append(getIndentation()).append(assignee).append(" :=").append(type).append(" ").append(child).append(";\n");
        }

        return "";
    }

    private String idVisit(JmmNode id, OllirInference ollirInference) {
        try {
            String idName = id.get("name");
            String stringType = OllirUtils.getOllirType(((SymbolTableImpl) symbolTable).findVariable(getCurrentMethodName(id), idName).getType());

            String methodName = getCurrentMethodName(id);

            if (((SymbolTableImpl) symbolTable).isField(methodName, idName)) {

                String operationString = "getfield(this, " + idName + stringType + ")" + stringType;

                if (ollirInference == null || ollirInference.getIsToAssignToTemp()) {
                    int tempVar = getAndAddTempVar(id);

                    code.append(getIndentation()).append("t").append(tempVar).append(stringType).append(" :=").append(stringType).append(" ")
                        .append(operationString).append(";\n");

                    return "t" + tempVar + stringType;
                } else {
                    return operationString;
                }

            } else {
                String ollirLikeReference = ((SymbolTableImpl) symbolTable).getOllirLikeReference(methodName, idName);
                return ollirLikeReference + idName + stringType;
            }

        } catch (VarNotInScopeException ignored) {}

        return "";
    }

    private String expressionNewVisit(JmmNode expressionNew, OllirInference dummy) {
        boolean isArrayNew = expressionNew.getNumChildren() > 0;

        String type;
        String arraySizeVar = null;
        if (isArrayNew) {
            type = "array.i32";
            arraySizeVar = visit(expressionNew.getJmmChild(0));
        } else {
            type = expressionNew.get("name");
        }

        int tempVar = getAndAddTempVar(expressionNew);

        code.append(getIndentation()).append("t").append(tempVar).append(".").append(type).append(" :=.").append(type).append(" ")
                .append("new(");

        if (isArrayNew) {
            code.append("array, ").append(arraySizeVar);
        } else {
            code.append(type);
        }

        code.append(").").append(type).append(";\n");

        if (!isArrayNew) {
            code.append(getIndentation()).append("invokespecial(t").append(tempVar).append(".").append(type).append(", \"<init>\").V;\n");
        }

        return "t" + tempVar + "." + type;
    }

    private String assigneeVisit(JmmNode assignee, OllirInference dummy) {
        String idName = assignee.get("name");
        String stringType = null;
        try {
            stringType = OllirUtils.getOllirType(((SymbolTableImpl) symbolTable).findVariable(getCurrentMethodName(assignee), idName).getType());
        } catch (VarNotInScopeException ignored) {}

        String methodName = getCurrentMethodName(assignee);

        String ollirLikeReference = ((SymbolTableImpl) symbolTable).getOllirLikeReference(methodName, idName);
        String idLikeVisit = ollirLikeReference + idName + stringType;

        // if is assignment to array at index
        if (assignee.getNumChildren() > 0) {
            String index = visit(assignee.getJmmChild(0));

            boolean isField = ((SymbolTableImpl) symbolTable).isField(methodName, idName);
            if (isField) {
                int tempVar = getAndAddTempVar(assignee);

                code.append(getIndentation()).append("t").append(tempVar).append(".array.i32 :=.array.i32 ")
                        .append("getfield(this, ").append(idName).append(".array.i32).array.i32;\n");

                return "t" + tempVar + ".array.i32";
            } else {
                if (OllirUtils.isImmediateValueIndex(index)) {
                    index = getImmediateIndexIntoReg(index, assignee);
                }

                return OllirUtils.getArrayIdWithoutType(idLikeVisit) + "[" + index + "].i32";
            }
        } else {
            return idLikeVisit;
        }
    }

    private String lengthVisit(JmmNode lengthNode, OllirInference ollirInference) {
        String child = visit(lengthNode.getJmmChild(0), new OllirInference(".array.i32", true));

        String operationString = "arraylength(" + child + ").i32";

        if (ollirInference == null || ollirInference.getIsToAssignToTemp()) {
            int tempVar = getAndAddTempVar(lengthNode);

            code.append(getIndentation()).append("t").append(tempVar).append(".i32 :=.i32 ").append(operationString).append(";\n");

            return "t" + tempVar + ".i32";
        } else {
            return operationString;
        }
    }

    private String arrayAccessVisit(JmmNode arrayAccess, OllirInference ollirInference) {
        String child = visit(arrayAccess.getJmmChild(0), new OllirInference(".array.i32", true));

        String id = OllirUtils.getArrayIdWithoutType(child);

        String index = visit(arrayAccess.getJmmChild(1),  new OllirInference(".i32", true));
        String indexReg = index;

        if (OllirUtils.isImmediateValueIndex(index)) {
            indexReg = getImmediateIndexIntoReg(index, arrayAccess);
        }

        String operationString = id + "[" + indexReg + "].i32";

        if (ollirInference == null || ollirInference.getIsToAssignToTemp()) {
            int tempVar = getAndAddTempVar(arrayAccess);

            code.append(getIndentation()).append("t").append(tempVar).append(".i32 :=.i32 ")
                .append(operationString)
                .append(";\n");

            return "t" + tempVar + ".i32";
        } else {
            return operationString;
        }

    }

    private String ifStatementVisit(JmmNode ifStatement, OllirInference dummy) {
        JmmNode condition = ifStatement.getJmmChild(0).getJmmChild(0).getJmmChild(0);
        JmmNode ifTrueScope = ifStatement.getJmmChild(0).getJmmChild(1);
        JmmNode ifFalseScope = ifStatement.getJmmChild(1).getJmmChild(0);

        int ifThenElseNum = getAndAddIfThenElseNum();

        boolean isNotToAssignToTemp = condition.getKind().equals(AstNode.BIN_OP)
                || condition.getKind().equals(AstNode.BOOL)
                || condition.getKind().equals(AstNode.NOT)
                || condition.getKind().equals(AstNode.ID);

        String conditionRegOrExpression = visit(condition, new OllirInference(".bool", !isNotToAssignToTemp));

        code.append(getIndentation()).append("if (").append(conditionRegOrExpression).append(") goto ifTrue").append(ifThenElseNum).append(";\n");

        this.incrementIndentation();
        visit(ifFalseScope);

        code.append(getIndentation()).append("goto endIf").append(ifThenElseNum).append(";\n");
        this.decrementIndentation();

        code.append(getIndentation()).append("ifTrue").append(ifThenElseNum).append(":\n");

        this.incrementIndentation();
        visit(ifTrueScope);

        this.decrementIndentation();

        code.append(getIndentation()).append("endIf").append(ifThenElseNum).append(":\n");

        return "";
    }

    private String whileVisit(JmmNode whileNode, OllirInference dummy) {
        JmmNode condition = whileNode.getJmmChild(0).getJmmChild(0);
        JmmNode whileScope = whileNode.getJmmChild(1);

        int whileNum = getAndAddWhileNum();

        boolean isNotToAssignToTemp = condition.getKind().equals(AstNode.BIN_OP)
                || condition.getKind().equals(AstNode.BOOL)
                || condition.getKind().equals(AstNode.NOT)
                || condition.getKind().equals(AstNode.ID);

        if (this.optimize) {
            boolean isDoWhile = whileNode.get("do_while") != null && whileNode.get("do_while").equals("true");

            if (!isDoWhile) {
                code.append(getIndentation()).append("goto whileCondition").append(whileNum).append(";\n");
            }

            code.append(getIndentation()).append("whileBody").append(whileNum).append(":\n");

            this.incrementIndentation();
            visit(whileScope);

            if (!isDoWhile) {
                code.append(getIndentation()).append("whileCondition").append(whileNum).append(":\n");
            }

            String conditionRegOrExpression = visit(condition, new OllirInference(".bool", !isNotToAssignToTemp));
            code.append(getIndentation()).append("if (").append(conditionRegOrExpression).append(") goto whileBody").append(whileNum).append(";\n");
            this.decrementIndentation();

        } else {
            code.append(getIndentation()).append("whileCondition").append(whileNum).append(":\n");

            String conditionRegOrExpression = visit(condition, new OllirInference(".bool", !isNotToAssignToTemp));

            code.append(getIndentation()).append("if (").append(conditionRegOrExpression).append(") goto whileBody").append(whileNum).append(";\n");
            this.incrementIndentation();
            code.append(getIndentation()).append("goto endWhile").append(whileNum).append(";\n");
            this.decrementIndentation();

            code.append(getIndentation()).append("whileBody").append(whileNum).append(":\n");

            this.incrementIndentation();
            visit(whileScope);

            code.append(getIndentation()).append("goto whileCondition").append(whileNum).append(";\n");
            this.decrementIndentation();

            code.append(getIndentation()).append("endWhile").append(whileNum).append(":\n");
        }

        return "";
    }

    private String scopeVisit(JmmNode scope, OllirInference dummy) {
        for (JmmNode child : scope.getChildren()) {
            visit(child);
        }

        return "";
    }

    private String thisVisit(JmmNode thisNode, OllirInference dummy) {
        return "this." + symbolTable.getClassName();
    }


    private String getImmediateIndexIntoReg(String index, JmmNode currentNode) {
        int tempVar = getAndAddTempVar(currentNode);

        code.append(getIndentation()).append("t").append(tempVar).append(".i32 :=.i32 ").append(index).append(";\n");
        return "t" + tempVar + ".i32";
    }

    private static String getCurrentMethodName(JmmNode jmmNode) {
        Optional<JmmNode> methodDecl = jmmNode.getAncestor(AstNode.METHOD_DECL);
        String methodName = "main";
        if (methodDecl.isPresent()) {
            methodName = methodDecl.get().getJmmChild(0).get("name");
        }
        return methodName;
    }

}
