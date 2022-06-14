package pt.up.fe.comp.optimization;

import org.specs.comp.ollir.Descriptor;

import java.util.Map;

public class AllocateVariablesRes {
    private final Map<String, Descriptor> updateVarTable;
    private final int localVariableNum;

    public AllocateVariablesRes(Map<String, Descriptor> updateVarTable, int localVariableNum) {
        this.updateVarTable = updateVarTable;
        this.localVariableNum = localVariableNum;
    }

    public int getLocalVariableNum() {
        return localVariableNum;
    }

    public Map<String, Descriptor> getUpdateVarTable() {
        return updateVarTable;
    }

}
