package pt.up.fe.comp;

import pt.up.fe.comp.analysis.JmmAnalyser;
import pt.up.fe.comp.jmm.jasmin.JasminBackender;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.ollir.JmmOptimizer;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Parse input and create config
        Map<String, String> config = parseInput(args);
        String input = SpecsIo.read(new File(config.get("inputFile")));

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

        Path resultsDirectory = Paths.get("generated-files/");

        try {
            if (!Files.exists(resultsDirectory)) {
                Files.createDirectory(resultsDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path path = Paths.get("generated-files/" + backendResult.getClassName() + "/");

        try {
            FileWriter fileWriter = new FileWriter(path + ".j");
            fileWriter.write(backendResult.getJasminCode());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        backendResult.compile(path.toFile());
    }

    static private Map<String, String> parseInput(String[] args) {
        // Create config
        Map<String, String> config = new HashMap<>();
        String rNum = "-1";
        String optimize = "false";
        String debug = "false";
        String inputFileName = null;
        for (String arg : args) {
            if (arg.startsWith("-r")) {
                String num = arg.split("=")[1];
                int parsedNum;
                try {
                    parsedNum = Integer.parseInt(num);
                } catch (Exception e) {
                    throw new RuntimeException("Invalid integer in option -r. " + num + " not a integer.");
                }

                if (parsedNum >= -1 && parsedNum <= 65535) {
                    rNum = num;
                } else {
                    throw new RuntimeException("Invalid option -r. Number needs to be between [-1, 65535].");
                }
            } else if (arg.startsWith("-o")) {
                optimize = "true";
            } else if (arg.startsWith("-d")) {
                debug = "true";
            } else if (arg.startsWith("-i")) {
                inputFileName = arg.split("=")[1];
            } else {
                throw new RuntimeException("Invalid option " + arg + " .");
            }
        }

        // Read the input code
        if (inputFileName == null) {
            throw new RuntimeException("Expected at least a single argument, a path to an existing input file.");
        }

        File inputFile = new File(inputFileName);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFileName + "'.");
        }

        config.put("inputFile", inputFileName);
        config.put("optimize", optimize);
        config.put("registerAllocation", rNum);
        config.put("debug", debug);

        return config;
    }
}
