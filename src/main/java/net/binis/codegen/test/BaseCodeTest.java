package net.binis.codegen.test;

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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.discoverer.AnnotationDiscoverer;
import net.binis.codegen.discovery.Discoverer;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.javaparser.CodeGenPrettyPrinter;
import net.binis.codegen.objects.Pair;
import net.binis.codegen.tools.Reflection;
import org.junit.jupiter.api.BeforeEach;

import javax.tools.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.CompileHelper.createFileManager;
import static net.binis.codegen.generation.core.CompileHelper.getCompilationUnits;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Tools.ifNull;
import static net.binis.codegen.tools.Tools.with;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public abstract class BaseCodeTest {

    protected JavaParser parser = lookup.getParser();

    protected boolean enablePreview;

    protected Map<String, String> options = new HashMap<>();

    static {
        AnnotationDiscoverer.findAnnotations().stream().filter(Discoverer.DiscoveredService::isTemplate).forEach(a ->
                Structures.registerTemplate(a.getCls()));
    }

    public void enablePreview() {
        enablePreview = true;
    }

    @BeforeEach
    public void beforeEach() {
        Helpers.cleanUp();
    }


    protected String getAsString(CompilationUnit file) {
        var printer = new CodeGenPrettyPrinter();

        sortImports(file);
        if (file.getType(0).isClassOrInterfaceDeclaration()) {
            sortClass(file.getType(0).asClassOrInterfaceDeclaration());
        }

        return printer.print(file);
    }

    protected void cleanUp() {
        Helpers.cleanUp();
    }

    @SneakyThrows
    protected String resourceAsString(String resource) {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
        } catch (Exception e) {
            log.error("Unable to load resource: {}!", resource);
            throw e;
        }
    }

    @SneakyThrows
    protected String silentResourceAsString(String resource) {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
        } catch (Exception e) {
            return null;
        }
    }

    protected void addOption(String key, String value) {
        options.put(key, value);
    }

    protected void removeOption(String key) {
        options.remove(key);
    }

    protected void cleanOptions() {
        options.clear();
    }

    @SuppressWarnings("unchecked")
    protected void load(List<Pair<String, String>> list, String resource) {
        var source = resourceAsString(resource);

        var parse = parser.parse(source);

        if (!parse.isSuccessful()) {
            var name = silentResourceAsString(resource + ".desc");
            if (nonNull(name)) {
                list.add(Pair.of(name, source));
            } else {
                assertTrue(parse.isSuccessful(), parse.toString());
            }
        } else {
            parse.getResult().get().findFirst(TypeDeclaration.class).ifPresent(declaration -> {
                if (nonNull(list)) {
                    declaration.getFullyQualifiedName().ifPresent(name ->
                            list.add(Pair.of((String) name, source)));
                }

                if (declaration.isAnnotationDeclaration()) {
                    Structures.registerTemplate(declaration.asAnnotationDeclaration());
                }

                parse.getResult().ifPresent(u ->
                        u.getTypes().forEach(t ->
                                Helpers.handleType(parser, t, resource, null)));
            });
        }
    }

    protected String loadExecute(List<Pair<String, String>> list, String resource) {
        var source = resourceAsString(resource);
        var className = source.substring(source.indexOf("package") + 8, source.indexOf(';')) + ".Execute";

        if (nonNull(list)) {
            list.add(Pair.of(className, source));
        }

        return className;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    protected boolean compile(TestClassLoader loader, List<Pair<String, String>> files, String resExecute, boolean loadAll, String... args) {
        String className = null;
        if (nonNull(resExecute)) {
            className = loadExecute(files, resExecute);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        var objects = files.stream().collect(Collectors.toMap(Pair::getKey, f -> new JavaByteObject(f.getKey())));

        StandardJavaFileManager standardFileManager =
                compiler.getStandardFileManager(diagnostics, null, null);

        JavaFileManager fileManager = createFileManager(standardFileManager, objects);

        var params = new ArrayList<>(Arrays.asList(args));
        params.add("-Xlint:unchecked");
        options.forEach((k, v) -> params.add("-A" + k + "=" + v));
        if (enablePreview) {
            params.add("--enable-preview");
            params.add("--release");
            params.add(System.getProperty("java.specification.version"));
        }
        JavaCompiler.CompilationTask task = compiler.getTask(null,
                fileManager, diagnostics, params, null, getCompilationUnits(files));

        if (!task.call()) {
            diagnostics.getDiagnostics().forEach(System.out::println);
            fileManager.close();
            return false;
        }
        diagnostics.getDiagnostics().forEach(System.out::println);
        fileManager.close();

        if (loadAll) {
            loadObjects(loader, objects);
        } else {
            files.forEach(f ->
                    with(objects.get(f.getKey()), o ->
                            ifNull(loader.findClass(f.getKey()), () ->
                                    loader.define(f.getKey(), o))));
        }

        if (nonNull(resExecute)) {
            var cls = loader.findClass(className);
            assertNotNull(cls, "Executor class not found!");
            assertNotNull(cls.getSuperclass(), "Executor doesn't inherit TestExecutor!");
            assertEquals(TestExecutor.class, cls.getSuperclass(), "Executor doesn't inherit TestExecutor!");
            defineObjects(loader, objects);
            Reflection.withLoader(loader, () ->
                    assertTrue(TestExecutor.test((Class<? extends TestExecutor>) cls), "Test execution failed!"));
        }

        return true;
    }

    protected void loadObjects(TestClassLoader loader, Map<String, JavaByteObject> objects) {
        var lastFailed = 0;
        var failed = 0;
        do {
            lastFailed = failed;
            failed = 0;
            for (var pair : objects.entrySet()) {
                if (isNull(loader.findClass(pair.getKey()))) {
                    try {
                        loader.define(pair.getKey(), pair.getValue());
                    } catch (NoClassDefFoundError e) {
                        failed++;
                    }
                }
            }
        } while (failed > 0 || failed != lastFailed);

    }

    protected void defineObjects(TestClassLoader loader, Map<String, JavaByteObject> objects) {
        defineObjects(loader, objects, objects.size());
    }

    protected void defineObjects(TestClassLoader loader, Map<String, JavaByteObject> objects, int tries) {
        var error = false;

        for (var entry : objects.entrySet()) {
            if (isNull(loader.findClass(entry.getKey()))) {
                try {
                    loader.define(entry.getKey(), entry.getValue());
                } catch (NoClassDefFoundError ex) {
                    error = true;
                }
            }
        }

        if (error && tries > 0) {
            defineObjects(loader, objects, tries - 1);
        }
    }

    protected List<Pair<String, String>> newList() {
        return new ArrayList<>();
    }

}
