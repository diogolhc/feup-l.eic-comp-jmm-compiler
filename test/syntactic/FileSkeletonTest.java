package syntactic;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;

public class FileSkeletonTest {

    @Test
    public void testImportDeclarations() {
        JmmParserResult parserResult = TestUtils.parse("import java.util\n", "ImportDeclaration");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());

        parserResult = TestUtils.parse("import java.util;\n", "ImportDeclaration");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("import java;\n", "ImportDeclaration");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("import java.util.LinkedList;\n", "ImportDeclaration");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());
    }

    @Test
    public void testClassDeclaration() {
        JmmParserResult parserResult = TestUtils.parse("class A {}\n", "ClassDeclaration");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("class {}\n", "ClassDeclaration");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());

        parserResult = TestUtils.parse("A {}\n", "ClassDeclaration");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());

        parserResult = TestUtils.parse("class A extends B {}\n", "ClassDeclaration");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("class A extends {}\n", "ClassDeclaration");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());
    }

    @Test
    public void testClassAndImportsDeclaration() {
        JmmParserResult parserResult = TestUtils.parse("class A {}\n", "Program");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse("import java.util; import java.net.Socket; class A {}\n", "Program");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());


    }

    @Test
    public void testMethodsAndVars() {
        String test1 = SpecsIo.getResource("fixtures/public/Test1.jmm");
        String test2 = SpecsIo.getResource("fixtures/public/Test2.jmm");
        String test3 = SpecsIo.getResource("fixtures/public/Test3.jmm");

        JmmParserResult parserResult = TestUtils.parse(test1, "Program");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());


        parserResult = TestUtils.parse(test2, "Program");
        parserResult.dumpReports();
        TestUtils.noErrors(parserResult.getReports());

        parserResult = TestUtils.parse(test3, "Program");
        parserResult.dumpReports();
        TestUtils.mustFail(parserResult.getReports());
    }
}