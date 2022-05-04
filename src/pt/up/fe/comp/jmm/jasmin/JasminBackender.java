package pt.up.fe.comp.jmm.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class JasminBackender implements JasminBackend {
    ClassUnit classUnit = null;

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
        stringBuilder.append(".class ").append(this.classUnit.getClassName()).append('\n');

        // .super  <class-name>
        String extendsClass = this.classUnit.getSuperClass();
        if (extendsClass != null) {
            stringBuilder.append(".super ").append(getClassFullName(extendsClass)).append('\n');
        } else {
            stringBuilder.append(".super java/lang/Object\n");
        }

        // fields
        for (Field field : this.classUnit.getFields()) {
            // .field <access-spec> <field-name> <descriptor>
            StringBuilder accessSpec = new StringBuilder();
            if (field.getFieldAccessModifier() != AccessModifiers.DEFAULT) {
                accessSpec.append(field.getFieldAccessModifier().name().toLowerCase()).append(' ');
            }

            if (field.isStaticField()) {
                accessSpec.append("static ");
            }
            if (field.isInitialized()) {
                accessSpec.append("final ");
            }

            stringBuilder.append(".field ").append(accessSpec.toString()).append(field.getFieldName())
                    .append(' ').append(this.getFieldDescriptor(field.getFieldType())).append('\n');
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
        String accessSpec = "";
        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT) {
            accessSpec = method.getMethodAccessModifier().name().toLowerCase() + ' ';
        }

        if (method.isStaticMethod()) stringBuilder.append("static ");
        if (method.isFinalMethod()) stringBuilder.append("final ");

        // <method-spec>
        if (method.isConstructMethod()) stringBuilder.append("<init>");
        else stringBuilder.append(method.getMethodName());
        stringBuilder.append('(');

        for (Element param : method.getParams()) {
            stringBuilder.append(this.getFieldDescriptor(param.getType())).append(' '); // TODO should this be ';' instead of ' '?
        }
        stringBuilder.append(')');
        stringBuilder.append(this.getFieldDescriptor(method.getReturnType())).append('\n');

        return stringBuilder.toString();
    }

    private String getMethodStatements(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        if (method.isConstructMethod()) {
            stringBuilder.append("aload_0\n");

            String superClass = this.classUnit.getSuperClass();
            if (superClass == null) {
                superClass = "java/lang/Object";
            }
            stringBuilder.append("invokespecial ").append(superClass).append("/<init>()V\n");
        }

        // "you can ignore stack and local limits for now, use limit_stack 99 and limit_locals 99)"
        // TODO after Check Point 2 deal with it
        stringBuilder.append(".limit stack 99\n.limit locals 99\n");

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
        }

        stringBuilder.append("return\n"); // TODO should this be added here? this is just so that there is always a return
        return stringBuilder.toString();
    }

    private String getInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {

        switch (instruction.getInstType()) {
            case ASSIGN: return this.getAssignInstruction((AssignInstruction) instruction, varTable);
            case CALL: return this.getCallInstruction((CallInstruction) instruction, varTable);
            case GOTO: return this.getGotoInstruction((GotoInstruction) instruction);
            case BRANCH: return this.getBranchInstruction((CondBranchInstruction) instruction, varTable);
            case RETURN: return this.getReturnInstruction((ReturnInstruction) instruction, varTable);
            case PUTFIELD: return this.getPutFieldInstruction((PutFieldInstruction) instruction, varTable);
            case GETFIELD: return this.getGetFieldInstruction((GetFieldInstruction) instruction, varTable);
            case UNARYOPER:
                // TODO
                break;
            case BINARYOPER:
                // TODO
                break;
            case NOPER: return this.getLoadToStack(((SingleOpInstruction) instruction).getSingleOperand(), varTable);
        }

        return "ERROR: getInstruction() " + instruction.getInstType() + "\n";
    }

    private String getBranchInstruction(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();
        instruction.show();

        /*

        Element leftElement = instruction.getLeftOperand(); // TODO this is not compiling somehow
        Element rightElement = instruction.getRightOperand();

        stringBuilder.append(this.getLoadToStack(leftElement, varTable)).append(this.getLoadToStack(rightElement, varTable))
                     .append(this.getOperation(instruction.getCondOperation())).append(" ").append(instruction.getLabel())
                     .append("\n");
*/

        return stringBuilder.toString();
    }

    private String getOperation(Operation operation) {
        return switch (operation.getOpType()) {
            case EQ -> "if_icmpeq";
            case GTH -> "if_icmpgt";
            case GTE -> "if_icmpge";
            case LTH -> "if_icmplt";
            case LTE -> "if_icmple";
            case NOTB, NEQ -> "if_icmpne";

            case ADD -> "iadd";
            case SUB -> "isub";
            case MUL -> "imul";
            case DIV -> "idiv";

            default -> "ERROR: operation not implemented\n";
        };
    }

    private String getPutFieldInstruction(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        return this.getLoadToStack(instruction.getFirstOperand(), varTable) +
                this.getLoadToStack(instruction.getThirdOperand(), varTable) +
                "putfield " + this.getClassFullName(((Operand) instruction.getFirstOperand()).getName()) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + this.getFieldDescriptor(instruction.getSecondOperand().getType());
    }

    private String getGetFieldInstruction(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        return this.getLoadToStack(instruction.getFirstOperand(), varTable) +
                "getfield " + this.getClassFullName(((Operand) instruction.getFirstOperand()).getName()) +
                "/" + ((Operand) instruction.getSecondOperand()).getName() +
                " " + this.getFieldDescriptor(instruction.getSecondOperand().getType());
    }

    private String getReturnInstruction(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        if (instruction.hasReturnValue()) {
            stringBuilder.append(this.getLoadToStack(instruction.getOperand(), varTable));
        }

        ElementType elementType = instruction.getOperand().getType().getTypeOfElement();

        if (elementType == ElementType.INT32 || elementType == ElementType.BOOLEAN) {
            stringBuilder.append('i');
        } else {
            stringBuilder.append('a');
        }

        stringBuilder.append("return\n");

        return stringBuilder.toString();
    }

    private String getGotoInstruction(GotoInstruction instruction) {
        return "goto " + instruction.getLabel() + "\n";
    }

    private String getLoadToStack(Element element, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        if (element instanceof LiteralElement) {
            String literal = ((LiteralElement) element).getLiteral();
            int parsedInt = Integer.parseInt(literal);

            if (element.getType().getTypeOfElement() == ElementType.INT32
                    || element.getType().getTypeOfElement() == ElementType.BOOLEAN) {

                if (parsedInt >= -1 && parsedInt <= 5) { // [-1,5]
                    stringBuilder.append("iconst_");
                } else if (parsedInt >= -128 && parsedInt <= 127) { // byte
                    stringBuilder.append("bipush ");
                } else if (parsedInt >= -32768 && parsedInt <= 32767) { // short
                    stringBuilder.append("sipush ");
                } else {
                    stringBuilder.append("ldc "); // int
                }
            } else {
                stringBuilder.append("ldc ");
            }

            if (parsedInt == -1) {
                stringBuilder.append("m1");
            } else {
                stringBuilder.append(parsedInt);
            }

        } else if (element instanceof ArrayOperand) {
            // TODO Check Point 3

        } else if (element instanceof Operand) {
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case INT32: case BOOLEAN: stringBuilder.append("iload").append(this.getVariableNumber(operand.getName(), varTable)); break;
                case ARRAYREF: break;// TODO Check Point 3
                case OBJECTREF: stringBuilder.append("aload").append(this.getVariableNumber(operand.getName(), varTable)); break;
                case THIS: stringBuilder.append("aload_0"); break;
                default:
                    stringBuilder.append("ERROR: getLoadToStack() operand " + operand.getType().getTypeOfElement() + '\n');
            }
        } else {
            stringBuilder.append("ERROR: getLoadToStack() invalid element instance\n");

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

                stringBuilder.append("invokevirtual ")
                             .append(this.getClassFullName(((ClassType) instruction.getFirstArg().getType()).getName()))
                             .append('/').append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
                             .append('(');

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(')').append(this.getFieldDescriptor(instruction.getReturnType())).append('\n');

                break;
            case invokespecial:
                // TODO

                break;
            case invokestatic:
                // TODO

                break;
            case NEW:
                ElementType elementType = instruction.getReturnType().getTypeOfElement();
                if (elementType == ElementType.OBJECTREF) {
                    for (Element element : instruction.getListOfOperands()) {
                        stringBuilder.append(this.getLoadToStack(element, varTable));
                    }

                    stringBuilder.append("new ").append(this.getClassFullName(((Operand) instruction.getFirstArg()).getName())).append('\n');
                } else if (elementType == ElementType.ARRAYREF) {
                    // TODO Check Point 3
                } else {
                    stringBuilder.append("ERROR: NEW invocation type not implemented\n");
                }
                break;
            case arraylength:
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                stringBuilder.append("arraylength\n");
                break;
            case ldc:
                stringBuilder.append(this.getLoadToStack(instruction.getFirstArg(), varTable));
                break;
            default:
                stringBuilder.append("ERROR: call instruction not implemented\n");
        }

        return stringBuilder.toString();
    }

    private String getAssignInstruction(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        return this.getInstruction(instruction.getRhs(), varTable) +
                this.getStore((Operand) instruction.getDest(), varTable);
    }

    private String getStore(Operand dest, HashMap<String, Descriptor> varTable) {
        return switch (dest.getType().getTypeOfElement()) {
            // BOOLEAN is represented as int in JVM
            case INT32, BOOLEAN -> "istore" + getVariableNumber(dest.getName(), varTable) + '\n';
            case ARRAYREF -> "TODO Check Point 3\n"; // TODO Check Point 3
            case OBJECTREF, THIS, STRING -> "astore" + getVariableNumber(dest.getName(), varTable) + '\n';
            default -> "ERROR: getStore()\n";
        };
    }

    private String getVariableNumber(String name, HashMap<String, Descriptor> varTable) {
        int virtualReg = varTable.get(name).getVirtualReg();

        StringBuilder stringBuilder = new StringBuilder();

        // virtual reg 0, 1, 2, 3 have specific operation
        if (virtualReg < 4) stringBuilder.append('_');
        else stringBuilder.append(' ');

        stringBuilder.append(virtualReg);

        return stringBuilder.toString();
    }


    private String getFieldDescriptor(Type type) {
        StringBuilder stringBuilder = new StringBuilder();
        ElementType elementType = type.getTypeOfElement();

        if (elementType == ElementType.ARRAYREF) {
            stringBuilder.append('[');
            elementType = ((ArrayType) type).getTypeOfElements();
        }

        switch (elementType) {
            case INT32 -> stringBuilder.append('I');
            case BOOLEAN -> stringBuilder.append('Z');
            case OBJECTREF -> {
                String name = ((ClassType) type).getName();
                stringBuilder.append('L').append(this.getClassFullName(name));
            }
            case STRING -> stringBuilder.append("Ljava/lang/String;");
            case VOID -> stringBuilder.append('V');
            default -> stringBuilder.append("ERROR: descriptor type not implemented\n");
        }

        return stringBuilder.toString();
    }

    private String getClassFullName(String classNameWithoutImports) {
        for (String importName : this.classUnit.getImports()) {
            if (importName.endsWith('.' + classNameWithoutImports)) {
                return importName.replaceAll("\\.", "/");
            }
        }
        return classNameWithoutImports;
    }

}
