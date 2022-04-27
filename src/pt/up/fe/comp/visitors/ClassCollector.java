package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class ClassCollector extends AJmmVisitor<List<String>, Integer> {
    private int visits;
    private String name;
    private String superclass;

    public ClassCollector() {
        this.visits = 0;

        addVisit(AstNode.CLASS_DECL, this::visitClassDecl);
        addVisit(AstNode.METHOD_DECL, this::visitMethodDecl);
        addVisit(AstNode.METHOD_HEADER, this::visitMethodHeader);
        addVisit(AstNode.PARAM_DECL, this::visitParamDecl);

        setDefaultVisit((node, imports) -> ++visits);
    }

    private Integer visitClassDecl(JmmNode classDecl, List<String> dummy) {
        name = classDecl.get("name");
        superclass = classDecl.get("superclass");

        return ++visits;
    }

    private Integer visitMethodDecl(JmmNode methodDecl, List<String> methods) {
        // TODO: Return list of methods (RETURN + VARS + PARAM)
        var methodString = methodDecl.getChildren().stream()
                    .map(id -> id.get("name"))
                    .collect(Collectors.joining("."));

        methods.add(methodString);

        return ++visits;
    }

    private Integer visitMethodHeader(JmmNode methodHeader, List<String> dummy) {
        var methodName = methodHeader.get("name");
        var returnType = methodHeader.get("returnType");

        return ++visits;
    }

    private Integer visitParamDecl(JmmNode paramDecl, List<String> param) {
        var paramString = paramDecl.getChildren().stream()
                .map(id -> id.get("name"))
                .collect(Collectors.joining("."));

        param.add(paramString);

        // TODO TYPE

        return ++visits;
    }
}


/*
var importCollector = new ImportCollector();
var imports = new ArrayList<String>();
var visits = importCollector.visit(rootNode, imports);
 */