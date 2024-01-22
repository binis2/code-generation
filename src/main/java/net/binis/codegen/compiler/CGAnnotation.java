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

import java.lang.annotation.Annotation;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGAnnotation extends CGExpression {

    protected CGList<CGExpression> arguments;
    protected CGIdent annotationType;

    public CGAnnotation(Object instance) {
        super(instance);
    }

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.JCTree$JCAnnotation");
    }

    public CGList<CGExpression> getArguments() {
        if (isNull(arguments)) {
            arguments = new CGList<>(invoke("getArguments", instance), this::onModify, CGExpression.class);
        }
        return arguments;
    }

    public boolean hasArgument(String argument) {
        for (var attr : getArguments()) {
            if (attr.getInstance().getClass().equals(CGAssign.theClass())) {
                var assign = new CGAssign(attr.getInstance());
                if (assign.getVariable().getInstance().toString().equals(argument)) {
                    return true;
                }
            }
        }
        return false;
    }

    public CGValueExpression getArgument(String argument) {
        for (var attr : getArguments()) {
            if (attr.getInstance().getClass().equals(CGAssign.theClass())) {
                var assign = new CGAssign(attr.getInstance());
                if (assign.getVariable().getInstance().toString().equals(argument)) {
                    return new CGValueExpression(attr.getInstance());
                }
            }
        }
        return null;
    }


    public CGIdent getAnnotationType() {
        if (isNull(annotationType)) {
            annotationType = new CGIdent(invoke("getAnnotationType", instance));
        }
        return annotationType;
    }

    public void setArguments(CGList<CGExpression> list) {
        onModify(list);
        list.onModify = this::onModify;
    }

    public boolean isAnnotation(Class<? extends Annotation> cls) {
        return getAnnotationType().getType().toString().equals(cls.getCanonicalName());
    }

    protected void onModify(CGList<CGExpression> list) {
        setFieldValue(instance, "args", list.getInstance());
        arguments = list;
    }


}
