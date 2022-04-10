package syntactic;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

public class ExpressionTest {

    @Test
    public void testBinaryExpressionAnd() {
        JmmParserResult parserResult = TestUtils.parse("3  &&  4", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testBinaryExpressionLess() {
        JmmParserResult parserResult = TestUtils.parse("3  <  4", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testBinaryExpressionAdd() {
        JmmParserResult parserResult = TestUtils.parse("3  +  4", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testBinaryExpressionSub() {
        JmmParserResult parserResult = TestUtils.parse("3  -  4", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testBinaryExpressionProduct() {
        JmmParserResult parserResult = TestUtils.parse("3  * 4", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testBinaryExpressionDivision() {
        JmmParserResult parserResult = TestUtils.parse("3 /   4", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionNew1() {
        JmmParserResult parserResult = TestUtils.parse("new int[4]", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionNew2() {
        JmmParserResult parserResult = TestUtils.parse("new int[4+5]", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionNewIdentifier() {
        JmmParserResult parserResult = TestUtils.parse("new identifier()", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionNot() {
        JmmParserResult parserResult = TestUtils.parse("!3", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionNot2() {
        JmmParserResult parserResult = TestUtils.parse("!(3&&5)", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionNot3() {
        JmmParserResult parserResult = TestUtils.parse("!(3&&(!4))", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionSeveral1() {
        JmmParserResult parserResult = TestUtils.parse("new int[3+4*6 < 10 + 4]", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionSeveral2() {
        JmmParserResult parserResult = TestUtils.parse("5 * 1 + 2", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionSeveral3() {
        JmmParserResult parserResult = TestUtils.parse("4 + 5 * 1 + 2 - 3 && 1 < true", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionSeveral4() {
        JmmParserResult parserResult = TestUtils.parse("new int[4 + 5 * 1 + 2 - 3 && 1 < true]", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testExpressionSeveral5() {
        JmmParserResult parserResult = TestUtils.parse("!5 + 6", "Expression");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

}
