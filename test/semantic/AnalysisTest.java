package semantic;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AnalysisTest {

    @Test
    public void manual_sem() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cp2/" +
                "IntInIfCondition.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }


    @Test
    public void arr_index_not_int() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/arr_index_not_int.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void arr_size_not_int() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/arr_size_not_int.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void badArguments() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/badArguments.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void binop_incomp() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/binop_incomp.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void funcNotFound() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/funcNotFound.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void simple_length() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/simple_length.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void var_exp_incomp() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/var_exp_incomp.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void var_lit_incomp() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/var_lit_incomp.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void var_undef() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/var_undef.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void varNotInit() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/varNotInit.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void extra() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/fail/" +
                "semantic/extra/miss_type.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

}