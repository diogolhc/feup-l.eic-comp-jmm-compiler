package semantic;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AnalysisTest {

    // Manual testing

    @Test
    public void my_tests() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/LotsOfExpressions.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    // CP2 TESTS

    @Test
    public void ArrayAccessOnInt() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ArrayAccessOnInt.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void ArrayIndexNotInt() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ArrayIndexNotInt.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void ArrayInWhileCondition() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ArrayInWhileCondition.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void ArrayPlusInt() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ArrayPlusInt.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void AssignIntToBool() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "AssignIntToBool.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void AssumeArguments() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "AssumeArguments.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void BoolTimesInt() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "BoolTimesInt.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void CallToMethodAssumedInExtends() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "CallToMethodAssumedInExtends.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void CallToMethodAssumedInImport() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "CallToMethodAssumedInImport.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void CallToUndeclaredMethod() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "CallToUndeclaredMethod.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void ClassNotImported() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ClassNotImported.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void IncompatibleArguments() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "IncompatibleArguments.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void IncompatibleReturn() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "IncompatibleReturn.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void IntInIfCondition() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "IntInIfCondition.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void IntPlusObject() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "IntPlusObject.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void ObjectAssignmentFail() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ObjectAssignmentFail.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void ObjectAssignmentPassExtends() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ObjectAssignmentPassExtends.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void ObjectAssignmentPassImports() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "ObjectAssignmentPassImports.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void SymbolTable() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "SymbolTable.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void VarNotDeclared() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "VarNotDeclared.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    // FIXTURES PUBLIC FAIL

    @Test
    public void arr_index_not_int() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/arr_index_not_int.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void arr_size_not_int() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/arr_size_not_int.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void badArguments() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/badArguments.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void binop_incomp() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/binop_incomp.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void funcNotFound() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/funcNotFound.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void simple_length() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/simple_length.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void var_exp_incomp() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/var_exp_incomp.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void var_lit_incomp() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/var_lit_incomp.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void var_undef() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/var_undef.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    /*@Test
    public void varNotInit() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/varNotInit.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }*/

    @Test
    public void extra() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/extra/miss_type.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

}