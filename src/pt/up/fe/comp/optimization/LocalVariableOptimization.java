package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class LocalVariableOptimization {

    private final ClassUnit unit;
    private final boolean debug;

    public LocalVariableOptimization(OllirResult ollirResult) {
        this.unit = ollirResult.getOllirClass();
        this.debug = ollirResult.getConfig().get("debug") != null && ollirResult.getConfig().get("debug").equals("true");
    }

    public void optimize(int localVariableNum) {
        try {
            unit.checkMethodLabels();
        } catch (OllirErrorException e) {
            e.printStackTrace();
            return;
        }
        unit.buildCFGs();
        unit.buildVarTables();

        if (this.debug) {
            System.out.println("Number of registers used per method:\n");
        }

        for (Method method : unit.getMethods()) {
            LivenessAnalyser analyser = new LivenessAnalyser(method);
            analyser.analyse();
            LocalVariableInterferenceGraph varGraph = new LocalVariableInterferenceGraph(analyser.getInAlive(), analyser.getOutAlive(), analyser.getDefined(), method, debug);

            if (this.debug) {
                System.out.println(method.getMethodName() + ":");
            }

            var updatedVarTable = varGraph.allocateLocalVariables(localVariableNum);

            for (String varName : updatedVarTable.keySet()) {
                method.getVarTable().put(varName, updatedVarTable.get(varName));
            }
        }
    }
}
