package pt.up.fe.comp.ollir.optimization;

import org.specs.comp.ollir.*;

import java.util.*;

public class LocalVariableInterferenceGraph {
    private static class VarNode {
        int ogLocalVariable;
        String varName;
        Set<VarNode> adjacentNodes;
        boolean isActive;

        public VarNode(String name, int localVariable) {
            ogLocalVariable = localVariable;
            varName = name;
            adjacentNodes = new HashSet<>();
            isActive = true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VarNode other = (VarNode) o;
            return this.ogLocalVariable == other.ogLocalVariable &&
                    this.varName.equals(other.varName) &&
                    this.adjacentNodes.equals(other.adjacentNodes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(varName, ogLocalVariable);
        }

        @Override
        public String toString() {
            return varName + " " + ogLocalVariable;
        }

        public List<String> getActiveAdjacentNodes() {
            List<String> vars = new ArrayList<>();
            for (VarNode adj : adjacentNodes) {
                if (adj.isActive) {
                    vars.add(adj.varName);
                }
            }
            return vars;
        }
    }


    private final Map<Integer, VarNode> nodes;
    private final Map<String, Descriptor> varTable;
    private int minLocalVariables;
    private final boolean isStaticMethod;

    public LocalVariableInterferenceGraph(Map<Node, BitSet> inAlive, Map<Node, BitSet> outAlive, Map<Node, BitSet> define, Method method) {
        nodes = new HashMap<>();

        varTable = method.getVarTable();
        isStaticMethod = method.isStaticMethod();
        minLocalVariables = isStaticMethod ? 0 : 1; // THIS

        addNodes();
        addEdges(inAlive);
        addEdges(outAlive);
        addEdges(define, outAlive);
    }

    private void addNodes() {
        for(String name: varTable.keySet()) {
            Descriptor descriptor = varTable.get(name);

            if (descriptor.getScope() == VarScope.PARAMETER ||
                    descriptor.getScope() == VarScope.FIELD) {
                // save local variables for the parameters and fields
                minLocalVariables++;
            } else if (descriptor.getVarType().getTypeOfElement() != ElementType.THIS) {
                nodes.put(descriptor.getVirtualReg(),
                        new VarNode(name, descriptor.getVirtualReg()));
            }
        }
    }

    // Connect each pair of variables that belong to the same IN or OUT set
    private void addEdges(Map<Node, BitSet> liveRange) {
        for (Node instruction : liveRange.keySet()) {
            BitSet bitset = liveRange.get(instruction);
            List<Integer> varIdxs = new ArrayList<>();

            for (int i = 0; i < bitset.length(); i++) {
                if (bitset.get(i)) {
                    varIdxs.add(i);
                }
            }

            for (int i = 0; i < varIdxs.size() - 1; i++) {
                VarNode node1 = nodes.get(varIdxs.get(i));

                for (int j = i + 1; j < varIdxs.size(); j++) {
                    VarNode node2 = nodes.get(varIdxs.get(j));

                    if (node1 != null && node2 != null) {
                        node1.adjacentNodes.add(node2);
                        node2.adjacentNodes.add(node1);
                    }
                }
            }
        }
    }

    // Connect variables in KILL[i] with those in OUT[i]
    private void addEdges(Map<Node, BitSet> define, Map<Node, BitSet> outAlive) {
        for (Node instruction : define.keySet()) {
            BitSet defineBitset = define.get(instruction);
            BitSet outBitset = outAlive.get(instruction);

            List<Integer> defVarIdxs = new ArrayList<>();
            List<Integer> outVarIdxs = new ArrayList<>();

            for (int i = 0; i < defineBitset.length(); i++) {
                if (defineBitset.get(i)) {
                    defVarIdxs.add(i);
                }
            }

            for (int i = 0; i < outBitset.length(); i++) {
                if (outBitset.get(i)) {
                    outVarIdxs.add(i);
                }
            }

            for (Integer defVarIdx : defVarIdxs) {
                VarNode node1 = nodes.get(defVarIdx);

                for (Integer outVarIdx : outVarIdxs) {
                    VarNode node2 = nodes.get(outVarIdx);

                    if (node1 != null && node2 != null) {
                        node1.adjacentNodes.add(node2);
                        node2.adjacentNodes.add(node1);
                    }
                }
            }
        }
    }

    public AllocateVariablesRes allocateLocalVariables(int localVariableNum) {
        if (localVariableNum < minLocalVariables) {
            return allocateLocalVariables(localVariableNum+1);
        }
        Stack<VarNode> stack = new Stack<>();

        while (!nodes.isEmpty()) {
            Iterator<Map.Entry<Integer, VarNode>> it = nodes.entrySet().iterator();
            while (it.hasNext()) {
                VarNode node = it.next().getValue();
                stack.push(node);
                node.isActive = false;
                it.remove();
            }
        }

        // maps colors to the instructions
        Map<Integer, List<String>> localVariables = new HashMap<>();
        for (int i = minLocalVariables; i < localVariableNum; i++) {
            localVariables.put(i, new ArrayList<>());
        }

        Map<String, Descriptor> updatedVarTable = new HashMap<>();

        while (!stack.isEmpty()) {
            VarNode node = stack.pop();
            node.isActive = true;
            nodes.put(node.ogLocalVariable, node);

            if (!assignLocalVariable(localVariables, node, updatedVarTable)) {
                while (!stack.isEmpty()) {
                    VarNode n = stack.pop();
                    n.isActive = true;
                    nodes.put(n.ogLocalVariable, n);
                }
                return allocateLocalVariables(localVariableNum+1);
            }
        }

        int local = isStaticMethod ? 0 : 1;

        for (String varName: varTable.keySet()) {
            Descriptor d = varTable.get(varName);
            if (d.getScope() == VarScope.PARAMETER || d.getScope() == VarScope.FIELD) {
                updatedVarTable.put(varName, new Descriptor(d.getScope(), local, d.getVarType()));
                local++;
            }
        }

        return new AllocateVariablesRes(updatedVarTable, localVariableNum);
    }

    private boolean assignLocalVariable(Map<Integer, List<String>> localVariables, VarNode node, Map<String, Descriptor> updatedVarTable) {
        for (Integer local : localVariables.keySet()) {
            if (canAssignLocalVariable(localVariables, node, local)) {
                localVariables.get(local).add(node.varName);
                Descriptor old = varTable.get(node.varName);
                updatedVarTable.put(node.varName, new Descriptor(old.getScope(), local, old.getVarType()));
                return true;
            }
        }

        return false;
    }

    private boolean canAssignLocalVariable(Map<Integer, List<String>> localVariables, VarNode node, Integer local) {
        for (String adjLocal : node.getActiveAdjacentNodes()) {
            if (localVariables.get(local).contains(adjLocal)) {
                return false;
            }
        }

        return true;
    }
}
