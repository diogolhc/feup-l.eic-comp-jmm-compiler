package pt.up.fe.comp.jmm.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

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

        // declaration
        // .class  <access-spec> <class-name>
        stringBuilder.append(".class ").append(this.classUnit.getClassName()).append('\n');

        // extends
        // .super  <class-name>
        String extendsClass = this.classUnit.getSuperClass();
        if (extendsClass != null) {
            stringBuilder.append(".super ").append(extendsClass).append('\n');
        } else {
            stringBuilder.append(".super java/lang/Object\n");
        }

        // fields
        for (Field field : this.classUnit.getFields()) {
            // .field <access-spec> <field-name> <descriptor> [ = <value> ]
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
        stringBuilder.append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        if (method.isStaticMethod()) stringBuilder.append("static ");
        if (method.isFinalMethod()) stringBuilder.append("final ");

        // <method-spec>
        stringBuilder.append(method.getMethodName()).append('(');
        for (Element param : method.getParams()) {
            stringBuilder.append(this.getFieldDescriptor(param.getType())).append(' ');
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

        // TODO


        stringBuilder.append("return\n");

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
            case INT32:stringBuilder.append('I'); break;
            case BOOLEAN: stringBuilder.append('Z'); break;
            case OBJECTREF:
                String name = ((ClassType) type).getName();
                stringBuilder.append("L").append(this.getClassFullName(name));
                break;
            case CLASS: stringBuilder.append("CLASS"); break;
            case STRING: stringBuilder.append("Ljava/lang/String;"); break;
            case VOID: stringBuilder.append('V'); break;
            default: break; // TODO (?)
        }

        return stringBuilder.toString();
    }

    private String getClassFullName(String classNameWithoutImports) {
        for (String importName : this.classUnit.getImports()) {
            if (importName.endsWith('.' + classNameWithoutImports)) {
                return importName;
            }
        }
        return classNameWithoutImports;
    }

}
