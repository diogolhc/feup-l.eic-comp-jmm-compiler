package pt.up.fe.comp.ollir;

public class OllirInference {
    private final String inferredType;
    private final Boolean isToAssignToTemp;

    public OllirInference(String inferredType, Boolean isToAssignToTemp) {
        this.inferredType = inferredType;
        this.isToAssignToTemp = isToAssignToTemp;
    }

    public OllirInference(Boolean isToAssignToTemp) {
        this(null, isToAssignToTemp);
    }

    public String getInferredType() {
        return inferredType;
    }

    public boolean getIsToAssignToTemp() {
        return isToAssignToTemp;
    }

}
