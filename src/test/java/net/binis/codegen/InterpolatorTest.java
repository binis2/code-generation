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

import net.binis.codegen.tools.Interpolator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterpolatorTest {

    @Test
    void test() {
        var result = Interpolator.build("pre{test}suf").with(param -> "mid").interpolate();
        assertEquals("premidsuf", result);
    }

    @Test
    void testMap() {
        var result = Interpolator.build("pre{test}{par}suf").params(Map.of("test", "mid", "par", "rap")).interpolate();
        assertEquals("premidrapsuf", result);
    }

    @Test
    void testIdentifier() {
        var result = Interpolator.build('$', "pre${test}{par}suf").params(Map.of("test", "mid", "par", "rap")).interpolate();
        assertEquals("premid{par}suf", result);
    }


}
