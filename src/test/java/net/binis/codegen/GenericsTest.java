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
public class GenericsTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void testGenerics() {
        testMulti(List.of(
                Triple.of("generics/Extended1.java", "generics/Extended1-0.java", "generics/Extended1-1.java"),
                Triple.of("generics/Test1.java", "generics/Test1-0.java", "generics/Test1-1.java")
        ));
    }

    @Test
    public void testGenericsWithBase() {
        testMulti(List.of(
                Triple.of("generics/Extended2.java", "generics/Extended1-0.java", "generics/Extended1-1.java"),
                Triple.of("generics/Test2.java", "generics/Test2-0.java", "generics/Test2-1.java")
        ));
    }

    @Test
    public void testGenericsWithCompiledBase() {
        testMulti(List.of(
                Triple.of("generics/Prototype4.java", "generics/Prototype4-0.java", "generics/Prototype4-1.java"),
                Triple.of("generics/Test4.java", "generics/Test4-0.java", "generics/Test4-1.java")
        ), 3);
    }


    @Test
    public void testGenericsWithBaseAndPrototype() {
        testMulti(List.of(
                Triple.of("generics/Extended2.java", "generics/Extended1-0.java", "generics/Extended1-1.java"),
                Triple.of("generics/Prototype3.java", "generics/Prototype3-0.java", "generics/Prototype3-1.java"),
                Triple.of("generics/Test3.java", "generics/Test3-0.java", "generics/Test3-1.java")
        ));
    }

    @Test
    public void testGenericsWithCompiledBaseAndNestedPrototype() {
        testSingle("generics/Test5.java", "generics/Test5-0.java", "generics/Test5-1.java", 3);
    }


}
