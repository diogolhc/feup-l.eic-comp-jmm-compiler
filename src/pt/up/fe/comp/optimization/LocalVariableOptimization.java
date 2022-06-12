package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OllirErrorException;

public class LocalVariableOptimization {

    private final ClassUnit unit;

    public LocalVariableOptimization(ClassUnit unit) {
        this.unit = unit;
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

        for (Method method : unit.getMethods()) {
            LivenessAnalyser analyser = new LivenessAnalyser(method);
            analyser.analyse();
            LocalVariableInterferenceGraph varGraph =
                    new LocalVariableInterferenceGraph(analyser.getInAlive(), analyser.getOutAlive(), method);
            var updatedVarTable = varGraph.allocateLocalVariables(localVariableNum);

            for (String varName : method.getVarTable().keySet()) {
                method.getVarTable().put(varName, updatedVarTable.get(varName));
            }
        }
    }
}
