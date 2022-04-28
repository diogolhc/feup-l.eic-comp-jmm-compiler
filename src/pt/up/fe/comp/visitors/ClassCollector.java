package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class ClassCollector extends AJmmVisitor<List<String>, Integer> {
    private int visits;
    private String name;
    private String superclass;
    private List<String> methods;

    public ClassCollector() {
        this.visits = 0;

        addVisit(AstNode.START, this::visitStart);
        addVisit(AstNode.PROGRAM, this::visitProgram);
        addVisit(AstNode.CLASS_DECL, this::visitClassDecl);
        addVisit(AstNode.VAR_DECL, this::visitVarDecl);
        addVisit(AstNode.METHOD_DECL, this::visitMethodDecl);
        addVisit(AstNode.METHOD_HEADER, this::visitMethodHeader);
        addVisit(AstNode.PARAM_DECL, this::visitParamDecl);

        setDefaultVisit((node, imports) -> ++visits);
    }

    private Integer visitStart(JmmNode start, List<String> methods) {
        visit(start.getChildren().get(0), methods);
        return ++visits;
    }

    private Integer visitProgram(JmmNode program, List<String> methods) {
        for (var child : program.getChildren()) {
            if (child.getKind().equals(AstNode.CLASS_DECL)){
                visit(child, methods);
            }
        }

        return ++visits;
    }

    private Integer visitClassDecl(JmmNode classDecl, List<String> dummy) {
        name = classDecl.get("name");

        try {
            superclass = classDecl.get("superclass");
        }
        catch (Exception ignored) { }

        System.out.println("classDecl: " + name + "-" + superclass);

        // TODO: FIELDS (VARS OF CLASS)

        for (var child : classDecl.getChildren()) {
                visit(child, dummy);
        }
        return ++visits;
    }

    private Integer visitVarDecl(JmmNode methodDecl, List<String> dummy) {
        // TODO: NAME + TYPE
        // TODO: MAYBE SAME AS PARAM_DECL?

        return ++visits;
    }

    private Integer visitMethodDecl(JmmNode methodDecl, List<String> methods) {
        // TODO: Return list of methods (RETURN + VARS + PARAM)
        System.out.println("hello:" + methodDecl.getChildren().stream());
        var methodString = methodDecl.getChildren().stream()
                    .map(id -> id.get("name"))
                    .collect(Collectors.joining("."));

        methods.add(methodString);

        return ++visits;
    }

    private Integer visitMethodHeader(JmmNode methodHeader, List<String> dummy) {
        var methodName = methodHeader.get("name");
        var returnType = methodHeader.get("returnType");

        System.out.println("visitMethodHeader:" + " " + methodName + '-' + returnType);
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