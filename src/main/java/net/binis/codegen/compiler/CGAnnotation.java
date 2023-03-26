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

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGAnnotation extends CGExpression {

    protected CGList<CGExpression> arguments;
    protected CGTree annotationType;

    public CGAnnotation(Object instance) {
        super(instance);
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCAnnotation");
    }

    public CGList<CGExpression> getArguments() {
        if (isNull(arguments)) {
            arguments = new CGList<>(invoke("getArguments", instance), this::onModify);
        }
        return arguments;
    }

    public CGTree getAnnotationType() {
        if (isNull(annotationType)) {
            annotationType = new CGTree(invoke("getAnnotationType", instance));
        }
        return annotationType;
    }

    public void setArguments(CGList<CGExpression> list) {
        onModify(list);
    }

    protected void onModify(CGList<CGExpression> list) {
        setFieldValue(instance, "args", list.getInstance());
        arguments = list;
    }


}
