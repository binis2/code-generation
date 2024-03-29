package net.binis.codegen.compiler.base;

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
import net.binis.codegen.compiler.CGName;
import net.binis.codegen.compiler.CGType;
import net.binis.codegen.compiler.JavacElements;
import net.binis.codegen.factory.CodeFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Helpers.lookup;
import static net.binis.codegen.tools.Reflection.*;
import static net.binis.codegen.tools.Tools.withRes;

@Slf4j
public abstract class JavaCompilerObject {

    protected final ProcessingEnvironment env;
    protected Object instance;
    protected Object context;
    protected Class cls;

    protected JavaCompilerObject() {
        this.env = CodeFactory.create(ProcessingEnvironment.class, lookup.getProcessingEnvironment());
        context = (nonNull(env)) ? invoke("getContext", env) : CodeFactory.create(loadClass("com.sun.tools.javac.util.Context"));
        if (isNull(context)) {
            log.error("Unable to get context from {}!", env.getClass());
        }
        init();
    }

    protected abstract void init();

    public Class getCls() {
        return cls;
    }

    public Object getInstance() {
        return instance;
    }

    public Object getContext() {
        return context;
    }

    public CGType getType() {
        return withRes(getFieldValue(instance, "type"), CGType::new);
    }

    public int getPos() {
        return getFieldValue(instance, "pos");
    }

    public void setPos(int pos) {
        setFieldValue(instance, "pos", pos);
    }

    public CGName toName(String name) {
        return JavacElements.create().getName(name);
    }

    public boolean is(Class cls) {
        if (JavaCompilerObject.class.isAssignableFrom(cls)) {
            return this.getClass().equals(cls);
        } else {
            return instance.getClass().equals(cls);
        }
    }

    @Override
    public String toString() {
        return instance.toString();
    }

}
