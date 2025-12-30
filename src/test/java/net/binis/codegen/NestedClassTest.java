package net.binis.codegen;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2026 Binis Belev
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
import org.junit.jupiter.api.Test;

import static net.binis.codegen.generation.core.Helpers.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class NestedClassTest extends BaseCodeGenTest {

    @Test
    void test() {
        testSingle("nested/Test1.java", "nested/Test1-0.java", "nested/Test1-1.java", 2);
    }

    @Test
    void testNestedPrototype() {
        testSingle("nested/Test2.java", "nested/Test2-0.java", "nested/Test2-1.java", 2);
    }

    @Test
    void testNestedImplementation() {
        testSingleImplementation("nested/Test3.java", "nested/Test3-0.java", 2);
    }

    @Test
    void testNestedPrototypeWithReference() {
        testSingle("nested/Test4.java", "nested/Test4-0.java", "nested/Test4-1.java", 3);
    }

    @Test
    void testNestedPrototypeInPrototype() {
        testSingle("nested/Test5.java", null, null, 5, false);
        assertEquals("code.test", lookup.findGenerated("code.test.PriceResponse").getInterfaceUnit().getPackageDeclaration().get().getNameAsString());
        assertEquals("code.test", lookup.findGenerated("code.test.PriceResponseImpl").getImplementationUnit().getPackageDeclaration().get().getNameAsString());
    }

    @Test
    void testNestedEnumInPrototype() {
        testSingle("nested/Test6.java", null, null, 2, true);
    }

    @Test
    void testNestedEnum() {
        testSingle("nested/Test7.java", "nested/Test7-0.java", "nested/Test7-1.java", 2);
    }

}
