package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.analysis.table.VarAlreadyDefinedException;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.analysis.table.Method;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.analysis.table.AstNode;

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
        symbolTable.setClassName(classDecl.get("name"));
        classDecl.getOptional("superclass").ifPresent(symbolTable::setSuperclassName);

        // Fields
        var fields = classDecl.getChildren().stream()
                .filter(node -> node.getKind().equals(AstNode.VAR_DECL))
                .collect(Collectors.toList());

        for (var variable : fields) {
            if (!symbolTable.addField(new Symbol(
                    new Type(variable.get("varType").equals("array") ? "int" : variable.get("varType"),
                            variable.get("varType").equals("array"))
                    , variable.get("name")))) {

                reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(variable.get("line")),
                        Integer.parseInt(variable.get("col")),
                        "Found duplicated field with signature '" + variable.get("name") + "'", null));
                return -1;
            }
        }

        return 0;
    }

    private Integer visitMethodDecl(JmmNode methodDecl, SymbolTableImpl symbolTable) {
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

        // Parameters
        var parameters = methodHeader.getChildren();
        List<Symbol> paramsSymbols = parameters.stream().map(
                param -> new Symbol(new Type(param.get("varType").equals("array") ? "int" : param.get("varType"),
                        param.get("varType").equals("array")),
                        param.get("name"))).collect(Collectors.toList());

        try {
            symbolTable.addMethod(methodName, returnType, paramsSymbols);
        } catch (VarAlreadyDefinedException e) {
            reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(methodDecl.get("line")),
                    Integer.parseInt(methodDecl.get("col")),
                    e.getMessage(), e));
            return -1;
        }

        // Local Variables
        Method method = symbolTable.findMethod(methodName);
        var vars = methodDecl.getJmmChild(1).getChildren().stream()
                .filter(node -> node.getKind().equals(AstNode.VAR_DECL))
                .collect(Collectors.toList());

        for (var variable : vars) {
            if (!method.addLocalVariable(new Symbol(
                    new Type(variable.get("varType").equals("array") ? "int" : variable.get("varType"),
                        variable.get("varType").equals("array"))
                    , variable.get("name")))) {

                reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(variable.get("line")),
                        Integer.parseInt(variable.get("col")),
                        "Found duplicated local variable '" + variable.get("name") + "'" , null));
                return -1;
            }
        }

        return 0;
    }

    private Integer visitMainDecl(JmmNode mainDecl, SymbolTableImpl symbolTable) {
        var methodName = "main";
        Type returnType = new Type("void", false);

        if (symbolTable.hasMethod(methodName)) {
            reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(mainDecl.get("line")),
                    Integer.parseInt(mainDecl.get("col")),
                    "Found duplicated method with signature '" + methodName + "'", null));
            return -1;
        }

        // Parameters
        var paramName = mainDecl.get("mainArray");
        var paramType = mainDecl.get("string");
        List<Symbol> params = new ArrayList<>();
        params.add(new Symbol(new Type(paramType, true), paramName));

        try {
            symbolTable.addMethod(methodName, returnType, params);
        } catch (VarAlreadyDefinedException e) {
            reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(mainDecl.get("line")),
                    Integer.parseInt(mainDecl.get("col")),
                    e.getMessage(), e));
            return -1;
        }

        // Local Variables
        Method method = symbolTable.findMethod(methodName);
        var vars = mainDecl.getJmmChild(0).getChildren().stream()
                .filter(node -> node.getKind().equals(AstNode.VAR_DECL))
                .collect(Collectors.toList());

        for (var variable : vars) {
            if (!method.addLocalVariable(new Symbol(
                    new Type(variable.get("varType").equals("array") ? "int" : variable.get("varType"),
                            variable.get("varType").equals("array"))
                    , variable.get("name")))) {

                reports.add(Report.newError(Stage.SEMANTIC, Integer.parseInt(variable.get("line")),
                        Integer.parseInt(variable.get("col")),
                        "Found duplicated local variable '" + variable.get("name") + "'" , null));
                return -1;
            }
        }

        return 0;
    }

}
