package pt.up.fe.comp.ollir;

import pt.up.fe.comp.analysis.table.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

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
        addVisit(AstNode.MAIN_DECL, this::mainDeclVisit);

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
        code.append("public").append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();

        if (superClass != null) {
            code.append(" extends ").append(superClass);
        }

        code.append(" {\n");

        for (var child : classDecl.getChildren()) {
            visit(child);   // TODO : FIELDS
        }

        code.append("}\n");
        return 0;
    }

    private Integer methodDeclVisit(JmmNode methodDecl, Integer dummy) {
        // First child of MethodDeclaration is MethodHeader
        JmmNode methodHeader = methodDecl.getJmmChild(0);
        var methodName = methodHeader.get("name");

        code.append(".method public ").append(methodName).append("(");

        var params = symbolTable.getParameters(methodName);

        var paramCode = params.stream()
                .map(OllirUtils::getCode).
                collect(Collectors.joining(", "));

        code.append(paramCode).append(").");
        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodName)));

        code.append(" {\n");
        // TODO
        code.append("}\n");

        return 0;
    }

    private Integer mainDeclVisit(JmmNode mainDecl, Integer dummy) {
        var methodName = "main";
        code.append(".method public static ").append(methodName).append("(");

        var params = symbolTable.getParameters(methodName);

        var paramCode = params.stream()
                .map(OllirUtils::getCode).
                collect(Collectors.joining(", "));

        code.append(paramCode).append(").");
        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodName)));

        code.append(" {\n");
        // TODO
        code.append("}\n");

        return 0;
    }
}
