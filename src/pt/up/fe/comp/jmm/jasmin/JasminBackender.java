package pt.up.fe.comp.jmm.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JasminBackender implements JasminBackend {
    ClassUnit classUnit = null;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.classUnit = ollirResult.getOllirClass();

        String jasminCode = buildJasminCode();
        List<Report> reports = new ArrayList<>(); // TODO what to add here?

        return new JasminResult(ollirResult, jasminCode, reports);
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
            stringBuilder.append(".field ").append(field.getFieldName()).append(' ').append(this.getFieldDescriptor(field.getFieldType())).append('\n');
        }

        // methods
        // .method <access-spec> <method-spec>
        //     <statements>
        // .end method
        for (Method method : this.classUnit.getMethods()) {
            stringBuilder.append(this.getMethodHeader(method));
            stringBuilder.append(this.getMethodStatements(method));
            stringBuilder.append(".end method\n");
        }

        return stringBuilder.toString();
    }

    private String getMethodHeader(Method method) {
        if (method.isConstructMethod()) {
            return "\n.method public <init>()V\n";
        }

        StringBuilder stringBuilder = new StringBuilder("\n.method ");

        // <access-spec>
        String accessSpec = "";
        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT) {
            accessSpec = method.getMethodAccessModifier().name().toLowerCase();
        }
        stringBuilder.append(accessSpec).append(" ");
        if (method.isStaticMethod()) stringBuilder.append("static ");
        if (method.isFinalMethod()) stringBuilder.append("final ");

        // <method-spec>
        stringBuilder.append(method.getMethodName()).append('(');
        for (Element param : method.getParams()) {
            stringBuilder.append(this.getFieldDescriptor(param.getType())).append(' '); // TODO should this be ';' instead of ' '?
        }
        stringBuilder.append(')');
        stringBuilder.append(this.getFieldDescriptor(method.getReturnType())).append('\n');

        return stringBuilder.toString();
    }

    private String getMethodStatements(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        if (method.isConstructMethod()) { // TODO devemos retornar apenas isto para constructors? (deu essa impressão no vídeo)
            stringBuilder.append("aload_0\n");

            String superClass = this.classUnit.getSuperClass();
            if (superClass == null) {
                superClass = "java/lang/Object";
            }
            stringBuilder.append("invokespecial ").append(superClass).append("/<init>()V\n");
        }

        // "you can ignore stack and local limits for now, use limit_stack 99 and limit_locals 99)"
        // TODO after CheckPoint2 deal with it
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

            method.buildVarTable();
            stringBuilder.append(this.getInstruction(instruction, method.getVarTable()));
        }

        stringBuilder.append("return\n");
        return stringBuilder.toString();
    }

    private String getInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {

        switch (instruction.getInstType()) {
            case ASSIGN: return this.getAssignInstruction((AssignInstruction) instruction, varTable);
            case CALL: return this.getCallInstruction((CallInstruction) instruction, varTable);
            case GOTO:
                // TODO
                break;
            case BRANCH:
                // TODO
                break;
            case RETURN:
                // TODO
                break;
            case PUTFIELD:
                // TODO
                break;
            case GETFIELD:
                // TODO
                break;
            case UNARYOPER:
                // TODO
                break;
            case BINARYOPER:
                // TODO
                break;
            case NOPER:
                // TODO
                break;
        }

        return "getInstruction() error\n";
    }

    private String getCallInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        instruction.show();

        return "";
    }

    private String getAssignInstruction(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getInstruction(instruction.getRhs(), varTable));
        stringBuilder.append(this.getStore((Operand) instruction.getDest(), varTable));

        return stringBuilder.toString();
    }

    private String getStore(Operand dest, HashMap<String, Descriptor> varTable) {
        return switch (dest.getType().getTypeOfElement()) {
            // BOOLEAN is represented as int in JVM
            case INT32, BOOLEAN -> "istore" + getVariableNumber(dest.getName(), varTable) + '\n';
            // TODO CheckPoint3
            case ARRAYREF -> "TODO CheckPoint3";
            case OBJECTREF, THIS, STRING -> "astore" + getVariableNumber(dest.getName(), varTable) + '\n';
            default -> "getStore() error";
        };
    }

    private String getVariableNumber(String name, HashMap<String, Descriptor> varTable) {

        //System.out.println("vartable: " + varTable);
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
            case CLASS -> {}// TODO ?;
            case STRING -> stringBuilder.append("Ljava/lang/String;");
            case VOID -> stringBuilder.append('V');
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
