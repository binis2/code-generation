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

import static net.binis.codegen.tools.Reflection.invoke;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGAssign extends CGExpression {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCAssign");
    }

    public CGAssign(Object instance) {
        super(instance);
    }

    public CGExpression getVariable() {
        return new CGExpression(invoke("getVariable", instance));
    }

    public CGExpression getExpression() {
        return new CGExpression(invoke("getExpression", instance));
    }

    @Override
    protected void init() {
        cls = theClass();
    }
}
