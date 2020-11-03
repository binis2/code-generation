package net.binis.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.demo.codegen.CollectionsHandler;
import net.binis.demo.codegen.Generator;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.demo.codegen.Helpers.*;
import static net.binis.demo.codegen.Structures.Parsed;
import static net.binis.demo.tools.Tools.*;

@Slf4j
public class CodeGen {

    public static final String NONE = "<none>";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String FILTER = "filter";

    public static void main(String[] args) throws IOException {

        var cmd = handleArgs(args);

        var files = new ArrayList<Path>();
        addTree(Paths.get(cmd.getOptionValue(SOURCE)), files, cmd.getOptionValue(FILTER));

        var parser = new JavaParser();
        for (var file : files) {
            try {
                var fileName = file.toAbsolutePath().toString();
                var parse = parser.parse(file);
                log.info("Parsed {} - {}", fileName, parse.toString());
                parse.getResult().ifPresent(u ->
                        u.getTypes().forEach(CodeGen::handleType));
            } catch (IOException e) {
                log.error("Unable to parse {}", file.getFileName(), e);
            }
        }

        for (var entry : enumParsed.entrySet()) {
            ifNull(entry.getValue().getFiles(), () ->
                    Generator.generateCodeForEnum(entry.getValue().getDeclaration().findCompilationUnit().get()));
        }

        enumParsed.values().stream().filter(v -> nonNull(v.getFiles())).forEach(p -> {
            if (isNull(p.getProperties().getMixInClass())) {
                saveFile(cmd.getOptionValue(DESTINATION), p.getFiles().get(0));
            }
        });

        var constants = Generator.generateCodeForConstants();
        if (nonNull(constants)) {
            saveFile(cmd.getOptionValue(DESTINATION), constants);
        }

        for (var entry : parsed.entrySet()) {
            ifNull(entry.getValue().getFiles(), () ->
                    Generator.generateCodeForClass(entry.getValue().getDeclaration().findCompilationUnit().get()));
        }

        parsed.values().stream().filter(v -> nonNull(v.getFiles())).forEach(p -> {
            if (isNull(p.getProperties().getMixInClass())) {
                var file = CollectionsHandler.finalizeEmbeddedModifier(p.getFiles().get(0));
                saveFile(cmd.getOptionValue(DESTINATION), file);
            }
            if (p.getProperties().isGenerateInterface()) {
                var file = CollectionsHandler.finalizeEmbeddedModifier(p.getFiles().get(1));
                saveFile(cmd.getOptionValue(DESTINATION), file);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static void handleType(TypeDeclaration<?> t) {
        if (t.isEnumDeclaration()) {
            enumParsed.put(getClassName(t.asEnumDeclaration()), Parsed.builder().declaration(t.asTypeDeclaration()).build());
        } else {
            if (t.getAnnotationByName("ConstantPrototype").isPresent()) {
                constantParsed.put(getClassName(t.asClassOrInterfaceDeclaration()), Parsed.builder().declaration(t.asTypeDeclaration()).build());
            } else {
                parsed.put(getClassName(t.asClassOrInterfaceDeclaration()), Parsed.builder().declaration(t.asTypeDeclaration()).build());
            }
        }
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

    private static void addTree(Path directory, final Collection<Path> all, String filter) throws IOException {
        var pm = FileSystems.getDefault().getPathMatcher("glob:" + nullCheck(filter, "**"));
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!file.toFile().isDirectory() && pm.matches(file)) {
                    all.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static CommandLine handleArgs(String[] args) {
        Options options = new Options();

        Option input = new Option("s", SOURCE, true, "Sources root folder");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("d", DESTINATION, true, "Destination folder");
        output.setRequired(true);
        options.addOption(output);

        Option filter = new Option("f", FILTER, true, "File pattern filter");
        filter.setRequired(false);
        options.addOption(filter);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            log.info("source: " + nullCheck(cmd.getOptionValue(SOURCE), NONE));
            log.info("destination: " + nullCheck(cmd.getOptionValue(DESTINATION), NONE));
            log.info("filter: " + nullCheck(cmd.getOptionValue(FILTER), NONE));
        } catch (ParseException e) {
            log.info(e.getMessage());
            formatter.printHelp("CodeGen", options);

            System.exit(1);
        }

        return cmd;
    }

}
