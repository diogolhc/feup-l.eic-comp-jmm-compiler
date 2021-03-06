package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.analysers.*;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {

        var symbolTable = new SymbolTableImpl();

        var symbolTableFiller = new SymbolTableFiller();

        System.out.println("Filling the symbol table ...");

        symbolTableFiller.visit(parserResult.getRootNode(), symbolTable);

        if (parserResult.getConfig().get("debug") != null && parserResult.getConfig().get("debug").equals("true")) {
            System.out.println("SYMBOL TABLE : \n" + symbolTable.print());
        }

        List<Report> reports = new ArrayList<>(symbolTableFiller.getReports());

        List<PreorderSemanticAnalyser> analysers = Arrays.asList(
                new MethodExistenceAnalyser(), new ArrayAccessAnalyser(), new VariableAnalyser(), new OperandCompatibilityAnalyser(),
                new AssignmentCompatibilityAnalyser(), new ConditionAnalyser(), new MethodCallCompatibilityAnalyser(),
                new MethodLiteralCallAnalyser(), new MethodReturnAnalyser(),
                new LengthAnalyser(), new DeclarationAnalyser(), new MainAnalyser());

        System.out.println("Performing semantic analysis ...");

        for (var analyser : analysers) {
            analyser.visit(parserResult.getRootNode(), symbolTable);
            reports.addAll(analyser.getReports());
        }

        if (parserResult.getConfig().get("debug") != null && parserResult.getConfig().get("debug").equals("true")) {
            System.out.println("ANNOTATED AST : \n" + parserResult.getRootNode().toTree());
        }

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
