package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertEquals;

public class BackEndTest {

    @Test
    public void testFac() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/Fac.jmm"));
        jasminResult.compile();
        assertEquals("3628800\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

//    @Test
//    public void testMonteCarloPi() {
//        // this requires input
//        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/Fac.jmm"));
//        jasminResult.compile();
//        assertEquals("314\r\n", jasminResult.run());
//        TestUtils.noErrors(jasminResult);
//    }

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
    public void testBinomialCoefficient() {
        JasminResult jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/BinomialCoefficient.jmm"));
        jasminResult.compile();
        assertEquals("28\r\n", jasminResult.run());
        TestUtils.noErrors(jasminResult);
    }

}
