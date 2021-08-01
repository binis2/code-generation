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

import net.binis.codegen.test.BaseTest;
import net.binis.codegen.generation.core.Helpers;
import org.junit.Before;
import org.junit.Test;

public class EnrichTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void enrichAs() {
        testSingle("enrich/enrichAs.java", "enrich/enrichAs-0.java", "enrich/enrichAs-1.java");
    }

    @Test
    public void enrichCreator() {
        testSingle("enrich/enrichCreator.java", "enrich/enrichCreator-0.java", "enrich/enrichCreator-1.java");
    }

    @Test
    public void enrichCreatorModifier() {
        testSingle("enrich/enrichCreatorModifier.java", "enrich/enrichCreatorModifier-0.java", "enrich/enrichCreatorModifier-1.java");
    }

    @Test
    public void enrichCreatorModifierWithoutModifier() {
        testSingle("enrich/enrichCreatorModifier2.java", "enrich/enrichCreatorModifier2-0.java", "enrich/enrichCreatorModifier2-1.java");
    }

    @Test
    public void enrichWithBaseWithModifier() {
        testSingleWithBase("enrich/enrichBase1.java", "net.binis.codegen.BaseImpl",
                "enrich/enrichBaseTest1.java", "net.binis.codegen.TestImpl",
                "enrich/enrichBase1-0.java", "enrich/enrichBase1-1.java",
                "enrich/enrichBaseTest1-0.java", "enrich/enrichBaseTest1-1.java");
    }

    @Test
    public void enrichCreatorModifierWithMixin() {
        testSingleWithMixIn("enrich/enrichCreatorModifier.java", "net.binis.codegen.TestImpl",
                "enrich/enrichCreatorModifierMixIn.java", "net.binis.codegen.MixInImpl",
                "enrich/enrichCreatorModifierMixIn-0.java", "enrich/enrichCreatorModifierMixIn-1.java",
                "enrich/enrichCreatorModifierMixIn-2.java");
    }

    @Test
    public void enrichClone() {
        testSingle("enrich/enrichClone.java", "enrich/enrichClone-0.java", "enrich/enrichClone-1.java");
    }



}
