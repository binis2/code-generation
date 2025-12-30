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
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class AnnotationsTest extends BaseCodeGenTest {

    @Test
    void test() {
        testSingle("annotation/default1.java", "annotation/default1-0.java", "annotation/default1-1.java");
    }

    @Test
    void testFor() {
        testSingle("annotation/testFor1.java", "annotation/testFor1-0.java", "annotation/testFor1-1.java");
    }

    @Test
    void testForDefaultMethod() {
        testSingle("annotation/testForDefault1.java", null, "annotation/testForDefault1-1.java");
    }

    @Test
    void testInheritedFieldAnnotation() {
        testMulti(List.of(
                Triple.of("annotation/testInherit1.java", null, "annotation/testInherit1-1.java"),
                Triple.of("annotation/testField1.java", "annotation/testField1-0.java", "annotation/testField1-1.java")));
    }


}
