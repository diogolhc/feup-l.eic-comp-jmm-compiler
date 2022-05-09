package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());

        System.out.println("ANNOTATED AST : \n" + semanticsResult.getRootNode().toTree());

        var ollirCode = ollirGenerator.getCode();

        System.out.println("OLLIR CODE : \n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }
}
