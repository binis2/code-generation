package net.binis.codegen.generation.core;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2022 Binis Belev
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

import lombok.SneakyThrows;
import net.binis.codegen.objects.Pair;
import net.binis.codegen.test.JavaByteObject;
import net.binis.codegen.test.JavaSourceObject;
import net.binis.codegen.test.TestClassLoader;

import javax.tools.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.binis.codegen.tools.Tools.with;

public abstract class CompileHelper {

    @SneakyThrows
    public static boolean compile(TestClassLoader loader, List<Pair<String, String>> files) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var objects = files.stream().collect(Collectors.toMap(Pair::getKey, f -> new JavaByteObject(f.getKey())));
        var standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        var fileManager = createFileManager(standardFileManager, objects);
        var task = compiler.getTask(null, fileManager, diagnostics, null, null, getCompilationUnits(files));

        if (!task.call()) {
            diagnostics.getDiagnostics().forEach(System.out::println);
            fileManager.close();
            return false;
        }
        fileManager.close();

        files.forEach(f ->
                with(objects.get(f.getKey()), o ->
                        loader.define(f.getKey(), o)));

        return true;
    }

    public static JavaFileManager createFileManager(StandardJavaFileManager fileManager, Map<String, JavaByteObject> files) {
        return new ForwardingJavaFileManager<>(fileManager) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                var pos = className.indexOf('$');
                if (pos > -1) {
                    files.put(className, new JavaByteObject(className));
                }
                return files.get(className);
            }
        };
    }

    public static Iterable<? extends JavaFileObject> getCompilationUnits(List<Pair<String, String>> files) {
        return files.stream().map(f -> new JavaSourceObject(f.getKey(), f.getValue())).toList();
    }

}
