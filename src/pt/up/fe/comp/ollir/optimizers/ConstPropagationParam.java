package pt.up.fe.comp.ollir.optimizers;

import java.util.HashMap;
import java.util.Map;

public class ConstPropagationParam {
    private final Map<String, String> constants;
    private boolean isToJustRemoveAssigned;

    public ConstPropagationParam() {
        this.constants = new HashMap<>();
        this.isToJustRemoveAssigned = false;
    }

    // copy constructor
    public ConstPropagationParam(ConstPropagationParam constPropagationParam) {
        this.constants = new HashMap<>(constPropagationParam.getConstants());
        this.isToJustRemoveAssigned = constPropagationParam.isToJustRemoveAssigned();
    }

    public Map<String, String> getConstants() {
        return constants;
    }

    public boolean isToJustRemoveAssigned() {
        return isToJustRemoveAssigned;
    }

    public void setToJustRemoveAssigned(boolean toJustRemoveAssigned) {
        isToJustRemoveAssigned = toJustRemoveAssigned;
    }

}
