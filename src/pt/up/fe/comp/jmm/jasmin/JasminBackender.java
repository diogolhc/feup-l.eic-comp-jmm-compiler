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
        for (Method method : this.classUnit.getMethods()) {
            // TODO
        }

        return stringBuilder.toString();
    }

    private String getFieldDescriptor(Type type) {
        StringBuilder stringBuilder = new StringBuilder();
        ElementType elementType = type.getTypeOfElement();

        if (elementType == ElementType.ARRAYREF) {
            stringBuilder.append("[ ");
            elementType = ((ArrayType) type).getTypeOfElements();
        }

        switch (elementType) {
            case INT32:stringBuilder.append('I'); break;
            case BOOLEAN: stringBuilder.append('Z'); break;
            case OBJECTREF:
                String name = ((ClassType) type).getName(); // TODO check if this name already has imports or not
                stringBuilder.append("L ").append(this.getClassFullName(name));
                break;
            case CLASS: stringBuilder.append("CLASS");
                break;
            case STRING: stringBuilder.append("L java/lang/String ;"); break;
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
