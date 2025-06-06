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
import net.binis.codegen.objects.TestCompiledEnumPrototype;
import net.binis.codegen.test.BaseCodeGenTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class EnumBasicsTest extends BaseCodeGenTest {

    @Test
    void test() {
        testSingle("basic/enum/EnumTest1.java", "basic/enum/EnumTest1-0.java", "basic/enum/EnumTest1-1.java");
    }

    @Test
    void testExtended() {
        testSingle("basic/enum/EnumTest2.java", "basic/enum/EnumTest2-0.java", "basic/enum/EnumTest2-1.java");
    }

    @Test
    void testMixIn() {
        testMulti(List.of(
                Triple.of("basic/enum/EnumTest3.java", "basic/enum/EnumTest3-0.java", "basic/enum/EnumTest3-1.java"),
                Triple.of("basic/enum/EnumTest3MixIn.java", null, "basic/enum/EnumTest3MixIn-1.java"),
                Triple.of("basic/enum/EnumTest3MixIn2.java", null, "basic/enum/EnumTest3MixIn2-1.java"),
                Triple.of("basic/enum/EnumTest3MixIn3.java", "basic/enum/EnumTest3MixIn3-0.java", "basic/enum/EnumTest3MixIn3-1.java")));
    }

    @Test
    void testEnrichment() {
        testSingle("basic/enum/enrichTest1.java", "basic/enum/enrichTest1-0.java", "basic/enum/enrichTest1-1.java");
    }

    @Test
    void testQuery() {
        testMulti(List.of(
                Triple.of("basic/enum/EnumTest3.java", "basic/enum/EnumTest3-0.java", "basic/enum/EnumTest3-1.java"),
                Triple.of("basic/enum/EnumTest4.java", "basic/enum/EnumTest4-0.java", "basic/enum/EnumTest4-1.java")));
    }

    @Test
    void testRequiredArgs() {
        testSingleExecute("basic/enum/EnumTest5.java", "basic/enum/EnumTest5-0.java", "basic/enum/EnumTest5-1.java", null, 1, "basic/enum/EnumTest5-2.java", false, false, true);
    }

    @Test
    void testOrdinal() {
        testSingle("basic/enum/EnumTest6.java", "basic/enum/EnumTest6-0.java", "basic/enum/EnumTest6-1.java");
    }

    @Test
    void testEmbeddedList() {
        testSingleExecute("basic/enum/EnumTest7.java", "basic/enum/EnumTest7-0.java", "basic/enum/EnumTest7-1.java", 3, "basic/enum/EnumTest7-2.java");
    }

    @Test
    void testMixInCompiled() {
        testSingle("basic/enum/EnumTest8.java", "basic/enum/EnumTest8-0.java", "basic/enum/EnumTest8-1.java", 2, true);
    }


}
