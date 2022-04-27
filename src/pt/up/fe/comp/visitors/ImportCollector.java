package pt.up.fe.comp.visitors;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class ImportCollector extends AJmmVisitor<List<String>, Integer> {
    private int visits;

    public ImportCollector() {
        this.visits = 0;
        addVisit(AstNode.PROGRAM, this::visitProgram);
        addVisit(AstNode.IMPORT_DECL, this::visitImportDecl);
        addVisit(AstNode.START, this::visitStart);
        setDefaultVisit((node, imports) -> ++visits);
    }

    private Integer visitStart(JmmNode start, List<String> imports) {
        visit(start.getChildren().get(0), imports);
        return null;
    }

    private Integer visitProgram(JmmNode program, List<String> imports) {
        for (var child : program.getChildren()) {
            if (child.getKind().equals(AstNode.IMPORT_DECL)) {
                visit(child, imports);
            }
        }

        return ++visits;
    }

    private Integer visitImportDecl(JmmNode importDecl, List<String> imports) {
        var importString = importDecl.get("name");

        var importStr = importDecl.getChildren().stream()
                .map(id -> id.get("name"))
                .collect(Collectors.joining("."));

        if (importStr.equals("")) {
            imports.add(importString);
        } else {
            imports.add(importString + "." + importStr);
        }

        return ++visits;
    }
}


/*
var importCollector = new ImportCollector();
var imports = new ArrayList<String>();
var visits = importCollector.visit(rootNode, imports);
 */