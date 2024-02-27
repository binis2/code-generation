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

import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.test.BaseCodeGenTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class WithBaseTest extends BaseCodeGenTest {

    @BeforeEach
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    void base() {
        testSingleWithBase("base/base1.java", "net.binis.codegen.BaseImpl",
                "base/baseTest1.java", "net.binis.codegen.TestImpl",
                "base/base1-0.java", "base/base1-1.java",
                "base/baseTest1-0.java", "base/baseTest1-1.java");
    }

    @Test
    void baseWithModifier() {
        testSingleWithBase("base/base1.java", "net.binis.codegen.BaseImpl",
                "base/baseTest2.java", "net.binis.codegen.TestImpl",
                "base/base1-0.java", "base/base1-1.java",
                "base/baseTest2-0.java", "base/baseTest2-1.java");
    }

    @Test
    void baseWithInheritedModifier() {
        testSingleWithBase("base/base2.java", "net.binis.codegen.BaseImpl",
                "base/baseTest3.java", "net.binis.codegen.TestImpl",
                "base/base2-0.java", "base/base2-1.java",
                "base/baseTest3-0.java", "base/baseTest3-1.java");
    }

    @Test
    void withCompiledBase() {
        testSingle("base/base3.java", "base/base3-0.java", "base/base3-1.java", 2);
    }

    @Test
    void withBaseCompiledGeneric() {
        testMulti(List.of(
                Triple.of("base/baseTestEnum4.java", "base/baseTestEnum4-0.java", "base/baseTestEnum4-1.java"),
                Triple.of("base/base4.java", "base/base4-0.java", "base/base4-1.java"),
                Triple.of("base/baseTest4.java", "base/baseTest4-0.java", "base/baseTest4-1.java")));
    }


}
