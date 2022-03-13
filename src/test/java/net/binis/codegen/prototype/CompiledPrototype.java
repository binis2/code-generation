package net.binis.codegen.prototype;

/*-
 * #%L
 * code-generation-test
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

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.spring.annotation.QueryFragment;

@CodePrototype(generateImplementation = false)
public interface CompiledPrototype {

    boolean test();

    default boolean isTestable() {
        return test();
    }

    @QueryFragment
    default void queryPreset() {
        //Do nothing
    }

    @QueryFragment
    default String queryPreset(CompiledPrototype parent) {
        return "test(parent.isTest())";
    }


}
