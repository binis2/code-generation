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
import net.binis.codegen.compiler.base.JavaCompilerObject;

import javax.lang.model.element.Modifier;
import java.util.Set;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGModifiers extends CGTree {

    protected final JavaCompilerObject declaration;
    protected CGList<CGAnnotation> annotations;

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCModifiers");
    }

    public CGModifiers(JavaCompilerObject declaration) {
        super(null);
        this.declaration = declaration;
        instance = invoke("getModifiers", declaration.getInstance());
    }

    public CGModifiers(Object instance, boolean dummy) {
        super(instance);
        declaration = null;
    }

    @SuppressWarnings("unchecked")
    public Set<Modifier> getFlags() {
        return (Set) invoke("getFlags", instance);
    }

    @SuppressWarnings("unchecked")
    public long flags() {
        return getFieldValue(instance, "flags");
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    public CGList<CGAnnotation> getAnnotations() {
        if (isNull(annotations)) {
            annotations = new CGList<>(invoke("getAnnotations", instance), this::onModify, CGAnnotation.class);
        }
        return annotations;
    }

    protected void onModify(CGList<CGAnnotation> list) {
        setFieldValue(instance, "annotations", list.getInstance());
        annotations = list;
    }

    public void setAnnotations(CGList<CGAnnotation> list) {
        onModify(list);
        list.onModify = this::onModify;
    }

}
