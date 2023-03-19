package net.binis.codegen;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2023 Binis Belev
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
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.objects.CodeExampleBuilder;
import net.binis.codegen.test.BaseCodeGenTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class CustomPrototypeTest extends BaseCodeGenTest {

    @Test
    void test() {
        testMulti(List.of(
                Triple.of("custom/custom.java", null, null),
                Triple.of("custom/custom1.java", "custom/custom1-0.java", "custom/custom1-1.java")));
    }

    @Test
    void testInherited() {
        testMultiImplementation(List.of(
                Triple.of("custom/customInherited.java", null, null),
                Triple.of("custom/customInherited1.java", "custom/customInherited1-0.java", null)));
    }

    @Test
    void testInheritedCompiled() {
        Structures.registerTemplate(CodeExampleBuilder.class);
        testSingleImplementation("custom/customInherited2.java", "custom/customInherited2-0.java");
    }

}
