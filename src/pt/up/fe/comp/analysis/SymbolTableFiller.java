package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.visitors.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolTableFiller extends PreorderJmmVisitor<SymbolTableImpl, Integer> {
    private final List<Report> reports;

    public SymbolTableFiller() {
        this.reports = new ArrayList<>();

        addVisit(AstNode.IMPORT_DECL, this::visitImportDecl);
        addVisit(AstNode.CLASS_DECL, this::visitClassDecl);
        addVisit(AstNode.METHOD_DECL, this::visitMethodDecl);
        addVisit(AstNode.MAIN_DECL, this::visitMainDecl);
    }

    public List<Report> getReports() {
        return reports;
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
        // TODO: LOCAL VARS
        // First child of MethodDeclaration is MethodHeader
        JmmNode methodHeader = methodDecl.getJmmChild(0);
        var methodName = methodHeader.get("name");

        if (symbolTable.hasMethod(methodName)) {
            reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(methodDecl.get("line")),
                    Integer.parseInt(methodDecl.get("col")),
                    "Found duplicated method with signature '" + methodName + "'", null));
            return -1;
        }

        var returnTypeString = methodHeader.get("returnType");
        Type returnType = new Type(returnTypeString.equals("array") ? "int" : returnTypeString,
                returnTypeString.equals("array"));

        var parameters = methodHeader.getChildren();

        List<Symbol> paramsSymbols = parameters.stream().map(
                param -> new Symbol(new Type(param.get("varType").equals("array") ? "int" : param.get("varType"),
                        param.get("varType").equals("array")),
                        param.get("name"))).collect(Collectors.toList());

        symbolTable.addMethod(methodName, returnType, paramsSymbols);

        return 0;
    }

    private Integer visitMainDecl(JmmNode mainDecl, SymbolTableImpl symbolTable) {
        // TODO: LOCAL VARS
        var methodName = "main";
        Type returnType = new Type("void", false);

        if (symbolTable.hasMethod(methodName)) {
            reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(mainDecl.get("line")),
                    Integer.parseInt(mainDecl.get("col")),
                    "Found duplicated method with signature '" + methodName + "'", null));
            return -1;
        }

        var paramName = mainDecl.get("mainArray");
        var paramType = mainDecl.get("string");
        List<Symbol> params = new ArrayList<>();
        params.add(new Symbol(new Type(paramType, true), paramName));

        symbolTable.addMethod(methodName, returnType, params);

        return 0;
    }
}

// TODO: 1HOUR:20MIN PART 1
