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

//        String ollirCode = "import ioPlus;\n" +
//                "import BoardBase;\n" +
//                "import java.io.File;\n" +
//                "\n" +
//                "public HelloWorld extends BoardBase {\n" +
//                "\n" +
//                "\t.construct HelloWorld().V {\n" +
//                "\t\tinvokespecial(this, \"<init>\").V;\n" +
//                "\t}\n" +
//                "\n" +
//                "\t.method public met().i32 {\n" +
//                "\t\tt1.i32 :=.i32 1.i32 +.i32 2.i32;\n" +
//                "\t\tt2.i32 :=.i32 3.i32 *.i32 4.i32;\n" +
//                "\t\tt3.i32 :=.i32 t1.i32 +.i32 t2.i32;\n" +
//                "\t}\n" +
//                "}";

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }
}
