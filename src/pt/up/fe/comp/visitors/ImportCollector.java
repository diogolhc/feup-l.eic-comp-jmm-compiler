package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class ImportCollector extends AJmmVisitor<List<String>, Integer> {
    private int visits;

    public ImportCollector() {
        this.visits = 0;
        System.out.println("constructor");
        addVisit(AstNode.PROGRAM, this::visitProgram);
        addVisit(AstNode.IMPORT_DECL, this::visitImportDecl);

        setDefaultVisit((node, imports) -> ++visits);
    }

    private Integer visitProgram(JmmNode program, List<String> imports) {
        System.out.println("here program");
        for (var child : program.getChildren()) {
            visit(child, imports);
        }

        return ++visits;
    }

    private Integer visitImportDecl(JmmNode importDecl, List<String> imports) {
        System.out.println("here");
        var importString = importDecl.getChildren().stream()
                .map(id -> id.get("name"))
                .collect(Collectors.joining("."));

        imports.add(importString);

        return ++visits;
    }
}


/*
var importCollector = new ImportCollector();
var imports = new ArrayList<String>();
var visits = importCollector.visit(rootNode, imports);
 */