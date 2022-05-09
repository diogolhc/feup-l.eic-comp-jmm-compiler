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

public class OllirGenerator extends AJmmVisitor<String, String> {
    private final StringBuilder code;
    private final SymbolTable symbolTable;
    private int tempVar;
    private int ifThenElseNum;

    public OllirGenerator(SymbolTable symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.tempVar = 1;
        this.ifThenElseNum = 1;

        // TODO check if there are already symbols "t{n}" in order to avoid them
        // just add an '_' to every user variable

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
    }

    public int getAndAddIfThenElseNum() {
        return this.ifThenElseNum++;
    }

    public String getCode() {
        return code.toString();
    }

    private String startVisit(JmmNode start, String dummy) {
        visit(start.getChildren().get(0));
        return "";
    }

    private String programVisit(JmmNode program, String dummy) {
        for (var importString : symbolTable.getImports()) {
            code.append("import ").append(importString).append(";\n");
        }

        code.append("\n");

        for (var child : program.getChildren()) {
            visit(child);
        }

        return "";
    }

    private String classDeclVisit(JmmNode classDecl, String dummy) {
        code.append("public ").append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();

        if (superClass != null) {
            code.append(" extends ").append(superClass);
        }

        code.append(" {\n");

        // fields
        for (var field : symbolTable.getFields()) {
            code.append("\t.field ").append(field.getName()).append(OllirUtils.getCode(field.getType())).append(";\n");
        }

        code.append("\n");

        // default constructor
        code.append("\t.construct ").append(symbolTable.getClassName()).append("().V {\n")
            .append("\t\tinvokespecial(this, \"<init>\").V;\n")
            .append("\t}\n");

        // methods
        for (var child : classDecl.getChildren()) {
            code.append("\n");
            visit(child);
        }

        code.append("}\n");

        return "";
    }

    private String methodDeclVisit(JmmNode methodDecl, String dummy) {
        var methodName = "";
        List<JmmNode> statements;

        boolean isMain = methodDecl.getKind().equals(AstNode.MAIN_DECL);

        if (isMain) {
            methodName = "main";
            code.append("\t.method public static ").append(methodName).append("(");

            statements = methodDecl.getJmmChild(0).getChildren();

        } else {
            // First child of MethodDeclaration is MethodHeader
            JmmNode methodHeader = methodDecl.getJmmChild(0);
            methodName = methodHeader.get("name");

            code.append("\t.method public ").append(methodName).append("(");

            statements = methodDecl.getJmmChild(1).getChildren();
        }

        var params = symbolTable.getParameters(methodName);

        var paramCode = params.stream()
                .map(OllirUtils::getCode).
                collect(Collectors.joining(", "));

        code.append(paramCode).append(")");
        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodName)));

        code.append(" {\n");

        for (var child : statements) {
            visit(child);
        }

        // return
        if (isMain) {
            code.append("\t\tret.V;\n");
        } else {
            String returnReg = visit(methodDecl.getJmmChild(2).getJmmChild(0));
            code.append("\t\tret").append(OllirUtils.getCode(symbolTable.getReturnType(methodName))).append(" ")
                    .append(returnReg).append(";\n");
        }

        code.append("\t}\n");

        return "";
    }

    private String expressionDotVisit(JmmNode expressionDot, String inferedType) {
        String firstArg;
        if (expressionDot.getJmmChild(0).getKind().equals(AstNode.THIS)) {
            firstArg = "this";
        } else {
            if (expressionDot.getJmmChild(0).getKind().equals(AstNode.EXPRESSION_DOT)) {
                firstArg = visit(expressionDot.getJmmChild(0));
            } else {
                firstArg = expressionDot.getJmmChild(0).get("name");

            }
        }

        String invokeType = OllirUtils.getInvokeType(firstArg, symbolTable);

        String method = expressionDot.getJmmChild(1).get("name");


        String returnString;
        if (inferedType == null) {
            String type;
            if (firstArg.equals("this")) {
                type = symbolTable.getClassName();
            } else {
                String[] firstArgsSplit = firstArg.split("\\.");
                if (firstArgsSplit.length == 1) {
                    type = firstArgsSplit[0];
                } else {
                    type = firstArgsSplit[1];
                }
            }

            if (type.equals(symbolTable.getClassName())) {
                Type returnType = symbolTable.getReturnType(method);
                returnString = OllirUtils.getOllirType(returnType.getName());
            } else {
                // This is the case where an unknown method is not the last method call on a chain of calls
                // and due to that, it's not possible to know its return type
                returnString = ".V";
            }
        } else {
            returnString = inferedType;
        }

        List<String> args = new ArrayList<>();
        for (JmmNode arg : expressionDot.getJmmChild(2).getChildren()) {
            args.add(visit(arg.getJmmChild(0)));
        }

        code.append("\t\t");

        if (!returnString.equals(".V")) {
            code.append("t").append(tempVar).append(returnString).append(" :=").append(returnString).append(" ");
        }

        code.append(invokeType).append("(").append(firstArg).append(", \"").append(method).append("\"");

        // args
        for (String arg : args) {
            code.append(", ").append(arg);
        }

        code.append(")")
            .append(returnString)
            .append(";\n");

        return "t" + tempVar++ + returnString;
    }

    private String binOpVisit(JmmNode binOp, String dummy) {

        String returnType = OllirUtils.getReturnType(binOp.get("op"));

        String assignmentType = OllirUtils.getOperandType(binOp.get("op"));

        String lhs = visit(binOp.getJmmChild(0), assignmentType);
        String rhs = visit(binOp.getJmmChild(1), assignmentType);

        code.append("\t\tt").append(tempVar).append(returnType).append(" :=")
                .append(returnType).append(" ").append(lhs).append(" ")
            .append(OllirUtils.getOperator(binOp.get("op")))
                .append(" ").append(rhs).append(";\n");

        return "t" + tempVar++ + returnType;
    }

    private String intLiteralVisit(JmmNode intLiteral, String dummy) {
        return intLiteral.get("value") + ".i32";
    }

    private String boolVisit(JmmNode bool, String dummy) {
        return OllirUtils.getBoolValue(bool.get("value")) + ".bool";
    }

    private String notVisit(JmmNode not, String dummy) {
        String child = visit(not.getJmmChild(0));

        code.append("\t\tt").append(tempVar).append(".bool").append(" :=.bool !.bool ").append(child).append(";\n");

        return "t" + tempVar++ + ".bool";
    }

    private String assignmentVisit(JmmNode assignment, String dummy) {
        String assigneeName = assignment.getJmmChild(0).get("name");
        String assignee = visit(assignment.getJmmChild(0));
        String type = "";

        String methodName = getCurrentMethodName(assignment);

        // if assignment to array at index
        if (assignment.getJmmChild(0).getNumChildren() > 0) {
            type = ".i32";
        } else {
            try {
                type = OllirUtils.getOllirType(((SymbolTableImpl) symbolTable).findVariable(methodName, assigneeName).getType().getName());
            } catch (VarNotInScopeException ignored) {}
        }

        String child = visit(assignment.getJmmChild(1).getJmmChild(0), type);

        if (((SymbolTableImpl) symbolTable).isField(methodName, assignee)) {
            code.append("\t\tputfield(this, ").append(assignee).append(", ").append(child).append(").V;\n");
        } else {
            code.append("\t\t").append(assignee).append(" :=").append(type).append(" ").append(child).append(";\n");
        }

        return "";
    }

    private String idVisit(JmmNode id, String dummy) {
        try {
            String idName = id.get("name");
            String stringType = OllirUtils.getOllirType(((SymbolTableImpl) symbolTable).findVariable(getCurrentMethodName(id), idName).getType().getName());

            String methodName = getCurrentMethodName(id);

            if (((SymbolTableImpl) symbolTable).isField(methodName, idName)) {
                code.append("\t\tt").append(tempVar).append(stringType).append(" :=").append(stringType).append(" getfield(this, ")
                    .append(idName).append(stringType).append(")").append(stringType).append(";\n");

                return "t" + tempVar++ + stringType;
            } else {
                String ollirLikeReference = ((SymbolTableImpl) symbolTable).getOllirLikeReference(methodName, idName);
                return ollirLikeReference + idName + stringType;
            }

        } catch (VarNotInScopeException ignored) {}

        return "";
    }

    private String expressionNewVisit(JmmNode expressionNew, String dummy) {
        boolean isArrayNew = expressionNew.getNumChildren() > 0;

        String type;
        String arraySizeVar = null;
        if (isArrayNew) {
            type = "array.i32";
            arraySizeVar = visit(expressionNew.getJmmChild(0));
        } else {
            type = expressionNew.get("name");
        }

        code.append("\t\tt").append(tempVar).append(".").append(type).append(" :=.").append(type).append(" ")
            .append("new(");

        if (isArrayNew) {
            code.append("array, ").append(arraySizeVar);
        } else {
            code.append(type);
        }

        code.append(").").append(type).append(";\n");

        if (!isArrayNew) {
            code.append("\t\tinvokespecial(t").append(tempVar).append(".").append(type).append(", \"<init>\").V;\n");
        }

        return "t" + tempVar++ + "." + type;
    }

    private String assigneeVisit(JmmNode assignee, String dummy) {
        String idLikeVisit = idVisit(assignee, "");

        // if is assignment to array at index
        if (assignee.getNumChildren() > 0) {
            String index = visit(assignee.getJmmChild(0));

            boolean isImmediateValueIndex = OllirUtils.isIntegerString(index.substring(0,1));
            if (isImmediateValueIndex) {
                index = getImmediateIndexIntoReg(index);
            }

            return OllirUtils.getArrayIdWithoutType(idLikeVisit) + "[" + index + "].i32";
        } else {
            return idLikeVisit;
        }
    }

    private String lengthVisit(JmmNode lengthNode, String dummy) {
        String child = visit(lengthNode.getJmmChild(0));
        code.append("\t\tt").append(tempVar).append(".i32 :=.i32 arraylength(").append(child).append(").i32;\n");
        return "t" + tempVar++ + ".i32";
    }

    private String arrayAccessVisit(JmmNode arrayAccess, String dummy) {
        String child = visit(arrayAccess.getJmmChild(0));

        String id = OllirUtils.getArrayIdWithoutType(child);

        String index = visit(arrayAccess.getJmmChild(1));
        String indexReg = index;

        boolean isImmediateValueIndex = OllirUtils.isIntegerString(index.substring(0,1));
        if (isImmediateValueIndex) {
            indexReg = getImmediateIndexIntoReg(index);
        }

        code.append("\t\tt").append(tempVar).append(".i32 :=.i32 ").append(id)
            .append("[").append(indexReg).append("].i32;\n");

        return "t" + tempVar++ + ".i32";
    }

    private String ifStatementVisit(JmmNode ifStatement, String dummy) {
        JmmNode condition = ifStatement.getJmmChild(0).getJmmChild(0).getJmmChild(0);
        JmmNode ifTrueScope = ifStatement.getJmmChild(0).getJmmChild(1);
        JmmNode ifFalseScope = ifStatement.getJmmChild(1).getJmmChild(0);

        int ifThenElseNum = getAndAddIfThenElseNum();

        String conditionReg = visit(condition);

        code.append("\t\tif (").append(conditionReg).append(") goto ifTrue").append(ifThenElseNum).append(";\n");

        for (JmmNode node : ifFalseScope.getChildren()) {
            visit(node);
        }
        code.append("\t\tgoto endIf").append(ifThenElseNum).append(";\n");

        code.append("\t\tifTrue").append(ifThenElseNum).append(":\n");

        for (JmmNode node : ifTrueScope.getChildren()) {
            visit(node);
        }
        code.append("\t\tendIf").append(ifThenElseNum++).append(":\n");

        return "";
    }





    private String getImmediateIndexIntoReg(String index) {
        code.append("\t\tt").append(tempVar).append(".i32 :=.i32 ").append(index).append(";\n");
        return "t" + tempVar++ + ".i32";
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
