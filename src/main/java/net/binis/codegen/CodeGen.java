package net.binis.codegen;

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
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.generation.core.Structures.Parsed;
import static net.binis.codegen.tools.Tools.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class CodeGen {

    public static final String NONE = "<none>";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String IMPL_DESTINATION = "idestination";
    public static final String FILTER = "filter";

    public static void main(String[] args) throws IOException {

        log.info("Class path: {}", System.getProperty("java.class.path"));

        var cmd = handleArgs(args);

        var files = new ArrayList<Path>();
        addTree(Paths.get(cmd.getOptionValue(SOURCE)), files, cmd.getOptionValue(FILTER));
        processFiles(files);

        enumParsed.values().stream().filter(v -> nonNull(v.getFiles())).forEach(p -> {
            if (isNull(p.getProperties().getMixInClass())) {
                saveFile(getBasePath(cmd.getOptionValue(DESTINATION), p.getProperties()), p.getFiles().get(0));
            }
        });

        var constants = Generator.generateCodeForConstants();
        if (nonNull(constants)) {
            saveFile(cmd.getOptionValue(DESTINATION), constants);
        }

        var destination = cmd.getOptionValue(DESTINATION);
        var impl_destination = cmd.getOptionValue(IMPL_DESTINATION);
        lookup.parsed().stream().filter(v -> nonNull(v.getFiles())).forEach(p -> {
            if (p.getProperties().isGenerateImplementation() && isNull(p.getProperties().getMixInClass())) {
                saveFile(nullCheck(getBasePath(impl_destination, p.getProperties()), destination), p.getFiles().get(0));
            }
            if (p.getProperties().isGenerateInterface()) {
                saveFile(getBasePath(destination, p.getProperties()), p.getFiles().get(1));
            }
        });
    }

    public static void processFiles(List<Path> files) {
        var parser = new JavaParser();
        for (var file : files) {
            try {
                var fileName = file.toAbsolutePath().toString();
                var parse = parser.parse(file);
                log.info("Parsed {} - {}", fileName, parse.toString());
                parse.getResult().ifPresent(u ->
                        u.getTypes().forEach(t ->
                                handleType(parser, t, fileName)));
            } catch (IOException e) {
                log.error("Unable to parse {}", file.getFileName(), e);
            }
        }

        for (var entry : enumParsed.entrySet()) {
            ifNull(entry.getValue().getFiles(), () ->
                    Generator.generateCodeForEnum(entry.getValue().getDeclaration().findCompilationUnit().get()));
        }

        for (var entry : lookup.parsed()) {
            ifNull(entry.getFiles(), () ->
                    Generator.generateCodeForClass(entry.getDeclaration().findCompilationUnit().get()));
        }

        recursiveExpr.forEach(pair ->
                pair.getRight().setType(findProperType(pair.getLeft(), pair.getMiddle(), pair.getRight())));

        lookup.parsed().stream().filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
        lookup.parsed().stream().filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
    }

    public static void processSources(List<String> files) {
        var parser = new JavaParser();
        for (var file : files) {
            try {
                var parse = parser.parse(file);
                var unit = parse.getResult().get();
                var fileName = unit.getPackageDeclaration().get().getNameAsString().replace('.', '/') + unit.getType(0).getNameAsString();
                log.info("Parsed {} - {}", fileName, parse.toString());
                parse.getResult().ifPresent(u ->
                        u.getTypes().forEach(t ->
                                handleType(parser, t, fileName)));
            } catch (Exception e) {
                log.error("Unable to parse {}", file, e);
            }
        }

        for (var entry : enumParsed.entrySet()) {
            ifNull(entry.getValue().getFiles(), () ->
                    Generator.generateCodeForEnum(entry.getValue().getDeclaration().findCompilationUnit().get()));
        }

        for (var entry : lookup.parsed()) {
            ifNull(entry.getFiles(), () ->
                    Generator.generateCodeForClass(entry.getDeclaration().findCompilationUnit().get()));
        }

        recursiveExpr.forEach(pair ->
                pair.getRight().setType(findProperType(pair.getLeft(), pair.getMiddle(), pair.getRight())));

        lookup.parsed().stream().filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
        lookup.parsed().stream().filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
    }


    @SuppressWarnings("unchecked")
    public static void handleType(JavaParser parser, TypeDeclaration<?> t, String fileName) {
        var className = t.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString() + '.' + t.getNameAsString();
        if (t.isEnumDeclaration()) {
            enumParsed.put(getClassName(t.asEnumDeclaration()),
                    Parsed.builder().declaration(t.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(className).parser(parser).build());
        } else {
            if (t.getAnnotationByName("ConstantPrototype").isPresent()) {
                constantParsed.put(getClassName(t.asClassOrInterfaceDeclaration()),
                        Parsed.builder().declaration(t.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(className).parser(parser).build());
            } else {
                lookup.registerParsed(getClassName(t.asClassOrInterfaceDeclaration()),
                        Parsed.builder().declaration(t.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(className).parser(parser).build());
            }
        }
    }

    private static void saveFile(String baseDir, CompilationUnit file) {
        var config = new PrettyPrinterConfiguration();
        var printer = new PrettyPrinter(config);

        sortImports(file);
        if (file.getType(0).isClassOrInterfaceDeclaration()) {
            sortClass(file.getType(0).asClassOrInterfaceDeclaration());
        }

        //System.out.println(printer.print(file));

        file.getPackageDeclaration().ifPresent(p -> {
            var fileName = baseDir + '/' + p.getNameAsString().replace(".", "/") + '/' + file.getType(0).getNameAsString() + ".java";
            log.info("Writing file - {}", fileName);
            var f = new File(fileName);
            if (f.getParentFile().exists() || f.getParentFile().mkdirs()) {
                try {
                    var writer = new BufferedWriter(new FileWriter(fileName));
                    writer.write(printer.print(file));
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

        Option iOutput = new Option("id", IMPL_DESTINATION, true, "Implementations Destination folder");
        iOutput.setRequired(false);
        options.addOption(iOutput);

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
            log.info("implementations destination: " + nullCheck(cmd.getOptionValue(IMPL_DESTINATION), "<destination>"));
            log.info("filter: " + nullCheck(cmd.getOptionValue(FILTER), NONE));
        } catch (ParseException e) {
            log.info(e.getMessage());
            formatter.printHelp("CodeGen", options);

            System.exit(1);
        }

        return cmd;
    }

    private static String getBasePath(String defaultPath, PrototypeData properties) {
        if (isNotBlank(properties.getBasePath())) {
            return properties.getBasePath();
        }
        return defaultPath;
    }

}
