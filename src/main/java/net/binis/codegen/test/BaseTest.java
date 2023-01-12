package net.binis.codegen.test;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
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
import net.binis.codegen.CodeGen;
import net.binis.codegen.discoverer.AnnotationDiscoverer;
import net.binis.codegen.discovery.Discoverer;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.javaparser.CodeGenPrettyPrinter;
import net.binis.codegen.objects.Pair;
import net.binis.codegen.tools.Reflection;
import org.apache.commons.lang3.tuple.Triple;

import javax.tools.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
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
public abstract class BaseTest {

    protected JavaParser parser = new JavaParser();

    static {
        AnnotationDiscoverer.findAnnotations().stream().filter(Discoverer.DiscoveredService::isTemplate).forEach(a ->
                Structures.registerTemplate(a.getCls()));
    }

    protected String getAsString(CompilationUnit file) {
        var printer = new CodeGenPrettyPrinter();

        sortImports(file);
        if (file.getType(0).isClassOrInterfaceDeclaration()) {
            sortClass(file.getType(0).asClassOrInterfaceDeclaration());
        }

        return printer.print(file);
    }

    protected void generate() {
        var parsed = new ArrayList<>(lookup.parsed());
        for (var entry : parsed) {
            ifNull(entry.getFiles(), () ->
                    Generator.generateCodeForClass(entry.getDeclaration().findCompilationUnit().get(), entry));
        }

        lookup.calcPrototypeMaps();

        with(lookup.parsed().stream().filter(PrototypeDescription::isValid).sorted(Helpers::sortForEnrich).toList(), list -> {
            list.forEach(Helpers::handleEnrichers);
            list.forEach(Helpers::finalizeEnrichers);
            list.forEach(Helpers::postProcessEnrichers);
        });
    }

    protected void cleanUp() {
        Helpers.cleanUp();
    }

    @SuppressWarnings("unchecked")
    protected void load(List<Pair<String, String>> list, String resource) {
        var source = resourceAsString(resource);

        var parse = parser.parse(source);
        assertTrue(parse.isSuccessful(), parse.toString());

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
                            CodeGen.handleType(parser, t, resource)));
        });
    }

    protected String loadExecute(List<Pair<String, String>> list, String resource) {
        var source = resourceAsString(resource);
        var className = source.substring(source.indexOf("package") + 8, source.indexOf(';')) + ".Execute";

        if (nonNull(list)) {
            list.add(Pair.of(className, source));
        }

        return className;
    }

    protected void compare(CompilationUnit unit, String resource) {
        if (nonNull(resource)) {
            assertEquals(resourceAsString(resource), getAsString(unit), resource);
        }
    }

    protected void testSingleExecute(String prototype, String resClass, String resInterface, String resExecute) {
        testSingleExecute(prototype, resClass, resInterface, null, 1, resExecute, false);
    }

    protected void testSingleExecute(String prototype, String resClass, String resInterface, int expected, String resExecute) {
        testSingleExecute(prototype, resClass, resInterface, null, expected, resExecute, false);
    }


    protected void testSingle(String prototype, String resClass, String resInterface) {
        testSingle(prototype, resClass, resInterface, null, 1);
    }

    protected void testSingle(String prototype, String resClass, String resInterface, String pathToSave) {
        testSingle(prototype, resClass, resInterface, pathToSave, 1);
    }

    protected void testSingle(String prototype, String resClass, String resInterface, int expected) {
        testSingle(prototype, resClass, resInterface, null, expected);
    }

    protected void testSingle(String prototype, String resClass, String resInterface, int expected, boolean skipCompilation) {
        testSingleExecute(prototype, resClass, resInterface, null, expected, null, skipCompilation);
    }


    protected void testSingle(String prototype, String resClass, String resInterface, String pathToSave, int expected) {
        testSingleExecute(prototype, resClass, resInterface, pathToSave, expected, null, false);
    }

    protected void testSingleExecute(String prototype, String resClass, String resInterface, String pathToSave, int expected, String resExecute, boolean skipCompilation) {
        var list = newList();
        load(list, prototype);
        assertTrue(compile(new TestClassLoader(), list, null));
        generate();

        assertEquals(expected, lookup.parsed().size());

        var list2 = newList();
        lookup.generated().stream().sorted((o1, o2) -> Boolean.compare(isNull(o1.getCompiled()), isNull(o2.getCompiled()))).forEach(parsed -> {
            if (isNull(parsed.getCompiled()) && (!parsed.isNested() || isNull(parsed.getParentClassName()))) {
                if (nonNull(pathToSave)) {
                    save(parsed.getProperties().getClassName(), parsed.getFiles().get(0), pathToSave);
                    save(parsed.getProperties().getInterfaceName(), parsed.getFiles().get(1), pathToSave);
                }

                compare(parsed.getFiles().get(1), resInterface);
                compare(parsed.getFiles().get(0), resClass);
            }

            if (!parsed.isNested() || isNull(parsed.getParentClassName())) {
                if (!classExists(parsed.getInterfaceFullName())) {
                    list2.add(Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
                }
                if (!classExists(parsed.getParsedFullName())) {
                    list2.add(Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))));
                }
            }

        });

        if (!skipCompilation) {
            var loader = new TestClassLoader();
            assertTrue(compile(loader, list2, resExecute));
        }

    }

    protected void testMultiPass(List<Pair<List<Triple<String, String, String>>, Integer>> passes) {
        testMultiPassExecute(passes, null, null);
    }

    protected void testMulti(List<Triple<String, String, String>> files) {
        testMulti(files, null);
    }

    protected void testMulti(List<Triple<String, String, String>> files, int expected) {
        testMultiExecute(files, expected, null, null);
    }

    protected void testMulti(List<Triple<String, String, String>> files, String pathToSave) {
        testMultiExecute(files, files.size(), pathToSave, null);
    }

    protected void testMultiExecute(List<Triple<String, String, String>> files, String resExecute) {
        testMultiExecute(files, files.size(), null, resExecute);
    }

    protected void testMultiExecute(List<Triple<String, String, String>> files, int expected, String pathToSave, String resExecute) {
        var list = newList();
        files.forEach(t ->
                load(list, t.getLeft()));
        assertTrue(compile(new TestClassLoader(), list, null));
        lookup.registerExternalLookup(s ->
                list.stream().filter(e ->
                        e.getLeft().equals(s)).map(Pair::getRight).findFirst().orElse(null));
        generate();

        assertEquals(expected, lookup.parsed().size());

        var compileList = new ArrayList<Pair<String, String>>();
        files.forEach(f ->
                lookup.findGeneratedByFileName(f.getLeft()).forEach(parsed -> {
                    compare(parsed.getFiles().get(1), f.getRight());
                    compare(parsed.getFiles().get(0), f.getMiddle());

                    if (nonNull(pathToSave)) {
                        if (isNull(parsed.getMixIn())) {
                            save(parsed.getProperties().getClassName(), parsed.getFiles().get(0), pathToSave);
                        }
                        save(parsed.getProperties().getInterfaceName(), parsed.getFiles().get(1), pathToSave);
                    }

                    if (isNull(parsed.getMixIn())) {
                        if (!parsed.isNested() || isNull(parsed.getParentClassName())) {
                            compileList.add(Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
                            compileList.add(Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))));
                        }
                    } else {
                        for (var i = 0; i < compileList.size(); i++) {
                            if (compileList.get(i).getKey().equals(parsed.getMixIn().getParsedFullName())) {
                                compileList.add(i, Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
                                break;
                            }
                        }
                    }
                }));

        var loader = new TestClassLoader();
        assertTrue(compile(loader, compileList, resExecute));
    }

    protected void testMultiPassExecute(List<Pair<List<Triple<String, String, String>>, Integer>> passes, String pathToSave, String resExecute) {
        var loader = new TestClassLoader();
        var list = newList();
        var compileList = new ArrayList<Pair<String, String>>();

        passes.forEach(pass -> {
            var files = pass.getKey();
            var expected = pass.getValue().intValue();
            files.forEach(t ->
                    load(list, t.getLeft()));
            assertTrue(compile(loader, list, null));
            generate();

            assertEquals(expected, lookup.parsed().size());

            files.forEach(f ->
                    lookup.findGeneratedByFileName(f.getLeft()).forEach(parsed -> {
                        compare(parsed.getFiles().get(1), f.getRight());
                        compare(parsed.getFiles().get(0), f.getMiddle());

                        if (nonNull(pathToSave)) {
                            if (isNull(parsed.getMixIn())) {
                                save(parsed.getProperties().getClassName(), parsed.getFiles().get(0), pathToSave);
                            }
                            save(parsed.getProperties().getInterfaceName(), parsed.getFiles().get(1), pathToSave);
                        }

                        if (isNull(parsed.getMixIn())) {
                            if (!parsed.isNested() || isNull(parsed.getParentClassName())) {
                                var intf = Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1)));
                                compileList.add(intf);
                                list.add(intf);
                                compileList.add(Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))));
                            }
                        } else {
                            for (var i = 0; i < compileList.size(); i++) {
                                if (compileList.get(i).getKey().equals(parsed.getMixIn().getParsedFullName())) {
                                    var intf = Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1)));
                                    compileList.add(intf);
                                    list.add(intf);
                                    break;
                                }
                            }
                        }
                    }));

            assertTrue(compile(loader, compileList, resExecute));
        });
    }


    @SneakyThrows
    protected void save(String name, CompilationUnit unit, String pathToSave) {
        var fileName = pathToSave + "/" + name + ".java";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(getAsString(unit));
        writer.close();
        log.info("Saving - {}", fileName);
    }

    protected void testSingleWithBase(String basePrototype, String baseClassName, String prototype, String className, String baseClass, String baseInterface, String resClass, String resInterface) {
        var src = newList();
        load(src, basePrototype);
        load(src, prototype);
        assertTrue(compile(new TestClassLoader(), src, null));
        generate();

        assertEquals(2, lookup.parsed().size());

        var list = newList();

        with(lookup.findGenerated(baseClassName), parsed -> {
            compare(parsed.getFiles().get(0), baseClass);
            compare(parsed.getFiles().get(1), baseInterface);

            list.add(Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
            list.add(Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))));
        });

        with(lookup.findGenerated(className), parsed -> {
            compare(parsed.getFiles().get(0), resClass);
            compare(parsed.getFiles().get(1), resInterface);

            list.add(Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
            list.add(Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))));
        });

        assertTrue(compile(new TestClassLoader(), list, null));
    }

    protected void testSingleWithMixIn(String basePrototype, String baseClassName, String prototype, String className, String baseClass, String baseInterface, String mixInInterface) {
        var src = newList();
        load(src, basePrototype);
        load(src, prototype);
        assertTrue(compile(new TestClassLoader(), src, null));
        generate();

        assertEquals(2, lookup.parsed().size());

        var list = newList();

        with(lookup.findGenerated(baseClassName), parsed ->
                with(lookup.findGenerated(className), parsedMixIn -> {
                    compare(parsed.getFiles().get(1), baseInterface);
                    compare(parsedMixIn.getFiles().get(1), mixInInterface);
                    compare(parsed.getFiles().get(0), baseClass);

                    list.add(Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
                    list.add(Pair.of(parsedMixIn.getInterfaceFullName(), getAsString(parsedMixIn.getFiles().get(1))));
                    list.add(Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))));
                }));

        assertTrue(compile(new TestClassLoader(), list, null));
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    protected boolean compile(TestClassLoader loader, List<Pair<String, String>> files, String resExecute) {
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

        JavaCompiler.CompilationTask task = compiler.getTask(null,
                fileManager, diagnostics, List.of("-Xlint:unchecked"), null, getCompilationUnits(files));

        if (!task.call()) {
            diagnostics.getDiagnostics().forEach(System.out::println);
            fileManager.close();
            return false;
        }
        diagnostics.getDiagnostics().forEach(System.out::println);
        fileManager.close();

        files.forEach(f ->
                with(objects.get(f.getKey()), o ->
                        ifNull(loader.findClass(f.getKey()), () ->
                                loader.define(f.getKey(), o))));

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

    private void defineObjects(TestClassLoader loader, Map<String, JavaByteObject> objects) {
        defineObjects(loader, objects, objects.size());
    }

    private void defineObjects(TestClassLoader loader, Map<String, JavaByteObject> objects, int tries) {
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

    @SneakyThrows
    private String resourceAsString(String resource) {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
        } catch (Exception e) {
            log.error("Unable to load resource: {}!", resource);
            throw e;
        }
    }

    protected List<Pair<String, String>> newList() {
        return new ArrayList<>();
    }

    protected UnaryOperator<String> testSourcesLookup() {
        return s -> {
            try {
                return Files.readString(Path.of("./src/test/java/" + s.replace('.', '/') + ".java"));
            } catch (Exception e) {
                return null;
            }
        };
    }

}
