package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.visitors.AstNode;

import java.util.stream.Collectors;

public class SymbolTableFiller extends PreorderJmmVisitor<SymbolTableImpl, Integer> {

    public SymbolTableFiller() {
        addVisit(AstNode.IMPORT_DECL, this::visitImportDecl);
        addVisit(AstNode.CLASS_DECL, this::visitClassDecl);
        addVisit(AstNode.METHOD_DECL, this::visitMethodDecl);
    }

    private Integer visitImportDecl(JmmNode importDecl, SymbolTableImpl symbolTable) {
        var importString = importDecl.get("name");

        var importStr = importDecl.getChildren().stream()
                .map(id -> id.get("name"))
                .collect(Collectors.joining("."));

        var importResult = "";
        if (importStr.equals("")) {
            importResult = importString;
        } else {
            importResult = importString + "." + importStr;
        }

        symbolTable.addImport(importResult);

        return 0;
    }

    private Integer visitClassDecl(JmmNode classDecl, SymbolTableImpl symbolTable) {
        symbolTable.setclassName(classDecl.get("name"));
        classDecl.getOptional("superclass").ifPresent(symbolTable::setSuperclassName);

        return 0;
    }

    private Integer visitMethodDecl(JmmNode methodDecl, SymbolTableImpl symbolTable) {
        // TODO: Return list of methods (RETURN + VARS + PARAM)


        return 0;
    }
}

// TODO: MINUTE 45 PART 1
