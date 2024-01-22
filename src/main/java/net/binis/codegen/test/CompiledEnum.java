package net.binis.codegen.test;

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

import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.objects.base.enumeration.CodeEnum;

public interface CompiledEnum extends CodeEnum {
    CompiledEnum UNKNOWN = CodeFactory.initializeEnumValue(CompiledEnum.class, "unknown", 0, "unknown");
    CompiledEnum KNOWN = CodeFactory.initializeEnumValue(CompiledEnum.class, "known", 0, "known");
}
