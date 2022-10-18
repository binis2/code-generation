package net.binis.codegen;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
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
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.test.BaseTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@Slf4j
public class EnumBasicsTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void test() {
        testSingle("basic/enum/EnumTest1.java", "basic/enum/EnumTest1-0.java", "basic/enum/EnumTest1-1.java");
    }

    @Test
    public void testExtended() {
        testSingle("basic/enum/EnumTest2.java", "basic/enum/EnumTest2-0.java", "basic/enum/EnumTest2-1.java");
    }

    @Test
    public void testMixIn() {
        testMulti(List.of(
                Triple.of("basic/enum/EnumTest3.java", "basic/enum/EnumTest3-0.java", "basic/enum/EnumTest3-1.java"),
                Triple.of("basic/enum/EnumTest3MixIn.java", null, "basic/enum/EnumTest3MixIn-1.java"),
                Triple.of("basic/enum/EnumTest3MixIn2.java", null, "basic/enum/EnumTest3MixIn2-1.java")));
    }

}