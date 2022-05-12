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
        symbolTableFiller.visit(parserResult.getRootNode(), symbolTable);

        List<Report> reports = new ArrayList<>(symbolTableFiller.getReports());

        // TODO check length
        List<PreorderSemanticAnalyser> analysers = Arrays.asList(
                new ArrayAccessAnalyser(), new VariableAnalyser(), new OperandCompatibilityAnalyser(),
                new AssignmentCompatibilityAnalyser(), new ConditionAnalyser(), new FunctionCallCompatibilityAnalyser(),
                new MethodExistanceAnalyser(), new MethodLiteralCallAnalyser(), new MethodReturnAnalyser());

        for (var analyser : analysers){
            analyser.visit(parserResult.getRootNode(), symbolTable);
            reports.addAll(analyser.getReports());
        }

        System.out.println("ANNOTATED AST : \n" + parserResult.getRootNode().toTree());

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
