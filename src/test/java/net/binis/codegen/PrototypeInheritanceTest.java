package net.binis.codegen;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2022 Binis Belev
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

class PrototypeInheritanceTest extends BaseCodeGenTest {

    @Test
    void test() {
        testMulti(List.of(
                Triple.of("inheritance/InheritTest1.java", "inheritance/InheritTest1-0.java", "inheritance/InheritTest1-1.java"),
                Triple.of("inheritance/InheritTestProto1.java", null, "inheritance/InheritTestProto1-1.java"),
                Triple.of("inheritance/InheritTestProto2.java", null, "inheritance/InheritTestProto2-1.java"),
                Triple.of("inheritance/InheritTest2.java", "inheritance/InheritTest2-0.java", "inheritance/InheritTest2-1.java")
                ));
    }

}
