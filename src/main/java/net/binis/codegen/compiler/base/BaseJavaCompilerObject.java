package net.binis.codegen.compiler.base;

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
import net.binis.codegen.factory.CodeFactory;

import javax.annotation.processing.ProcessingEnvironment;

import static java.util.Objects.isNull;
import static net.binis.codegen.generation.core.Helpers.lookup;
import static net.binis.codegen.tools.Reflection.invoke;

@Slf4j
public abstract class BaseJavaCompilerObject {

    protected final ProcessingEnvironment env;
    protected Object instance;
    protected Object context;
    protected Class cls;

    protected BaseJavaCompilerObject() {
        this.env = CodeFactory.create(ProcessingEnvironment.class, lookup.getProcessingEnvironment());
        context = invoke("getContext", env);
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


}
