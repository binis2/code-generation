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
import net.binis.codegen.test.BaseCodeGenTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.binis.codegen.generation.core.Helpers.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoneStrategyBasicTest extends BaseCodeGenTest {

    @BeforeEach
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    void plain() {
        testSingleImplementation("strategy/none1.java", null);
    }

    @Test
    void plainWithEnricher() {
        testSingleImplementation("strategy/none2.java", null);
        assertEquals(1, lookup.custom().size());
        var custom = lookup.custom().stream().findFirst().get();
        compare(custom.getIntf().findCompilationUnit().get(), "strategy/none2.java");
    }


}
