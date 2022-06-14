package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.optimization.optimizers.ConstFoldingVisitor;
import pt.up.fe.comp.ollir.optimization.optimizers.ConstPropagationParam;
import pt.up.fe.comp.ollir.optimization.optimizers.ConstPropagationVisitor;
import pt.up.fe.comp.ollir.optimization.LocalVariableOptimization;
import pt.up.fe.comp.ollir.optimization.optimizers.DeadCodeEliminationVisitor;
import pt.up.fe.comp.ollir.optimization.optimizers.*;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {

    // before OLLIR
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        boolean optimize = semanticsResult.getConfig().get("optimize") != null
                && semanticsResult.getConfig().get("optimize").equals("true");

        if (!optimize) return semanticsResult;

        boolean debug = semanticsResult.getConfig().get("debug") != null && semanticsResult.getConfig().get("debug").equals("true");

        System.out.println("Performing optimizations before OLLIR ...");

        // duplicate condition of while loops in order to check if they can be promoted to do-while loops at compile-time
        WhileConditionDuplicatorVisitor whileConditionDuplicatorVisitor = new WhileConditionDuplicatorVisitor();
        whileConditionDuplicatorVisitor.visit(semanticsResult.getRootNode());

        boolean hasChanges = true;
        while (hasChanges) {
            ConstPropagationVisitor constPropagationVisitor = new ConstPropagationVisitor();
            if (debug) {
                System.out.println("Performing constant propagation ...");
            }
            hasChanges = constPropagationVisitor.visit(semanticsResult.getRootNode(), new ConstPropagationParam());

            ConstFoldingVisitor constFoldingVisitor = new ConstFoldingVisitor();
            if (debug) {
                System.out.println("Performing constant folding ...");
            }
            hasChanges = constFoldingVisitor.visit(semanticsResult.getRootNode()) ||  hasChanges;

            DeadCodeEliminationVisitor deadCodeEliminationVisitor = new DeadCodeEliminationVisitor();
            if (debug) {
                System.out.println("Performing dead code elimination ...");
            }
            hasChanges = deadCodeEliminationVisitor.visit(semanticsResult.getRootNode()) || hasChanges;
        }

        // remove duplicated while condition and annotate with do-while true/false at compile-time
        DoWhileAnnotatorVisitor doWhileAnnotatorVisitor = new DoWhileAnnotatorVisitor();
        doWhileAnnotatorVisitor.visit(semanticsResult.getRootNode());

        if (debug) {
            System.out.println("OPTIMIZED ANNOTATED AST : \n" + semanticsResult.getRootNode().toTree());
        }

        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        boolean optimize = semanticsResult.getConfig().get("optimize") != null
                && semanticsResult.getConfig().get("optimize").equals("true");

        System.out.println("Generating OLLIR code ...");

        var ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable(), optimize);
        ollirGenerator.visit(semanticsResult.getRootNode());

        var ollirCode = ollirGenerator.getCode();

        if (semanticsResult.getConfig().get("debug") != null && semanticsResult.getConfig().get("debug").equals("true")) {
            System.out.println("OLLIR CODE : \n" + ollirCode);
        }

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        String localVariableAllocation = ollirResult.getConfig().get("registerAllocation");
        int localVariableNum = localVariableAllocation == null? -1 : Integer.parseInt(localVariableAllocation);

        if (localVariableNum > -1) {
            System.out.println("Performing register allocation ...");
            LocalVariableOptimization optimization = new LocalVariableOptimization(ollirResult);
            optimization.optimize(localVariableNum);

        }

        return ollirResult;
    }
}
