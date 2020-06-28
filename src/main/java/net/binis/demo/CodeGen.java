package net.binis.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import net.binis.demo.codegen.Generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.demo.codegen.Helpers.getClassName;
import static net.binis.demo.codegen.Helpers.parsed;
import static net.binis.demo.codegen.Structures.Parsed;
import static net.binis.demo.tools.Tools.ifNull;

@Slf4j
public class CodeGen {

    public static void main(String[] args) throws IOException {

        log.info(Arrays.toString(args));

        var files = new ArrayList<Path>();
        addTree(Paths.get(args[0]), files);

        var parser = new JavaParser();
        for (var file : files) {
            try {
                var fileName = file.getFileName().toAbsolutePath().toString();
                var parse = parser.parse(file);
                log.info("Parsed {} - {}", fileName, parse.toString());
                parse.getResult().ifPresent(u ->
                        u.getTypes().forEach(t ->
                                parsed.put(getClassName(t.asClassOrInterfaceDeclaration()), Parsed.builder().declaration(t.asClassOrInterfaceDeclaration()).build())));
            } catch (IOException e) {
                log.error("Unable to parse {}", file.getFileName(), e);
            }
        }

        for (var entry : parsed.entrySet()) {
            ifNull(parsed.get(entry.getKey()).getFiles(), () ->
                    Generator.generateCodeForClass(entry.getValue().getDeclaration().findCompilationUnit().get()));
        }

        parsed.values().stream().filter(v -> nonNull(v.getFiles())).forEach(p -> {
            if (isNull(p.getProperties().getMixInClass())) {
                var file = p.getFiles().get(0);
                saveFile(args[1], file);
            }
            if (p.getProperties().isGenerateInterface()) {
                var file = p.getFiles().get(1);
                saveFile(args[1], file);
            }
        });
    }

    private static void saveFile(String baseDir, CompilationUnit file) {
        System.out.println(file.toString());

        file.getPackageDeclaration().ifPresent(p -> {
            var fileName = baseDir + '/' + p.getNameAsString().replace(".", "/") + '/' + file.getType(0).getNameAsString() + ".java";
            var f = new File(fileName);
            if (f.getParentFile().exists() || f.getParentFile().mkdirs()) {
                try {
                    var writer = new BufferedWriter(new FileWriter(fileName));
                    writer.write(file.toString());
                    writer.close();
                } catch (IOException e) {
                    log.error("Unable to open for write file {}", fileName);
                }
            } else {
                log.error("Unable to write file {}", fileName);
            }
        });
    }

    private static void addTree(Path directory, final Collection<Path> all) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!file.toFile().isDirectory() && file.getFileName().toString().endsWith(".java")) {
                    all.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
