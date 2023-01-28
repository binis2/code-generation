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
import net.binis.codegen.test.BaseCodeGenTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class BasicsTest extends BaseCodeGenTest {

    @BeforeEach
    public void cleanUp() {
        Helpers.cleanUp();
    }

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


}
