package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testSelfMade5() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/selfMade/SelfMade5.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testFacR() {
        Map<String, String> config = new HashMap<>();
        config.put("debug", "true");
        config.put("optimize", "true");
        config.put("registerAllocation", "4");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/Fac.jmm"), config);
        jasminResult.compile();
        assertEquals("3628800\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testNestedWhilesIfs() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "0");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/NestedWhilesIfs.jmm"), config);
        jasminResult.compile();
        assertEquals("24\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testSimpleConflict() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "0");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SimpleConflict.jmm"), config);
        jasminResult.compile();
        assertEquals("10\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testComplicatedFail() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "4");

        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/selfMade/Complicated.jmm"), config);
        TestUtils.mustFail(ollirResult);
    }

    @Test
    public void testComplicated() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "5");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/Complicated.jmm"), config);
        jasminResult.compile();
        assertEquals("Result: 21\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testShortCutAndReg() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "10");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/ShortCutAnd.jmm"), config);
        jasminResult.compile();
        assertEquals("0\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testShortCutAndRegFail() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "1");

        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/selfMade/ShortCutAnd.jmm"), config);
        TestUtils.mustFail(ollirResult);
    }

    @Test
    public void testSelfMade2Reg() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "5");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SelfMade2.jmm"), config);
        jasminResult.compile();
        assertEquals("2\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testBinomialCoefficientReg() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "6");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/BinomialCoefficient.jmm"), config);
        jasminResult.compile();
        assertEquals("28\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testMonteCarloPiReg() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "10");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/MonteCarloPiModified.jmm"), config);
        jasminResult.compile();
        int result = Integer.parseInt(jasminResult.run().split("\r")[0]);
        assertTrue(result >= 313 && result <= 316);
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testGetterAndSetterExtendReg() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        config.put("debug", "true");
        config.put("registerAllocation", "3");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/GetterAndSetterExtend.jmm"), config);
        jasminResult.compile();
        assertEquals("1\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testShortCutAnd() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/ShortCutAnd.jmm"), config);
        jasminResult.compile();
        assertEquals("0\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testConstPropagationAndFolding() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/ConstPropagationAndFolding.jmm"), config);
        jasminResult.compile();
        assertEquals("-5\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testConstPropFold2() {
        // NOTE: this test is just to visualize
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/ConstPropFold2.jmm"), config);
        jasminResult.compile();
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testWhileDead() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/WhileDead.jmm"), config);
        jasminResult.compile();
        assertEquals("0\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testIfDead() {
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");

        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/IfDead.jmm"), config);
        jasminResult.compile();
        assertEquals("-2\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

}
