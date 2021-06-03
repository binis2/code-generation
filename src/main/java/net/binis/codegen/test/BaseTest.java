package net.binis.codegen.test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.CodeGen;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.Helpers;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.tools.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Tools.ifNull;
import static net.binis.codegen.tools.Tools.with;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public abstract class BaseTest {

    protected JavaParser parser = new JavaParser();

    protected String getAsString(CompilationUnit file) {
        var config = new PrettyPrinterConfiguration();
        var printer = new PrettyPrinter(config);

        sortImports(file);
        if (file.getType(0).isClassOrInterfaceDeclaration()) {
            sortClass(file.getType(0).asClassOrInterfaceDeclaration());
        }

        return printer.print(file);
    }

    protected void generate() {
        for (var entry : lookup.parsed()) {
            ifNull(entry.getFiles(), () ->
                    Generator.generateCodeForClass(entry.getDeclaration().findCompilationUnit().get()));
        }

        lookup.parsed().forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(v -> nonNull(v.getFiles())).forEach(Helpers::finalizeEnrichers);
    }

    protected void cleanUp() {
        Helpers.cleanUp();
    }

    protected void load(List<Pair<String, String>> list, String resource) {
        var source = resourceAsString(resource);
        var parse = parser.parse(source);
        assertTrue(parse.isSuccessful());
        if (nonNull(list)) {
            list.add(Pair.of(parse.getResult().get().findFirst(ClassOrInterfaceDeclaration.class).get().getFullyQualifiedName().get(), source));
        }
        parse.getResult().ifPresent(u ->
                u.getTypes().forEach(t ->
                        CodeGen.handleType(t, resource)));
    }

    protected void compare(CompilationUnit unit, String resource) {
        assertEquals(resourceAsString(resource), getAsString(unit));
    }

    protected void testSingle(String prototype, String resClass, String resInterface) {
        testSingle(prototype, resClass, resInterface, null);
    }

    protected void testSingle(String prototype, String resClass, String resInterface, String pathToSave) {
        var list = newList();
        load(list, prototype);
        assertTrue(compile(new TestClassLoader(), list));
        generate();

        assertEquals(1, lookup.parsed().size());


        with(lookup.generated().iterator().next(), parsed -> {
            if (nonNull(pathToSave)) {
                save(parsed.getProperties().getClassName(), parsed.getFiles().get(0), pathToSave);
                save(parsed.getProperties().getInterfaceName(), parsed.getFiles().get(1), pathToSave);
            }

            compare(parsed.getFiles().get(1), resInterface);
            compare(parsed.getFiles().get(0), resClass);

            var loader = new TestClassLoader();
            assertTrue(compile(loader,
                    List.of(
                            Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))),
                            Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))))));
        });
    }

    protected void testMulti(List<Triple<String, String, String>> files) {
        testMulti(files, null);
    }

    protected void testMulti(List<Triple<String, String, String>> files, String pathToSave) {
        var list = newList();
        files.forEach(t ->
                load(list, t.getLeft()));
        assertTrue(compile(new TestClassLoader(), list));
        generate();

        assertEquals(files.size(), lookup.parsed().size());


        var compileList = new ArrayList<Pair<String, String>>();
        files.forEach(f -> {
            lookup.findGeneratedByFileName(f.getLeft()).forEach(parsed -> {
                if (nonNull(pathToSave)) {
                    save(parsed.getProperties().getClassName(), parsed.getFiles().get(0), pathToSave);
                    save(parsed.getProperties().getInterfaceName(), parsed.getFiles().get(1), pathToSave);
                }

                compare(parsed.getFiles().get(1), f.getRight());
                compare(parsed.getFiles().get(0), f.getMiddle());

                compileList.add(Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))));
                compileList.add(Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))));
            });
        });

        var loader = new TestClassLoader();
        assertTrue(compile(loader, compileList));
    }

    @SneakyThrows
    protected void save(String name, CompilationUnit unit, String pathToSave) {
        var fileName = pathToSave + "/" + name + ".java";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(getAsString(unit));
        writer.close();
    }

    protected void testSingleWithBase(String basePrototype, String baseClassName, String prototype, String className, String baseClass, String baseInterface, String resClass, String resInterface) {
        var src = newList();
        load(src, basePrototype);
        load(src, prototype);
        assertTrue(compile(new TestClassLoader(), src));
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

        assertTrue(compile(new TestClassLoader(), list));
    }

    protected void testSingleWithMixIn(String basePrototype, String baseClassName, String prototype, String className, String baseClass, String baseInterface, String mixInInterface) {
        var src = newList();
        load(src, basePrototype);
        load(src, prototype);
        assertTrue(compile(new TestClassLoader(), src));
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

        assertTrue(compile(new TestClassLoader(), list));
    }


    @SneakyThrows
    protected boolean compile(TestClassLoader loader, List<Pair<String, String>> files) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics =
                new DiagnosticCollector<>();

        var objects = files.stream().collect(Collectors.toMap(Pair::getKey, f -> new JavaByteObject(f.getKey())));

        StandardJavaFileManager standardFileManager =
                compiler.getStandardFileManager(diagnostics, null, null);

        JavaFileManager fileManager = createFileManager(loader, standardFileManager, objects);

        JavaCompiler.CompilationTask task = compiler.getTask(null,
                fileManager, diagnostics, null, null, getCompilationUnits(files));

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

    @SneakyThrows
    private String resourceAsString(String resource) {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
        } catch (Exception e) {
            log.error("Unable to load resource: {}!", resource);
            throw e;
        }
    }

    private static JavaFileManager createFileManager(ClassLoader loader, StandardJavaFileManager fileManager, Map<String, JavaByteObject> files) {
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
        return files.stream().map(f -> new JavaSourceObject(f.getKey(), f.getValue())).collect(Collectors.toList());
    }

    protected List<Pair<String, String>> newList() {
        return new ArrayList<>();
    }

}
