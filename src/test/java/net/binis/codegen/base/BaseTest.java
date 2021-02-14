package net.binis.codegen.base;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import lombok.SneakyThrows;
import net.binis.codegen.CodeGen;
import net.binis.codegen.codegen.Generator;
import net.binis.codegen.codegen.Helpers;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.MultiCollectorManager;

import javax.tools.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.binis.codegen.codegen.Helpers.*;
import static net.binis.codegen.tools.Tools.ifNull;
import static net.binis.codegen.tools.Tools.with;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    }

    protected void cleanUp() {
        Helpers.cleanUp();
    }


    protected void load(String resource) {
        var parse = parser.parse(getClass().getClassLoader().getResourceAsStream(resource));
        parse.getResult().ifPresent(u ->
                u.getTypes().forEach(CodeGen::handleType));
    }

    protected void compare(CompilationUnit unit, String resource) {
        assertEquals(resourceAsString(resource), getAsString(unit));
    }

    protected void testSingle(String prototype, String resClass, String resInterface) {
        load(prototype);
        generate();

        assertEquals(1, lookup.parsed().size());
        with(lookup.generated().iterator().next(), parsed -> {
            compare(parsed.getFiles().get(0), resClass);
            compare(parsed.getFiles().get(1), resInterface);

            var loader = new TestClassLoader();
            assertTrue(compile(loader,
                    List.of(
                            Pair.of(parsed.getInterfaceFullName(), getAsString(parsed.getFiles().get(1))),
                            Pair.of(parsed.getParsedFullName(), getAsString(parsed.getFiles().get(0))))));
        });
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

        objects.forEach(loader::define);

        return true;
    }

    @SneakyThrows
    private String resourceAsString(String resource) {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
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

}
