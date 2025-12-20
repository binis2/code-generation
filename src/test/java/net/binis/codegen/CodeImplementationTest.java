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

import net.binis.codegen.test.BaseCodeGenTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;

class CodeImplementationTest extends BaseCodeGenTest {

    @Test
    void testChangeGetterType() {
        testSingle("basic/CodeImplementation1.java", "basic/CodeImplementation1-0.java", "basic/CodeImplementation1-1.java");
    }

    @Test
    void testChangeImplementation() {
        testSingle("basic/CodeImplementation2.java", "basic/CodeImplementation2-0.java", "basic/CodeImplementation2-1.java");
    }

    @Test
    void testChangeImplementationNormalExpr() {
        testSingle("basic/CodeImplementation3.java", "basic/CodeImplementation2-0.java", "basic/CodeImplementation2-1.java");
    }

    @Test
    void testReferencePrototypes() {
        testMulti(List.of(
                Triple.of("basic/Test1.java", "basic/Test1-0.java", "basic/Test1-1.java"),
                Triple.of("basic/CodeImplementation4.java", "basic/CodeImplementation4-0.java", "basic/CodeImplementation4-1.java")));
    }

    @Test
    void testTextBlock() {
        testSingle("basic/CodeImplementation5.java", "basic/CodeImplementation5-0.java", "basic/CodeImplementation5-1.java");
    }




}
