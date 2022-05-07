package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, String> {
    private final StringBuilder code;
    private final SymbolTable symbolTable;
    private int tempVar;

    public OllirGenerator(SymbolTable symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.tempVar = 1;

        // TODO check if there are already symbols "t{n}" in order to avoid them

        addVisit(AstNode.START, this::startVisit);
        addVisit(AstNode.PROGRAM, this::programVisit);
        addVisit(AstNode.CLASS_DECL, this::classDeclVisit);
        addVisit(AstNode.METHOD_DECL, this::methodDeclVisit);
        addVisit(AstNode.MAIN_DECL, this::methodDeclVisit);
        addVisit(AstNode.EXPRESSION_DOT, this::expressionDotVisit);
        addVisit(AstNode.BIN_OP, this::binOpVisit);
        addVisit(AstNode.INT_LITERAL, this::intLiteralVisit);
        addVisit(AstNode.BOOL, this::boolVisit);
    }

    public String getCode() {
        return code.toString();
    }

    private String startVisit(JmmNode start, Integer dummy) {
        visit(start.getChildren().get(0));
        return "";
    }

    private String programVisit(JmmNode program, Integer dummy) {
        for (var importString : symbolTable.getImports()) {
            code.append("import ").append(importString).append(";\n");
        }

        code.append("\n");

        for (var child : program.getChildren()) {
            visit(child);
        }

        return "";
    }

    private String classDeclVisit(JmmNode classDecl, Integer dummy) {
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

    private String methodDeclVisit(JmmNode methodDecl, Integer dummy) {
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

    private String expressionDotVisit(JmmNode expressionDot, Integer dummy) {
        // TODO this is just the base case
        String firstArg;
        if (expressionDot.getJmmChild(0).getKind().equals("This")) {
            firstArg = "this";
        } else {
            firstArg = expressionDot.getJmmChild(0).get("name");
        }

        String invokeType = OllirUtils.getInvokeType(firstArg, symbolTable);
        String returnString = ".V";

        String method = expressionDot.getJmmChild(1).get("name");
        Type returnType = symbolTable.getReturnType(method);

        if (returnType == null) {
            // TODO check type, assume VOID? or if it is inside an expression it is from the same type?
        } else {
            returnString = OllirUtils.getOllirType(returnType.getName());
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

    private String binOpVisit(JmmNode binOp, Integer dummy) {

        String lhs = visit(binOp.getJmmChild(0));
        String rhs = visit(binOp.getJmmChild(1));

        String returnType = OllirUtils.getReturnType(binOp.get("op"));

        code.append("\t\tt").append(tempVar).append(returnType).append(" :=")
                .append(returnType).append(" ").append(lhs).append(" ")
            .append(OllirUtils.getOperator(binOp.get("op")))
                .append(" ").append(rhs).append(";\n");

        return "t" + tempVar++ + returnType;
    }

    private String intLiteralVisit(JmmNode intLiteral, Integer dummy) {
        return intLiteral.get("value") + ".i32";
    }

    private String boolVisit(JmmNode bool, Integer dummy) {
        return OllirUtils.getBoolValue(bool.get("value")) + ".bool";
    }

}
