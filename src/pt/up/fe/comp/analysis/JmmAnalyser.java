package pt.up.fe.comp.analysis;

import pt.up.fe.comp.analysis.analysers.*;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.lang.reflect.Array;
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

        List<PreorderSemanticAnalyser> analysers = Arrays.asList(
                new VariableAnalyser(), new OperandCompatibilityAnalyser(), new ArrayAccessAnalyser(),
                new AssignmentCompatibilityAnalyser(), new ConditionAnalyser(), new FunctionCallCompatibilityAnalyser(),
                new MethodExistanceAnalyser(), new MethodLiteralCallAnalyser());

        for (var analyser : analysers){
            analyser.visit(parserResult.getRootNode(), symbolTable);
            reports.addAll(analyser.getReports());
        }

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
