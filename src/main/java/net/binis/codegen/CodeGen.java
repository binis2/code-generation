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
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.discoverer.AnnotationDiscoverer;
import net.binis.codegen.exception.GenericCodeGenException;
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
import static net.binis.codegen.tools.Tools.*;
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

        AnnotationDiscoverer.findAnnotations().forEach(a ->
                Structures.registerTemplate(a.getCls()));

        var cmd = handleArgs(args);

        if (nonNull(cmd.getOptionValue(CLASS_DESTINATION))) {
            addGenerationFile(cmd.getOptionValue(CLASS_DESTINATION));
        }

        var files = new ArrayList<Path>();
        addTree(Paths.get(cmd.getOptionValue(SOURCE)), files, cmd.getOptionValue(FILTER));
        processFiles(files);

        var constants = Generator.generateCodeForConstants();
        if (nonNull(constants)) {
            saveFile(cmd.getOptionValue(DESTINATION), constants);
        }

        var destination = cmd.getOptionValue(DESTINATION);
        var impl_destination = cmd.getOptionValue(IMPL_DESTINATION);
        lookup.parsed().stream().filter(PrototypeDescription::isProcessed).filter(p -> !p.isNested() || isNull(p.getParentClassName())).forEach(p -> {
            saveParsed(destination, impl_destination, p);
        });
        lookup.custom().forEach(p -> {
            saveParsed(destination, impl_destination, p);
        });
    }

    private static void saveParsed(String destination, String impl_destination, PrototypeDescription<ClassOrInterfaceDeclaration> p) {
        if (p.getProperties().isGenerateImplementation() && isNull(p.getProperties().getMixInClass()) && isNull(p.getCompiled())) {
            saveFile(nullCheck(getBasePath(impl_destination, p.getProperties(), true), destination), p.getFiles().get(0));
        }
        if (p.getProperties().isGenerateInterface() && isNull(p.getCompiled())) {
            saveFile(getBasePath(destination, p.getProperties(), false), p.getFiles().get(1));
        }
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

        var entry = lookup.parsed().stream().filter(e -> !e.isProcessed() && !e.isInvalid()).findFirst();
        while (entry.isPresent()) {
            Generator.generateCodeForClass(entry.get().getDeclaration().findCompilationUnit().get(), entry.get());
            entry = lookup.parsed().stream().filter(e -> !e.isProcessed() && !e.isInvalid()).findFirst();
        }

        recursiveExpr.forEach(pair ->
                pair.getRight().setType(findProperType(pair.getLeft(), pair.getMiddle(), pair.getRight())));

        lookup.calcPrototypeMaps();

        with(lookup.parsed().stream().filter(PrototypeDescription::isValid).sorted(Helpers::sortForEnrich).toList(), list -> {
            list.forEach(Helpers::handleEnrichers);
            list.forEach(Helpers::finalizeEnrichers);
            list.forEach(Helpers::postProcessEnrichers);
        });
    }

    public static void processSources(List<String> files) {
        var parser = new JavaParser();
        for (var file : files) {
            try {
                var parse = parser.parse(file);
                var unit = parse.getResult().orElseThrow(() -> new GenericCodeGenException("Can't parse file '" + file + "'!"));
                var pack = unit.getPackageDeclaration().orElseThrow(() -> new GenericCodeGenException("'" + file + "' have no package declaration!"));
                var fileName = pack.getNameAsString().replace('.', '/') + '/' + unit.getType(0).getNameAsString();
                log.info("Parsed {} - {}", fileName, parse);
                parse.getResult().ifPresent(u ->
                        u.getTypes().forEach(t ->
                                handleType(parser, t, fileName)));
            } catch (Exception e) {
                log.error("Unable to parse {}", file, e);
            }
        }

        for (var entry : CollectionUtils.copyList(lookup.parsed())) {
            if (!entry.isProcessed()) {
                Generator.generateCodeForClass(entry.getDeclaration().findCompilationUnit().get(), entry);
            }
        }

        recursiveExpr.forEach(pair ->
                pair.getRight().setType(findProperType(pair.getLeft(), pair.getMiddle(), pair.getRight())));

        lookup.calcPrototypeMaps();

        with(lookup.parsed().stream().filter(PrototypeDescription::isValid).sorted(Helpers::sortForEnrich).toList(), list -> {
            list.forEach(Helpers::handleEnrichers);
            list.forEach(Helpers::finalizeEnrichers);
            list.forEach(Helpers::postProcessEnrichers);
        });
    }


    @SuppressWarnings("unchecked")
    public static void handleType(JavaParser parser, TypeDeclaration<?> t, String fileName) {
        var pack = t.findCompilationUnit().get().getPackageDeclaration().orElseThrow(() -> new GenericCodeGenException("'" + fileName + "' have no package declaration!"));
        var className = pack.getNameAsString() + '.' + t.getNameAsString();
        if (t.getAnnotationByName("ConstantPrototype").isPresent()) {
            constantParsed.put(getClassName(t.asClassOrInterfaceDeclaration()),
                    Parsed.builder().declaration(t.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(className).parser(parser).build());
        } else {
            var name = getClassName(t);
            checkForNestedClasses(t.asTypeDeclaration(), fileName, parser);
            lookup.registerParsed(name,
                    Parsed.builder().declaration(t.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(className).parser(parser).build());
        }
    }

    @SuppressWarnings("unchecked")
    public static void checkForNestedClasses(TypeDeclaration<?> type, String fileName, JavaParser parser) {
        if (type.isClassOrInterfaceDeclaration() && Generator.getCodeAnnotation(type.asClassOrInterfaceDeclaration()).isEmpty()) {
            type.getChildNodes().stream().filter(ClassOrInterfaceDeclaration.class::isInstance).map(ClassOrInterfaceDeclaration.class::cast).forEach(nested -> {
                if (nested.asClassOrInterfaceDeclaration().isInterface() && Generator.getCodeAnnotation(nested).isPresent()) {
                    var parent = type.findCompilationUnit().get();
                    var unit = new CompilationUnit().setPackageDeclaration(parent.getPackageDeclaration().get());
                    var nestedType = nested.clone();
                    parent.getImports().forEach(unit::addImport);
                    unit.addType(nestedType);
                    var cName = getClassName(nestedType);

                    lookup.registerParsed(cName,
                            Structures.Parsed.builder().declaration(nestedType.asTypeDeclaration()).prototypeFileName(fileName).prototypeClassName(cName).parser(parser).nested(true).build());
                }
            });
        }
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

    public static void processTemplate(String name, String source) {
        log.info("Processing template: {}", name);
        var parser = new JavaParser();
        var parse = parser.parse(source);
        parse.getResult().ifPresentOrElse(u ->
                        u.getTypes().forEach(CodeGen::handleTemplate),
                () -> {
                    log.error("Failed template processing ({}) with:", name);
                    parse.getProblems().forEach(p ->
                            log.error("    {}:{} {}", p.getCause().map(Object::toString).orElse(""), p.getMessage(), p.getLocation().map(Object::toString).orElse("")));
                });
    }

    public static void handleTemplate(TypeDeclaration<?> t) {
        if (t instanceof AnnotationDeclaration) {
            Structures.registerTemplate(t.asAnnotationDeclaration());
        } else {
            log.error("Invalid template declaration!");
        }
    }
}
