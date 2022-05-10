package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;

public class OptimizationTest {
    @Test
    public void test() {
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/NewHello.jmm"));
        TestUtils.noErrors(ollirResult);
    }

    @Test
    public void testHelloWorld() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testMonteCarlo() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testLife() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Life.jmm"));
        TestUtils.noErrors(result.getReports());
    }

}
