package pt.up.fe.comp.jmm.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;


public class JasminBackender implements JasminBackend {
    ClassUnit classUnit = null;
    int conditionalNumber = 0;
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
                    Arrays.asList(Report.newError(Stage.GENERATION, -1, -1,
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

        // fields
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

            stringBuilder.append(".field ").append(accessSpec.toString()).append(field.getFieldName())
                    .append(" ").append(this.getFieldDescriptor(field.getFieldType())).append("\n");
        }

        // methods
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
            stringBuilder.append(method.getMethodAccessModifier().name().toLowerCase() + " ");
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
        StringBuilder stringBuilder = new StringBuilder();

        // "you can ignore stack and local limits for now, use limit_stack 99 and limit_locals 99)"
        // TODO after Check Point 2 deal with it
        stringBuilder.append("\t.limit stack 99\n");
        stringBuilder.append("\t.limit locals 99\n");

        stringBuilder.append(this.getMethodInstructions(method));

        return stringBuilder.toString();
    }

    private String getMethodInstructions(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Instruction instruction : method.getInstructions()) {
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
            }

        }

        if (method.getReturnType().getTypeOfElement() == ElementType.VOID) {
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
            default -> "; ERROR: getInstruction() " + instruction.getInstType() + "\n";
        };
    }

    private String getUnaryOperationInstruction(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getLoadToStack(instruction.getOperand(), varTable))
                     .append("\t").append(this.getOperation(instruction.getOperation()));

        Boolean isBooleanOperation = instruction.getOperation().getOpType() == OperationType.NOTB;
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

        // TODO add "iinc" case

        stringBuilder.append(this.getLoadToStack(leftElement, varTable))
                     .append(this.getLoadToStack(rightElement, varTable))
                     .append("\t").append(this.getOperation(instruction.getOperation()));

        OperationType opType = instruction.getOperation().getOpType();
        Boolean isBooleanOperation =
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
        return this.getLoadToStack(instruction.getFirstOperand(), varTable) +
                this.getLoadToStack(instruction.getThirdOperand(), varTable) +
                "\tputfield " + this.getClassFullName(((Operand) instruction.getFirstOperand()).getName()) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + this.getFieldDescriptor(instruction.getSecondOperand().getType()) + "\n";
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


        } else if (element instanceof ArrayOperand) {
            // TODO Check Point 3
            stringBuilder.append("; CP3");

        } else if (element instanceof Operand) {
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case INT32: case BOOLEAN: stringBuilder.append("\tiload").append(this.getVariableNumber(operand.getName(), varTable)); break;
                case ARRAYREF: stringBuilder.append("; CP3"); break; // TODO Check Point 3
                case OBJECTREF: case STRING: stringBuilder.append("\taload").append(this.getVariableNumber(operand.getName(), varTable)); break;
                case THIS: stringBuilder.append("\taload_0"); break;
                default:
                    stringBuilder.append("; ERROR: getLoadToStack() operand " + operand.getType().getTypeOfElement() + "\n");
            }
        } else {
            stringBuilder.append("; ERROR: getLoadToStack() invalid element instance\n");
        }

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String getCallInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        switch (instruction.getInvocationType()) {
            case invokevirtual:
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStack(element, varTable));
                }

                stringBuilder.append("\tinvokevirtual ")
                             .append(this.getClassFullName(((ClassType) instruction.getFirstArg().getType()).getName()))
                             .append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                             .append("(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                break;
            case invokespecial:
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));

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

                break;
            case invokestatic:
                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStack(element, varTable));
                }

                stringBuilder.append("\tinvokestatic ")
                        .append(this.getClassFullName(((Operand)instruction.getFirstArg()).getName()))
                        .append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                        .append("(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                break;
            case NEW:
                ElementType elementType = instruction.getReturnType().getTypeOfElement();
                if (elementType == ElementType.OBJECTREF) {
                    for (Element element : instruction.getListOfOperands()) {
                        stringBuilder.append(this.getLoadToStack(element, varTable));
                    }

                    stringBuilder.append("\tnew ").append(this.getClassFullName(((Operand) instruction.getFirstArg()).getName()))
                            .append("\n\tdup\n"); // TODO confirm if this dup is necessary and if pop is also necessary after a call (void or not)
                } else if (elementType == ElementType.ARRAYREF) {
                    // TODO Check Point 3
                    stringBuilder.append("; CP3\n");
                } else {
                    stringBuilder.append("; ERROR: NEW invocation type not implemented\n");
                }
                break;
            case arraylength:
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                stringBuilder.append("\tarraylength\n");
                break;
            case ldc:
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                break;
            default:
                stringBuilder.append("; ERROR: call instruction not implemented\n");
        }

        return stringBuilder.toString();
    }

    private String getAssignInstruction(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        return this.getInstruction(instruction.getRhs(), varTable) +
                this.getStore((Operand) instruction.getDest(), varTable);
    }

    private String getStore(Operand dest, HashMap<String, Descriptor> varTable) {
<<<<<<< HEAD
        /*return switch (dest.getType().getTypeOfElement()) {
            // TODO may booleans be treated as INT32 here?
            case INT32, BOOLEAN -> "istore" + getVariableNumber(dest.getName(), varTable) + '\n';
            // TODO CheckPoint3
            case ARRAYREF -> "TODO CheckPoint3";
            // TODO can STRING and THIS be treated as OBJECTREF?
            case OBJECTREF, THIS, STRING -> "astore" + getVariableNumber(dest.getName(), varTable) + '\n';
            default -> "getStore() error";
        };*/ return null;
=======
        return switch (dest.getType().getTypeOfElement()) {
            // BOOLEAN is represented as int in JVM
            case INT32, BOOLEAN -> "\tistore" + getVariableNumber(dest.getName(), varTable) + "\n";
            case ARRAYREF -> "; CP3\n"; // TODO Check Point 3
            case OBJECTREF, THIS, STRING -> "\tastore" + getVariableNumber(dest.getName(), varTable) + "\n";
            default -> "; ERROR: getStore()\n";
        };
>>>>>>> master
    }

    private String getVariableNumber(String name, HashMap<String, Descriptor> varTable) {
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
            elementType = ((ArrayType) type).getTypeOfElements();
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

}
