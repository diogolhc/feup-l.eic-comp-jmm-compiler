
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class BackendTest {

    @Test
    public void testBackend() {
        var result = TestUtils.backend(new OllirResult(SpecsIo.getResource("fixtures/public/ollir/Fac.ollir"), Collections.emptyMap()));
        System.out.println("==============JASMIN:==============");
        System.out.println(result.getJasminCode());
        System.out.println("====================================");

        TestUtils.noErrors(result.getReports());

        var output = TestUtils.runJasmin(result.getJasminCode());
        //assertEquals("Hello World!\nHello World Again!\n", SpecsStrings.normalizeFileContents(output));

        //var output = result.run();
        //System.out.println(output.trim());
        //assertEquals("Hello, World!", output.trim());

    }

    @Test
    public void testHelloWorld() {

        String jasminCode = SpecsIo.getResource("fixtures/public/jasmin/HelloWorld.j");
        var output = TestUtils.runJasmin(jasminCode);
        assertEquals("Hello World!\nHello World Again!\n", SpecsStrings.normalizeFileContents(output));
    }


}
