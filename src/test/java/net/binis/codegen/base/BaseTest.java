package net.binis.codegen.base;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import lombok.SneakyThrows;
import net.binis.codegen.CodeGen;
import net.binis.codegen.codegen.Generator;

import java.nio.file.Files;
import java.nio.file.Paths;

import static net.binis.codegen.codegen.Helpers.*;
import static net.binis.codegen.tools.Tools.ifNull;
import static net.binis.codegen.tools.Tools.with;
import static org.junit.Assert.assertEquals;

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
        });
    }

    @SneakyThrows
    private String resourceAsString(String resource) {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resource).toURI())));
    }

}
