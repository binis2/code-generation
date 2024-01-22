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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.test.BaseCodeGenTest;
import org.junit.jupiter.api.Test;

@Slf4j
class CodeMappingTest extends BaseCodeGenTest {

    @Test
    void test() {
        testSingle("mapping/Test1.java", "mapping/Test1-0.java", "mapping/Test1-1.java");
    }

    @Test
    void testIgnore() {
        testSingle("mapping/Test2.java", "mapping/Test2-0.java", "mapping/Test2-1.java");
    }


}
