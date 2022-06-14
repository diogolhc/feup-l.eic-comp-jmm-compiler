package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.jasmin.JasminBackender;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class LocalVariableOptimization {

    private final OllirResult ollirResult;
    private final ClassUnit unit;
    private final boolean debug;

    public LocalVariableOptimization(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
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
            System.out.println("Number of registers used per method:");
        }

        int maxLocalsNeeded = -1;

        for (Method method : unit.getMethods()) {
            LivenessAnalyser analyser = new LivenessAnalyser(method);
            analyser.analyse();
            LocalVariableInterferenceGraph varGraph = new LocalVariableInterferenceGraph(analyser.getInAlive(), analyser.getOutAlive(), analyser.getDefined(), method);

            AllocateVariablesRes allocateVariablesRes = varGraph.allocateLocalVariables(localVariableNum);
            var updatedVarTable = allocateVariablesRes.getUpdateVarTable();
            int localVariableNumUsed = allocateVariablesRes.getLocalVariableNum();

            maxLocalsNeeded = Math.max(maxLocalsNeeded, localVariableNumUsed);

            for (String varName : updatedVarTable.keySet()) {
                method.getVarTable().put(varName, updatedVarTable.get(varName));
            }

            if (this.debug) {
                System.out.println(method.getMethodName() + ":");
                System.out.println("Used " + JasminBackender.calculateLimitLocals(method) + " registers.");
            }
        }

        // add report only if -r=n > 0 and n is insufficient
        if (localVariableNum > 0 && maxLocalsNeeded > localVariableNum) {
            ollirResult.getReports().add(new Report(
                    ReportType.ERROR, Stage.OPTIMIZATION,
                    -1, -1,
                    "Insufficient -r=" + localVariableNum + ". Needs -r=" + maxLocalsNeeded + "."));
        }
    }
}
