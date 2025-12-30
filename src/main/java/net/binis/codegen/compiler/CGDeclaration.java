package net.binis.codegen.compiler;

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

import com.sun.source.util.Trees;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.JavaCompilerObject;

import javax.lang.model.element.Element;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.getFieldValue;
import static net.binis.codegen.tools.Reflection.setFieldValue;

@Slf4j
public abstract class CGDeclaration extends JavaCompilerObject {

    protected CGModifiers modifiers;

    @SuppressWarnings("unchecked")
    protected CGDeclaration(Trees trees, Element element) {
        super();
        instance = trees.getTree(element);
        if (!cls.isAssignableFrom(instance.getClass())) {
            log.error("Unable to get class declaration!");
        }
    }

    @SuppressWarnings("unchecked")
    protected CGDeclaration(Object instance) {
        super();
        this.instance = instance;
        if (!cls.isAssignableFrom(instance.getClass())) {
            log.error("Unable to get class declaration!");
        }
    }

    public CGModifiers getModifiers() {
        if (isNull(modifiers)) {
            modifiers = new CGModifiers(this);
        }
        return modifiers;
    }

    public CGList<JavaCompilerObject> getDefs() {
        return new CGList<>(getFieldValue(instance, "defs"), this::onDefsModify, JavaCompilerObject.class);
    }

    public CGName getName() {
        return new CGName(getFieldValue(instance, "name"), true);
    }

    protected <T extends JavaCompilerObject> void onDefsModify(CGList<T> list) {
        setFieldValue(instance, "defs", list.getInstance());
    }

    public CGSymbol getSymbol() {
        return new CGSymbol(getFieldValue(instance, "sym"));
    }

    public boolean isFinal() {
        return (getModifiers().flags() & CGFlags.FINAL) == CGFlags.FINAL;
    }

    public boolean isPublic() {
        return (getModifiers().flags() & CGFlags.PUBLIC) == CGFlags.PUBLIC;
    }

    public boolean isPrivate() {
        return (getModifiers().flags() & CGFlags.PRIVATE) == CGFlags.PRIVATE;
    }

    public boolean isProtected() {
        return (getModifiers().flags() & CGFlags.PROTECTED) == CGFlags.PROTECTED;
    }

    public boolean isStatic() {
        return (getModifiers().flags() & CGFlags.STATIC) == CGFlags.STATIC;
    }


}
