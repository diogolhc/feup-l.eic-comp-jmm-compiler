package pt.up.fe.comp.jmm.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

import static org.specs.comp.ollir.InstructionType.BINARYOPER;
import static org.specs.comp.ollir.InstructionType.RETURN;


public class JasminBackender implements JasminBackend {
    ClassUnit classUnit = null;
    int conditionalNumber = 0;
    int methodStackLimit = 0;
    int currentStack = 0;
    String superClass;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        try {
            this.classUnit = ollirResult.getOllirClass();

            // SETUP classUnit
            this.classUnit.checkMethodLabels();
            this.classUnit.buildCFGs();
            this.classUnit.buildVarTables();

            String jasminCode = buildJasminCode();
            List<Report> reports = new ArrayList<>();

            System.out.println("JASMIN CODE : \n" + jasminCode);

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(classUnit.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1,
                            "Jasmin generation exception.", e)));
        }

    }

    private String buildJasminCode() {
        StringBuilder stringBuilder = new StringBuilder();

        // .class  <access-spec> <class-name>
        stringBuilder.append(".class ").append(this.classUnit.getClassName()).append("\n");


        this.superClass = this.classUnit.getSuperClass();
        if (this.superClass == null) {
            this.superClass = "java/lang/Object";
        }

        // .super  <class-name>
        stringBuilder.append(".super ").append(getClassFullName(this.superClass)).append("\n");

        // Fields
        for (Field field : this.classUnit.getFields()) {
            // .field <access-spec> <field-name> <descriptor>
            StringBuilder accessSpec = new StringBuilder();
            if (field.getFieldAccessModifier() != AccessModifiers.DEFAULT) {
                accessSpec.append(field.getFieldAccessModifier().name().toLowerCase()).append(" ");
            }

            if (field.isStaticField()) {
                accessSpec.append("static ");
            }
            if (field.isInitialized()) {
                accessSpec.append("final ");
            }

            stringBuilder.append(".field ").append(accessSpec).append(field.getFieldName())
                    .append(" ").append(this.getFieldDescriptor(field.getFieldType())).append("\n");
        }

        // Methods
        for (Method method : this.classUnit.getMethods()) {
            // .method <access-spec> <method-spec>
            //     <statements>
            // .end method
            stringBuilder.append(this.getMethodHeader(method));
            stringBuilder.append(this.getMethodStatements(method));
            stringBuilder.append(".end method\n");
        }

        return stringBuilder.toString();
    }

    private String getMethodHeader(Method method) {
        StringBuilder stringBuilder = new StringBuilder("\n.method ");

        // <access-spec>
        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT) {
            stringBuilder.append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        }

        if (method.isStaticMethod()) stringBuilder.append("static ");
        if (method.isFinalMethod()) stringBuilder.append("final ");

        // <method-spec>
        if (method.isConstructMethod()) stringBuilder.append("<init>");
        else stringBuilder.append(method.getMethodName());
        stringBuilder.append("(");

        for (Element param : method.getParams()) {
            stringBuilder.append(this.getFieldDescriptor(param.getType()));
        }
        stringBuilder.append(")");
        stringBuilder.append(this.getFieldDescriptor(method.getReturnType())).append("\n");

        return stringBuilder.toString();
    }

    private String getMethodStatements(Method method) {
        Set<Integer> virtualRegs = new TreeSet<>();
        virtualRegs.add(0);
        for (Descriptor descriptor : method.getVarTable().values()) {
            virtualRegs.add(descriptor.getVirtualReg());
        }

        this.currentStack = 0;
        this.methodStackLimit = 0;
        String methodInstructions = this.getMethodInstructions(method);

        return "\t.limit stack " + this.methodStackLimit + "\n" +
                "\t.limit locals " + virtualRegs.size() + "\n" +
                methodInstructions;
    }

    private String getMethodInstructions(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        List<Instruction> methodInstructions = method.getInstructions();
        for (Instruction instruction : methodInstructions) {
            // LABEL:
            for (Map.Entry<String, Instruction> label : method.getLabels().entrySet()) {
                if (label.getValue().equals(instruction)) {
                    stringBuilder.append(label.getKey()).append(":\n");
                }
            }

            stringBuilder.append(this.getInstruction(instruction, method.getVarTable()));
            if (instruction.getInstType() == InstructionType.CALL
                    && ((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {

                stringBuilder.append("\tpop\n");
                this.changeStackLimits(-1);
            }

        }

        boolean hasReturnInstruction = methodInstructions.size() > 0
                && methodInstructions.get(methodInstructions.size() - 1).getInstType() == RETURN;

        if (!hasReturnInstruction && method.getReturnType().getTypeOfElement() == ElementType.VOID) {
            stringBuilder.append("\treturn\n");
        }

        return stringBuilder.toString();
    }

    private String getInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {
        return switch (instruction.getInstType()) {
            case ASSIGN -> this.getAssignInstruction((AssignInstruction) instruction, varTable);
            case CALL -> this.getCallInstruction((CallInstruction) instruction, varTable);
            case GOTO -> this.getGotoInstruction((GotoInstruction) instruction);
            case BRANCH -> this.getBranchInstruction((CondBranchInstruction) instruction, varTable);
            case RETURN -> this.getReturnInstruction((ReturnInstruction) instruction, varTable);
            case PUTFIELD -> this.getPutFieldInstruction((PutFieldInstruction) instruction, varTable);
            case GETFIELD -> this.getGetFieldInstruction((GetFieldInstruction) instruction, varTable);
            case UNARYOPER -> this.getUnaryOperationInstruction((UnaryOpInstruction) instruction, varTable);
            case BINARYOPER -> this.getBinaryOperationInstruction((BinaryOpInstruction) instruction, varTable);
            case NOPER -> this.getLoadToStack(((SingleOpInstruction) instruction).getSingleOperand(), varTable);
        };
    }

    private String getUnaryOperationInstruction(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getLoadToStack(instruction.getOperand(), varTable))
                .append("\t").append(this.getOperation(instruction.getOperation()));

        boolean isBooleanOperation = instruction.getOperation().getOpType() == OperationType.NOTB;
        if (isBooleanOperation) {
            stringBuilder.append(this.getBooleanOperationResultToStack());
        }

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String getBinaryOperationInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        Element leftElement = instruction.getLeftOperand();
        Element rightElement = instruction.getRightOperand();

        stringBuilder.append(this.getLoadToStack(leftElement, varTable))
                .append(this.getLoadToStack(rightElement, varTable))
                .append("\t").append(this.getOperation(instruction.getOperation()));

        OperationType opType = instruction.getOperation().getOpType();
        boolean isBooleanOperation =
                opType == OperationType.EQ
                        || opType == OperationType.GTH
                        || opType == OperationType.GTE
                        || opType == OperationType.LTH
                        || opType == OperationType.LTE
                        || opType == OperationType.NEQ;

        if (isBooleanOperation) {
            stringBuilder.append(this.getBooleanOperationResultToStack());
        }

        stringBuilder.append("\n");

        this.changeStackLimits(-1);
        return stringBuilder.toString();
    }

    private String getBranchInstruction(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        Instruction condition;
        if (instruction instanceof SingleOpCondInstruction) {
            SingleOpCondInstruction singleOpCondInstruction = (SingleOpCondInstruction) instruction;
            condition = singleOpCondInstruction.getCondition();

        } else if (instruction instanceof OpCondInstruction) {
            OpCondInstruction opCondInstruction = (OpCondInstruction) instruction;
            condition = opCondInstruction.getCondition();

        } else {
            return "; ERROR: invalid CondBranchInstruction instance\n";
        }

        stringBuilder.append(this.getInstruction(condition, varTable));
        stringBuilder.append("\tifne ").append(instruction.getLabel()).append("\n");
        this.changeStackLimits(-1);

        return stringBuilder.toString();
    }

    private String getOperation(Operation operation) {
        return switch (operation.getOpType()) {
            case LTH -> "if_icmplt";
            case ANDB -> "iand";
            case NOTB -> "ifeq";

            case ADD -> "iadd";
            case SUB -> "isub";
            case MUL -> "imul";
            case DIV -> "idiv";

            default -> "; ERROR: operation not implemented: " + operation.getOpType() + "\n";
        };
    }

    private String getPutFieldInstruction(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        String res = this.getLoadToStack(instruction.getFirstOperand(), varTable) +
                this.getLoadToStack(instruction.getThirdOperand(), varTable) +
                "\tputfield " + this.getClassFullName(((Operand) instruction.getFirstOperand()).getName()) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + this.getFieldDescriptor(instruction.getSecondOperand().getType()) + "\n";

        this.changeStackLimits(-2);
        return res;
    }

    private String getGetFieldInstruction(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        return this.getLoadToStack(instruction.getFirstOperand(), varTable) +
                "\tgetfield " + this.getClassFullName(((Operand) instruction.getFirstOperand()).getName()) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + this.getFieldDescriptor(instruction.getSecondOperand().getType()) + "\n";
    }

    private String getReturnInstruction(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        if (instruction.hasReturnValue()) {
            stringBuilder.append(this.getLoadToStack(instruction.getOperand(), varTable));
        }

        stringBuilder.append("\t");
        if (instruction.getOperand() != null) {
            ElementType elementType = instruction.getOperand().getType().getTypeOfElement();

            if (elementType == ElementType.INT32 || elementType == ElementType.BOOLEAN) {
                stringBuilder.append("i");
            } else {
                stringBuilder.append("a");
            }
        }

        stringBuilder.append("return\n");

        return stringBuilder.toString();
    }

    private String getGotoInstruction(GotoInstruction instruction) {
        return "\tgoto " + instruction.getLabel() + "\n";
    }

    private String getLoadToStack(Element element, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        if (element instanceof LiteralElement) {
            String literal = ((LiteralElement) element).getLiteral();

            if (element.getType().getTypeOfElement() == ElementType.INT32
                    || element.getType().getTypeOfElement() == ElementType.BOOLEAN) {

                int parsedInt = Integer.parseInt(literal);

                if (parsedInt >= -1 && parsedInt <= 5) { // [-1,5]
                    stringBuilder.append("\ticonst_");
                } else if (parsedInt >= -128 && parsedInt <= 127) { // byte
                    stringBuilder.append("\tbipush ");
                } else if (parsedInt >= -32768 && parsedInt <= 32767) { // short
                    stringBuilder.append("\tsipush ");
                } else {
                    stringBuilder.append("\tldc "); // int
                }

                if (parsedInt == -1) {
                    stringBuilder.append("m1");
                } else {
                    stringBuilder.append(parsedInt);
                }

            } else {
                stringBuilder.append("\tldc ").append(literal);
            }

            this.changeStackLimits(+1);

        } else if (element instanceof ArrayOperand) {
            ArrayOperand operand = (ArrayOperand) element;

            stringBuilder.append("\taload").append(this.getVariableNumber(operand.getName(), varTable)).append("\n"); // load array (ref)
            stringBuilder.append(getLoadToStack(operand.getIndexOperands().get(0), varTable)); // load index
            stringBuilder.append("\tiaload"); // load array[index]

            this.changeStackLimits(+3);
        } else if (element instanceof Operand) {
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case INT32, BOOLEAN -> stringBuilder.append("\tiload").append(this.getVariableNumber(operand.getName(), varTable));
                case OBJECTREF, STRING, ARRAYREF -> stringBuilder.append("\taload").append(this.getVariableNumber(operand.getName(), varTable));
                case THIS -> stringBuilder.append("\taload_0");
                default -> stringBuilder.append("; ERROR: getLoadToStack() operand ").append(operand.getType().getTypeOfElement()).append("\n");
            }

            this.changeStackLimits(+1);
        } else {
            stringBuilder.append("; ERROR: getLoadToStack() invalid element instance\n");
        }

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String getCallInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        int numToPop = 0;

        switch (instruction.getInvocationType()) {
            case invokevirtual -> {
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                numToPop = 1;

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStack(element, varTable));
                    numToPop++;
                }

                stringBuilder.append("\tinvokevirtual ")
                        .append(this.getClassFullName(((ClassType) instruction.getFirstArg().getType()).getName()))
                        .append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                        .append("(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    numToPop--;
                }

            }
            case invokespecial -> {
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                numToPop = 1;

                stringBuilder.append("\tinvokespecial ");

                if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS) {
                    stringBuilder.append(this.superClass);
                } else {
                    stringBuilder.append(this.classUnit.getClassName());
                }

                stringBuilder.append("/").append("<init>(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    numToPop--;
                }

            }
            case invokestatic -> {
                numToPop = 0;

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStack(element, varTable));
                    numToPop++;
                }

                stringBuilder.append("\tinvokestatic ")
                        .append(this.getClassFullName(((Operand) instruction.getFirstArg()).getName()))
                        .append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                        .append("(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    numToPop--;
                }

            }
            case NEW -> {
                numToPop = -1; // -1 already counting with the return of the new

                ElementType elementType = instruction.getReturnType().getTypeOfElement();

                if (elementType == ElementType.OBJECTREF) {
                    for (Element element : instruction.getListOfOperands()) {
                        stringBuilder.append(this.getLoadToStack(element, varTable));
                        numToPop++;
                    }

                    stringBuilder.append("\tnew ").append(this.getClassFullName(((Operand) instruction.getFirstArg()).getName())).append("\n");
                } else if (elementType == ElementType.ARRAYREF) {
                    for (Element element : instruction.getListOfOperands()) {
                        stringBuilder.append(this.getLoadToStack(element, varTable));
                        numToPop++;
                    }

                    stringBuilder.append("\tnewarray ");
                    if (instruction.getListOfOperands().get(0).getType().getTypeOfElement() == ElementType.INT32) {
                        stringBuilder.append("int\n");
                    } else {
                        stringBuilder.append("; only int arrays are implemented\n");
                    }

                } else {
                    stringBuilder.append("; ERROR: NEW invocation type not implemented\n");
                }
            }
            case arraylength -> {
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                stringBuilder.append("\tarraylength\n");
            }
            case ldc -> stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
            default -> stringBuilder.append("; ERROR: call instruction not implemented\n");
        }

        this.changeStackLimits(-numToPop);

        return stringBuilder.toString();
    }

    private String getAssignInstruction(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        Operand dest = (Operand) instruction.getDest();
        if (dest instanceof ArrayOperand) {
            ArrayOperand arrayOperand = (ArrayOperand) dest;
            this.changeStackLimits(+1);
            stringBuilder.append("\taload").append(this.getVariableNumber(arrayOperand.getName(), varTable)).append("\n"); // load array (ref)
            stringBuilder.append(this.getLoadToStack(arrayOperand.getIndexOperands().get(0), varTable)); // load index

        } else {
            // "iinc" instruction selection
            if (instruction.getRhs().getInstType() == BINARYOPER) {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) instruction.getRhs();

                if (binaryOpInstruction.getOperation().getOpType() == OperationType.ADD) {
                    boolean leftIsLiteral = binaryOpInstruction.getLeftOperand().isLiteral();
                    boolean rightIsLiteral = binaryOpInstruction.getRightOperand().isLiteral();

                    LiteralElement literal = null;
                    Operand operand = null;

                    if (leftIsLiteral && !rightIsLiteral) {
                        literal = (LiteralElement) binaryOpInstruction.getLeftOperand();
                        operand = (Operand) binaryOpInstruction.getRightOperand();
                    } else if (!leftIsLiteral && rightIsLiteral) {
                        literal = (LiteralElement) binaryOpInstruction.getRightOperand();
                        operand = (Operand) binaryOpInstruction.getLeftOperand();
                    }

                    if (literal != null && operand != null) {
                        if (operand.getName().equals(dest.getName())) {
                            int literalValue = Integer.parseInt((literal).getLiteral());

                            if (literalValue >= -128 && literalValue <= 127) {
                                return "\tiinc " + varTable.get(operand.getName()).getVirtualReg() + " " + literalValue + "\n";
                            }
                        }
                    }

                }
            }
        }

        stringBuilder.append(this.getInstruction(instruction.getRhs(), varTable));
        stringBuilder.append(this.getStore(dest, varTable)); // store in array[index] if (dest instanceof ArrayOperand)

        return stringBuilder.toString();
    }

    private String getStore(Operand dest, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        switch (dest.getType().getTypeOfElement()) {
            // BOOLEAN is represented as int in JVM
            case INT32, BOOLEAN -> {
                if (varTable.get(dest.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
                    stringBuilder.append("\tiastore").append("\n");
                    this.changeStackLimits(-3);
                } else {
                    stringBuilder.append("\tistore").append(this.getVariableNumber(dest.getName(), varTable)).append("\n");
                    this.changeStackLimits(-1);
                }
            }
            case OBJECTREF, THIS, STRING, ARRAYREF -> {
                stringBuilder.append("\tastore").append(this.getVariableNumber(dest.getName(), varTable)).append("\n");
                this.changeStackLimits(-1);
            }
            default -> stringBuilder.append("; ERROR: getStore()\n");
        }

        return stringBuilder.toString();
    }

    private String getVariableNumber(String name, HashMap<String, Descriptor> varTable) {
        if (name.equals("this")) {
            return "_0";
        }

        int virtualReg = varTable.get(name).getVirtualReg();

        StringBuilder stringBuilder = new StringBuilder();

        // virtual reg 0, 1, 2, 3 have specific operation
        if (virtualReg < 4) stringBuilder.append("_");
        else stringBuilder.append(" ");

        stringBuilder.append(virtualReg);

        return stringBuilder.toString();
    }

    private String getFieldDescriptor(Type type) {
        StringBuilder stringBuilder = new StringBuilder();
        ElementType elementType = type.getTypeOfElement();

        if (elementType == ElementType.ARRAYREF) {
            stringBuilder.append("[");
            elementType = ((ArrayType) type).getArrayType();
        }

        switch (elementType) {
            case INT32 -> stringBuilder.append("I");
            case BOOLEAN -> stringBuilder.append("Z");
            case OBJECTREF -> {
                String name = ((ClassType) type).getName();
                stringBuilder.append("L").append(this.getClassFullName(name)).append(";");
            }
            case STRING -> stringBuilder.append("Ljava/lang/String;");
            case VOID -> stringBuilder.append("V");
            default -> stringBuilder.append("; ERROR: descriptor type not implemented\n");
        }

        return stringBuilder.toString();
    }

    private String getClassFullName(String classNameWithoutImports) {
        if (classNameWithoutImports.equals("this")) {
            return this.classUnit.getClassName();
        }

        for (String importName : this.classUnit.getImports()) {
            if (importName.endsWith(classNameWithoutImports)) {
                return importName.replaceAll("\\.", "/");
            }
        }

        return classNameWithoutImports;
    }

    private String getBooleanOperationResultToStack() {
        return " TRUE" + this.conditionalNumber + "\n"
                + "\ticonst_0\n"
                + "\tgoto NEXT" + this.conditionalNumber + "\n"
                + "TRUE" + this.conditionalNumber + ":\n"
                + "\ticonst_1\n"
                + "NEXT" + this.conditionalNumber++ + ":";
    }

    private void changeStackLimits(int variation) {
        this.currentStack += variation;
        this.methodStackLimit = Math.max(this.methodStackLimit, this.currentStack);
    }

}
