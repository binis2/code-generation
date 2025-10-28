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

class ToStringEnricherTest extends BaseCodeGenTest {

    @Test
    void test() {
        testSingleExecute("enrich/enrichToString.java", "enrich/enrichToString-0.java", "enrich/enrichToString-1.java", "enrich/enrichToString-2.java");
    }

    @Test
    void testWithBase() {
        testMultiExecute(List.of(
                Triple.of("enrich/enrichToStringBase.java", "enrich/enrichToStringBase-0.java", "enrich/enrichToStringBase-1.java"),
                Triple.of("enrich/enrichToString2.java", "enrich/enrichToString2-0.java", "enrich/enrichToString2-1.java")),
                "enrich/enrichToString2-2.java");
    }

    @Test
    void testEnum() {
        testSingleExecute("enrich/enrichToString3.java", "enrich/enrichToString3-0.java", "enrich/enrichToString3-1.java", "enrich/enrichToString3-2.java");
    }

    @Test
    void testIncludesExcludes() {
        testMultiExecute(List.of(
                        Triple.of("enrich/enrichToStringBase2.java", "enrich/enrichToStringBase2-0.java", "enrich/enrichToStringBase2-1.java"),
                        Triple.of("enrich/enrichToString4.java", "enrich/enrichToString4-0.java", "enrich/enrichToString4-1.java")),
                "enrich/enrichToString4-2.java");
    }

    @Test
    void testJustBaseFields() {
        testMultiExecute(List.of(
                        Triple.of("enrich/enrichToStringBase2.java", "enrich/enrichToStringBase2-0.java", "enrich/enrichToStringBase2-1.java"),
                        Triple.of("enrich/enrichToString5.java", "enrich/enrichToString5-0.java", "enrich/enrichToString5-1.java")),
                "enrich/enrichToString5-2.java");
    }

    @Test
    void testWithoutBaseFields() {
        testMultiExecute(List.of(
                        Triple.of("enrich/enrichToStringBase3.java", "enrich/enrichToStringBase3-0.java", "enrich/enrichToStringBase3-1.java"),
                        Triple.of("enrich/enrichToString6.java", "enrich/enrichToString6-0.java", "enrich/enrichToString6-1.java")),
                "enrich/enrichToString6-2.java");
    }

    @Test
    void testAnnotatedInterface() {
        testSingleExecute("enrich/enrichToString7.java", "enrich/enrichToString7-0.java", "enrich/enrichToString7-1.java", "enrich/enrichToString7-2.java");
    }

    @Test
    void testWithCustomToStringDeclared() {
        testSingleExecute("enrich/enrichToString8.java", "enrich/enrichToString8-0.java", "enrich/enrichToString8-1.java", "enrich/enrichToString8-2.java");
    }

}
