package net.binis.codegen.compiler;

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

import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGImport extends CGTree {

    public CGImport(Object instance) {
        super(instance);
    }

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCImport");
    }

    public boolean isStatic() {
        return invoke("isStatic", instance);
    }

    public CGFieldAccess getQualifiedIdentifier() {
        return new CGFieldAccess(getFieldValue(instance, "qualid"));
    }

    @Override
    protected void init() {
        cls = CGImport.theClass();
    }
}
