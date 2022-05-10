package pt.up.fe.comp;

import pt.up.fe.comp.analysis.JmmAnalyser;
import pt.up.fe.comp.jmm.jasmin.JasminBackender;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.ollir.JmmOptimizer;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // read the input code
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }
        File inputFile = new File(args[0]);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + args[0] + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(input, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());


        // Instantiate JmmAnalyser
        var analyser = new JmmAnalyser();

        // Analysis stage
        var semanticsResult = analyser.semanticAnalysis(parserResult);

        // Check if there are parsing errors
        TestUtils.noErrors(semanticsResult);


        // Instantiate JmmOptimizer
        var optimizer = new JmmOptimizer();

        // Optimization stage
        var optimizationResult1 = optimizer.optimize(semanticsResult);

        // Check if there are optimization errors
        TestUtils.noErrors(optimizationResult1);

        var ollirResult = optimizer.toOllir(optimizationResult1);

        // Check if there are ollir errors
        TestUtils.noErrors(ollirResult);

        var optimizationResult2 = optimizer.optimize(ollirResult);

        // Check if there are optimization errors
        TestUtils.noErrors(optimizationResult2);


        // Instantiate JasminBackender
        var jasminBackend = new JasminBackender();

        var backendResult = jasminBackend.toJasmin(optimizationResult2);

        // Check if there are jasmin errors
        TestUtils.noErrors(backendResult);

        // ... add remaining stages
    }

}
