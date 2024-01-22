package net.binis.codegen.test.prototype;

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

import lombok.Getter;
import net.binis.codegen.annotation.EnumPrototype;

@EnumPrototype
public enum CompiledEnumPrototype {
    UNKNOWN("unknown"),
    KNOWN("known");

    @Getter
    private final String value;

    CompiledEnumPrototype(String value) {
        this.value = value;
    }

}
