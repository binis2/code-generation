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

import net.binis.codegen.compiler.base.JavaCompilerObject;

import static net.binis.codegen.tools.Reflection.*;

public class CGName extends JavaCompilerObject {

    public static CGName create(String name) {
        return new CGName(name);
    }

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.util.Name");
    }

    public CGName(String name) {
        super();
        instance = invoke("fromString", instance, name);
    }

    public CGName(Object instance, boolean dummy) {
        super();
        this.instance = instance;
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.util.Names");
        instance = invokeStatic("instance", cls, context);
    }
}
