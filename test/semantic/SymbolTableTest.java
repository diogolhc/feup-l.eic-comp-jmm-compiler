package semantic;

import org.junit.Test;
import org.junit.Assert;
import pt.up.fe.comp.analysis.table.SymbolTableImpl;
import pt.up.fe.comp.analysis.table.VarAlreadyDefinedException;
import pt.up.fe.comp.analysis.table.VarNotInScopeException;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTableTest {
    @Test
    public void testRepeatedFields() {
        List<String> imports = new ArrayList<>();
        List<Symbol> fields = new ArrayList<>();

        fields.add(new Symbol(new Type("int", false), "a"));
        fields.add(new Symbol(new Type("int", true), "a"));

        Map<String, List<Symbol>> parameters = new HashMap<>();
        Map<String, List<Symbol>> localVariables = new HashMap<>();
        Map<String, Type> methodTypes = new HashMap<>();

        try {
            new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), VarAlreadyDefinedException.class);
        }

        fields = new ArrayList<>();
        fields.add(new Symbol(new Type("int", false), "a"));
        fields.add(new Symbol(new Type("int", true), "b"));

        try {
            new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testRepeatedParameters() {
        List<String> imports = new ArrayList<>();
        List<Symbol> fields = new ArrayList<>();
        Map<String, List<Symbol>> parameters = new HashMap<>();
        List<Symbol> params = new ArrayList<>();
        params.add(new Symbol(new Type("int", false), "a"));
        params.add(new Symbol(new Type("int", false), "a"));
        parameters.put("f", params);
        Map<String, List<Symbol>> localVariables = new HashMap<>();

        Map<String, Type> methodTypes = new HashMap<>();

        try {
            new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), VarAlreadyDefinedException.class);
        }

        parameters = new HashMap<>();
        params = new ArrayList<>();
        params.add(new Symbol(new Type("int", false), "a"));
        params.add(new Symbol(new Type("int", false), "b"));
        parameters.put("f", params);

        try {
            new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testRepeatedLocalVariables() {
        List<String> imports = new ArrayList<>();
        List<Symbol> fields = new ArrayList<>();
        Map<String, List<Symbol>> parameters = new HashMap<>();
        List<Symbol> params = new ArrayList<>();
        params.add(new Symbol(new Type("int", false), "a"));
        parameters.put("f", params);
        Map<String, List<Symbol>> localVariables = new HashMap<>();
        List<Symbol> locals = new ArrayList<>();
        locals.add(new Symbol(new Type("int", false), "a"));
        localVariables.put("f", locals);
        Map<String, Type> methodTypes = new HashMap<>();

        try {
            new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), VarAlreadyDefinedException.class);
        }


        parameters = new HashMap<>();
        params = new ArrayList<>();
        params.add(new Symbol(new Type("int", false), "a"));
        parameters.put("f", params);
        localVariables = new HashMap<>();
        locals = new ArrayList<>();
        locals.add(new Symbol(new Type("int", false), "b"));
        localVariables.put("f", locals);

        try {
            new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testFindVariable() throws VarAlreadyDefinedException {
        List<String> imports = new ArrayList<>();
        List<Symbol> fields = new ArrayList<>();
        Map<String, List<Symbol>> parameters = new HashMap<>();
        List<Symbol> params = new ArrayList<>();
        params.add(new Symbol(new Type("int", false), "a"));
        parameters.put("f", params);
        Map<String, List<Symbol>> localVariables = new HashMap<>();
        List<Symbol> locals = new ArrayList<>();
        locals.add(new Symbol(new Type("int", false), "b"));
        localVariables.put("f", locals);
        Map<String, Type> methodTypes = new HashMap<>();
        methodTypes.put("f", new Type("int", false));

        var m = new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        try {
            m.findVariable("f", "d");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), VarNotInScopeException.class);
        }

        fields.add(new Symbol(new Type("int", false), "d"));

        m = new SymbolTableImpl("A", "B", imports, fields, parameters, localVariables, methodTypes);
        try {
            m.findVariable("f", "d");
        } catch (Exception e) {
            Assert.fail();
        }

    }
}
