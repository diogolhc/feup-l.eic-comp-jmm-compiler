package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.optimizers.ConstFoldingVisitor;
import pt.up.fe.comp.ollir.optimizers.ConstPropagationParam;
import pt.up.fe.comp.ollir.optimizers.ConstPropagationVisitor;
import pt.up.fe.comp.optimization.LocalVariableOptimization;
import pt.up.fe.comp.ollir.optimizers.DeadCodeEliminationVisitor;
import pt.up.fe.comp.ollir.optimizers.*;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {

    // before OLLIR
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        boolean optimize = semanticsResult.getConfig().get("optimize") != null
                && semanticsResult.getConfig().get("optimize").equals("true");

        // TODO assure this outside (?)
        if (!optimize) return semanticsResult;

        // duplicate condition of while loops in order to check if they can be promoted to do-while loops at compile-time
        WhileConditionDuplicatorVisitor whileConditionDuplicatorVisitor = new WhileConditionDuplicatorVisitor();
        whileConditionDuplicatorVisitor.visit(semanticsResult.getRootNode());

        boolean hasChanges = true;
        while (hasChanges) {
            ConstPropagationVisitor constPropagationVisitor = new ConstPropagationVisitor();
            hasChanges = constPropagationVisitor.visit(semanticsResult.getRootNode(), new ConstPropagationParam());

            ConstFoldingVisitor constFoldingVisitor = new ConstFoldingVisitor();
            hasChanges = constFoldingVisitor.visit(semanticsResult.getRootNode()) ||  hasChanges;

            DeadCodeEliminationVisitor deadCodeEliminationVisitor = new DeadCodeEliminationVisitor();
            hasChanges = deadCodeEliminationVisitor.visit(semanticsResult.getRootNode()) || hasChanges;
        }

        // remove duplicated while condition and annotate with do-while true/false at compile-time
        DoWhileAnnotatorVisitor doWhileAnnotatorVisitor = new DoWhileAnnotatorVisitor();
        doWhileAnnotatorVisitor.visit(semanticsResult.getRootNode());

        System.out.println("OPTIMIZED ANNOTATED AST:");
        System.out.println(semanticsResult.getRootNode().toTree());

        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        boolean optimize = semanticsResult.getConfig().get("optimize") != null
                && semanticsResult.getConfig().get("optimize").equals("true");

        var ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable(), optimize);
        ollirGenerator.visit(semanticsResult.getRootNode());

        var ollirCode = ollirGenerator.getCode();

        System.out.println("OLLIR CODE : \n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        String localVariableAllocation = ollirResult.getConfig().get("registerAllocation");
        int localVariableNum = localVariableAllocation == null? -1 : Integer.parseInt(localVariableAllocation);
        System.out.println("LOCAL VARIABLE NUM " + localVariableNum);

        // TODO assure this outside (?)

        if (localVariableNum != -1) {
            LocalVariableOptimization optimization = new LocalVariableOptimization(ollirResult.getOllirClass());
            optimization.optimize(localVariableNum);

        }

        return ollirResult;
    }
}
