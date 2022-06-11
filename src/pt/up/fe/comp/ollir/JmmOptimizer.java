package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.optimizers.ConstFoldingVisitor;
import pt.up.fe.comp.ollir.optimizers.ConstPropagationVisitor;

import java.util.Collections;
import java.util.HashMap;

public class JmmOptimizer implements JmmOptimization {

    // before OLLIR
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        boolean optimize = semanticsResult.getConfig().get("optimize") != null
                && semanticsResult.getConfig().get("optimize").equals("true");

        // TODO assure this outside (?)
        if (!optimize) return semanticsResult;

        boolean hasChanges = true;
        while (hasChanges) {
            ConstPropagationVisitor constPropagationVisitor = new ConstPropagationVisitor();
            hasChanges = constPropagationVisitor.visit(semanticsResult.getRootNode(), new HashMap<>());

            ConstFoldingVisitor constFoldingVisitor = new ConstFoldingVisitor();
            hasChanges = constFoldingVisitor.visit(semanticsResult.getRootNode()) ||  hasChanges;
        }

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

}
