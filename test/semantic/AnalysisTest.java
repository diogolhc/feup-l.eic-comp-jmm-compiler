package semantic;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AnalysisTest {
    @Test
    public void test() {
        var results = TestUtils.analyse(SpecsIo.getResource("fixtures/public/LotsOfExpressions.jmm"));
        System.out.println("SymbolTable: " + results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }
}
