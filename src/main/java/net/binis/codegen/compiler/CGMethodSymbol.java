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

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.invoke;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CGMethodSymbol extends CGSymbol {

    protected Set<Modifier> modifiers;

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Symbol$MethodSymbol");
    }

    public static CGMethodSymbol create(Element element) {
        return new CGMethodSymbol(element);
    }

    public CGMethodSymbol(Object instance) {
        super(instance);
    }

    public CGList<CGVarSymbol> params() {
        return new CGList<>(invoke("params", instance), null, CGVarSymbol.class);
    }

    @SuppressWarnings("unchecked")
    public Set<Modifier> getModifiers() {
        if (isNull(modifiers)) {
            modifiers = (Set) invoke("getModifiers", instance);
        }
        return modifiers;
    }

    public CGType getReturnType() {
        var type = invoke("getReturnType", instance);
        return nonNull(type) ? new CGType(type) : null;
    }

    public boolean isPublic() {
        return getModifiers().contains(Modifier.PUBLIC);
    }

    public boolean isStatic() {
        return getModifiers().contains(Modifier.STATIC);
    }

    public boolean isPrivate() {
        return getModifiers().contains(Modifier.PRIVATE);
    }

    public boolean isProtected() {
        return getModifiers().contains(Modifier.PROTECTED);
    }

    public boolean isAbstract() {
        return getModifiers().contains(Modifier.ABSTRACT);
    }

    public boolean isFinal() {
        return getModifiers().contains(Modifier.FINAL);
    }

    public boolean isSynchronized() {
        return getModifiers().contains(Modifier.SYNCHRONIZED);
    }

    public boolean isNative() {
        return getModifiers().contains(Modifier.NATIVE);
    }

    public boolean isStrict() {
        return getModifiers().contains(Modifier.STRICTFP);
    }

    @Override
    protected void init() {
        cls = theClass();
    }

}
