package net.binis.codegen.compiler.plugin;

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

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.tools.Reflection;

import static java.util.Objects.nonNull;

@Slf4j
public class CodeGenCompilerPluginBootstrap implements Plugin {
    private Plugin _delegate;

    public CodeGenCompilerPluginBootstrap() {
        log.info("Binis CodeGen plugin started...");
        try {
            var util = Reflection.loadClass("net.binis.codegen.utils.CodeGenAnnotationProcessorUtils");
            var method = Reflection.findMethod("addOpensForCodeGen", util, boolean.class);
            Reflection.invokeStatic(method, true);

            var cls = Reflection.loadClass("net.binis.codegen.compiler.plugin.CodeGenCompilerPlugin");
            _delegate = (Plugin) Reflection.instantiate(cls);
        } catch (Exception e) {
            _delegate = null;
        }
    }

    @Override
    public String getName() {
        return "BinisCodeGen";
    }

    @Override
    public void init(JavacTask task, String... args) {
        if (nonNull(_delegate)) {
            _delegate.init(task, args);
        }
    }
}
