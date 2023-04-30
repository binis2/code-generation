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
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGModifiers extends BaseJavaCompilerObject {

    protected final BaseJavaCompilerObject declaration;
    protected CGList<CGAnnotation> annotations;

    public CGModifiers(BaseJavaCompilerObject declaration) {
        super();
        this.declaration = declaration;
        instance = invoke("getModifiers", declaration.getInstance());
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCModifiers");
    }

    public CGList<CGAnnotation> getAnnotations() {
        if (isNull(annotations)) {
            annotations = new CGList<>(invoke("getAnnotations", instance), this::onModify, CGAnnotation.class);
        }
        return annotations;
    }

    private void onModify(CGList<CGAnnotation> list) {
        setFieldValue(instance, "annotations", list.getInstance());
        annotations = list;
    }

    public void setAnnotations(CGList<CGAnnotation> list) {
        onModify(list);
    }
}
