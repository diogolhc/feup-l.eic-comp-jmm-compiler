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

        System.out.println("\n===================\n\n" + semanticsResult.getRootNode().toTree() + "\n===================\n");
        var ollirCode = ollirGenerator.getCode();

        System.out.println("OLLIR CODE : \n" + ollirCode);

        /*String ollirCode = "import ioPlus;\n" +
                "import BoardBase;\n" +
                "import java.io.File;\n" +
                "\n" +
                "HelloWorld extends BoardBase {\n" +
                "    .method public static main(args.array.String).V {\n" +
                "\tinvokestatic(ioPlus, \"printHelloWorld\").V;\n" +
                "    }\n" +
                "}\n";*/

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }
}
