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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.javaparser.CodeGenPrettyPrinter;
import net.binis.codegen.tools.CollectionUtils;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.generation.core.Structures.Parsed;
import static net.binis.codegen.tools.Tools.ifNull;
import static net.binis.codegen.tools.Tools.nullCheck;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class CodeGen {

    public static final String NONE = "<none>";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String CLASS_DESTINATION = "output";
    public static final String IMPL_DESTINATION = "idestination";
    public static final String FILTER = "filter";

    public static void main(String[] args) throws IOException {
        log.info("Class path: {}", System.getProperty("java.class.path"));

        var cmd = handleArgs(args);

        if (nonNull(cmd.getOptionValue(CLASS_DESTINATION))) {
            addGenerationFile(cmd.getOptionValue(CLASS_DESTINATION));
        }

        var files = new ArrayList<Path>();
        addTree(Paths.get(cmd.getOptionValue(SOURCE)), files, cmd.getOptionValue(FILTER));
        processFiles(files);

        enumParsed.values().stream().filter(v -> nonNull(v.getFiles())).forEach(p -> {
            if (isNull(p.getProperties().getMixInClass()) && isNull(p.getCompiled())) {
                saveFile(getBasePath(cmd.getOptionValue(DESTINATION), p.getProperties(), true), p.getFiles().get(0));
            }
        });

        var constants = Generator.generateCodeForConstants();
        if (nonNull(constants)) {
            saveFile(cmd.getOptionValue(DESTINATION), constants);
        }

        var destination = cmd.getOptionValue(DESTINATION);
        var impl_destination = cmd.getOptionValue(IMPL_DESTINATION);
        lookup.parsed().stream().filter(v -> nonNull(v.getFiles())).forEach(p -> {
            if (p.getProperties().isGenerateImplementation() && isNull(p.getProperties().getMixInClass()) && isNull(p.getCompiled())) {
                saveFile(nullCheck(getBasePath(impl_destination, p.getProperties(), true), destination), p.getFiles().get(0));
            }
            if (p.getProperties().isGenerateInterface() && isNull(p.getCompiled())) {
                saveFile(getBasePath(destination, p.getProperties(), false), p.getFiles().get(1));
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

        var entry = lookup.parsed().stream().filter(e -> isNull(e.getFiles()) && !e.isInvalid()).findFirst();
        while (entry.isPresent()) {
            Generator.generateCodeForClass(entry.get().getDeclaration().findCompilationUnit().get(), entry.get());
            entry = lookup.parsed().stream().filter(e -> isNull(e.getFiles()) && !e.isInvalid()).findFirst();
        }

        recursiveExpr.forEach(pair ->
                pair.getRight().setType(findProperType(pair.getLeft(), pair.getMiddle(), pair.getRight())));

        lookup.calcPrototypeMaps();

        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
    }

    public static void processSources(List<String> files) {
        var parser = new JavaParser();
        for (var file : files) {
            try {
                var parse = parser.parse(file);
                var unit = parse.getResult().get();
                var fileName = unit.getPackageDeclaration().get().getNameAsString().replace('.', '/') + "/" + unit.getType(0).getNameAsString();
                log.info("Parsed {} - {}", fileName, parse);
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

        for (var entry : CollectionUtils.copyList(lookup.parsed())) {
            ifNull(entry.getFiles(), () ->
                    Generator.generateCodeForClass(entry.getDeclaration().findCompilationUnit().get(), entry));
        }

        recursiveExpr.forEach(pair ->
                pair.getRight().setType(findProperType(pair.getLeft(), pair.getMiddle(), pair.getRight())));

        lookup.calcPrototypeMaps();

        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::handleEnrichers);
        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> isNull(p.getBase()) && isNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
        lookup.parsed().stream().filter(PrototypeDescription::isValid).filter(p -> nonNull(p.getBase()) || nonNull(p.getMixIn())).forEach(Helpers::finalizeEnrichers);
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
                var name = getClassName(t.asClassOrInterfaceDeclaration());
                checkForNestedClasses(t.asTypeDeclaration(), fileName, className, parser);
                lookup.registerParsed(name,
                        Parsed.builder().declaration(t.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(className).parser(parser).build());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void checkForNestedClasses(TypeDeclaration<?> type, String fileName, String className, JavaParser parser) {
        type.getChildNodes().stream().filter(ClassOrInterfaceDeclaration.class::isInstance).map(ClassOrInterfaceDeclaration.class::cast).forEach(nested -> {
            if (nested.asClassOrInterfaceDeclaration().isInterface() && Generator.getCodeAnnotation(nested).isPresent()) {
                var parent = type.findCompilationUnit().get();
                var unit = new CompilationUnit().setPackageDeclaration(parent.getPackageDeclaration().get());
                var nestedType = nested.clone();
                parent.getImports().forEach(unit::addImport);
                unit.addType(nestedType);

                lookup.registerParsed(getClassName(nestedType),
                        Structures.Parsed.builder().declaration(nestedType.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(className).parser(parser).nested(true).build());

            }
        });
    }

    private static void saveFile(String baseDir, CompilationUnit file) {
        var printer = new CodeGenPrettyPrinter();

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

        Option clsOutput = new Option("o", CLASS_DESTINATION, true, "Classes output folder");
        clsOutput.setRequired(false);
        options.addOption(clsOutput);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            log.info("source: " + nullCheck(cmd.getOptionValue(SOURCE), NONE));
            log.info("destination: " + nullCheck(cmd.getOptionValue(DESTINATION), NONE));
            log.info("implementations destination: " + nullCheck(cmd.getOptionValue(IMPL_DESTINATION), "<destination>"));
            if (nonNull(cmd.getOptionValue(CLASS_DESTINATION))) {
                log.info("classes output: " + cmd.getOptionValue(CLASS_DESTINATION));
            }
            log.info("filter: " + nullCheck(cmd.getOptionValue(FILTER), NONE));
        } catch (ParseException e) {
            log.info(e.getMessage());
            formatter.printHelp("CodeGen", options);

            System.exit(1);
        }

        return cmd;
    }

    private static String getBasePath(String defaultPath, PrototypeData properties, boolean implementation) {
        var result = defaultPath;

        if (isNotBlank(properties.getBasePath())) {
            result = properties.getBasePath();
        }

        if (implementation) {
            if (isNotBlank(properties.getImplementationPath())) {
                result = properties.getImplementationPath();
            }
        } else {
            if (isNotBlank(properties.getInterfacePath())) {
                result = properties.getInterfacePath();
            }
        }

        return result;
    }

    private static void addGenerationFile(String folder) {
        try (var myWriter = new FileWriter(folder + "/codegen.info")) {
            myWriter.write("CodeGen generation started at " + LocalDateTime.now());
        } catch (Exception e) {
            log.error("Can't create marker file!");
        }
    }

}
