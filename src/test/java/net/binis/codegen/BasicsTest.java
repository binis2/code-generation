package net.binis.codegen;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2024 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.test.BaseCodeGenTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.binis.codegen.generation.core.Helpers.lookup;

@Slf4j
class BasicsTest extends BaseCodeGenTest {

    @Test
    void test() {
        testSingle("basic/Test1.java", "basic/Test1-0.java", "basic/Test1-1.java");
    }

    @Test
    void testForAnnotations() {
        testSingle("basic/TestFor.java", "basic/TestFor-0.java", "basic/TestFor-1.java");
    }

    @Test
    void testExtended() {
        testMulti(List.of(
                Triple.of("extended/Extended1.java", "extended/Extended1-0.java", "extended/Extended1-1.java"),
                Triple.of("extended/Test1.java", "extended/Test1-0.java", "extended/Test1-1.java")
        ));
    }

    @Test
    void testAddField() {
        testSingle("basic/TestAdd.java", "basic/TestAdd-0.java", "basic/TestAdd-1.java");
    }

    @Test
    void testFieldFromExternalInterfaceAndDefaultImplementation() {
        testSingle("basic/TestDefault.java", "basic/TestDefault-0.java", "basic/TestDefault-1.java");
    }

    @Test
    void testFieldFromExternalInterfaceAndDefaultImplementationBoolean() {
        testSingle("basic/TestDefault3.java", "basic/TestDefault3-0.java", "basic/TestDefault3-1.java");
    }


    @Test
    void testFieldFromInterfaceAndDefaultImplementation() {
        lookup.registerExternalLookup(s -> {
            if ("net.binis.codegen.test.TestTitle".equals(s)) {
                return """
                        package net.binis.codegen.test;
                        
                        public interface TestTitle {
                            String getTitle();
                        }
                        """;
            }
            return null;
        });
        testSingleSkip("basic/TestDefault2.java", "basic/TestDefault2-0.java", "basic/TestDefault2-1.java", true, true);
    }

    @Test
    void testTransient() {
        testSingle("base/baseIgnore1.java", "base/baseIgnore1-0.java", "base/baseIgnore1-1.java");
    }

    public interface TestTitle {
        String getTitle();
    }

}
