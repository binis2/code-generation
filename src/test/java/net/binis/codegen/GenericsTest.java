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
import net.binis.codegen.test.BaseCodeGenTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.binis.codegen.generation.core.Helpers.lookup;

@Slf4j
class GenericsTest extends BaseCodeGenTest {

    @Test
    void testGenerics() {
        testMulti(List.of(
                Triple.of("generics/Extended1.java", "generics/Extended1-0.java", "generics/Extended1-1.java"),
                Triple.of("generics/Test1.java", "generics/Test1-0.java", "generics/Test1-1.java")
        ));
    }

    @Test
    void testGenericsWithBase() {
        testMulti(List.of(
                Triple.of("generics/Extended2.java", "generics/Extended1-0.java", "generics/Extended1-1.java"),
                Triple.of("generics/Test2.java", "generics/Test2-0.java", "generics/Test2-1.java")
        ));
    }

    @Test
    void testGenericsWithCompiledBase() {
        testMulti(List.of(
                Triple.of("generics/Prototype4.java", "generics/Prototype4-0.java", "generics/Prototype4-1.java"),
                Triple.of("generics/Test4.java", "generics/Test4-0.java", "generics/Test4-1.java")
        ), 3);
    }


    @Test
    void testGenericsWithBaseAndPrototype() {
        testMulti(List.of(
                Triple.of("generics/Extended2.java", "generics/Extended1-0.java", "generics/Extended1-1.java"),
                Triple.of("generics/Prototype3.java", "generics/Prototype3-0.java", "generics/Prototype3-1.java"),
                Triple.of("generics/Test3.java", "generics/Test3-0.java", "generics/Test3-1.java")
        ));
    }

    @Test
    void testGenericsWithCompiledBaseAndNestedPrototype() {
        testSingleExecute("generics/Test5.java", "generics/Test5-0.java", "generics/Test5-1.java", 3, "generics/Test5-2.java");
    }

    @Test
    void testGenericsWithEnum() {
        testMulti(List.of(
                Triple.of("generics/Prototype5.java", "generics/Prototype5-0.java", "generics/Prototype5-1.java"),
                Triple.of("generics/Prototype6.java", "generics/Prototype6-0.java", "generics/Prototype6-1.java")
        ));
    }

    @Test
    void testGenericsWithCompiledInheritedInterface() {
        testSingle("generics/Prototype7.java", "generics/Prototype7-0.java", "generics/Prototype7-1.java");
    }


    @Test
    void testGenericsWithCompiledEnum() {
        testSingle("generics/Prototype8.java", "generics/Prototype8-0.java", "generics/Prototype8-1.java", 2);
    }

    @Test
    void testGenericsWithInheritedInterfaceThatInheritsCompiledInterface() {
        lookup.registerExternalLookup(s -> {
            if ("net.binis.codegen.types.TestType".equals(s)) {
                return """
                        package net.binis.codegen.types;
                                            
                        import net.binis.codegen.objects.Typeable;
                                            
                        public interface TestType extends Typeable<TestType> {
                                            
                        }
                        """;
            }
            return null;
        });
        testSingleSkip("generics/Prototype9.java", "generics/Prototype9-0.java", "generics/Prototype9-1.java", true, true);
    }


}
