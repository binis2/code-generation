package net.binis.codegen.compiler;

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

import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Set;

import static net.binis.codegen.tools.Reflection.invoke;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGClassSymbol extends CGSymbol {

    protected Set<Modifier> modifiers;

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Symbol$ClassSymbol");
    }

    public static CGClassSymbol create(Element element) {
        return new CGClassSymbol(element);
    }

    public CGClassSymbol(Object instance) {
        super(instance);
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    public CGName getQualifiedName() {
        return new CGName(invoke("getQualifiedName", instance), true);
    }
}
