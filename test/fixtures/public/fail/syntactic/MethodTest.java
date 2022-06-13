package syntactic;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;

public class MethodTest {

    @Test
    public void testHeaders() {
        JmmParserResult parserResult = TestUtils.parse("int f()", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("Socket getSocket()", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("Socket getSocketAt(int index)", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("Socket copyOf(Socket socket)", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("int min(int a, int b)", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("boolean bigger(int a, int b)", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("boolean (int a, int b)", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());

        parserResult = TestUtils.parse("boolean (int a, int b", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());

        parserResult = TestUtils.parse("f()", "MethodHeader");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());
    }


}