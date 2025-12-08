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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.objects.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Helpers.classExists;
import static net.binis.codegen.generation.core.Helpers.lookup;
import static net.binis.codegen.tools.Tools.with;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public abstract class BaseCodeGenTest extends BaseCodeTest {

    @BeforeEach
    public void beforeEach() {
        Helpers.cleanUp();
        CodeFactory.forceRegisterType(BaseCodeGenTest.class, () -> this);
    }

    @AfterEach
    public void afterEach() {
        CodeFactory.unregisterType(BaseCodeGenTest.class);
    }

    protected void generate() {
        var parsed = new ArrayList<>(lookup.parsed());
        for (var entry : parsed) {
            if (!entry.isProcessed()) {
                Generator.generateCodeForClass(entry.getDeclarationUnit(), entry);
            }
        }

        lookup.calcPrototypeMaps();

        with(lookup.parsed().stream().filter(PrototypeDescription::isValid).sorted(Helpers::sortForEnrich).toList(), list -> {
            list.forEach(Helpers::handleEnrichers);
            list.forEach(Helpers::finalizeEnrichers);
            list.forEach(Helpers::postProcessEnrichers);
        });
    }

    protected void compare(CompilationUnit unit, String resource) {
        if (nonNull(resource)) {
            assertEquals(resourceAsString(resource), getAsString(unit), resource);
        }
    }

    protected void compare(CompilationUnit unit, TypeDeclaration type) {
        if (nonNull(type)) {
            assertEquals(getAsString(type.findCompilationUnit().get()), getAsString(unit), type.getFullyQualifiedName().get().toString());
        }
    }


    protected TestClassLoader testSingleExecute(String prototype, String resClass, String resInterface, String resExecute) {
        return testSingleExecute(prototype, resClass, resInterface, null, 1, resExecute, false, false, false);
    }

    protected TestClassLoader testSingleExecute(String prototype, String resClass, String resInterface, int expected, String resExecute) {
        return testSingleExecute(prototype, resClass, resInterface, null, expected, resExecute, false, false, false);
    }

    protected TestClassLoader testSingleImplementation(String prototype, String resClass, int expected) {
        return testSingleExecute(prototype, resClass, null, null, expected, null, false, true, false);
    }

    protected TestClassLoader testSingleImplementation(String prototype, String resClass) {
        return testSingleExecute(prototype, resClass, null, null, 1, null, false, true, false);
    }

    protected TestClassLoader testSingle(String prototype, String resClass, String resInterface) {
        return testSingle(prototype, resClass, resInterface, null, 1);
    }

    protected TestClassLoader testSingleWithCustom(String prototype, String resClass, String resInterface, String... custom) {
        return testSingleExecute(prototype, resClass, resInterface, null, 1, null, false, false, false, custom);
    }

    protected TestClassLoader testSingle(String prototype, String resClass, String resInterface, String pathToSave) {
        return testSingle(prototype, resClass, resInterface, pathToSave, 1);
    }

    protected TestClassLoader testSingle(String prototype, String resClass, String resInterface, int expected) {
        return testSingle(prototype, resClass, resInterface, null, expected);
    }

    protected TestClassLoader testSingleSkip(String prototype, String resClass, String resInterface, boolean skipPrototype, boolean skipCompilation) {
        return testSingleSkip(prototype, resClass, resInterface, 1, skipPrototype, skipCompilation);
    }

    protected TestClassLoader testSingleSkip(String prototype, String resClass, String resInterface, int expected, boolean skipPrototype, boolean skipCompilation) {
        return testSingleExecute(prototype, resClass, resInterface, null, expected, null, skipCompilation, false, skipPrototype);
    }


    protected TestClassLoader testSingle(String prototype, String resClass, String resInterface, int expected, boolean skipCompilation) {
        return testSingleExecute(prototype, resClass, resInterface, null, expected, null, skipCompilation, false, false);
    }

    protected TestClassLoader testSingle(String prototype, String resClass, String resInterface, String pathToSave, int expected) {
        return testSingleExecute(prototype, resClass, resInterface, pathToSave, expected, null, false, false, false);
    }

    @SuppressWarnings("unchecked")
    protected TestClassLoader testSingleExecute(String prototype, String resClass, String resInterface, String pathToSave, int expected, String resExecute, boolean skipCompilation, boolean includePrototype, boolean skipPrototypeCompilation, String... custom) {
        var list = newList();
        load(list, prototype);
        TestClassLoader protoLoader = null;
        if (!skipPrototypeCompilation) {
            protoLoader = new TestClassLoader();
            assertTrue(compile(protoLoader, list, null, true));
        }
        generate();

        assertEquals(expected, lookup.parsed().size());

        var list2 = newList();
        if (includePrototype) {
            list2.add(list.get(0));
        }

        var customTypes = loadCustom(custom);

        lookup.parsed().forEach(p -> p.getCustomFiles().forEach((name, file) -> {
            if (nonNull(file.getJavaClass())) {
                var className = file.getJavaClass().getFullyQualifiedName().get().toString();
                var type = customTypes.get(className);
                if (nonNull(type)) {
                    compare(file.getJavaClass().findCompilationUnit().get(), type);
                    list2.add(Pair.of(className, getAsString(file.getJavaClass().findCompilationUnit().get())));
                } else {
                    fail("Can't find generated class: \n" + getAsString(file.getJavaClass().findCompilationUnit().get()));
                }
            }
        }));

        var stream = lookup.generated().stream().distinct();
        if (lookup.generated().isEmpty()) {
            stream = (Stream) lookup.custom().stream();
        }
        stream.sorted((o1, o2) -> Boolean.compare(isNull(o1.getCompiled()), isNull(o2.getCompiled()))).forEach(parsed -> {
            if (isNull(parsed.getCompiled()) && (!parsed.isNested() || isNull(parsed.getParentClassName())) && !parsed.isExternal()) {
                if (nonNull(pathToSave)) {
                    save(parsed.getProperties().getClassName(), parsed.getFiles().get(0), pathToSave);
                    save(parsed.getProperties().getInterfaceName(), parsed.getFiles().get(1), pathToSave);
                }

                compare(parsed.getFiles().get(1), resInterface);
                compare(parsed.getFiles().get(0), resClass);
            }

            if (!parsed.isNested() || isNull(parsed.getParentClassName())) {
                if (!classExists(parsed.getInterfaceFullName())) {
                    with(parsed.getFiles().get(1), file ->
                            list2.add(Pair.of(parsed.getInterfaceFullName(), getAsString(file))));
                }
                if (!classExists(parsed.getParsedFullName())) {
                    with(parsed.getFiles().get(0), file ->
                            list2.add(Pair.of(parsed.getParsedFullName(), getAsString(file))));
                }
            }
        });

        if (!skipCompilation) {
            var loader = new TestClassLoader();
            assertTrue(compile(loader, list2, resExecute, true));
            return loader;
        } else {
            return protoLoader;
        }
    }

    protected Map<String, TypeDeclaration> loadCustom(String... custom) {
        var result = new HashMap<String, TypeDeclaration>();
        for (var file : custom) {
            var source = resourceAsString(file);
            lookup.getParser().parse(source).ifSuccessful(unit ->
                    result.put(unit.getType(0).getFullyQualifiedName().get(), unit.getType(0)));
        }
        return result;
    }

    protected TestClassLoader testMultiPass(List<Pair<List<Triple<String, String, String>>, Integer>> passes) {
        return testMultiPassExecute(passes, null, null);
    }

    protected TestClassLoader testMulti(List<Triple<String, String, String>> files) {
        return testMulti(files, null);
    }

    protected TestClassLoader testMulti(List<Triple<String, String, String>> files, int expected) {
        return testMultiExecute(files, expected, null, null, false);
    }

    protected TestClassLoader testMulti(List<Triple<String, String, String>> files, String pathToSave) {
        return testMultiExecute(files, files.size(), pathToSave, null, false);
    }

    protected TestClassLoader testMultiImplementation(List<Triple<String, String, String>> files) {
        return testMultiExecute(files, files.size(), null, null, true);
    }


    protected TestClassLoader testMultiExecute(List<Triple<String, String, String>> files, String resExecute) {
        return testMultiExecute(files, files.size(), null, resExecute, false);
    }

    protected TestClassLoader testMultiExecute(List<Triple<String, String, String>> files, int expected, String pathToSave, String resExecute, boolean includePrototype) {
        var list = newList();
        files.forEach(t ->
                load(list, t.getLeft()));
        assertTrue(compile(new TestClassLoader(), list, null, false));
        lookup.registerExternalLookup(s ->
                list.stream().filter(e ->
                        e.getLeft().equals(s)).map(Pair::getRight).findFirst().orElse(null));
        generate();

        assertEquals(expected, lookup.parsed().size());

        var compileList = new ArrayList<Pair<String, String>>();
        if (includePrototype) {
            compileList.addAll(list);
        }
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
                            if (nonNull(parsed.getFiles().get(1))) {
                                compileList.add(Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
                            }
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
        assertTrue(compile(loader, compileList, resExecute, false));
        return loader;
    }

    protected TestClassLoader testMultiPassExecute(List<Pair<List<Triple<String, String, String>>, Integer>> passes, String pathToSave, String resExecute) {
        var loader = new TestClassLoader();
        var list = newList();
        var compileList = new ArrayList<Pair<String, String>>();

        passes.forEach(pass -> {
            var files = pass.getKey();
            var expected = pass.getValue().intValue();
            files.forEach(t ->
                    load(list, t.getLeft()));
            assertTrue(compile(loader, list, null, false));
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

            assertTrue(compile(loader, compileList, resExecute, false));
        });
        return loader;
    }


    @SneakyThrows
    protected void save(String name, CompilationUnit unit, String pathToSave) {
        var fileName = pathToSave + '/' + name + ".java";
        try (var writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(getAsString(unit));
        }
        log.info("Saving - {}", fileName);
    }

    protected TestClassLoader testSingleWithBase(String basePrototype, String baseClassName, String prototype, String className, String baseClass, String baseInterface, String resClass, String resInterface) {
        var src = newList();
        load(src, basePrototype);
        load(src, prototype);
        var loader = new TestClassLoader();
        assertTrue(compile(loader, src, null, false));
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

        assertTrue(compile(new TestClassLoader(), list, null, false));
        return loader;
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
