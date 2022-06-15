package pt.up.fe.comp.ollir.optimization;

import org.specs.comp.ollir.*;

import java.util.*;

public class LivenessAnalyser {
    private final Map<Node, BitSet> defined;
    private final Map<Node, BitSet> used;
    private final Map<Node, BitSet> inAlive;
    private final Map<Node, BitSet> outAlive;
    private final List<Instruction> nodes;
    private final int varNum;

    public LivenessAnalyser(Method method) {
        defined =  new HashMap<>();
        used = new HashMap<>();
        inAlive = new HashMap<>();
        outAlive = new HashMap<>();

        varNum = method.getVarTable().size();

        for (Instruction instruction : method.getInstructions()) {
            defined.put(instruction, getDefinedVars(instruction, method.getVarTable()));
            used.put(instruction, getUsedVars(instruction, method.getVarTable()));
            inAlive.put(instruction, new BitSet(varNum));
            outAlive.put(instruction, new BitSet(varNum));
        }

        nodes = new ArrayList<>(method.getInstructions());
        Collections.reverse(nodes);
    }

    public void analyse(boolean debug) {
        if (debug) {
            System.out.println("LIVENESS ANALYSIS:");
        }

        int iterNum = 0;

        while (true) {
            if (debug) {
                showMaps(iterNum);
            }

            Map<Node, BitSet> inAlivePrev = new HashMap<>(inAlive);
            Map<Node, BitSet> outAlivePrev = new HashMap<>(outAlive);

            for (Instruction node : nodes) {
                BitSet outAliveNew = new BitSet(varNum);

                if (node.getSucc1() != null) {
                    if (node.getSucc1().getNodeType() != NodeType.END) {
                        outAliveNew = (BitSet) inAlive.get(node.getSucc1()).clone();

                        if (node.getSucc2() != null) {
                            outAliveNew.or(inAlive.get(node.getSucc2()));
                        }
                    }
                }

                outAlive.replace(node, outAliveNew);

                BitSet inAliveNew = (BitSet) outAlive.get(node).clone();
                BitSet def = defined.get(node);

                for (int i = 0; i < inAliveNew.length(); i++) {
                    if (inAliveNew.get(i) && def.get(i)) {
                        inAliveNew.clear(i);
                    }
                }

                inAliveNew.or(used.get(node));
                inAlive.replace(node, inAliveNew);
            }

            if (isDone(inAlivePrev, outAlivePrev)) {
                break;
            }

            iterNum++;
        }

        iterNum++;
        if (debug) {
            showMaps(iterNum);
        }
    }

    private boolean isDone(Map<Node, BitSet> inAlivePrev, Map<Node, BitSet> outAlivePrev) {
        for (Instruction instruction : nodes) {
            if (!inAlive.get(instruction).equals(inAlivePrev.get(instruction)) ||
                    !outAlive.get(instruction).equals(outAlivePrev.get(instruction))) {
                return false;
            }
        }
        return true;
    }

    public Map<Node, BitSet> getInAlive() {
        return inAlive;
    }

    public Map<Node, BitSet> getOutAlive() {
        return outAlive;
    }

    public Map<Node, BitSet> getDefined() {
        return this.defined;
    }

    private BitSet getUsedVars(Instruction instruction, Map<String, Descriptor> varTable) {
        switch (instruction.getInstType()) {
            case UNARYOPER:
                return getUsedVars((UnaryOpInstruction) instruction, varTable);
            case BINARYOPER:
                return getUsedVars((BinaryOpInstruction) instruction, varTable);
            case NOPER:
                return getUsedVars((SingleOpInstruction) instruction, varTable);
            case ASSIGN:
                return getUsedVars((AssignInstruction) instruction, varTable);
            case CALL:
                return getUsedVars((CallInstruction) instruction, varTable);
            case BRANCH:
                return getUsedVars((CondBranchInstruction) instruction, varTable);
            case RETURN:
                return getUsedVars((ReturnInstruction) instruction, varTable);
            case GETFIELD:
                return getUsedVars((GetFieldInstruction) instruction, varTable);
            case PUTFIELD:
                return getUsedVars((PutFieldInstruction) instruction, varTable);
            default:
                break;
        }

        return new BitSet();
    }

    private BitSet getUsedVars(UnaryOpInstruction instruction, Map<String, Descriptor> varTable) {
        BitSet vars = new BitSet();
        setElement(vars, instruction.getOperand(), varTable);
        return vars;
    }

    private BitSet getUsedVars(BinaryOpInstruction instruction, Map<String, Descriptor> varTable) {
        BitSet vars = new BitSet();
        setElement(vars, instruction.getRightOperand(), varTable);
        setElement(vars, instruction.getLeftOperand(), varTable);
        return vars;
    }

    private BitSet getUsedVars(SingleOpInstruction instruction, Map<String, Descriptor> varTable) {
        BitSet vars = new BitSet();
        setElement(vars, instruction.getSingleOperand(), varTable);
        return vars;
    }

    private BitSet getUsedVars(AssignInstruction instruction, Map<String, Descriptor> varTable) {
        Operand dest = (Operand) instruction.getDest();
        Descriptor descriptor = varTable.get(dest.getName());

        BitSet vars = new BitSet();

        // array[index] = ...
        // 'array' and 'index' are used
        if (descriptor.getVarType().getTypeOfElement() == ElementType.ARRAYREF
                && dest.getType().getTypeOfElement() == ElementType.INT32) {
            for (Element index : ((ArrayOperand) dest).getIndexOperands()) {
                setElement(vars, index, varTable);
            }

            setElement(vars, dest, varTable);
        }

        vars.or(getUsedVars(instruction.getRhs(), varTable));

        return vars;
    }

    private BitSet getUsedVars(CallInstruction instruction, Map<String, Descriptor> varTable) {
        BitSet vars = new BitSet();

        CallType callType = instruction.getInvocationType();
        if (callType.equals(CallType.invokevirtual) || callType.equals(CallType.invokespecial) ||  callType.equals(CallType.arraylength)) {
            setElement(vars, instruction.getFirstArg(), varTable);
        }

        if (instruction.getNumOperands() > 1) {
            if (instruction.getInvocationType() != CallType.NEW) {
                setElement(vars, instruction.getSecondArg(), varTable);
            }
            for (Element arg : instruction.getListOfOperands()) {
                setElement(vars, arg, varTable);
            }
        }

        return vars;
    }

    private BitSet getUsedVars(CondBranchInstruction instruction, Map<String, Descriptor> varTable) {
        return getUsedVars(instruction.getCondition(), varTable);
    }

    private BitSet getUsedVars(ReturnInstruction instruction, Map<String, Descriptor> varTable) {
        BitSet vars = new BitSet();
        if (instruction.hasReturnValue()) {
            setElement(vars, instruction.getOperand(), varTable);
        }

        return vars;
    }

    private BitSet getUsedVars(GetFieldInstruction instruction, Map<String, Descriptor> varTable) {
        BitSet vars = new BitSet();
        setElement(vars, instruction.getFirstOperand(), varTable);
        return vars;
    }

    private BitSet getUsedVars(PutFieldInstruction instruction, Map<String, Descriptor> varTable) {
        BitSet vars = new BitSet();
        setElement(vars, instruction.getFirstOperand(), varTable);
        setElement(vars, instruction.getThirdOperand(), varTable);
        return vars;
    }

    private BitSet getDefinedVars(Instruction instruction, HashMap<String, Descriptor> varTable) {
        BitSet vars = new BitSet();

        if (instruction.getInstType() == InstructionType.ASSIGN) {
            setElement(vars, ((AssignInstruction) instruction).getDest(), varTable, false);
        } else if (instruction.getInstType() == InstructionType.PUTFIELD) {
            setElement(vars, ((PutFieldInstruction) instruction).getFirstOperand(), varTable, false);
        }

        return vars;
    }

    private void setElement(BitSet vars, Element element, Map<String, Descriptor> varTable) {
        setElement(vars, element, varTable, true);
    }

    private void setElement(BitSet vars, Element element, Map<String, Descriptor> varTable, boolean getUsed) {
        if (element.isLiteral()) {
            return;
        }

        if (element.getType().getTypeOfElement() == ElementType.THIS
            || (element.getType().getTypeOfElement() == ElementType.OBJECTREF && ((Operand) element).getName().equals("this"))) {
            vars.set(0);
            return;
        }

        Descriptor descriptor = varTable.get(((Operand) element).getName());

        if (getUsed) {
            // array[index], set 'index' as used also
            if (descriptor.getVarType().getTypeOfElement() == ElementType.ARRAYREF
                    && element.getType().getTypeOfElement() == ElementType.INT32) {
                for (Element index : ((ArrayOperand) element).getIndexOperands()) {
                    setElement(vars, index, varTable);
                }
            }
        }

        if (descriptor.getScope() == VarScope.PARAMETER ||
                descriptor.getScope() == VarScope.FIELD) {
            return;
        }

        int reg = descriptor.getVirtualReg();

        vars.set(reg);
    }

    public void showMaps(int iterNum) {
        System.out.println("\nIteration: (" + iterNum + ")");
        for (var v : nodes) {
            System.out.println("KEY: " + v.getId());

            System.out.println("DEF      : " + defined.get(v));
            System.out.println("USED     : " + used.get(v));
            System.out.println("IN ALIVE : " + inAlive.get(v));
            System.out.println("OUT ALIVE: " + outAlive.get(v));
        }
    }

}
