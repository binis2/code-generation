package net.binis.codegen.generation.core;

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
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.*;
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.compiler.CGMethodSymbol;
import net.binis.codegen.compiler.utils.ElementAnnotationUtils;
import net.binis.codegen.compiler.utils.ElementMethodUtils;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.constructor.AllArgsConstructorEnricher;
import net.binis.codegen.enrich.constructor.NotInitializedArgsConstructorEnricher;
import net.binis.codegen.enrich.constructor.RequiredArgsConstructorEnricher;
import net.binis.codegen.enrich.field.GetterEnricher;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.objects.Pair;
import net.binis.codegen.options.CodeOption;
import net.binis.codegen.tools.Holder;
import net.binis.codegen.tools.Tools;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static com.github.javaparser.StaticJavaParser.parseName;
import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.CompiledPrototypesHandler.handleCompiledEnumPrototype;
import static net.binis.codegen.generation.core.CompiledPrototypesHandler.handleCompiledPrototype;
import static net.binis.codegen.generation.core.EnrichHelpers.*;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.generation.core.Structures.VALUE;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Reflection.loadNestedClass;
import static net.binis.codegen.tools.Tools.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@SuppressWarnings("unchecked")
public class Generator {

    public static final String MIX_IN_EXTENSION = "$MixIn";

    private static final List<Pair<PrototypeData, PrototypeDescription<ClassOrInterfaceDeclaration>>> notProcessed = new ArrayList<>();

    private Generator() {
        //Do nothing
    }

    public static void generateCodeForClass(CompilationUnit parser) {
        generateCodeForClass(parser, null);
    }

    public static void generateCodeForClass(CompilationUnit parser, PrototypeDescription<ClassOrInterfaceDeclaration> prsd) {
        var processed = Holder.of(0);

        for (var type : parser.getTypes()) {
            if (type.isClassOrInterfaceDeclaration()) {
                getCodeAnnotations(type).ifPresent(prototypes -> {
                    if (type.asClassOrInterfaceDeclaration().isInterface()) {
                        generateCodeForPrototype(prsd, type, prototypes);
                        processed.set(processed.get() + 1);
                    } else if (processForClass(parser, prsd, type, prototypes)) {
                        processed.set(processed.get() + 1);
                    } else {
                        prototypes.forEach(prototype ->
                                with(ErrorHelpers.calculatePrototypeAnnotationError(type.asClassOrInterfaceDeclaration(), prototype.getValue()), message ->
                                        lookup.error(message, prsd.findElement(type.getNameAsString(), ElementKind.CLASS, ElementKind.INTERFACE))));
                    }
                });
            } else if (type.isEnumDeclaration()) {
                getCodeAnnotations(type).ifPresent(prototypes -> {
                    generateCodeForEnum(parser, prsd, type, prototypes);
                    processed.set(processed.get() + 1);
                });
            }
        }

        processElementsForAnnotations(prsd);

        if (!notProcessed.isEmpty()) {
            var i = notProcessed.iterator();
            while (i.hasNext()) {
                var item = i.next();
                if (item.getValue().isProcessed()) {
                    var parse = (Structures.Parsed<ClassOrInterfaceDeclaration>) lookup.findParsed(item.getKey().getPrototypeFullName());
                    parse.getDeclaration().asClassOrInterfaceDeclaration().getExtendedTypes().stream().filter(t -> t.getNameAsString().equals(item.getValue().getDeclaration().asClassOrInterfaceDeclaration().getNameAsString())).forEach(t ->
                            handleParsedExtendedType(parse, item.getValue(), parse.getImplementation(), parse.getInterface(), parse.getProperties(), t, false));
                    i.remove();
                }
            }
        }

        if (nonNull(prsd) && processed.get() == 0) {
            ((Structures.Parsed) prsd).setInvalid(true);
        }
    }

    private static void processElementsForAnnotations(PrototypeDescription<ClassOrInterfaceDeclaration> prsd) {
        processNodeForAnnotations(prsd, prsd.getDeclarationUnit());
        generateCodeForElements(prsd);
    }

    private static void processNodeForAnnotations(PrototypeDescription<ClassOrInterfaceDeclaration> prsd, CompilationUnit declarationUnit) {
        declarationUnit.findAll(AnnotationExpr.class).stream()
                .filter(a -> Structures.defaultProperties.containsKey(getExternalClassName(declarationUnit, a.getNameAsString())))
                .forEach(a ->
                        a.getParentNode().ifPresent(parent -> {
                            var name = getElementName(parent);
                            if (prsd.getElements().containsKey(name)) {
                                prsd.getElements().get(name).add(Structures.ParsedElementDescription.builder()
                                        .node(parent)
                                        .element(findElement(parent, prsd))
                                        .prototype(a)
                                        .properties(getProperties(a))
                                        .description(prsd)
                                        .build());
                            } else {
                                prsd.getElements().put(name,
                                        new ArrayList<>(Collections.singletonList(Structures.ParsedElementDescription.builder()
                                                .node(parent)
                                                .element(findElement(parent, prsd))
                                                .prototype(a)
                                                .properties(getProperties(a))
                                                .description(prsd)
                                                .build())));
                            }
                        }));
        with(prsd.getElement(), element -> {
            if (!GenerationStrategy.NONE.equals(prsd.getProperties().getStrategy())) {
                ElementAnnotationUtils.addOrReplaceAnnotation(element, lombok.Generated.class, Map.of());
            }
        });

    }

    protected static String getElementName(Node node) {
        var name = getNodeName(node);
        if (node instanceof MethodDeclaration method) {
            name = "method." + name;
            for (var param : method.getParameters()) {
                name += "." + (param.getType().isClassOrInterfaceType() ? param.getType().asClassOrInterfaceType().getName() : param.getTypeAsString());
            }
        } else if (name.isEmpty() && node instanceof FieldDeclaration field) {
            name = "field." + field.getVariables().get(0).getNameAsString();
        } else if (node instanceof Parameter) {
            name = "param." + name;
        } else if (node instanceof ConstructorDeclaration) {
            name = "<init>";
        }
        var parent = node.getParentNode();
        if (parent.isPresent() && !(parent.get() instanceof CompilationUnit)) {
            return getElementName(parent.get()) + "." + name;
        }
        return name;
    }

    protected static String getElementName(Element element) {
        var name = element.getSimpleName().toString();
        switch (element.getKind()) {
            case FIELD -> name = "field." + name;
            case PARAMETER -> name = "param." + name;
            case METHOD -> {
                name = "method." + name;
                for (var param : new CGMethodSymbol(element).params()) {
                    name += "." + param.getVariableSimpleType();
                }
            }
        }
        if (nonNull(element.getEnclosingElement()) && !ElementKind.PACKAGE.equals(element.getEnclosingElement().getKind())) {
            return getElementName(element.getEnclosingElement()) + "." + name;
        }
        return name;
    }

    private static Element findElement(Node node, PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        if (nonNull(parsed.getRawElements())) {
            var name = getElementName(node);
            return parsed.getRawElements().stream()
                    .map(Parsables.Entry.Bag::getElement)
                    .filter(e -> name.equals(getElementName(e)))
                    .findFirst()
                    .orElseGet(() -> deepFindElement(node, parsed));
        } else {
            return deepFindElement(node, parsed);
        }
    }

    private static boolean processForClass(CompilationUnit parser, PrototypeDescription<ClassOrInterfaceDeclaration> prsd, TypeDeclaration<?> type, List<Pair<AnnotationExpr, Structures.PrototypeDataHandler>> prototypes) {
        var typeDeclaration = type.asClassOrInterfaceDeclaration();

        var result = false;
        for (var prototype : prototypes) {

            var properties = prototype.getValue();

            if (GenerationStrategy.NONE.equals(properties.getStrategy())) {
                log.info("Processing - {} (@{})", typeDeclaration.getNameAsString(), prototype.getKey().getNameAsString());
                handleEnrichersSetup(properties);
                handleNoneStrategy(prsd, type, typeDeclaration, properties);
                result = true;
            }

            checkForNestedPrototypes(typeDeclaration);
        }

        return result;
    }

    public static void generateCodeForPrototype(PrototypeDescription<ClassOrInterfaceDeclaration> prsd, TypeDeclaration<?> type, List<Pair<AnnotationExpr, Structures.PrototypeDataHandler>> prototypes) {

        var typeDeclaration = type.asClassOrInterfaceDeclaration();

        log.info("Processing - {}", typeDeclaration.getNameAsString());

        var properties = nonNull(prsd) && nonNull(prsd.getCompiled()) ? (Structures.PrototypeDataHandler) prsd.getProperties() : prototypes.get(0).getValue();
        properties.setPrototypeName(typeDeclaration.getNameAsString());
        properties.setPrototypeFullName(typeDeclaration.getFullyQualifiedName().orElseThrow());
        addProcessingType(typeDeclaration.getNameAsString(), properties.getInterfacePackage(), properties.getInterfaceName(), properties.getClassPackage(), properties.getClassName());
        ((Structures.Parsed) prsd).setProperties(properties);
        ensureParsedParents(typeDeclaration, properties);
        handleEnrichersSetup(properties);

        var parse = switch (properties.getStrategy()) {
            case PROTOTYPE -> handlePrototypeStrategy(prsd, type, typeDeclaration, properties);
            case IMPLEMENTATION -> handleImplementationStrategy(prsd, type, typeDeclaration, properties);
            case PLAIN -> handlePlainStrategy(prsd, type, typeDeclaration, properties);
            case NONE -> handleNoneStrategy(prsd, type, typeDeclaration, properties);
        };

        processingTypes.remove(typeDeclaration.getNameAsString());
        parse.setProcessed(true);
    }

    private static Structures.Parsed handlePrototypeStrategy(PrototypeDescription<ClassOrInterfaceDeclaration> prsd, TypeDeclaration<?> type, ClassOrInterfaceDeclaration typeDeclaration, Structures.PrototypeDataHandler properties) {
        if (properties.getInterfaceFullName().equals(properties.getPrototypeFullName())) {
            lookup.error("Either rename class to '" + properties.getInterfaceName() + "Prototype' or move it to a '*.prototypes.*' package!", prsd.getPrototypeElement());
        }

        var unit = new CompilationUnit();
        unit.addImport("javax.annotation.processing.Generated");
        var spec = unit.addClass(properties.getClassName());
        unit.setPackageDeclaration(isNull(prsd.getParentPackage()) || properties.isClassPackageSet() ? properties.getClassPackage() : prsd.getParentPackage());
        spec.addModifier(PUBLIC);

        if (properties.isGenerateConstructor()) {
            spec.addConstructor(PUBLIC);
        }

        var iUnit = new CompilationUnit();
        iUnit.addImport("javax.annotation.processing.Generated");
        var intf = iUnit.addClass(properties.getInterfaceName()).setInterface(true);
        iUnit.setPackageDeclaration(calcInterfacePackage(prsd));
        intf.addModifier(PUBLIC);

        var parse = (Structures.Parsed) lookup.findParsed(getClassName(typeDeclaration));

        parse.setProperties(properties);
        parse.setImplementation(spec);
        parse.setInterface(intf);

        if (isNull(prsd) || !prsd.isNested() || isNull(prsd.getParentClassName())) {
            spec.addAnnotation(annotation("@Generated(value=\"" + properties.getPrototypeFullName() + "\", comments=\"" + properties.getInterfaceName() + "\")"));
            intf.addAnnotation(annotation("@Generated(value=\"" + properties.getPrototypeFullName() + "\", comments=\"" + properties.getClassName() + "\")"));
        }

        adjustNestedPrototypes(parse);

        typeDeclaration.getExtendedTypes().forEach(t -> {
            var parsed = getParsed(t);

            if (nonNull(parsed) && parsed.isProcessed() && parsed.getProperties().isBase()) {
                properties.setBaseClassName(parsed.getParsedName());
                if (isNull(parse.getBase())) {
                    parse.setBase((Structures.Parsed) parsed);
                } else {
                    throw new GenericCodeGenException(parse.getDeclaration().getNameAsString() + " can't have more that one base class!");
                }
                unit.addImport(parsed.getParsedFullName());
                spec.addExtendedType(parsed.getParsedName());
                var eType = spec.getExtendedTypes().getLast().get();
                t.getTypeArguments().ifPresent(args -> args.forEach(tt -> {
                    if (eType.getTypeArguments().isEmpty()) {
                        eType.setTypeArguments(new NodeList<>());
                    }
                    var arg = handleType(parse.getDeclaration().findCompilationUnit().get(), spec.findCompilationUnit().get(), tt);
                    eType.getTypeArguments().get().add(parsed.getParser().parseClassOrInterfaceType(arg).getResult().get());
                }));

                if (parsed.getProperties().isGenerateConstructor() && properties.isGenerateConstructor()) {
                    spec.findFirst(ConstructorDeclaration.class).ifPresent(c ->
                            c.getBody().addStatement("super();")
                    );
                }
            }
        });

        handleClassGenerics(parse);

        typeDeclaration.getExtendedTypes().forEach(t -> {
            var parsed = getParsed(t);

            if (nonNull(parsed)) {
                if (parsed.isProcessed()) {
                    handleParsedExtendedType(parse, parsed, spec, intf, properties, t, false);
                }
            } else {
                handleExternalInterface(parse, typeDeclaration, spec, intf, t, false);
            }
        });

        if (nonNull(properties.getMixInClass()) && isNull(parse.getMixIn())) {
            lookup.error("Mix in Class " + properties.getPrototypeName() + " must inherit " + properties.getMixInClass(), prsd.getPrototypeElement());
        }

        if (properties.isGenerateInterface()) {
            var im = spec.addImplementedType(properties.getInterfaceName()).getImplementedTypes().getLast().get();
            unit.addImport(getClassName(intf));
            spec.getTypeParameters().forEach(t -> {
                if (im.getTypeArguments().isEmpty()) {
                    im.setTypeArguments(new NodeList<>());
                }
                im.getTypeArguments().get().add(t.clone().setTypeBound(new NodeList<>()));
            });
        }

        var defaultMethods = new ArrayList<MethodDeclaration>();
        for (var member : type.getMembers()) {
            if (member.isMethodDeclaration()) {
                var declaration = member.asMethodDeclaration();

                if (!declaration.isDefault()) {
                    var ignore = getIgnores(member);
                    PrototypeField field = Structures.FieldData.builder().parsed(parse).ignores(ignore).build();
                    if (!ignore.isForField()) {
                        field = addField(parse, typeDeclaration, spec, declaration, null, null);
                    }
                    if (!ignore.isForClass()) {
                        if (properties.isClassGetters()) {
                            addGetter(typeDeclaration, spec, declaration, true, field, false);
                        }
                        if (properties.isClassSetters()) {
                            addSetter(typeDeclaration, spec, declaration, true, field, false);
                        }
                    }
                    if (!ignore.isForInterface()) {
                        addGetter(typeDeclaration, intf, declaration, false, field, false);
                        if (properties.isInterfaceSetters()) {
                            addSetter(typeDeclaration, intf, declaration, false, field, false);
                        }
                    }
                } else {
                    defaultMethods.add(declaration);
                }
            } else if (member.isClassOrInterfaceDeclaration()) {
                processInnerClass(parse, typeDeclaration, spec, member.asClassOrInterfaceDeclaration());
            } else if (member.isFieldDeclaration()) {
                processConstant(parse, typeDeclaration, spec, intf, member.asFieldDeclaration());
            } else if (member.isEnumDeclaration()) {
                //Do nothing?
            } else {
                log.error("Can't process method " + member);
            }
        }

        defaultMethods.forEach(declaration -> handleDefaultMethod(parse, spec, intf, declaration));

        unit.setComment(new BlockComment("Generated code by Binis' code generator."));
        iUnit.setComment(new BlockComment("Generated code by Binis' code generator."));

        lookup.registerGenerated(properties.getPrototypeFullName(), parse);

        cleanUpInterface(typeDeclaration, intf);
        handleClassAnnotations(typeDeclaration, spec, intf);
        checkForDeclaredConstants(spec);
        checkForDeclaredConstants(intf);
        checkForClassExpressions(spec, typeDeclaration);
        checkForClassExpressions(intf, typeDeclaration);
        handleInitializations(parse);

        handleMixin(parse);
        mergeNestedPrototypes(parse);

        handleImports(typeDeclaration, spec);
        handleImports(typeDeclaration, intf);
        return parse;
    }

    protected static String calcInterfacePackage(PrototypeDescription<ClassOrInterfaceDeclaration> prsd) {
        if (nonNull(prsd.getParent())) {
            var parent = lookup.findParsed(prsd.getParentClassName());
            return calcInterfacePackage(parent) + "." + parent.getProperties().getInterfaceName();
        }
        return isNull(prsd.getParentPackage()) ? prsd.getProperties().getInterfacePackage() : prsd.getParentPackage();
    }

    private static Structures.Parsed handleImplementationStrategy(PrototypeDescription<ClassOrInterfaceDeclaration> prsd, TypeDeclaration<?> type, ClassOrInterfaceDeclaration typeDeclaration, Structures.PrototypeDataHandler properties) {
        var unit = new CompilationUnit();
        unit.addImport("javax.annotation.processing.Generated");
        var spec = unit.addClass(properties.getClassName());
        unit.setPackageDeclaration(isNull(prsd.getParentPackage()) ? properties.getClassPackage() : prsd.getParentPackage());
        spec.addModifier(PUBLIC);
        spec.addImplementedType(prsd.getDeclaration().getNameAsString());
        prsd.getDeclaration().getFullyQualifiedName().ifPresent(unit::addImport);

        if (properties.isGenerateConstructor()) {
            spec.addConstructor(PUBLIC);
        }

        var parse = (Structures.Parsed) lookup.findParsed(getClassName(typeDeclaration));

        parse.setInterfaceName(type.getNameAsString());
        parse.setInterfaceFullName(type.getFullyQualifiedName().get());
        parse.setProperties(properties);
        parse.setImplementation(spec);

        if (!prsd.isNested() || isNull(prsd.getParentClassName())) {
            spec.addAnnotation(annotation("@Generated(value=\"" + properties.getPrototypeFullName() + "\", comments=\"" + properties.getInterfaceName() + "\")"));
        }

        handleClassGenerics(parse);

        for (var member : type.getMembers()) {
            if (member.isMethodDeclaration()) {
                var method = member.asMethodDeclaration().clone().setModifiers(PUBLIC);
                spec.addMember(method.setBody(getDefaultReturnBody(method.getType())));
            }
        }

        typeDeclaration.getExtendedTypes().forEach(t -> {
            var parsed = getParsed(t);

            if (nonNull(parsed)) {
                if (parsed.isProcessed()) {
                    //handleParsedExtendedType(parse, parsed, spec, intf, properties, t);
                }
            } else {
                handleExternalInterface(parse, typeDeclaration, spec, null, t, false);
            }
        });

        unit.setComment(new BlockComment("Generated code by Binis' code generator."));

        lookup.registerGenerated(properties.getPrototypeFullName(), parse);

        checkForDeclaredConstants(spec);
        checkForClassExpressions(spec, typeDeclaration);
        mergeNestedPrototypes(parse);

        handleImports(typeDeclaration, spec);
        return parse;
    }

    private static Structures.Parsed handlePlainStrategy(PrototypeDescription<ClassOrInterfaceDeclaration> prsd, TypeDeclaration<?> type, ClassOrInterfaceDeclaration typeDeclaration, Structures.PrototypeDataHandler properties) {
        //TODO: Generate empty interface and implementation
        throw new NotImplementedException();
    }

    private static Structures.Parsed handleNoneStrategy(PrototypeDescription<ClassOrInterfaceDeclaration> prsd, TypeDeclaration<?> type, ClassOrInterfaceDeclaration typeDeclaration, Structures.PrototypeDataHandler properties) {
        var parse = (Structures.Parsed) prsd;

        parse.addProperties(properties);
        return parse;
    }

    private static BlockStmt getDefaultReturnBody(Type type) {
        if (type.isVoidType()) {
            return new BlockStmt();
        } else if (type.isPrimitiveType()) {
            var result = switch (type.asString()) {
                case "boolean" -> "false";
                case "long" -> "0L";
                case "float" -> "0.0f";
                case "double" -> "0.0";
                default -> "0";
            };
            return returnBlock(result);
        } else {
            return returnBlock("null");
        }
    }

    private static void handleParsedExtendedType(Structures.Parsed<ClassOrInterfaceDeclaration> parse, PrototypeDescription<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, PrototypeData properties, ClassOrInterfaceType type, boolean alreadyAdded) {
        if (!parsed.getProperties().isBase() && !parsed.getProperties().getPrototypeName().equals(parse.getProperties().getMixInClass())) {
            parsed.getFields().forEach(field -> {
                var method = field.getDescription().clone();
                var dummy = envelopWithDummyClass(method, field.getDescription());
                field.getDescription().findCompilationUnit().ifPresent(u -> u.getImports().forEach(dummy::addImport));

                addField(parse, parsed.getDeclaration().asClassOrInterfaceDeclaration(), spec, method, nonNull(field.getGenerics()) ? field.getGenerics().values().iterator().next() : buildGeneric(field.getType().asString(), type, parsed.getDeclaration().asClassOrInterfaceDeclaration()), field);
            });
        }

        if (isNull(properties.getMixInClass()) || !properties.getMixInClass().equals(type.toString())) {
            if (!parsed.getProperties().isBase()) {
                implementPrototype(parse, spec, parsed, buildGenerics(type, parsed.getDeclaration().asClassOrInterfaceDeclaration()), false);
            }
            if (StringUtils.isNotBlank(parsed.getInterfaceName())) {
                intf.findCompilationUnit().ifPresent(u -> u.addImport(parsed.getInterfaceFullName()));
                if (nonNull(properties.getMixInClass())) {
                    intf.addExtendedType(parsed.getInterfaceName() + MIX_IN_EXTENSION);
                } else {
                    intf.addExtendedType(parsed.getInterfaceName());
                    var eType = intf.getExtendedTypes().getLast().get();
                    type.getTypeArguments().ifPresent(args -> args.forEach(tt -> {
                        if (eType.getTypeArguments().isEmpty()) {
                            eType.setTypeArguments(new NodeList<>());
                        }
                        var arg = handleType(parse.getDeclaration().findCompilationUnit().get(), intf.findCompilationUnit().get(), tt);
                        eType.getTypeArguments().get().add(parsed.getParser().parseClassOrInterfaceType(arg).getResult().get());
                    }));

                }
            }
        } else {
            parse.setMixIn((Structures.Parsed) parsed);
        }
    }

    private static void adjustNestedPrototypes(Structures.Parsed<ClassOrInterfaceDeclaration> parse) {
        lookup.parsed().stream().filter(p -> p.isNested() && nonNull(p.getParentClassName()) && p.getParentClassName().equals(parse.getPrototypeClassName())).map(Structures.Parsed.class::cast).sorted((o1, o2) -> Boolean.compare(o1.isMixIn(), o2.isMixIn())).forEach(p -> {
            p.setInterfaceName(parse.getInterfaceName() + '.' + p.getInterfaceName());
            p.getProperties().setInterfacePackage(parse.getProperties().getInterfacePackage() + '.' + parse.getInterfaceName());
            p.setInterfaceFullName(parse.getProperties().getInterfacePackage() + '.' + p.getInterfaceName());
            p.setParsedName(parse.getParsedName() + '.' + p.getParsedName());
            p.getProperties().setClassPackage(parse.getProperties().getClassPackage() + '.' + parse.getParsedName());
            p.setParsedFullName(parse.getProperties().getClassPackage() + '.' + p.getParsedName());
            if (p.isCodeEnum()) {
                p.getInterface().getAnnotationByName("Default").ifPresent(a -> {
                    if (p.isMixIn()) {
                        a.asSingleMemberAnnotationExpr().setMemberValue(new StringLiteralExpr(parse.getProperties().getClassPackage() + "." + p.getMixIn().getParsedName().replace(".", "$")));
                    } else {
                        a.asSingleMemberAnnotationExpr().setMemberValue(new StringLiteralExpr(parse.getProperties().getClassPackage() + "." + p.getParsedName().replace(".", "$")));
                    }
                });
            }
        });
    }

    private static void mergeNestedPrototypes(Structures.Parsed<ClassOrInterfaceDeclaration> parse) {
        lookup.parsed().stream().filter(p -> p.isNested() && nonNull(p.getParentClassName()) && p.getParentClassName().equals(parse.getPrototypeClassName())).forEach(p -> {
            parse.getImplementation().addMember(p.getImplementation().addModifier(STATIC));
            mergeImports(p.getImplementationUnit(), parse.getImplementation().findCompilationUnit().get());
            parse.getInterface().addMember(p.getInterface());
            mergeImports(p.getInterfaceUnit(), parse.getInterface().findCompilationUnit().get());
        });
    }

    private static void handleClassGenerics(Structures.Parsed<ClassOrInterfaceDeclaration> parse) {
        var unit = parse.getDeclaration().findCompilationUnit().get();
        parse.getDeclaration().asClassOrInterfaceDeclaration().getTypeParameters().forEach(t -> {
            parse.getImplementation().addTypeParameter(t);
            with(parse.getInterface(), intf -> intf.addTypeParameter(t));
            t.getTypeBound().forEach(tb -> {
                handleType(unit, parse.getImplementation().findCompilationUnit().get(), tb);
                with(parse.getInterface(), intf -> handleType(unit, intf.findCompilationUnit().get(), tb));
            });
        });
    }

    private static void handleInitializations(Structures.Parsed<ClassOrInterfaceDeclaration> parse) {
        var list = parse.getDeclaration().getAnnotations().stream().filter(a -> "Initialize".equals(a.getNameAsString())).toList();
        var constructor = parse.getImplementation().getDefaultConstructor().orElseGet(
                () -> parse.getImplementation().addConstructor(PUBLIC).setBody(nonNull(parse.getBase()) ? new BlockStmt().addStatement("super();") : new BlockStmt()));
        var unit = parse.getImplementation().findCompilationUnit().get();
        list.forEach(ann -> {
            var statement = Holder.of("");
            ann.getChildNodes().stream().filter(MemberValuePair.class::isInstance).map(MemberValuePair.class::cast).forEach(pair -> {
                switch (pair.getNameAsString()) {
                    case "field" -> statement.set("this." + pair.getValue().asStringLiteralExpr().asString() + " = " + statement.get());
                    case "expression" -> statement.set(statement.get() + pair.getValue().asStringLiteralExpr().asString() + ";");
                    case "imports" -> pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).forEach(i -> unit.addImport(i.asString()));
                    default -> log.warn("Invalid @Initialize member {}", pair.getNameAsString());
                }
            });
            constructor.getBody().addStatement(statement.get());
        });
    }

    public static Optional<List<Pair<AnnotationExpr, Structures.PrototypeDataHandler>>> getCodeAnnotations(BodyDeclaration<?> type) {
        var list = new ArrayList<Pair<AnnotationExpr, Structures.PrototypeDataHandler>>();
        for (var name : Structures.defaultProperties.keySet()) {
            Helpers.getAnnotationByFullName(type, name).ifPresent(ann -> list.add(Pair.of(ann, getProperties(ann))));
        }

        list.sort(Comparator.comparing(a -> a.getValue().getStrategy()));

        if (list.size() > 1) {
            var i = 1;
            while (i < list.size()) {
                if (!GenerationStrategy.NONE.equals(list.get(i).getValue().getStrategy())) {
                    log.warn("Multiple prototype annotations found ({})", list.get(i).getKey().getNameAsString());
                }
                i++;
            }
        }

        return list.isEmpty() ? Optional.empty() : Optional.of(list);
    }

    public static Optional<PrototypeData> getCodeAnnotationProperties(BodyDeclaration<?> type) {
        for (var name : Structures.defaultProperties.keySet()) {
            var ann = Helpers.getAnnotationByFullName(type, name);
            if (ann.isPresent()) {
                return Optional.of(Structures.defaultProperties.get(name).get().build());
            }
        }
        return Optional.empty();
    }


    public static Optional<Annotation> getCodeAnnotations(Class cls) {
        for (var name : Structures.defaultProperties.keySet()) {
            var aCls = loadClass(name);
            if (nonNull(aCls)) {
                var ann = cls.getAnnotation(aCls);
                if (nonNull(ann)) {
                    return Optional.of(ann);
                }
            }
        }
        return Optional.empty();
    }


    private static void cleanUpInterface(Class<?> cls, ClassOrInterfaceDeclaration intf) {
        var toRemove = new ArrayList<MethodDeclaration>();

        Arrays.stream(cls.getInterfaces()).forEach(i -> cleanUpInterface(i, intf));
        Arrays.stream(cls.getDeclaredMethods()).forEach(mtd ->
                intf.getMethods().stream().filter(m ->
                        m.getNameAsString().equals(mtd.getName()) &&
                                m.getParameters().size() == mtd.getParameterCount()).forEach(toRemove::add));
        //TODO: check parameter types as well

        toRemove.forEach(intf::remove);
    }

    private static void cleanUpInterface(ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration intf) {
        declaration.findCompilationUnit().ifPresent(unit ->
                intf.getExtendedTypes().forEach(t ->
                        Tools.with(getExternalClassName(unit, t.getNameAsString()), className ->
                                Tools.with(loadClass(className), cls ->
                                        cleanUpInterface(cls, intf)))));
    }

    private static void handleDefaultMethod(Structures.Parsed<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, MethodDeclaration declaration) {
        var unit = declaration.findCompilationUnit().get();
        var ignores = getIgnores(declaration);
        var method = declaration.clone().removeModifier(DEFAULT);
        envelopWithDummyClass(method, declaration);
        method.getAnnotationByClass(Ignore.class).ifPresent(method::remove);
        if (!ignores.isForInterface() && !method.getNameAsString().equals("_equals") && !method.getNameAsString().equals("_hashCode") ) {
            if (ignores.isForClass()) {
                declaration.getBody().ifPresent(b -> {
                    var body = b.clone();
                    handleDefaultInterfaceMethodBody(parse, body, false, declaration);
                    method.setBody(body).addModifier(DEFAULT);
                    intf.addMember(handleForAnnotations(unit, method, false));
                    handleCodeImplementationInjection(parse.getPrototypeElement(), method, declaration);
                });
            } else {
                if (methodExists(intf, method, false)) {
                    intf.getChildNodes().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).filter(m -> m.getNameAsString().equals(method.getNameAsString())).findFirst().ifPresent(intf::remove);
                }

                var m = method.clone().setBody(null);
                intf.addMember(handleForAnnotations(unit, m, false));
                m.setType(handleType(declaration, intf, method.getType()));
                m.getParameters().forEach(param -> param.setType(handleType(declaration, intf, param.getType())));
            }
        }

        if (!ignores.isForClass()) {
            var name = method.getNameAsString();
            if (name.equals("_equals") || name.equals("_hashCode")) {
                method.setName(name.substring(1, name.length()));
            }
            method.addModifier(PUBLIC);

            method.setType(handleType(declaration, spec, method.getType()));
            method.getParameters().forEach(param -> param.setType(handleType(declaration, method, param.getType())));

            var ann = declaration.getAnnotationByClass(CodeImplementation.class);
            if (ann.isPresent()) {
                var code = "";
                if (ann.get() instanceof SingleMemberAnnotationExpr a) {
                    code = getStringValue(a.getMemberValue());
                } else if (ann.get() instanceof NormalAnnotationExpr a) {
                    for (var p : a.getPairs()) {
                        switch (p.getNameAsString()) {
                            case "value" -> {
                                code = getStringValue(p.getValue());
                            }
                            case "imports" -> p.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).forEach(i -> unit.addImport(i.asString()));
                        }
                    }
                }
                method.setBody(block(calcBlock(code)));
            } else {
                declaration.getBody().ifPresent(b -> {
                    var body = b.clone();
                    handleDefaultMethodBody(parse, body, false, declaration);
                    method.setBody(body);
                    handleCodeImplementationInjection(parse.getPrototypeElement(), method, declaration);
                });
            }

            if (methodExists(spec, method, false)) {
                spec.getChildNodes().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).filter(m -> m.getNameAsString().equals(method.getNameAsString())).findFirst().ifPresent(spec::remove);
            }

            spec.addMember(handleForAnnotations(unit, method, true));
        }
    }

    protected static String getStringValue(Expression p) {
        if (p instanceof StringLiteralExpr exp) {
            return exp.asString();
        } else {
            log.warn("Only string literals are supported as values for @CodeImplementation");
        }
        return null;
    }

    protected static void handleCodeImplementationInjection(Element element, MethodDeclaration method, MethodDeclaration original) {
        if (nonNull(element) && !method.isAnnotationPresent(CodeImplementation.class)) {
            element.getEnclosedElements().stream()
                    .filter(e -> ElementKind.METHOD.equals(e.getKind()))
                    .filter(e -> e.getSimpleName().toString().equals(method.getNameAsString()))
                    .filter(e ->
                            ElementMethodUtils.paramsMatch(e, method.getParameters().stream().map(p ->
                                    getExternalClassName(method, p.getType().asString())).toList()))
                    .findFirst()
                    .ifPresent(e ->
                            //TODO: Handle imports
                            ElementAnnotationUtils.addAnnotation(e, CodeImplementation.class, Map.of("value", withRes(method.getBody().get().toString(), s -> s.substring(1, s.length() - 1)).trim())));
        }
    }

    protected static MethodDeclaration handleForAnnotations(CompilationUnit unit, MethodDeclaration method, boolean isClass) {
        var chk = isClass ? "ForInterface" : "ForImplementation";

        for (var i = method.getAnnotations().size() - 1; i > 0; i--) {
            if (chk.equals(method.getAnnotation(i - 1).getNameAsString())) {
                method.remove(method.getAnnotation(i));
            }
        }

        for (var i = method.getAnnotations().size() - 1; i >= 0; i--) {
            var ann = method.getAnnotation(i);
            Tools.with(getExternalClassName(unit, method.getAnnotation(i).getNameAsString()), className ->
                    Tools.with(loadClass(className), cls -> {
                        if (cls.isAnnotationPresent(CodeAnnotation.class)) {
                            method.remove(ann);
                        }
                    }));
        }

        return method;
    }

    protected static boolean handleDefaultMethodBody(PrototypeDescription<ClassOrInterfaceDeclaration> parse, Node node, boolean isGetter, MethodDeclaration declaration) {
        //TODO: Actual params type checks!
        if (isGetter && node.getParentNode().isPresent() && node.getParentNode().get().getChildNodes().size() == 2 && node.getParentNode().get().getChildNodes().get(1) instanceof SimpleName name) {
            var parent = parse.findField(name.toString());
            if (parent.isEmpty() && nonNull(parse.getBase())) {
                parent = parse.getBase().findField(name.toString());
            }
            if (parent.isPresent()) {
                name.setIdentifier(getGetterName(name.asString(), ""));
            }
        } else {
            for (var i = 0; i < node.getChildNodes().size(); i++) {
                var n = node.getChildNodes().get(i);
                if (n instanceof MethodCallExpr method) {
                    var parent = parse.findField(method.getNameAsString());
                    if (parent.isEmpty() && nonNull(parse.getBase())) {
                        parent = parse.getBase().findField(method.getNameAsString());
                    }

                    if (parent.isPresent()) {
                        Tools.with(parent.get().getPrototype(), p ->
                                handleDefaultMethodBody(p, n, true, declaration));

                        var getter = getGetterName(method.getNameAsString(), parent.get().getType());
                        //TODO: Check for parameter types as well
                        if (getter.equals(declaration.getNameAsString()) && method.getArguments().size() == declaration.getParameters().size() && method.getScope().isEmpty()) {
                            var exp = new FieldAccessExpr().setName(method.getName());
                            if (method.getScope().isPresent()) {
                                exp.setScope(method.getScope().get());
                            }
                            return node.replace(method, exp);
                        } else {
                            method.setName(getter);
                        }
                    }
                } else if (n instanceof MethodReferenceExpr ref) {
                    Tools.with(lookup.findParsed(getExternalClassName(declaration, ref.getScope().toString())), parsed ->
                            parsed.findField(ref.getIdentifier()).ifPresent(field -> {
                                ref.setIdentifier(getGetterName(field.getName(), field.getType()));
                            }));
                } else if (n instanceof ClassOrInterfaceType type) {
                    Tools.with(lookup.findParsed(getExternalClassName(declaration, type.toString())), parsed -> {
                        type.setName(parsed.getInterfaceName());
                        addImport(type, parsed.getInterfaceFullName());
                    });
                }

                if (handleDefaultMethodBody(parse, n, isGetter, declaration)) {
                    handleDefaultMethodBody(parse, node, isGetter, declaration);
                }
            }
        }
        return false;
    }

    protected static boolean handleDefaultInterfaceMethodBody(PrototypeDescription<ClassOrInterfaceDeclaration> parse, Node node, boolean isGetter, MethodDeclaration declaration) {
        if (isGetter && node.getParentNode().isPresent() && node.getParentNode().get().getChildNodes().size() == 2 && node.getParentNode().get().getChildNodes().get(1) instanceof SimpleName name) {
            var parent = parse.findField(name.toString());
            if (parent.isEmpty() && nonNull(parse.getBase())) {
                parent = parse.getBase().findField(name.toString());
            }
            if (parent.isPresent()) {
                name.setIdentifier(getGetterName(name.asString(), ""));
            }
        } else {
            for (var i = 0; i < node.getChildNodes().size(); i++) {
                var n = node.getChildNodes().get(i);
                if (n instanceof MethodCallExpr method) {
                    var parent = parse.findField(method.getNameAsString());
                    if (parent.isEmpty() && nonNull(parse.getBase())) {
                        parent = parse.getBase().findField(method.getNameAsString());
                    }

                    if (parent.isPresent()) {
                        Tools.with(lookup.findParsed(getExternalClassName(parse.getDeclaration(), parent.get().getType().asString())), p ->
                                handleDefaultInterfaceMethodBody(p, n, true, declaration));

                        if (nonNull(parent.get().getInterfaceGetter())) {
                            method.setName(parent.get().getInterfaceGetter().getName());
                        }

                        return true;
                    } else {
                        if (handleDefaultInterfaceMethodBody(parse, n, false, declaration)) {
                            handleDefaultInterfaceMethodBody(parse, node, false, declaration);
                        }
                    }
                } else {
                    if (handleDefaultInterfaceMethodBody(parse, n, isGetter, declaration)) {
                        handleDefaultInterfaceMethodBody(parse, node, isGetter, declaration);
                    }
                }
            }
        }
        return false;
    }


    private static void checkForDeclaredConstants(Node type) {
        //TODO: Handle more cases
        for (var node : type.getChildNodes()) {
            if (node instanceof FieldAccessExpr expr) {
                if (expr.getChildNodes().size() > 1 && expr.getChildNodes().get(0) instanceof NameExpr namespace && expr.getChildNodes().get(1) instanceof SimpleName name) {
                    var decl = declaredConstants.get(namespace.getNameAsString());
                    if (nonNull(decl)) {
                        decl.stream().filter(p -> p.getValue().equals(name.asString())).findFirst().ifPresent(
                                c -> namespace.setName(c.getKey())
                        );
                    }
                }
            }
            checkForDeclaredConstants(node);
        }
    }

    private static void checkForClassExpressions(Node type, ClassOrInterfaceDeclaration declaration) {
        declaration.findCompilationUnit().ifPresent(unit -> {
            for (var node : type.getChildNodes()) {
                if (node instanceof ClassExpr expr) {
                    Tools.with(lookup.findParsed(getExternalClassName(unit, expr.getTypeAsString())), p -> {
                        if (isNull(p.getMixIn())) {
                            expr.setType(findProperType(p, unit, expr));
                        } else {
                            expr.setType(findProperType(p.getMixIn(), unit, expr));
                        }
                    });
                }
                checkForClassExpressions(node, declaration);
            }
        });
    }

    private static void processConstant(Structures.Parsed parse, ClassOrInterfaceDeclaration prototype, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, FieldDeclaration field) {
        var consts = getConstants(field);
        var f = field.clone();
        var name = field.getVariable(0).getNameAsString();
        var data = Structures.ConstantData.builder().name(name).field(f);
        f.getAnnotationByName("CodeConstant").ifPresent(f::remove);
        if (consts.isForInterface()) {
            intf.addMember(f);
            addDeclaredConstant(prototype.getNameAsString(), intf.getNameAsString(), name);
            data.destination(intf);
        } else {
            spec.addMember(f.addModifier(consts.isForPublic() ? PUBLIC : "serialVersionUID".equals(name) ? PRIVATE : PROTECTED).addModifier(STATIC).addModifier(FINAL));
            addDeclaredConstant(prototype.getNameAsString(), spec.getNameAsString(), name);
            data.destination(spec);
        }
        parse.getConstants().put(name, data.build());
    }

    public static Structures.PrototypeDataHandler getProperties(AnnotationExpr prototype) {
        var prototypeClass = getExternalClassName(prototype, prototype.getNameAsString());
        var builder = Structures.builder(prototypeClass);

        var iName = Holder.of("");
        var cName = "";
        if (prototype.getParentNode().get() instanceof BodyDeclaration<?> body && body instanceof ClassOrInterfaceDeclaration type) {
            iName.set(defaultInterfaceName(type));
            cName = defaultClassName(type);
        }

        builder.prototypeAnnotationExpression(prototype);
        nullCheck(loadClass(prototypeClass), cls -> builder.prototypeAnnotation((Class) cls));

        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair pair) {
                var name = pair.getNameAsString();
                switch (name) {
                    case "name":
                        if (pair.getValue().isStringLiteralExpr()) {
                            var value = pair.getValue().asStringLiteralExpr().asString();
                            if (StringUtils.isNotBlank(value)) {
                                var intf = value.replace("Entity", "");
                                builder.name(value)
                                        .className(value)
                                        .interfaceName(intf)
                                        .longModifierName(intf + "." + Constants.MODIFIER_INTERFACE_NAME);
                            }
                        } else {
                            builder.custom("_name", pair.getValue());
                        }
                        break;
                    case "generateConstructor":
                        builder.generateConstructor(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "generateImplementation":
                        builder.generateImplementation(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "generateInterface":
                        builder.generateInterface(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "interfaceName":
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            iName.set(pair.getValue().asStringLiteralExpr().asString());
                        }
                        break;
                    case "classGetters":
                        builder.classGetters(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "classSetters":
                        builder.classSetters(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "interfaceSetters":
                        builder.interfaceSetters(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "base":
                        builder.base(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "baseModifierClass":
                        value = pair.getValue().asClassExpr().getTypeAsString();
                        if (StringUtils.isNotBlank(value) && !"void".equals(value)) {
                            var full = Helpers.getExternalClassNameIfExists(pair, value);
                            builder.baseModifierClass(nonNull(full) ? full : value);
                        }
                        break;
                    case "mixInClass":
                        value = pair.getValue().asClassExpr().getTypeAsString();
                        if (StringUtils.isNotBlank(value) && !"void".equals(value)) {
                            builder.mixInClass(value);
                        }
                        break;
                    case "implementationPackage":
                        value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.classPackage(value);
                        }
                        break;
                    case "strategy": {
                        if (pair.getValue().isNameExpr()) {
                            value = pair.getValue().asNameExpr().getNameAsString();
                        } else {
                            value = pair.getValue().asFieldAccessExpr().getNameAsString();
                        }
                        if (StringUtils.isNotBlank(value)) {
                            builder.strategy(GenerationStrategy.valueOf(value));
                        }
                        break;
                    }
                    case "basePath":
                        value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.basePath(value);
                        }
                        break;
                    case "interfacePath":
                        value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.interfacePath(value);
                        }
                        break;
                    case "implementationPath":
                        value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.implementationPath(value);
                        }
                        break;
                    case "enrichers":
                        checkEnrichers(builder::enrichers, handleInitializerAnnotation(pair));
                        break;
                    case "inheritedEnrichers":
                        checkEnrichers(builder::inheritedEnrichers, handleInitializerAnnotation(pair));
                        break;
                    case "options":
                        checkOptions(builder::options, handleInitializerAnnotation(pair));
                        break;
                    default:
                        builder.custom(name, getExpressionValue(pair.getValue()));
                }
            } else if (node instanceof Name) {
                //Continue
            } else {
                builder.custom(VALUE, getExpressionValue(node));
            }
        });

        if (cName.equals(iName.get())) {
            cName = iName.get() + "Impl";
        }

        builder.className(cName).interfaceName(iName.get()).longModifierName(iName.get() + ".Modify");

        var result = builder.build();

        var parent = prototype.getParentNode().get();
        if (isNull(result.getClassPackage()) && parent instanceof ClassOrInterfaceDeclaration type) {
            result.setClassPackage(defaultClassPackage(type));
        }

        if (isNull(result.getInterfacePackage()) && parent instanceof ClassOrInterfaceDeclaration type) {
            result.setInterfacePackage(defaultInterfacePackage(type));
        }

        if (isNull(result.getEnrichers())) {
            result.setEnrichers(new ArrayList<>());
        }

        if (isNull(result.getInheritedEnrichers())) {
            result.setInheritedEnrichers(new ArrayList<>());
        }

        Tools.with(result.getPredefinedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getEnrichers(), e)));

        Tools.with(result.getPredefinedInheritedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getInheritedEnrichers(), e)));

        return result;
    }

    private static ArrayInitializerExpr handleInitializerAnnotation(MemberValuePair pair) {
        var expression = pair.getValue();
        if (expression.isClassExpr()) {
            var expr = new ArrayInitializerExpr();
            expr.getValues().add(expression);
            pair.setValue(expr);
            return expr;
        } else {
            return expression.asArrayInitializerExpr();
        }
    }

    private static void checkEnrichers(Consumer<List<PrototypeEnricher>> consumer, ArrayInitializerExpr expression) {
        var map = new HashMap<Class<? extends Enricher>, PrototypeEnricher>();
        expression.getValues().stream()
                .filter(Expression::isClassExpr)
                .map(e -> {
                    var className = e.asClassExpr().getType().asString();
                    var cls = loadClass(className);
                    if (isNull(cls)) {
                        className = getExternalClassName(expression.findCompilationUnit().get(), className);
                        cls = loadClass(className);
                    }

                    if (isNull(cls)) {
                        if (lookup.isExternal(className)) {
                            if (expression.findAncestor(ClassOrInterfaceDeclaration.class).isPresent()) {
                                lookup.error("Enricher " + className + " is being compiled! It's not usable in the same module as it is defined!", null);
                            }
                        } else {
                            lookup.error("Enricher " + className + " is not found in the classpath!", null);
                        }
                    }

                    return cls;
                })
                .filter(Objects::nonNull)
                .filter(Enricher.class::isAssignableFrom)
                .forEach(enricher -> initEnricher(enricher, map));

        consumer.accept(new ArrayList<>(map.values()));
    }

    private static void initEnricher(Class enricher, Map<Class<? extends Enricher>, PrototypeEnricher> map) {
        Optional.of(enricher)
                .map(CodeFactory::create)
                .filter(Objects::nonNull)
                .filter(i -> PrototypeEnricher.class.isAssignableFrom(i.getClass()))
                .ifPresent(e ->
                        with((PrototypeEnricher) e, enr -> {
                            enr.init(lookup);
                            map.put(enr.getClass(), enr);
                            enr.dependencies().forEach(d -> initEnricher(d, map));
                        }));
    }

    @SuppressWarnings("unchecked")
    private static void checkOptions(Consumer<Set<Class<? extends CodeOption>>> consumer, ArrayInitializerExpr expression) {
        var set = new HashSet<Class<? extends CodeOption>>();
        expression.getValues().stream()
                .filter(Expression::isClassExpr)
                .map(e -> loadClass(getExternalClassName(expression.findCompilationUnit().get(), e.asClassExpr().getType().asString())))
                .filter(Objects::nonNull)
                .filter(CodeOption.class::isAssignableFrom)
                .forEach(o -> set.add((Class) o));
        consumer.accept(set);
    }


    @SuppressWarnings("unchecked")
    public static void checkEnrichers(List<PrototypeEnricher> list, Class enricher) {
        if (list.stream().noneMatch(e -> enricher.isAssignableFrom(e.getClass()))) {
            var e = CodeFactory.create(enricher);
            if (e instanceof PrototypeEnricher en) {
                en.init(lookup);
                list.add(en);
                en.dependencies().forEach(d ->
                        checkEnrichers(list, d));
            }
        }
    }

    private static void processParentType(ClassOrInterfaceDeclaration declaration, PrototypeData properties, Type type, PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        if (nonNull(parsed)) {
            if (!parsed.isProcessed()) {
                generateCodeForClass(parsed.getDeclaration().findCompilationUnit().get(), parsed);
            } else {
                if (!parsed.isProcessed()) {
                    notProcessed.add(Pair.of(properties, parsed));
                }
            }
        } else {
            handleCompiledPrototype(getExternalClassName(declaration.findCompilationUnit().get(), type.asString()));
        }

    }

    private static void ensureParsedParents(ClassOrInterfaceDeclaration declaration, PrototypeData properties) {
        for (var extended : declaration.getExtendedTypes()) {
            extended.getTypeArguments().ifPresent(args ->
                    args.forEach(arg -> {
                        var parsed = lookup.findParsed(getExternalClassName(declaration.findCompilationUnit().get(), arg.asString()));
                        if (isNull(parsed) || !properties.equals(parsed.getProperties())) {
                            processParentType(declaration, properties, new ClassOrInterfaceType(null, arg.asString()), parsed);
                        }
                    }));
            processParentType(declaration, properties, extended, getParsed(extended));
        }

        Tools.with(properties.getMixInClass(), c ->
                Tools.with(getExternalClassName(declaration.findCompilationUnit().get(), c),
                        name -> Tools.with(lookup.findParsed(name), parse ->
                                condition(!parse.isProcessed(), () -> generateCodeForClass(parse.getDeclaration().findCompilationUnit().get(), parse)))));

        checkForNestedPrototypes(declaration);
    }

    protected static void checkForNestedPrototypes(ClassOrInterfaceDeclaration declaration) {
        declaration.getChildNodes().stream().filter(ClassOrInterfaceDeclaration.class::isInstance).map(ClassOrInterfaceDeclaration.class::cast).forEach(cls ->
                Generator.getCodeAnnotations(cls).ifPresent(ann -> {
                    var clsName = getClassName(cls);
                    lookup.registerParsed(clsName,
                            Structures.Parsed.builder()
                                    .declaration(cls.asTypeDeclaration())
                                    .declarationUnit(cls.findCompilationUnit().orElse(null))
                                    .parser(lookup.getParser())
                                    .nested(true)
                                    .parentClassName(getClassName(declaration))
                                    .parent(declaration)
                                    .build());

                    Tools.with(lookup.findParsed(clsName), parse ->
                            condition(!parse.isProcessed(), () -> generateCodeForPrototype(parse, cls, ann)));
                }));

        declaration.getChildNodes().stream().filter(EnumDeclaration.class::isInstance).map(EnumDeclaration.class::cast).forEach(cls ->
                Generator.getCodeAnnotations(cls).ifPresent(ann -> {
                    var clsName = getClassName(cls);
                    lookup.registerParsed(clsName,
                            Structures.Parsed.builder()
                                    .declaration(cls.asTypeDeclaration())
                                    .declarationUnit(cls.findCompilationUnit().orElse(null))
                                    .parser(lookup.getParser())
                                    .nested(true)
                                    .parentClassName(getClassName(declaration))
                                    .parent(declaration)
                                    .codeEnum(true)
                                    .build());

                    Tools.with(lookup.findParsed(clsName), parse ->
                            condition(!parse.isProcessed(), () -> generateCodeForEnum(declaration.findCompilationUnit().get(), parse, cls, ann)));
                }));
    }

    private static PrototypeDescription<?> ensureParsedParents(EnumDeclaration declaration, PrototypeDescription<?> parse) {
        if (nonNull(parse) && isNull(parse.getCompiled()) && !parse.isProcessed()) {
            if (parse.getDeclaration().isEnumDeclaration()) {
                return generateCodeForEnum(parse.getDeclaration().findCompilationUnit().get(), parse, parse.getDeclaration(), getCodeAnnotations(parse.getDeclaration()).orElse(null));
            } else {
                throw new GenericCodeGenException("Class '" + parse.getDeclaration().getFullyQualifiedName().get() + "' is not enum!");
            }
        }
        return parse;
    }

    private static void implementPrototype(Structures.Parsed<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, PrototypeDescription<ClassOrInterfaceDeclaration> declaration, Map<String, Type> generic, boolean external) {
        var properties = parse.getProperties();
        for (var method : declaration.getImplementation().getMethods()) {
            if (declaration.isValid() && declaration.getDeclaration().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).anyMatch(m -> m.getNameAsString().equals(method.getNameAsString()))) {
                spec.addMember(method.clone());
            } else {
                if (method.getNameAsString().startsWith("get") || method.getNameAsString().startsWith("is")) {
                    if (!external || parse.getDeclaration().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).noneMatch(m ->
                            m.isDefault() && m.getNameAsString().equals(method.getNameAsString()) && m.getTypeAsString().equals(method.getTypeAsString()))) {
                        var field = addFieldFromGetter(parse, spec, method, generic, external);
                        if (nonNull(field)) {
                            if (properties.isClassGetters()) {
                                addGetterFromGetter(spec, method, true, generic, field);
                            }
                            if (properties.isClassGetters()) {
                                field.generateSetter();
                            }
                        }
                    }
                } else if (method.getNameAsString().startsWith("set")) {
                    if (!external || parse.getDeclaration().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).noneMatch(m ->
                            m.isDefault() && m.getNameAsString().equals(method.getNameAsString()) && m.getTypeAsString().equals(method.getTypeAsString()))) {
                        var field = addFieldFromSetter(parse, spec, method, generic, external);
                        if (nonNull(field) && properties.isClassSetters() || declaration.getProperties().isInterfaceSetters()) {
                            addSetterFromSetter(spec, method, true, generic, field);
                        }
                    }
                }
            }
        }
        handleImports(declaration.getImplementation(), spec);
    }

    private static boolean handleExternalInterface(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceType type, boolean alreadyAdded) {
        var className = getExternalClassName(declaration.findCompilationUnit().get(), type);
        if (nonNull(className)) {
            var cls = loadNestedClass(className);
            if (nonNull(cls)) {
                if (cls.isInterface()) {
                    var generics = cls.getGenericInterfaces();
                    var interfaces = cls.getInterfaces();
                    Map<String, Type> typeArguments = null;
                    if (type.getTypeArguments().isPresent()) {
                        typeArguments = processGenerics(parsed, cls, type.getTypeArguments().get());
                    }
                    for (var i = 0; i < interfaces.length; i++) {
                        java.lang.reflect.Type[] types = null;
                        if (generics[i] instanceof ParameterizedType pType) {
                            types = pType.getActualTypeArguments();
                        }
                        handleExternalInterface(parsed, declaration, spec, interfaces[i], typeArguments, types);
                    }
                    handleExternalInterface(parsed, declaration, spec, cls, typeArguments, null);
                    if (nonNull(intf)) {
                        if (!alreadyAdded) {
                            intf.addExtendedType(handleType(declaration.findCompilationUnit().get(), intf.findCompilationUnit().get(), type));
                        }
                        if (nonNull(spec)) {
                            handleGenericTypes(declaration.findCompilationUnit().get(), declaration.findCompilationUnit().get(), type, null);
                        }
                    } else {
                        if (spec.getImplementedTypes().stream().noneMatch(type::equals)) {
                            if (!alreadyAdded) {
                                spec.addImplementedType(handleType(declaration.findCompilationUnit().get(), spec.findCompilationUnit().get(), type));
                            }
                        }
                    }
                } else {
                    log.error("{} is not interface!", className);
                }
            } else {
                var external = lookup.findExternal(className);
                if (nonNull(external)) {
                    if (external.getDeclaration().isClassOrInterfaceDeclaration()) {
                        var ext = external.getDeclaration().asClassOrInterfaceDeclaration();
                        if (ext.isInterface()) {
                            var generics = buildGenerics(type, ext);

                            var org = external.getImplementation();
                            ((Structures.Parsed) external).setImplementation(org.findCompilationUnit().get().clone().getType(0).asClassOrInterfaceDeclaration());
                            implementPrototype(parsed, spec, external, generics, true);
                            ((Structures.Parsed) external).setImplementation(org);
                            if (nonNull(intf)) {
                                if (!alreadyAdded) {
                                    intf.addExtendedType(external.getDeclaration().getNameAsString());
                                }
                                var eType = intf.getExtendedTypes().getLast().get();
                                type.getTypeArguments().ifPresent(args -> args.forEach(tt -> {
                                    if (eType.getTypeArguments().isEmpty()) {
                                        eType.setTypeArguments(new NodeList<>());
                                    }
                                    var arg = handleType(parsed.getDeclaration().findCompilationUnit().get(), intf.findCompilationUnit().get(), tt);
                                    eType.getTypeArguments().get().add(parsed.getParser().parseClassOrInterfaceType(arg).getResult().get());
                                }));
                                intf.findCompilationUnit().get().addImport(external.getDeclaration().getFullyQualifiedName().get());
                            }
                            org.getExtendedTypes().forEach(t -> {
                                var p = getParsed(t);

                                if (nonNull(p)) {
                                    if (p.isProcessed()) {
                                        handleParsedExtendedType(parsed, parsed, spec, null, parsed.getProperties(), t, true);
                                    }
                                } else {
                                    handleExternalInterface(parsed, org, spec, null, t, true);
                                }
                            });

                            return true;
                        }
                    }
                }
            }
        } else {
            log.error("Can't process interface {} cause can't find its type!", type.getNameAsString());
        }
        return false;
    }

    private static void handleExternalInterface(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, Class<?> cls, Map<String, Type> generics, java.lang.reflect.Type[] generics2) {
        Map<String, Type> generic = nonNull(generics2) ? processGenerics(cls, generics, generics2) : generics;

        var genericIntf = cls.getGenericInterfaces();
        var interfaces = cls.getInterfaces();
        for (var i = 0; i < interfaces.length; i++) {
            java.lang.reflect.Type[] types = null;
            if (genericIntf[i] instanceof ParameterizedType type) {
                types = type.getActualTypeArguments();
            }
            handleExternalInterface(parsed, declaration, spec, interfaces[i], generic, types);
        }

        switch (parsed.getProperties().getStrategy()) {
            case PROTOTYPE -> handleExternalMethodPrototypeStrategy(parsed, declaration, spec, cls, generic);
            case IMPLEMENTATION -> handleExternalMethodImplementationStrategy(parsed, declaration, spec, cls, generic);
        }

    }

    private static void handleExternalMethodPrototypeStrategy(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, Class<?> cls, Map<String, Type> generic) {
        var properties = parsed.getProperties();

        for (var method : cls.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !method.isDefault() && !defaultMethodExists(declaration, method)) {
                if (method.getParameterCount() == 0 && method.getName().startsWith("get") || method.getName().startsWith("is") && method.getReturnType().getCanonicalName().equals("boolean")) {
                    var field = addFieldFromGetter(parsed, spec, method, generic);
                    if (nonNull(field)) {
                        if (properties.isClassGetters()) {
                            field.generateGetter();
                        }
                        if (properties.isClassSetters()) {
                            field.generateSetter();
                        }
                    }
                } else if (method.getParameterCount() == 1 && method.getName().startsWith("set") && method.getReturnType().getCanonicalName().equals("void")) {
                    var field = addFieldFromSetter(parsed, spec, method, generic);
                    if (nonNull(field) && properties.isClassSetters()) {
                        if (properties.isClassGetters()) {
                            field.generateGetter();
                        }
                        if (properties.isClassSetters()) {
                            field.generateSetter();
                        }
                    }
                } else {
                    log.error("Method {} of {} is nor getter or setter. Not implemented!", method.getName(), cls.getCanonicalName());
                }
            }
        }
    }

    private static void handleExternalMethodImplementationStrategy(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, Class<?> cls, Map<String, Type> generic) {
        for (var method : cls.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !method.isDefault() && !defaultMethodExists(declaration, method)) {
                var mtd = new MethodDeclaration().setName(method.getName()).addModifier(PUBLIC);
                if (generic.containsKey(method.getGenericReturnType().getTypeName())) {
                    mtd.setType(generic.get(method.getGenericReturnType().getTypeName()));
                } else {
                    mtd.setType(method.getReturnType());
                }
                for (var p : method.getParameters()) {
                    if (generic.containsKey(p.getParameterizedType().getTypeName())) {
                        mtd.addParameter(p.getParameterizedType().getTypeName(), p.getName());
                    } else {
                        mtd.addParameter(p.getType(), p.getName());
                    }
                }

                if (!methodExists(spec, mtd, false)) {
                    mtd.setBody(getDefaultReturnBody(mtd.getType()));
                    spec.addMember(mtd);
                }
            }
        }
    }


    private static void handleMixin(PrototypeDescription<ClassOrInterfaceDeclaration> parse) {
        if (nonNull(parse.getProperties().getMixInClass())) {
            var parent = lookup.findParsed(getExternalClassName(parse.getDeclaration().findCompilationUnit().get(), parse.getProperties().getMixInClass()));
            if (parent != null) {
                var spec = parse.getImplementation();
                var intf = parse.getInterface();
                var parentSpec = parent.getImplementation();
                var parentIntf = parent.getInterface();
                intf.findCompilationUnit().get().addImport(parentIntf.getFullyQualifiedName().get());
                parentSpec.findCompilationUnit().get().addImport(intf.getFullyQualifiedName().get());
                parentSpec.addImplementedType(intf.getNameAsString());
                mergeTypes(parent, spec, parentSpec, m -> true, a -> a);
                intf.getExtendedTypes().forEach(t -> condition(t.getNameAsString().endsWith(MIX_IN_EXTENSION), () -> t.setName(t.getNameAsString().replace(MIX_IN_EXTENSION, ""))));
                if (intf.getExtendedTypes().stream().noneMatch(t -> t.getNameAsString().equals(parentIntf.getNameAsString()))) {
                    intf.addExtendedType(parentIntf.getNameAsString());
                }
                handleImports(parse.getDeclaration().asClassOrInterfaceDeclaration(), intf);
                handleImports(parent.getDeclaration().asClassOrInterfaceDeclaration(), intf);
                handleImports(parse.getDeclaration().asClassOrInterfaceDeclaration(), parentSpec);
            }
        }
    }

    public static String handleType(Node source, Node destination, Type type) {
        return handleType(source, destination, type, null);
    }

    public static String handleType(Node source, Node destination, Type type, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap) {
        return handleType(source.findCompilationUnit().get(), destination.findCompilationUnit().get(), type, prototypeMap);
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, Type type) {
        return handleType(source, destination, type, null);
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, Type type, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap) {
        var result = type.isClassOrInterfaceType() ? type.asClassOrInterfaceType().getNameWithScope() : type.toString();
        if (type.isClassOrInterfaceType()) {
            var generic = handleGenericTypes(source, destination, type.asClassOrInterfaceType(), prototypeMap);
            if (!isEmpty(generic)) {
                result = type.asClassOrInterfaceType().getNameWithScope() + "<" + String.join(",", generic.stream().map(t ->
                        t.startsWith("?") ? t : handleType(source, destination, new ClassOrInterfaceType(null, t))).toList()) + ">";
            }
        }

        return handleType(source, destination, result, false);
    }

    public static List<String> handleGenericTypes(CompilationUnit source, CompilationUnit destination, ClassOrInterfaceType type, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap) {
        var result = new ArrayList<String>();
        var arguments = type.getTypeArguments();
        if (arguments.isEmpty() || arguments.get().isEmpty()) {
            return result;
        } else {
            return arguments.get().stream().map(n -> handleType(source, destination, typeToString(n), true, prototypeMap)).toList();
        }
    }

    public static List<Pair<String, Boolean>> getGenericsList(CompilationUnit source, CompilationUnit destination, ClassOrInterfaceType type, boolean isCollection) {
        var arguments = type.getTypeArguments();
        if (arguments.isEmpty() || arguments.get().isEmpty()) {
            return Collections.singletonList(Pair.of("Object", false));
        } else {
            return arguments.get().stream().map(n -> handleType(source, destination, typeToString(n), true)).map(t ->
                    Pair.of(t, lookup.parsed().stream().anyMatch(p -> getExternalClassName(destination, t).equals(p.getInterfaceFullName())))).toList();
        }
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, String type, boolean embedded) {
        return handleType(source, destination, type, embedded, null);
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, String type, boolean embedded, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap) {
        var full = getExternalClassName(source, type);
        var parse = lookup.findParsed(full);

        if (nonNull(parse)) {
            var processing = processingTypes.get(type);

            if (embedded && nonNull(prototypeMap)) {
                prototypeMap.put(parse.getDeclaration().getNameAsString(), parse);
                if (parse.isMixIn()) {
                    prototypeMap.put(parse.getMixIn().getDeclaration().getNameAsString(), parse.getMixIn());
                    with(parse.getMixIn().getInterfaceName(), name -> prototypeMap.put(name, parse.getMixIn()));
                }
                lookup.addPrototypeMap(parse, prototypeMap);
            }

            if (isNull(processing)) {
                if (!parse.isProcessed()) {
                    generateCodeForClass(parse.getDeclarationUnit(), parse);
                }

                if (parse.isCodeEnum() && parse.isMixIn()) {
                    destination.addImport(parse.getMixIn().getInterface().getFullyQualifiedName().get());
                    return parse.getMixIn().getInterfaceName();
                } else {
                    destination.addImport(parse.getInterface().getFullyQualifiedName().get());
                }

                return parse.getInterfaceName();
            } else {
                destination.addImport(processing.getInterfacePackage() + "." + processing.getInterfaceName());
                return processing.getInterfaceName();
            }
        } else {
            if (!isJavaType(type)) {
                if (handleCompiledPrototype(full)) {
                    return handleType(source, destination, type, embedded, prototypeMap);
                } else {
                    if (!full.contains(".prototype.") && !full.startsWith("dummy.") && isNull(getExternalClassNameIfExists(destination, type))) { //TODO: Better way to hanle prototype self references
                        destination.findCompilationUnit().ifPresent(u -> importType(u, full));
                    }
                }
            }
            return type;
        }
    }

    protected static void handleFieldAnnotations(CompilationUnit unit, FieldDeclaration field, MethodDeclaration method, boolean compiledAnnotations, PrototypeField proto) {
        var next = Holder.of(false);
        method.getAnnotations().forEach(an ->
                Tools.with(getExternalClassName(unit, an.getNameAsString()), name -> {
                    var ann = loadClass(name);
                    if (nonNull(ann)) {
                        if (ForInterface.class.equals(ann)) {
                            next.set(true);
                            return;
                        } else if (isNull(ann.getAnnotation(CodeAnnotation.class)) && isNull(ann.getAnnotation(CodePrototypeTemplate.class))) {
                            var target = ann.getAnnotation(Target.class);
                            if (next.get()) {
                                if (target == null || target.toString().contains("METHOD")) {
                                    handleAnnotation(unit, proto.generateInterfaceGetter(), an);
                                }
                            } else {
                                if (target == null || target.toString().contains("FIELD")) {
                                    handleAnnotation(unit, field, an);
                                }
                            }
                        } else {
                            if (CodeFieldAnnotations.class.isAssignableFrom(ann)) {
                                an.getChildNodes().stream().filter(ArrayInitializerExpr.class::isInstance).findFirst().ifPresent(e ->
                                        e.getChildNodes().stream().filter(StringLiteralExpr.class::isInstance).map(StringLiteralExpr.class::cast).forEach(n ->
                                                handleMissingAnnotation(unit, field, new NormalAnnotationExpr().setName(parseName(n.asString())), true)));
                                an.getChildNodes().stream().filter(StringLiteralExpr.class::isInstance).map(StringLiteralExpr.class::cast).findFirst().ifPresent(e ->
                                        handleMissingAnnotation(unit, field, new NormalAnnotationExpr().setName(parseName(e.asString())), true));

                            } else if (Default.class.isAssignableFrom(ann)) {
                                if (an.isSingleMemberAnnotationExpr()) {
                                    field.getVariables().iterator().next().setInitializer(an.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString());
                                } else if (an.isNormalAnnotationExpr()) {
                                    an.asNormalAnnotationExpr().getPairs().forEach(p -> {
                                        if (VALUE.equals(p.getName().asString())) {
                                            field.getVariables().iterator().next().setInitializer(p.getValue().asStringLiteralExpr().asString());
                                        }
                                    });
                                }
                            } else if (DefaultString.class.isAssignableFrom(ann)) {
                                if (an.isSingleMemberAnnotationExpr()) {
                                    field.getVariables().iterator().next().setInitializer("\"" + an.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString() + "\"");
                                } else if (an.isNormalAnnotationExpr()) {
                                    an.asNormalAnnotationExpr().getPairs().forEach(p -> {
                                        if (VALUE.equals(p.getName().asString())) {
                                            field.getVariables().iterator().next().setInitializer("\"" + p.getValue().asStringLiteralExpr().asString() + "\"");
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        var parsed = lookup.findExternal(name);
                        if (nonNull(parsed) && parsed.getDeclaration().isAnnotationDeclaration()) {
                            if (parsed.getDeclaration().getAnnotationByClass(CodeAnnotation.class).isEmpty() && parsed.getDeclaration().getAnnotationByClass(CodePrototypeTemplate.class).isEmpty()) {
                                if (next.get()) {
                                    if (Helpers.annotationHasTarget(parsed, "ElementType.METHOD")) {
                                        handleAnnotation(unit, proto.generateInterfaceGetter(), an);
                                    } else {
                                        log.warn("Invalid annotation target {}", name);
                                    }
                                } else {
                                    if (Helpers.annotationHasTarget(parsed, "ElementType.FIELD")) {
                                        handleAnnotation(unit, field, an);
                                    } else {
                                        log.warn("Invalid annotation target {}", name);
                                    }
                                }
                            }
                        } else {
                            if (compiledAnnotations) {
                                if (next.get()) {
                                    handleMissingAnnotation(unit, proto.generateInterfaceGetter(), an);
                                } else {
                                    handleMissingAnnotation(unit, field, an);
                                }
                            } else {
                                log.warn("Can't process annotation {}", name);
                            }
                        }
                    }
                    next.set(false);
                })
        );
    }

    protected static void handleAnnotation(CompilationUnit unit, BodyDeclaration<?> body, AnnotationExpr ann) {
        body.getAnnotations().stream().filter(a -> a.getNameAsString().equals(ann.getNameAsString())).findFirst().ifPresent(a ->
                body.getAnnotations().remove(a));
        body.addAnnotation(ann.clone());

        Tools.with(getExternalClassNameIfExists(unit, ann.getNameAsString()), i ->
                body.findCompilationUnit().ifPresent(u -> u.addImport(sanitizeImport(i))));

    }

    protected static void handleMissingAnnotation(CompilationUnit unit, BodyDeclaration<?> body, AnnotationExpr ann) {
        handleMissingAnnotation(unit, body, ann, false);
    }

    protected static void handleMissingAnnotation(CompilationUnit unit, BodyDeclaration<?> body, AnnotationExpr ann, boolean loadable) {
        var existing = body.getAnnotations().stream().filter(a -> a.getNameAsString().equals(ann.getNameAsString())).findFirst();
        if (existing.isEmpty()) {
            var added = Holder.of(false);
            if (!loadable) {
                body.addAnnotation(ann);
                added.set(true);
            }
            Tools.with(getExternalClassNameIfExists(unit, ann.getNameAsString()), i -> {
                if (loadable && nonNull(loadClass(i))) {
                    body.addAnnotation(ann);
                    added.set(true);
                }
                if (added.get()) {
                    body.findCompilationUnit().ifPresent(u -> u.addImport(sanitizeImport(i)));
                }
            });
        }
    }

    private static void handleAnnotation(CompilationUnit unit, MethodDeclaration method, AnnotationExpr ann, CompilationUnit destinationUnit) {
        method.getAnnotations().stream().filter(a -> a.getNameAsString().equals(ann.getNameAsString())).findFirst().ifPresent(a ->
                method.getAnnotations().remove(a));
        method.addAnnotation(ann);

        Tools.with(getExternalClassNameIfExists(unit, ann.getNameAsString()), destinationUnit::addImport);
    }


    @SuppressWarnings("unchecked")
    private static void handleMethodAnnotations(MethodDeclaration method, MethodDeclaration declaration, PrototypeField field) {
        declaration.getAnnotations().forEach(a ->
                Tools.with(getExternalClassName(declaration.findCompilationUnit().get(), a.getNameAsString()), name -> {
                    var ann = loadClass(name);
                    if (nonNull(ann) && !ann.isAnnotationPresent(CodeAnnotation.class) && !field.getDeclaration().isAnnotationPresent((Class) ann)) {
                        var target = ann.getAnnotation(Target.class);
                        if (target != null && target.toString().contains("METHOD")) {
                            method.addAnnotation(a);
                        }
                    }
                })
        );
    }

    private static void handleClassAnnotations(ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf) {
        var next = Holder.of(false);
        declaration.findCompilationUnit().ifPresent(unit ->
                declaration.getAnnotations().forEach(a ->
                        Tools.with(getExternalClassName(unit, a.getNameAsString()), name -> {
                            var ann = loadClass(name);
                            if (nonNull(ann)) {
                                if (ForInterface.class.equals(ann)) {
                                    next.set(true);
                                    return;
                                }
                                if (isNull(ann.getAnnotation(CodeAnnotation.class)) && isNull(ann.getAnnotation(CodePrototypeTemplate.class))) {
                                    var target = ann.getAnnotation(Target.class);
                                    if (target == null || Arrays.asList(target.value()).contains(ElementType.TYPE)) {
                                        if (next.get()) {
                                            intf.addAnnotation(a);
                                        } else {
                                            spec.addAnnotation(a);
                                        }
                                    }
                                }
                            } else {
                                var parsed = lookup.findExternal(name);
                                if (nonNull(parsed) && parsed.getDeclaration().isAnnotationDeclaration()) {
                                    if (parsed.getDeclaration().stream().filter(AnnotationExpr.class::isInstance)
                                            .map(AnnotationExpr.class::cast)
                                            .filter(an -> "java.lang.annotation.Target".equals(getExternalClassName(parsed.getDeclaration().findCompilationUnit().get(), an.getNameAsString())))
                                            .findFirst()
                                            .map(t -> t.stream().filter(ArrayInitializerExpr.class::isInstance)
                                                    .map(ArrayInitializerExpr.class::cast)
                                                    .findFirst()
                                                    .map(arr -> arr.getValues().stream().map(Object::toString)
                                                            .anyMatch("ElementType.TYPE"::equals))
                                                    .orElse(false))
                                            .orElse(true)) {
                                        if (parsed.getDeclaration().getAnnotationByClass(CodePrototypeTemplate.class).isEmpty()) {
                                            spec.addAnnotation(a);
                                        }
                                    } else {
                                        log.warn("Invalid annotation target {}", name);
                                    }
                                } else {
                                    log.warn("Can't process annotation {}", name);
                                }
                            }
                            next.set(false);
                        })
                ));
    }

    private static void processInnerClass(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration cls) {
        cls.getImplementedTypes().forEach(t -> {
            if (handleExternalInterface(parsed, declaration, spec, null, t, false)) {
                handleType(cls, spec, t);
                spec.addImplementedType(t);
            }
        });
        cls.getAnnotationByName("CodeClassAnnotations").ifPresent(a ->
                cls.getAnnotations().forEach(ann -> {
                    if (!"CodeClassAnnotations".equals(ann.getNameAsString())) {
                        spec.addAnnotation(ann.clone());
                    }
                }));
    }

    protected static PrototypeField addField(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration method, Type generic, PrototypeField parent) {
        var compiledAnnotations = false;
        PrototypeField result = null;
        var fieldName = method.getNameAsString();
        var fieldParent = getFieldParent(parent);
        var fieldProto = findField(parsed, fieldName);
        var field = nonNull(fieldProto) ? fieldProto.getDeclaration() : null;
        var unit = type.findCompilationUnit().get();
        var ignores = getIgnores(method);
        if (isNull(field)) {
            var genericMethod = !method.getTypeParameters().isEmpty() && method.getTypeAsString().equals(method.getTypeParameter(0).getNameAsString());
            var prototypeMap = new HashMap<String, PrototypeDescription<ClassOrInterfaceDeclaration>>();
            if (nonNull(generic)) {
                field = spec.addField(generic, fieldName, PROTECTED);
            } else {
                if (method.getTypeParameters().isEmpty() || !method.getType().asString().equals(method.getTypeParameter(0).asString())) {
                    field = spec.addField(handleType(nonNull(fieldParent) ? fieldParent.getDescription() : type, spec, method.getType(), prototypeMap), fieldName, PROTECTED);
                } else {
                    field = spec.addField("Object", fieldName, PROTECTED);
                }
            }
            var collection = CollectionsHandler.isCollection(field.getVariable(0).getType());
            var proto = lookup.findParsed(getExternalClassName(unit, method.getType().asString()));

            var fullType = nullCheck(proto, Generator::calcProtoFullType,
                    nullCheck(getExternalClassNameIfExists(spec, field.getElementType().asString()),
                            getExternalClassNameIfExists(unit, field.getElementType().asString())));

            if (method.getType().isArrayType()) {
                fullType = fullType + "[]";
            }

            if (ignores.isForSerialization()) {
                field.setTransient(true);
            }

            result = Structures.FieldData.builder()
                    .parsed(parsed)
                    .name(fieldName)
                    .description(method)
                    .declaration(field)
                    .collection(collection)
                    .ignores(ignores)
                    .genericMethod(genericMethod)
                    .genericField(isGenericType(method.getType(), parsed.getDeclaration()))
                    .generics(nonNull(generic) ? Map.of(generic.asString(), generic) : null)
                    .prototype(collection ? prototypeMap.get(CollectionsHandler.getCollectionType(method.getType())) :
                            (isNull(generic) ? proto : null))
                    .typePrototypes(!prototypeMap.isEmpty() ? prototypeMap : null)
                    .type(field.getCommonType())
                    .fullType(fullType)
                    .parent(nonNull(parent) ? parent : nonNull(parsed.getBase()) ? findField(parsed.getBase(), fieldName) : null)
                    .build();
            parsed.getFields().add(result);
        } else {
            if (ignores.isExplicitlySet()) {
                ((Structures.FieldData) fieldProto).setIgnores(ignores);
            }

            var proto = parsed.getFields().stream().filter(d -> d.getName().equals(fieldName)).findFirst();
            if (proto.isPresent()) {
                result = proto.get();
                handleType(method, spec, method.getType());
                ((Structures.FieldData) result).setPrototype(isNull(generic) ? lookup.findParsed(getExternalClassName(unit, method.getType().asString())) : null);
                mergeAnnotations(method, result.getDescription());
            } else {
                result = fieldProto;
                var prototypeMap = new HashMap<String, PrototypeDescription<ClassOrInterfaceDeclaration>>();
                if (nonNull(generic)) {
                    field = spec.addField(generic, fieldName, PROTECTED);
                } else {
                    if (method.getTypeParameters().isEmpty() || !method.getType().asString().equals(method.getTypeParameter(0).asString())) {
                        field = spec.addField(handleType(type, spec, method.getType(), prototypeMap), fieldName, PROTECTED);
                    } else {
                        field = spec.addField("Object", fieldName, PROTECTED);
                    }
                }
                method = method.clone();
                mergeAnnotations(result.getDescription(), unit, method);
                compiledAnnotations = true;
                unit = fieldProto.getDescription().findCompilationUnit().get();
            }
        }
        handleFieldAnnotations(unit, field, method, compiledAnnotations, result);
        handleIgnores(result);

        return result;
    }

    protected static String calcProtoFullType(PrototypeDescription<ClassOrInterfaceDeclaration> proto) {
        var properties = proto.getProperties();
        if (properties.isGenerateInterface()) {
            return properties.getInterfaceFullName();
        } else if (properties.isGenerateImplementation()) {
            return properties.getImplementorFullName();
        }
        return null;
    }

    protected static boolean isGenericType(Type type, TypeDeclaration<ClassOrInterfaceDeclaration> declaration) {
        return declaration.asClassOrInterfaceDeclaration().getTypeParameters().stream().anyMatch(p -> p.getNameAsString().equals(type.asString()));
    }

    protected static PrototypeField addFieldFromGetter(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, MethodDeclaration method, Map<String, Type> generic, boolean external) {
        PrototypeField result = null;
        var genericMethod = !method.getTypeParameters().isEmpty() && method.getTypeAsString().equals(method.getTypeParameter(0).getNameAsString());
        var fieldName = getFieldName(method.getNameAsString());
        if (!fieldExists(parsed, fieldName)) {
            FieldDeclaration field;
            if (nonNull(generic) && !generic.isEmpty()) {
                var type = generic.get(method.getTypeAsString());
                if (isNull(type)) {
                    type = method.getType();
                }
                field = spec.addField(type, fieldName, PROTECTED);
            } else {
                if (genericMethod) {
                    field = spec.addField("Object", fieldName, PROTECTED);
                } else {
                    field = spec.addField(method.getType(), fieldName, PROTECTED);
                }
            }

            result = Structures.FieldData.builder()
                    .parsed(parsed)
                    .name(fieldName)
                    .description(method)
                    .declaration(field)
                    .ignores(getIgnores(method))
                    .collection(CollectionsHandler.isCollection(field.getVariable(0).getType()))
                    .generics(nonNull(generic) && !generic.isEmpty() ? generic : null)
                    .genericMethod(genericMethod)
                    .fullType(genericMethod ? null : getExternalClassNameIfExists(method, field.getElementType().asString()))
                    .type(genericMethod ? lookup.getParser().parseType(method.getTypeParameter(0).getNameAsString()).getResult().get() : field.getElementType())
                    //TODO: enable prototypes
                    .build();
            parsed.getFields().add(result);
        } else {
            var proto = parsed.getFields().stream().filter(d -> d.getName().equals(fieldName)).findFirst();
            if (proto.isPresent()) {
                result = proto.get();
            }
        }
        handleFieldAnnotations(result.getDescription().findCompilationUnit().get(), result.getDeclaration(), result.getDescription(), false, result);
        handleIgnores(result);

        return result;
    }

    protected static void handleIgnores(PrototypeField field) {
        var properties = field.getParsed().getProperties();

        if (properties.isGenerateInterface()) {
            handleAnnotationIgnores(field::forceGenerateInterfaceGetter, field.getIgnores());
            if (properties.isInterfaceSetters()) {
                handleAnnotationIgnores(field::forceGenerateInterfaceSetter, field.getIgnores());
            }
        }
        if (properties.isGenerateImplementation()) {
            if (properties.isClassGetters()) {
                handleAnnotationIgnores(field::forceGenerateGetter, field.getIgnores());
            }
            if (properties.isClassSetters()) {
                handleAnnotationIgnores(field::forceGenerateSetter, field.getIgnores());
            }
        }
        handleAnnotationIgnores(field::getDeclaration, field.getIgnores());
    }

    private static PrototypeField addFieldFromSetter(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, MethodDeclaration method, Map<String, Type> generic, boolean external) {
        PrototypeField result = null;
        var fieldName = getFieldName(method.getNameAsString());
        if (!fieldExists(parsed, fieldName)) {
            FieldDeclaration field;
            if (nonNull(generic)) {
                field = spec.addField(generic.get(parseMethodSignature(method)), fieldName, PROTECTED);
            } else {
                field = spec.addField(method.getParameter(0).getType(), fieldName, PROTECTED);
            }

            result = Structures.FieldData.builder()
                    .parsed(parsed)
                    .name(fieldName)
                    .description(method)
                    .declaration(field)
                    .ignores(getIgnores(method))
                    .collection(CollectionsHandler.isCollection(field.getVariable(0).getType()))
                    .generics(generic)
                    .genericMethod(false) //TODO: Handling for generic methods
                    .fullType(getExternalClassNameIfExists(spec.findCompilationUnit().get(), field.getElementType().asString()))
                    .type(field.getElementType())
                    //TODO: enable prototypes
                    .build();
            parsed.getFields().add(result);
        } else {
            var proto = parsed.getFields().stream().filter(d -> d.getName().equals(fieldName)).findFirst();
            if (proto.isPresent()) {
                result = proto.get();
            }
        }

        return result;
    }

    private static PrototypeField addFieldFromGetter(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, Method method, Map<String, Type> generic) {
        PrototypeField result = null;
        var fieldName = getFieldName(method.getName());
        var genericMethod = false;
        String full = null;
        Type fullType = null;
        if (!fieldExists(parsed, fieldName)) {
            FieldDeclaration field;
            MethodDeclaration description;
            PrototypeDescription<ClassOrInterfaceDeclaration> prototype = null;
            if (nonNull(generic)) {
                var sig = parseMethodSignature(method);
                fullType = generic.get(sig);
                if (isNull(fullType)) {
                    if (Helpers.isPrimitiveType(sig)) {
                        fullType = new PrimitiveType().setType(PrimitiveType.Primitive.valueOf(sig.toUpperCase()));
                    } else {
                        fullType = new ClassOrInterfaceType().setName(sig);
                    }
                }

                full = fullType instanceof ClassOrInterfaceType t && t.getScope().isPresent() ? t.asString() : getExternalClassNameIfExists(parsed.getDeclaration().findCompilationUnit().get(), fullType.asString());
                if (isNull(full)) {
                    full = getExternalClassNameIfExists(parsed.getDeclaration().findCompilationUnit().get(), fullType.asString() + "Prototype");
                }
                if (nonNull(full)) {
                    prototype = lookup.findParsed(full);
                    if (isNull(prototype)) {
                        prototype = lookup.findGenerated(full);
                    }
                    if (isNull(prototype)) {
                        if (full.equals(parsed.getInterfaceFullName()) || full.equals(parsed.getPrototypeClassName())) {
                            prototype = parsed;
                        }
                    }
                }

                if (!parsed.equals(prototype)) {
                    handleType(parsed.getDeclaration().asClassOrInterfaceDeclaration(), spec, fullType);
                }

                if (nonNull(prototype)) {
                    fullType = new ClassOrInterfaceType().setName(prototype.getInterfaceName());
                }

                field = spec.addField(fullType, fieldName, PROTECTED);
                description = new MethodDeclaration().setName(fieldName).setType(fullType);
            } else {
                genericMethod = !method.getReturnType().getCanonicalName().equals(parseMethodSignature(method));
                var type = method.getReturnType().getSimpleName();
                if (method.getGenericReturnType() instanceof ParameterizedType parameterizedType && parameterizedType.getActualTypeArguments().length > 0) {
                    type = parameterizedType.toString();
                }
                field = spec.addField(type, fieldName, PROTECTED);
                description = new MethodDeclaration().setName(fieldName).setType(type);
                if (!method.getReturnType().isPrimitive() && !method.getReturnType().getCanonicalName().startsWith("java.lang.")) {
                    spec.findCompilationUnit().get().addImport(method.getReturnType().getCanonicalName());
                }
            }

            var dummy = envelopWithDummyClass(description, field);
            NormalAnnotationExpr getterAnn = null;

            for (var ann : method.getDeclaredAnnotations()) {
                if (isNull(ann.annotationType().getAnnotation(CodeAnnotation.class))) {
                    description.addAnnotation(ann.annotationType());
                    addImport(dummy, ann.annotationType());
                    addImport(field, ann.annotationType());
                    var a = new NormalAnnotationExpr();
                    a.setName(ann.annotationType().getSimpleName());

                    Helpers.copyAnnotationParams(ann, a, field);
                    if (annotationTargetsField(ann)) {
                        field.addAnnotation(a);
                    } else {
                        getterAnn = a;
                    }
                }
            }

            result = Structures.FieldData.builder()
                    .parsed(parsed)
                    .description(description)
                    .name(fieldName)
                    .declaration(field)
                    .collection(CollectionsHandler.isCollection(field.getVariable(0).getType()))
                    .ignores(getIgnores(method))
                    .generics(generic)
                    .genericMethod(genericMethod)
                    .fullType(nonNull(full) ? full : genericMethod ? null : nonNull(prototype) ? prototype.getInterfaceFullName() : getExternalClassNameIfExists(spec, field.getElementType().asString()))
                    .type(nonNull(fullType) ? fullType : discoverType(method, genericMethod, field))
                    .ignores(getIgnores(method))
                    .prototype(prototype)
                    .build();
            parsed.getFields().add(result);
            if (nonNull(getterAnn)) {
                result.generateGetter().addAnnotation(getterAnn);
            }
        } else {
            var proto = parsed.getFields().stream().filter(d -> d.getName().equals(fieldName)).findFirst();
            if (proto.isPresent()) {
                result = proto.get();
            }
        }

        return result;
    }

    protected static Type discoverType(Method method, boolean genericMethod, FieldDeclaration field) {
        return genericMethod ? lookup.getParser().parseType(parseMethodSignature(method)).getResult().get() : field.getElementType();
    }

    protected static PrototypeField addFieldFromSetter(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, Method method, Map<String, Type> generic) {
        PrototypeField result = null;
        var fieldName = getFieldName(method.getName());
        var genericMethod = false;
        if (!fieldExists(parsed, fieldName)) {
            FieldDeclaration field;
            MethodDeclaration description;
            if (nonNull(generic)) {
                var type = generic.get(parseMethodSignature(method));
                field = spec.addField(type, fieldName, PROTECTED);
                description = new MethodDeclaration().setName(fieldName).setType(type);
                handleType(parsed.getDeclaration().asClassOrInterfaceDeclaration(), spec, type);
            } else {
                var type = method.getParameters()[0].getType();
                genericMethod = !type.equals(method.getGenericParameterTypes()[0]);
                field = spec.addField(type, fieldName, PROTECTED);
                description = new MethodDeclaration().setName(fieldName).setType(type);
                if (!type.isPrimitive() && !type.getCanonicalName().startsWith("java.lang.")) {
                    spec.findCompilationUnit().get().addImport(type.getCanonicalName());
                }
            }

            var dummy = envelopWithDummyClass(description, field);

            for (var ann : method.getDeclaredAnnotations()) {
                description.addAnnotation(ann.annotationType());
                dummy.addImport(ann.annotationType().getPackageName());
                field.addAnnotation(ann.annotationType());
                //TODO: Check if the annotation can be applied to field.
                //TODO: Handle annotation params
            }

            result = Structures.FieldData.builder()
                    .parsed(parsed)
                    .description(description)
                    .name(fieldName)
                    .declaration(field)
                    .collection(CollectionsHandler.isCollection(field.getVariable(0).getType()))
                    .ignores(Structures.Ignores.builder().build())
                    .generics(generic)
                    .genericMethod(genericMethod)
                    .fullType(genericMethod ? null : getExternalClassNameIfExists(spec.findCompilationUnit().get(), field.getElementType().asString()))
                    .type(discoverType(method, genericMethod, field))
                    //TODO: enable ignores
                    //TODO: enable prototypes
                    .build();
            parsed.getFields().add(result);
        } else {
            var proto = parsed.getFields().stream().filter(d -> d.getName().equals(fieldName)).findFirst();
            if (proto.isPresent()) {
                result = proto.get();
            }
        }

        return result;
    }

    public static MethodDeclaration addGetter(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field, boolean force) {
        var name = getGetterName(declaration.getNameAsString(), declaration.getType().asString());
        if (force || !methodExists(spec, declaration, name, isClass)) {
            String rType;
            if (declaration.getTypeParameters().isEmpty()) {
                var parent = getFieldParent(field);
                rType = handleType(nonNull(parent) ? parent.getDescription() : type, spec, declaration.getType());
            } else {
                rType = declaration.getType().asString();
            }
            var method = spec
                    .addMethod(name)
                    .setType(rType);
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(declaration.getName()))));
                ((Structures.FieldData) field).setImplementationGetter(method);
                handleMethodAnnotations(method, declaration, field);
                if (declaration.getTypeParameters().isNonEmpty()) {
                    method.setType("Object");
                }
            } else {
                method.setBody(null);
                ((Structures.FieldData) field).setInterfaceGetter(method);

                if (declaration.getTypeParameters().isNonEmpty()) {
                    declaration.getTypeParameters().forEach(method::addTypeParameter);
                }
            }
            return method;
        }
        return null;
    }

    protected static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field) {
        addGetterFromGetter(spec, declaration, isClass, null, field);
    }

    protected static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, Map<String, Type> generic, PrototypeField field) {
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec
                    .addMethod(declaration.getNameAsString());
            method.setType(field.getType());
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(getFieldName(declaration.getNameAsString())))));
                ((Structures.FieldData) field).setImplementationGetter(method);
            } else {
                method.setBody(null);
                ((Structures.FieldData) field).setInterfaceGetter(method);
            }

            Helpers.addImport(spec, field);
        }
    }

    protected static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, Method declaration, boolean isClass, Map<String, Type> generic, PrototypeField field) {
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec.addMethod(declaration.getName());
            if (nonNull(generic)) {
                method.setType(generic.get(parseMethodSignature(declaration)));
            } else {
                method.setType(declaration.getReturnType());
            }
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(getFieldName(declaration.getName())))));
                ((Structures.FieldData) field).setImplementationGetter(method);
            } else {
                method.setBody(null);
                ((Structures.FieldData) field).setInterfaceGetter(method);
            }
        }
    }

    public static MethodDeclaration addSetter(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field, boolean force) {
        var fieldName = nonNull(field.getName()) ? field.getName() : declaration.getNameAsString();
        var name = getSetterName(fieldName);
        String returnType = null;
        if (nonNull(field.getType())) {
            returnType = field.getType().asString();
        } else if (nonNull(field.getGenerics())) {
            returnType = field.getGenerics().get(declaration.getType().asString()).asString();
        }
        if (isNull(returnType)) {
            handleType(type, spec, declaration.getType());
        }

        if (isClass && field.isGenericMethod() && !returnType.equals(field.getDeclaration().getVariable(0).getType().asString())) {
            returnType = "Object";
        }

        var method = new MethodDeclaration()
                .setName(name)
                .setType("void")
                .addParameter(new Parameter().setName(fieldName).setType(returnType));
        envelopWithDummyClass(method, field.getDeclaration());
        if (!methodExists(spec, method, name, isClass || force)) {
            spec.addMember(method);
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + fieldName)).setValue(new NameExpr().setName(fieldName))));
                ((Structures.FieldData) field).setImplementationSetter(method);

                if (declaration.getTypeParameters().isNonEmpty()) {
                    method.getParameter(0).setType("Object");
                }
                if (field.isGenericMethod()) {
                    addSuppressWarningsUnchecked(method);
                }
            } else {
                method.setBody(null);
                ((Structures.FieldData) field).setInterfaceSetter(method);

                if (declaration.getTypeParameters().isNonEmpty()) {
                    declaration.getTypeParameters().forEach(method::addTypeParameter);
                }
            }

            return method;
        }
        return null;
    }

    private static void addSetterFromSetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field) {
        addSetterFromSetter(spec, declaration, isClass, null, field);
    }

    private static void addSetterFromSetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, Map<String, Type> generic, PrototypeField field) {
        var method = new MethodDeclaration().setName(declaration.getNameAsString()).setType("void");
        if (nonNull(generic)) {
            method.addParameter(new Parameter().setName(field.getName()).setType(generic.get(declaration.getParameter(0).getType().asString())));
        } else {
            method.addParameter(new Parameter().setName(field.getName()).setType(declaration.getParameter(0).getType()));
        }

        if (!methodExists(spec, method, isClass, true)) {
            spec.addMember(method);

            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + getFieldName(declaration.getNameAsString()))).setValue(new NameExpr().setName(getFieldName(declaration.getNameAsString())))));
                ((Structures.FieldData) field).setImplementationSetter(method);
            } else {
                method.setBody(null);
                ((Structures.FieldData) field).setInterfaceSetter(method);
            }
        }
    }

    private static void addSetterFromSetter(ClassOrInterfaceDeclaration spec, Method declaration, boolean isClass, Map<String, Type> generic, PrototypeField proto) {
        if (!methodExists(spec, declaration, isClass)) {
            var field = getFieldName(declaration.getName());
            var method = spec.addMethod(declaration.getName());
            if (nonNull(generic)) {
                method.addParameter(new Parameter().setName(field).setType(generic.get(parseMethodSignature(declaration))));
            } else {
                method.addParameter(new Parameter().setName(field).setType(declaration.getParameterTypes()[0]));
            }
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + field)).setValue(new NameExpr().setName(field))));
                ((Structures.FieldData) proto).setImplementationSetter(method);
            } else {
                method.setBody(null);
                ((Structures.FieldData) proto).setInterfaceSetter(method);
            }
        }
    }

    public static void addMethod(ClassOrInterfaceDeclaration spec, Method declaration, Map<String, String> signature) {
        if (!methodExists(spec, declaration, false)) {
            var unit = spec.findCompilationUnit().get();
            var method = spec.addMethod(declaration.getName());
            method.setType(mapGenericMethodSignature(declaration, signature));
            var names = getParameterNames(declaration);
            for (var i = 0; i < declaration.getParameterCount(); i++) {
                var param = declaration.getParameters()[i];
                if (declaration.getGenericParameterTypes()[i] instanceof ParameterizedType) {
                    method.addParameter(mapGenericSignature(declaration.getGenericParameterTypes()[i], signature), names[i]);
                } else {
                    importClass(unit, param.getType());
                    method.addParameter(param.getType().getSimpleName(), names[i]);
                }
            }
            method.setBody(null);
        }
    }

    private static void mergeTypes(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration destination, Predicate<BodyDeclaration<?>> filter, UnaryOperator<MethodDeclaration> adjuster) {
        for (var member : source.getMembers()) {
            if (filter.test(member)) {
                if (member instanceof FieldDeclaration) {
                    var field = findField(parsed, member.asFieldDeclaration().getVariable(0).getNameAsString());
                    if (isNull(field)) {
                        destination.addMember(member.clone());
                    } else {
                        //TODO: Merge annotations
                    }
                } else if (member instanceof MethodDeclaration) {
                    var method = findMethod(destination, member.asMethodDeclaration().getNameAsString());
                    if (isNull(method)) {
                        destination.addMember(adjuster.apply((MethodDeclaration) member.clone()));
                    } else {
                        //TODO: Merge annotations
                    }
                }
            }
            //TODO: Merge annotations too!
        }
    }

    private static void mergeAnnotations(MethodDeclaration source, MethodDeclaration destination) {
        source.findCompilationUnit().ifPresent(unit -> {
            for (var ann : source.getAnnotations()) {
                handleAnnotation(unit, destination, ann);
            }
        });
    }

    private static void mergeAnnotations(MethodDeclaration source, CompilationUnit destinationUnit, MethodDeclaration destination) {
        source.findCompilationUnit().ifPresent(unit -> {
            for (var ann : destination.getAnnotations()) {
                Tools.with(getExternalClassNameIfExists(destinationUnit, ann.getNameAsString()), unit::addImport);
            }
            for (var ann : source.getAnnotations()) {
                handleAnnotation(unit, destination, ann, destinationUnit);
            }
        });
    }


    private static void mergeAnnotations(FieldDeclaration source, FieldDeclaration destination) {
        source.findCompilationUnit().ifPresent(unit -> {
            for (var ann : source.getAnnotations()) {
                handleAnnotation(unit, destination, ann);
            }
        });
    }

    public static PrototypeDescription<?> generateCodeForEnum(CompilationUnit declarationUnit, PrototypeDescription<?> prsd, TypeDeclaration<?> type, List<Pair<AnnotationExpr, Structures.PrototypeDataHandler>> prototype) {
        if (type.isEnumDeclaration()) {
            var typeDeclaration = type.asEnumDeclaration();

            log.info("Processing - {}", typeDeclaration.getNameAsString());

            var properties = getEnumProperties(prototype.get(0).getKey());
            properties.setPrototypeName(typeDeclaration.getNameAsString());
            properties.setPrototypeFullName(typeDeclaration.getFullyQualifiedName().orElseThrow());

            handleEnrichersSetup(properties);

            var mixIn = withRes(properties.getMixInClass(), c ->
                    withRes(getExternalClassName(declarationUnit.findCompilationUnit().get(), c), Generator::findEnum));

            var mixInParse = ensureParsedParents(typeDeclaration, mixIn);

            var iUnit = new CompilationUnit();
            iUnit.addImport("javax.annotation.processing.Generated");
            var intf = iUnit.addClass(properties.getInterfaceName()).setInterface(true);
            iUnit.setPackageDeclaration(properties.getInterfacePackage());
            intf.addModifier(PUBLIC);
            iUnit.addImport("net.binis.codegen.objects.base.enumeration.CodeEnum");
            intf.addExtendedType("CodeEnum");
            iUnit.addImport(CodeFactory.class);

            var unit = new CompilationUnit();
            unit.addImport("javax.annotation.processing.Generated");
            var spec = unit.addClass(properties.getClassName());
            unit.setPackageDeclaration(properties.getClassPackage());
            spec.addModifier(PUBLIC);
            unit.addImport("net.binis.codegen.objects.base.enumeration.CodeEnumImpl");
            spec.addExtendedType("CodeEnumImpl");
            spec.addImplementedType(intf.getNameAsString());

            var parse = (Structures.Parsed) lookup.findParsed(getClassName(typeDeclaration));

            parse.setProperties(properties);
            parse.setImplementation(spec);
            parse.setInterface(intf);
            parse.setCodeEnum(true);
            parse.setMixIn((Structures.Parsed) mixInParse);

            if (isNull(prsd) || !prsd.isNested() || isNull(prsd.getParentClassName())) {
                spec.addAnnotation(annotation("@Generated(value=\"" + properties.getPrototypeFullName() + "\", comments=\"" + properties.getInterfaceName() + "\")"));
                intf.addAnnotation(annotation("@Generated(value=\"" + properties.getPrototypeFullName() + "\", comments=\"" + (nonNull(mixIn) ? mixIn.getProperties().getClassName() : properties.getClassName()) + "\")"));
            }
            spec.addAnnotation(annotation("@net.binis.codegen.annotation.Generated(by=\"" + properties.getPrototypeFullName() + "\")"));
            intf.addAnnotation(annotation("@net.binis.codegen.annotation.Generated(by=\"" + properties.getPrototypeFullName() + "\")"));

            unit.setComment(new BlockComment("Generated code by Binis' code generator."));
            iUnit.setComment(new BlockComment("Generated code by Binis' code generator."));

            processEntries(parse, typeDeclaration, intf, mixIn, properties.getOrdinalOffset());
            processEnumImplementation(parse, typeDeclaration, spec);
            handleImports(typeDeclaration, spec);

            lookup.registerGenerated(properties.getPrototypeFullName(), parse);

            addDefaultCreation(parse);
            if (nonNull(mixIn)) {
                iUnit.addImport(mixIn.getInterfaceFullName());
            }

            handleImports(typeDeclaration, intf);

            processingTypes.remove(typeDeclaration.getNameAsString());

            parse.setProcessed(true);
            return parse;
        }
        return null;
    }

    private static PrototypeDescription<?> findEnum(String cls) {
        var result = lookup.findParsed(cls);
        if (isNull(result)) {
            if (handleCompiledEnumPrototype(cls)) {
                result = lookup.findParsed(cls);
            } else {
                throw new GenericCodeGenException("Can't find class '" + cls + "' or it isn't enum class!");
            }
        }
        return result;
    }

    private static void processEnumImplementation(Structures.Parsed desc, EnumDeclaration declaration, ClassOrInterfaceDeclaration spec) {
        var constructor = spec.addConstructor(PUBLIC)
                .addParameter(int.class, "$ordinal")
                .addParameter(String.class, "$name")
                .setBody(new BlockStmt().addStatement("super($ordinal, $name);"));

        var constructors = declaration.getConstructors();
        if (!constructors.isEmpty()) {
            if (constructors.size() > 1) {
                throw new GenericCodeGenException("Enums with more than one constructors are unsupported!");
            }
            var con = constructors.get(0);
            con.getParameters().forEach(constructor::addParameter);
            con.getBody().getStatements().forEach(s -> constructor.getBody().addStatement(s));
        } else {
            var fields = declaration.getFields().stream().filter(f -> !f.getModifiers().contains(Modifier.staticModifier()));
            var extendConstructor = false;
            if (desc.hasEnricher(RequiredArgsConstructorEnricher.class)) {
                fields = fields.filter(f -> f.getModifiers().contains(Modifier.finalModifier()));
                extendConstructor = true;
            }
            if (desc.hasEnricher(NotInitializedArgsConstructorEnricher.class)) {
                fields = fields.filter(f -> f.getVariable(0).getInitializer().isEmpty());
                extendConstructor = true;
            }
            if (desc.hasEnricher(AllArgsConstructorEnricher.class)) {
                extendConstructor = true;
            }

            if (extendConstructor) {
                fields.forEach(f -> {
                    var name = f.getVariable(0).getNameAsString();
                    constructor.addParameter(f.getVariable(0).getType(), name);
                    constructor.getBody().addStatement("this." + name + " = " + name + ";");
                });
            }
        }

        declaration.getMethods().forEach(m -> spec.addMember(m.clone().setModifier(PUBLIC, true)));
        declaration.getFields().stream().filter(f -> f.getModifiers().contains(Modifier.staticModifier())).forEach(f -> spec.addMember(f.clone().setModifier(PUBLIC, true)));

        if (desc.hasEnricher(GetterEnricher.class)) {
            declaration.getFields().stream()
                    .filter(f -> !f.getModifiers().contains(Modifier.staticModifier()))
                    .forEach(f -> {
                        var type = f.getVariable(0).getType();
                        var name = getGetterName(f.getVariable(0).getNameAsString(), type);
                        var method = new MethodDeclaration().setName(name).setType(type).setBody(null);

                        if (!methodExists(desc.getInterface(), method, false, true)) {
                            desc.getInterface().addMember(method);
                        }
                        if (!methodExists(spec, method, false, true)) {
                            spec.addMember(method.clone().setModifier(PUBLIC, true).setBody(returnBlock(f.getVariable(0).getNameAsString())));
                        }
                    });
        }

        spec.addMethod("equals", PUBLIC)
                .addParameter(Object.class, "o")
                .setType(boolean.class)
                .setBody(block("{ return super.equals(o); }"));

        spec.addMethod("hashCode", PUBLIC)
                .setType(int.class)
                .setBody(block("{ return super.hashCode(); }"));
    }

    private static void processEntries(Structures.Parsed parse, EnumDeclaration declaration, ClassOrInterfaceDeclaration intf, PrototypeDescription<?> mixIn, long offset) {
        var name = nonNull(mixIn) ? mixIn.getInterfaceName() : intf.getNameAsString();

        if (nonNull(mixIn) && offset == 0L) {
            offset = mixIn.getProperties().getOrdinalOffset() + mixIn.getDeclaration().asEnumDeclaration().getEntries().size();
        }

        for (var i = 0; i < declaration.getEntries().size(); i++) {
            var entry = declaration.getEntries().get(i);
            var expression = new StringBuilder("CodeFactory.initializeEnumValue(").append(name).append(".class, \"").append(entry.getNameAsString()).append("\", ");
            var ann = entry.getAnnotationByClass(Ordinal.class);
            if (ann.isEmpty()) {
                expression.append(offset + i);
            } else {
                expression.append(getAnnotationValue(ann.get()));
            }
            for (var arg : entry.getArguments()) {
                expression.append(", ").append(arg.toString());
            }
            expression.append(")");
            intf.addFieldWithInitializer(name, entry.getNameAsString(), EnrichHelpers.expression(expression.toString()), STATIC, FINAL);
        }
        declaration.getFields().stream().filter(f -> f.getModifiers().contains(Modifier.publicModifier()) && f.getModifiers().contains(Modifier.staticModifier()) && f.getModifiers().contains(Modifier.finalModifier())).forEach(f -> {
            var field = f.clone();
            field.getModifiers().remove(Modifier.publicModifier());
            intf.addMember(field);
        });
        declaration.getMethods().forEach(m -> {
            var method = m.clone().setBody(null);
            method.getModifiers().remove(Modifier.publicModifier());
            intf.addMember(method);
        });

        declaration.getFields().stream().filter(f -> !f.getModifiers().contains(Modifier.staticModifier())).forEach(f ->
                EnrichHelpers.addField(parse, f.getVariable(0).getNameAsString(), f.getElementType()).getDeclaration().setAnnotations(f.getAnnotations()).addModifier(FINAL));

        declaration.getFields().stream().filter(f -> f.isAnnotationPresent(Getter.class)).forEach(f ->
                intf.addMethod(getGetterName(f.getVariable(0).getNameAsString(), f.getVariable(0).getType())).setType(f.getVariable(0).getType()).setBody(null));

        declaration.getFields().stream().filter(f -> f.isAnnotationPresent(Setter.class)).forEach(f ->
                intf.addMethod(getSetterName(f.getVariable(0).getNameAsString())).setBody(null));

        intf.addMethod("valueOf", STATIC)
                .addParameter("String", "name")
                .setType(name)
                .setBody(new BlockStmt().addStatement("return CodeFactory.enumValueOf(" + name + ".class, name);"));

        intf.addMethod("valueOf", STATIC)
                .addParameter("int", "ordinal")
                .setType(name)
                .setBody(new BlockStmt().addStatement("return CodeFactory.enumValueOf(" + name + ".class, ordinal);"));

        intf.addMethod("values", STATIC)
                .setType(name + "[]")
                .setBody(new BlockStmt().addStatement("return CodeFactory.enumValues(" + name + ".class);"));

        if (nonNull(mixIn)) {
            mixIn.getDeclaration().asEnumDeclaration().getEntries().forEach(entry ->
                    intf.addFieldWithInitializer(name, entry.getNameAsString(), expression(name + "." + entry.getNameAsString()), STATIC, FINAL));
        }
    }

    protected static Structures.PrototypeDataHandler getEnumProperties(AnnotationExpr prototype) {
        var type = (EnumDeclaration) prototype.getParentNode().get();
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);

        var prototypeClass = getExternalClassName(prototype, prototype.getNameAsString());
        var builder = Structures.builder(prototypeClass)
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type));

        builder.prototypeAnnotationExpression(prototype);
        nullCheck(loadClass(getExternalClassName(prototype, prototype.getNameAsString())), cls -> builder.prototypeAnnotation((Class) cls));

        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair pair) {
                var name = pair.getNameAsString();
                switch (name) {
                    case "name" -> builder.name(pair.getValue().asStringLiteralExpr().asString());
                    case "mixIn" -> {
                        var value = pair.getValue().asClassExpr().getTypeAsString();
                        if (!"void".equals(value)) {
                            builder.mixInClass(value);
                        }
                    }
                    case "ordinalOffset" -> builder.ordinalOffset(pair.getValue().asIntegerLiteralExpr().asNumber().intValue());
                    case "enrichers" -> checkEnrichers(builder::enrichers, handleInitializerAnnotation(pair));
                    default -> {
                    }
                }
            }
        });

        if (cName.equals(iName.get())) {
            cName = iName.get() + "Impl";
        }

        builder.className(cName).interfaceName(iName.get());

        var result = builder.build();

        if (isNull(result.getEnrichers())) {
            result.setEnrichers(new ArrayList<>());
        }

        if (isNull(result.getInheritedEnrichers())) {
            result.setInheritedEnrichers(new ArrayList<>());
        }

        Tools.with(result.getPredefinedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getEnrichers(), e)));

        Tools.with(result.getPredefinedInheritedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getInheritedEnrichers(), e)));

        return result;
    }

    private static Structures.PrototypeDataHandler getConstantProperties(AnnotationExpr prototype) {
        var type = (ClassOrInterfaceDeclaration) prototype.getParentNode().get();
        var builder = Structures.PrototypeDataHandler.builder()
                .className(defaultClassName(type))
                .classPackage(defaultPackage(type, null));
        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair pair) {
                var name = pair.getNameAsString();
                switch (name) {
                    case "mixIn" -> builder.mixInClass(pair.getValue().asClassExpr().getTypeAsString());
                    default -> {
                    }
                }
            }
        });

        return builder.build();
    }

    public static CompilationUnit generateCodeForConstants() {
        if (!constantParsed.isEmpty()) {
            var result = new CompilationUnit();
            var parent = result.addClass("Constants");
            parent.addConstructor(PRIVATE);

            for (var entry : constantParsed.entrySet()) {
                var type = entry.getValue().getDeclaration();
                if (type.isClassOrInterfaceDeclaration()) {
                    type.getAnnotationByName("ConstantPrototype").ifPresent(prototype -> {
                        var typeDeclaration = type.asClassOrInterfaceDeclaration();

                        log.info("Processing - {}", prototype);

                        var properties = getConstantProperties(prototype);

                        var name = Holder.of(defaultClassName(entry.getValue().getDeclaration()));
                        if (nonNull(properties.getMixInClass())) {
                            name.set(defaultClassName(properties.getMixInClass()));
                        }

                        var cls = (ClassOrInterfaceDeclaration) parent.getMembers().stream().filter(c -> c.isClassOrInterfaceDeclaration() && c.asClassOrInterfaceDeclaration().getNameAsString().equals(name.get())).findFirst().orElse(null);
                        if (isNull(cls)) {
                            cls = (new ClassOrInterfaceDeclaration()).setName(name.get()).setModifiers(PUBLIC, STATIC);
                            cls.addConstructor(PRIVATE);
                            parent.addMember(cls);
                        }

                        mergeConstants(typeDeclaration, cls);
                    });
                }
            }

            return result;
        }
        return null;
    }

    public static void generateCodeForElements(PrototypeDescription<ClassOrInterfaceDeclaration> prsd) {
        for (var elements : prsd.getElements().values()) {
            for (var element : elements) {
                handleEnrichersSetup(element.getProperties());
                Helpers.handleEnrichers(element);
            }
        }
    }

    private static void mergeConstants(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration destination) {
//        mergeImports(source.findCompilationUnit().get(), destination.findCompilationUnit().get());
//
//        for (var member : source.getMembers()) {
//            if (member.isFieldDeclaration()) {
//                var type = member.asFieldDeclaration();
//                var field = new FieldDeclaration();
//                field.setModifiers(type.getModifiers());
//                type.getVariables().forEach(v -> {
//                    var variable = new VariableDeclarator().setName(v.getName());
//                    if (v.getType().isClassOrInterfaceType()) {
//                        var enm = getEnumNameFromPrototype(source, v.getType().asClassOrInterfaceType().getNameAsString());
//                        if (nonNull(enm)) {
//                            variable.setType(enm);
//                        } else {
//                            variable.setType(v.getType());
//                        }
//                    } else {
//                        variable.setType(v.getType());
//                    }
//
//                    v.getInitializer().ifPresent(i -> {
//                        if (i.isFieldAccessExpr()) {
//                            var expr = i.asFieldAccessExpr();
//                            if (expr.getScope().isNameExpr()) {
//                                var enm = getEnumNameFromPrototype(source, expr.getScope().asNameExpr().getNameAsString());
//                                if (nonNull(enm)) {
//                                    variable.setInitializer(new FieldAccessExpr().setName(expr.getName()).setScope(new NameExpr(enm)));
//                                } else {
//                                    variable.setInitializer(i);
//                                }
//                            } else {
//                                variable.setInitializer(i);
//                            }
//                        } else {
//                            variable.setInitializer(i);
//                        }
//                    });
//
//                    field.addVariable(variable);
//                });
//                destination.addMember(field);
//            }
//        }
    }
}
