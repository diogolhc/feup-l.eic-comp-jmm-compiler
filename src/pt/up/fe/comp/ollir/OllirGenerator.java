package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer> {
    private final StringBuilder code;
    private final SymbolTable symbolTable;

    public OllirGenerator(SymbolTable symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;

        addVisit(AstNode.START, this::startVisit);
        addVisit(AstNode.PROGRAM, this::programVisit);
        addVisit(AstNode.CLASS_DECL, this::classDeclVisit);
        addVisit(AstNode.METHOD_DECL, this::methodDeclVisit);
        addVisit(AstNode.MAIN_DECL, this::methodDeclVisit);

    }

    public String getCode() {
        return code.toString();
    }

    private Integer startVisit(JmmNode start, Integer dummy) {
        visit(start.getChildren().get(0));
        return 0;
    }

    private Integer programVisit(JmmNode program, Integer dummy) {
        for (var importString : symbolTable.getImports()) {
            code.append("import ").append(importString).append(";\n");
        }

        for (var child : program.getChildren()) {
            visit(child);
        }
        return 0;
    }

    private Integer classDeclVisit(JmmNode classDecl, Integer dummy) {
        code.append("public ").append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();

        if (superClass != null) {
            code.append(" extends ").append(superClass);
        }

        code.append(" {\n");

        // fields
        for (var field : symbolTable.getFields()) {
            code.append("\t.field ").append(field.getName()).append(".").append(OllirUtils.getCode(field.getType())).append(";\n");
        }

        code.append("\n");

        // default constructor
        code.append("\t.construct ").append(symbolTable.getClassName()).append("().V {\n")
            .append("\t\tinvokespecial(this, \"<init>\").V;\n")
            .append("\t}");

        // methods
        for (var child : classDecl.getChildren()) {
            code.append("\n");
            visit(child);
        }

        code.append("}\n");
        return 0;
    }

    private Integer methodDeclVisit(JmmNode methodDecl, Integer dummy) {
        var methodName = "";

        if (methodDecl.getKind().equals(AstNode.MAIN_DECL)) {
            methodName = "main";
            code.append("\t.method public static ").append(methodName).append("(");
        } else {
            // First child of MethodDeclaration is MethodHeader
            JmmNode methodHeader = methodDecl.getJmmChild(0);
            methodName = methodHeader.get("name");

            code.append("\t.method public ").append(methodName).append("(");
        }

        var params = symbolTable.getParameters(methodName);

        var paramCode = params.stream()
                .map(OllirUtils::getCode).
                collect(Collectors.joining(", "));

        code.append(paramCode).append(").");
        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodName)));

        code.append(" {\n");

        // TODO
        List<JmmNode> statements;
        if (methodDecl.getKind().equals(AstNode.MAIN_DECL)) {
            statements = methodDecl.getJmmChild(0).getChildren();

        } else {
            statements = methodDecl.getJmmChild(1).getChildren();
        }

        /*for (var child : statements) {
            visit(child);
        }*/

        code.append("\t}\n");

        return 0;
    }

}
