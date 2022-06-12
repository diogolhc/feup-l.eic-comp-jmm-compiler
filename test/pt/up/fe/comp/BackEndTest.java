package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BackEndTest {

    @Test
    public void testFac() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/Fac.jmm"));
        jasminResult.compile();
        assertEquals("3628800\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testSelfMade() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SelfMade1.jmm"));
        jasminResult.compile();
        assertEquals("5\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testSelfMade2() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SelfMade2.jmm"));
        jasminResult.compile();
        assertEquals("2\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testSelfMade3() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SelfMade3.jmm"));
        jasminResult.compile();
        assertEquals("12\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testSelfMade4() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SelfMade4.jmm"));
        jasminResult.compile();
        assertEquals("3\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testSelfMade5() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SelfMade5.jmm"));
        jasminResult.compile();
        assertEquals("98\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testSelfMade6() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/SelfMade6.jmm"));
        jasminResult.compile();
        assertEquals("39\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testBinomialCoefficient() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/BinomialCoefficient.jmm"));
        jasminResult.compile();
        assertEquals("28\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testVarConflict() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/VarConflict.jmm"));
        jasminResult.compile();
        assertEquals("6\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testArray() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/Array.jmm"));
        jasminResult.compile();
        assertEquals("1\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testArrayLoop() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/ArrayLoop.jmm"));
        jasminResult.compile();
        assertEquals("5050\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

    @Test
    public void testIfs() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/selfMade/Ifs.jmm"));
        jasminResult.compile();
        assertEquals("1812313\r\n", jasminResult.run());
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

}
