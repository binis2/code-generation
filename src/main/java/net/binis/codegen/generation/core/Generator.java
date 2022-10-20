package net.binis.codegen.generation.core;

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
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.options.CodeOption;
import net.binis.codegen.tools.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.CompiledPrototypesHandler.handleCompiledEnumPrototype;
import static net.binis.codegen.generation.core.CompiledPrototypesHandler.handleCompiledPrototype;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class Generator {

    public static final String MIX_IN_EXTENSION = "$MixIn";

    private static List<Pair<PrototypeData, PrototypeDescription<ClassOrInterfaceDeclaration>>> notProcessed = new ArrayList<>();

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
                if (type.asClassOrInterfaceDeclaration().isInterface()) {
                    getCodeAnnotation(type).ifPresent(prototype -> {
                        generateCodeForPrototype(parser, prsd, type, prototype);
                        processed.set(processed.get() + 1);
                    });
                }
            } else if (type.isEnumDeclaration()) {
                getCodeAnnotation(type).ifPresent(prototype -> {
                    generateCodeForEnum(parser, prsd, type, prototype);
                    processed.set(processed.get() + 1);
                });
            }
        }

        if (!notProcessed.isEmpty()) {
            var i = notProcessed.iterator();
            while (i.hasNext()) {
                var item = i.next();
                if (item.getValue().isProcessed()) {
                    var parse = (Structures.Parsed<ClassOrInterfaceDeclaration>) lookup.findParsed(item.getKey().getPrototypeFullName());
                    parse.getDeclaration().asClassOrInterfaceDeclaration().getExtendedTypes().stream().filter(t -> t.getNameAsString().equals(item.getValue().getDeclaration().asClassOrInterfaceDeclaration().getNameAsString())).forEach(t ->
                            handleParsedExtendedType(parse, item.getValue(), parse.getSpec(), parse.getIntf(), parse.getProperties(), t));
                    i.remove();
                }
            }
        }

        if (prsd != null && processed.get() == 0) {
            ((Structures.Parsed) prsd).setInvalid(true);
        }
    }

    @SuppressWarnings("unchecked")
    public static void generateCodeForPrototype(CompilationUnit parser, PrototypeDescription<ClassOrInterfaceDeclaration> prsd, TypeDeclaration<?> type, AnnotationExpr prototype) {

        var typeDeclaration = type.asClassOrInterfaceDeclaration();

        log.info("Processing - {}", typeDeclaration.getNameAsString());

        var properties = nonNull(prsd) && nonNull(prsd.getCompiled()) ? (Structures.PrototypeDataHandler) prsd.getProperties() : getProperties(prototype);
        properties.setPrototypeName(typeDeclaration.getNameAsString());
        properties.setPrototypeFullName(typeDeclaration.getFullyQualifiedName().orElseThrow());
        addProcessingType(typeDeclaration.getNameAsString(), properties.getInterfacePackage(), properties.getInterfaceName(), properties.getClassPackage(), properties.getClassName());
        ensureParsedParents(typeDeclaration, properties);
        handleEnrichersSetup(properties);

        var unit = new CompilationUnit();
        unit.addImport("javax.annotation.processing.Generated");
        var spec = unit.addClass(properties.getClassName());
        unit.setPackageDeclaration(properties.getClassPackage());
        spec.addModifier(PUBLIC);

        if (properties.isGenerateConstructor()) {
            spec.addConstructor(PUBLIC);
        }

        var iUnit = new CompilationUnit();
        iUnit.addImport("javax.annotation.processing.Generated");
        var intf = iUnit.addClass(properties.getInterfaceName()).setInterface(true);
        iUnit.setPackageDeclaration(properties.getInterfacePackage());
        intf.addModifier(PUBLIC);

        var parse = (Structures.Parsed) lookup.findParsed(getClassName(typeDeclaration));

        parse.setParsedName(spec.getNameAsString());
        parse.setParsedFullName(spec.getFullyQualifiedName().get());
        parse.setInterfaceName(intf.getNameAsString());
        parse.setInterfaceFullName(intf.getFullyQualifiedName().get());
        parse.setProperties(properties);
        parse.setFiles(List.of(unit, iUnit));
        parse.setSpec(spec);
        parse.setIntf(intf);

        if (isNull(prsd) || !prsd.isNested() || isNull(prsd.getParentClassName())) {
            spec.addAnnotation(parse.getParser().parseAnnotation("@Generated(value=\"" + properties.getPrototypeName() + "\", comments=\"" + properties.getInterfaceName() + "\")").getResult().get());
            intf.addAnnotation(parse.getParser().parseAnnotation("@Generated(value=\"" + properties.getPrototypeName() + "\", comments=\"" + properties.getClassName() + "\")").getResult().get());
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
                    handleParsedExtendedType(parse, parsed, spec, intf, properties, t);
                }
            } else {
                handleExternalInterface(parse, typeDeclaration, spec, intf, t);
            }
        });

        if (nonNull(properties.getMixInClass()) && isNull(parse.getMixIn())) {
            throw new GenericCodeGenException("Mix in Class " + properties.getPrototypeName() + " must inherit " + properties.getMixInClass());
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

        for (var member : type.getMembers()) {
            if (member.isMethodDeclaration()) {
                var declaration = member.asMethodDeclaration();

                if (!declaration.isDefault()) {
                    var ignore = getIgnores(member);
                    PrototypeField field = Structures.FieldData.builder().parsed(parse).build();
                    if (!ignore.isForField()) {
                        field = addField(parse, typeDeclaration, spec, declaration, null);
                    }
                    if (!ignore.isForClass()) {
                        if (properties.isClassGetters()) {
                            addGetter(typeDeclaration, spec, declaration, true, field);
                        }
                        if (properties.isClassSetters()) {
                            addSetter(typeDeclaration, spec, declaration, true, field);
                        }
                    }
                    if (!ignore.isForInterface()) {
                        addGetter(typeDeclaration, intf, declaration, false, field);
                        if (properties.isInterfaceSetters()) {
                            addSetter(typeDeclaration, intf, declaration, false, field);
                        }
                    }
                } else {
                    handleDefaultMethod(parse, spec, intf, declaration);
                }
            } else if (member.isClassOrInterfaceDeclaration()) {
                processInnerClass(parse, typeDeclaration, spec, member.asClassOrInterfaceDeclaration());
            } else if (member.isFieldDeclaration()) {
                processConstant(typeDeclaration, spec, intf, member.asFieldDeclaration());
            } else {
                log.error("Can't process method " + member);
            }
        }

        unit.setComment(new BlockComment("Generated code by Binis' code generator."));
        iUnit.setComment(new BlockComment("Generated code by Binis' code generator."));

        lookup.registerGenerated(getClassName(spec), parse);

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

        processingTypes.remove(typeDeclaration.getNameAsString());
        parse.setProcessed(true);
    }

    private static void handleParsedExtendedType(Structures.Parsed<ClassOrInterfaceDeclaration> parse, PrototypeDescription<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, PrototypeData properties, ClassOrInterfaceType type) {
        if (!parsed.getProperties().isBase() && !parsed.getProperties().getPrototypeName().equals(parse.getProperties().getMixInClass())) {
            parsed.getFields().forEach(field -> {
                var method = field.getDescription().clone();
                var dummy = envelopWithDummyClass(method);
                field.getDescription().findCompilationUnit().ifPresent(u -> u.getImports().forEach(dummy::addImport));

                addField(parse, parsed.getDeclaration().asClassOrInterfaceDeclaration(), spec, method, nonNull(field.getGenerics()) ? field.getGenerics().values().iterator().next() : buildGeneric(field.getType(), type, parsed.getDeclaration().asClassOrInterfaceDeclaration()));
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
        lookup.parsed().stream().filter(p -> p.isNested() && nonNull(p.getParentClassName()) && p.getParentClassName().equals(parse.getPrototypeClassName())).map(Structures.Parsed.class::cast).forEach(p -> {
            p.setInterfaceName(parse.getInterfaceName() + '.' + p.getInterfaceName());
            p.getProperties().setInterfacePackage(parse.getProperties().getInterfacePackage() + '.' + parse.getInterfaceName());
            p.setInterfaceFullName(parse.getProperties().getInterfacePackage() + '.' + p.getInterfaceName());
            p.setParsedName(parse.getParsedName() + '.' + p.getParsedName());
            p.getProperties().setClassPackage(parse.getProperties().getClassPackage() + '.' + parse.getParsedName());
            p.setParsedFullName(parse.getProperties().getClassPackage() + '.' + p.getParsedName());
        });
    }

    private static void mergeNestedPrototypes(Structures.Parsed<ClassOrInterfaceDeclaration> parse) {
        lookup.parsed().stream().filter(p -> p.isNested() && nonNull(p.getParentClassName()) && p.getParentClassName().equals(parse.getPrototypeClassName())).forEach(p -> {
            parse.getSpec().addMember(p.getSpec().addModifier(STATIC));
            mergeImports(p.getSpec().findCompilationUnit().get(), parse.getSpec().findCompilationUnit().get());
            parse.getIntf().addMember(p.getIntf());
            mergeImports(p.getIntf().findCompilationUnit().get(), parse.getIntf().findCompilationUnit().get());
        });
    }

    private static void handleClassGenerics(Structures.Parsed<ClassOrInterfaceDeclaration> parse) {
        var unit = parse.getDeclaration().findCompilationUnit().get();
        parse.getDeclaration().asClassOrInterfaceDeclaration().getTypeParameters().forEach(t -> {
            parse.getSpec().addTypeParameter(t);
            parse.getIntf().addTypeParameter(t);
            t.getTypeBound().forEach(tb -> {
                handleType(unit, parse.getSpec().findCompilationUnit().get(), tb);
                handleType(unit, parse.getIntf().findCompilationUnit().get(), tb);
            });
        });
    }

    private static void handleInitializations(Structures.Parsed<ClassOrInterfaceDeclaration> parse) {
        var list = parse.getDeclaration().getAnnotations().stream().filter(a -> "Initialize".equals(a.getNameAsString())).collect(Collectors.toList());
        var constructor = parse.getSpec().getDefaultConstructor().orElseGet(
                () -> parse.getSpec().addConstructor(PUBLIC).setBody(nonNull(parse.getBase()) ? new BlockStmt().addStatement("super();") : new BlockStmt()));
        var unit = parse.getSpec().findCompilationUnit().get();
        list.forEach(ann -> {
            var statement = Holder.of("");
            ann.getChildNodes().stream().filter(MemberValuePair.class::isInstance).map(MemberValuePair.class::cast).forEach(pair -> {
                switch (pair.getNameAsString()) {
                    case "field":
                        statement.set("this." + pair.getValue().asStringLiteralExpr().asString() + " = " + statement.get());
                        break;
                    case "expression":
                        statement.set(statement.get() + pair.getValue().asStringLiteralExpr().asString() + ";");
                        break;
                    case "imports":
                        pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).forEach(i -> unit.addImport(i.asString()));
                        break;
                    default:
                        log.warn("Invalid @Initialize member {}", pair.getNameAsString());
                }
            });
            constructor.getBody().addStatement(statement.get());
        });
    }

    public static Optional<AnnotationExpr> getCodeAnnotation(TypeDeclaration<?> type) {
        for (var name : Structures.defaultProperties.keySet()) {
            var ann = type.getAnnotationByName(name);
            if (ann.isPresent()) {
                return ann;
            }
        }
        if (type.isClassOrInterfaceDeclaration() && type.asClassOrInterfaceDeclaration().getAnnotationByClass(CodeClassAnnotations.class).isEmpty()) {
            log.warn("Type {} is not annotated with Code annotation!", type.getName());
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
                        notNull(getExternalClassName(unit, t.getNameAsString()), className ->
                                notNull(loadClass(className), cls ->
                                        cleanUpInterface(cls, intf)))));
    }

    private static void handleDefaultMethod(Structures.Parsed<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, MethodDeclaration declaration) {
        var unit = declaration.findCompilationUnit().get();
        var ignores = getIgnores(declaration);
        var method = declaration.clone().removeModifier(DEFAULT);
        method.getAnnotationByClass(Ignore.class).ifPresent(method::remove);
        if (!ignores.isForInterface()) {
            if (ignores.isForClass()) {
                declaration.getBody().ifPresent(b -> {
                    var body = b.clone();
                    handleDefaultInterfaceMethodBody(parse, body, false);
                    method.setBody(body).addModifier(DEFAULT);
                    intf.addMember(handleForAnnotations(unit, method, false));
                });
            } else {
                if (methodExists(intf, method, false)) {
                    intf.getChildNodes().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).filter(m -> m.getNameAsString().equals(method.getNameAsString())).findFirst().ifPresent(intf::remove);
                }

                intf.addMember(handleForAnnotations(unit, method.clone(), false).setBody(null));
            }
        }

        if (!ignores.isForClass()) {
            method.addModifier(PUBLIC);

            declaration.getBody().ifPresent(b -> {
                var body = b.clone();
                handleDefaultMethodBody(parse, body, false);
                method.setBody(body);
            });

            if (methodExists(spec, method, false)) {
                spec.getChildNodes().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).filter(m -> m.getNameAsString().equals(method.getNameAsString())).findFirst().ifPresent(spec::remove);
            }

            spec.addMember(handleForAnnotations(unit, method, true));
        }
    }

    private static MethodDeclaration handleForAnnotations(CompilationUnit unit, MethodDeclaration method, boolean isClass) {
        var chk = isClass ? "ForInterface" : "ForImplementation";

        for (var i = method.getAnnotations().size() - 1; i > 0; i--) {
            if (chk.equals(method.getAnnotation(i - 1).getNameAsString())) {
                method.remove(method.getAnnotation(i));
            }
        }

        for (var i = method.getAnnotations().size() - 1; i >= 0; i--) {
            var ann = method.getAnnotation(i);
            notNull(getExternalClassName(unit, method.getAnnotation(i).getNameAsString()), className ->
                    notNull(loadClass(className), cls -> {
                        if (cls.isAnnotationPresent(CodeAnnotation.class)) {
                            method.remove(ann);
                        }
                    }));
        }

        return method;
    }

    private static boolean handleDefaultMethodBody(PrototypeDescription<ClassOrInterfaceDeclaration> parse, Node node, boolean isGetter) {
        var typeDeclaration = parse.getDeclaration();
        if (isGetter && node.getParentNode().isPresent() && node.getParentNode().get().getChildNodes().size() == 2 && node.getParentNode().get().getChildNodes().get(1) instanceof SimpleName) {
            var name = (SimpleName) node.getParentNode().get().getChildNodes().get(1);
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
                if (n instanceof MethodCallExpr) {
                    var method = (MethodCallExpr) n;
                    var parent = parse.findField(method.getNameAsString());
                    if (parent.isEmpty() && nonNull(parse.getBase())) {
                        parent = parse.getBase().findField(method.getNameAsString());
                    }

                    if (parent.isPresent()) {
                        notNull(parent.get().getPrototype(), p ->
                                handleDefaultMethodBody(p, n, true));

                        return node.replace(method, new FieldAccessExpr().setName(method.getName()));
                    } else {
                        if (handleDefaultMethodBody(parse, n, false)) {
                            handleDefaultMethodBody(parse, node, false);
                        }
                    }

                } else {
                    if (handleDefaultMethodBody(parse, n, isGetter)) {
                        handleDefaultMethodBody(parse, node, isGetter);
                    }
                }
            }
        }
        return false;
    }

    private static boolean handleDefaultInterfaceMethodBody(PrototypeDescription<ClassOrInterfaceDeclaration> parse, Node node, boolean isGetter) {
        var typeDeclaration = parse.getDeclaration();
        if (isGetter && node.getParentNode().isPresent() && node.getParentNode().get().getChildNodes().size() == 2 && node.getParentNode().get().getChildNodes().get(1) instanceof SimpleName) {
            var name = (SimpleName) node.getParentNode().get().getChildNodes().get(1);
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
                if (n instanceof MethodCallExpr) {
                    var method = (MethodCallExpr) n;
                    var parent = parse.findField(method.getNameAsString());
                    if (parent.isEmpty() && nonNull(parse.getBase())) {
                        parent = parse.getBase().findField(method.getNameAsString());
                    }

                    if (parent.isPresent()) {
                        notNull(lookup.findParsed(getExternalClassName(typeDeclaration.findCompilationUnit().get(), parent.get().getType())), p ->
                                handleDefaultInterfaceMethodBody(p, n, true));

                        if (nonNull(parent.get().getInterfaceGetter())) {
                            method.setName(parent.get().getInterfaceGetter().getName());
                        }

                        return true;
                    } else {
                        if (handleDefaultInterfaceMethodBody(parse, n, false)) {
                            handleDefaultInterfaceMethodBody(parse, node, false);
                        }
                    }

                } else {
                    if (handleDefaultInterfaceMethodBody(parse, n, isGetter)) {
                        handleDefaultInterfaceMethodBody(parse, node, isGetter);
                    }
                }
            }
        }
        return false;
    }


    private static void checkForDeclaredConstants(Node type) {
        //TODO: Handle more cases
        for (var node : type.getChildNodes()) {
            if (node instanceof FieldAccessExpr) {
                var expr = (FieldAccessExpr) node;
                if (expr.getChildNodes().size() > 1 && expr.getChildNodes().get(0) instanceof NameExpr && expr.getChildNodes().get(1) instanceof SimpleName) {
                    var namespace = (NameExpr) expr.getChildNodes().get(0);
                    var name = ((SimpleName) expr.getChildNodes().get(1)).asString();

                    var decl = declaredConstants.get(namespace.getNameAsString());
                    if (nonNull(decl)) {
                        decl.stream().filter(p -> p.getValue().equals(name)).findFirst().ifPresent(
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
                if (node instanceof ClassExpr) {
                    var expr = (ClassExpr) node;
                    notNull(lookup.findParsed(getExternalClassName(unit, expr.getTypeAsString())), p -> {
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

    private static void processConstant(ClassOrInterfaceDeclaration prototype, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, FieldDeclaration field) {
        var consts = getConstants(field);
        var f = field.clone();
        var name = field.getVariable(0).getNameAsString();
        f.getAnnotationByName("CodeConstant").ifPresent(f::remove);
        if (consts.isForInterface()) {
            intf.addMember(f);
            addDeclaredConstant(prototype.getNameAsString(), intf.getNameAsString(), name);
        } else {
            spec.addMember(f.addModifier(consts.isForPublic() ? PUBLIC : "serialVersionUID".equals(name) ? PRIVATE : PROTECTED).addModifier(STATIC).addModifier(FINAL));
            addDeclaredConstant(prototype.getNameAsString(), spec.getNameAsString(), name);
        }
    }

    private static Structures.PrototypeDataHandler getProperties(AnnotationExpr prototype) {
        var type = (ClassOrInterfaceDeclaration) prototype.getParentNode().get();
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);

        var builder = Structures.builder(prototype.getNameAsString())
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type));
        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                var name = pair.getNameAsString();
                switch (name) {
                    case "name":
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            var intf = value.replace("Entity", "");
                            builder.name(value)
                                    .className(value)
                                    .interfaceName(intf)
                                    .longModifierName(intf + "." + Constants.MODIFIER_INTERFACE_NAME);
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
                        value = pair.getValue().asStringLiteralExpr().asString();
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
                        if (StringUtils.isNotBlank(value)) {
                            builder.baseModifierClass(value);
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
                            builder.classPackage(pair.getValue().asStringLiteralExpr().asString());
                        }
                        break;
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
                        checkEnrichers(builder::enrichers, pair.getValue().asArrayInitializerExpr());
                        break;
                    case "inheritedEnrichers":
                        checkEnrichers(builder::inheritedEnrichers, pair.getValue().asArrayInitializerExpr());
                        break;
                    case "options":
                        checkOptions(builder::options, pair.getValue().asArrayInitializerExpr());
                        break;
                    default:
                }
            }
        });

        if (cName.equals(iName.get())) {
            cName = iName.get() + "Impl";
        }

        builder.className(cName).interfaceName(iName.get()).longModifierName(iName.get() + ".Modify");

        var result = builder.build();

        if (isNull(result.getEnrichers())) {
            result.setEnrichers(new ArrayList<>());
        }

        if (isNull(result.getInheritedEnrichers())) {
            result.setInheritedEnrichers(new ArrayList<>());
        }

        notNull(result.getPredefinedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getEnrichers(), e)));

        notNull(result.getPredefinedInheritedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getInheritedEnrichers(), e)));

        return result;
    }

    private static void checkEnrichers(Consumer<List<PrototypeEnricher>> consumer, ArrayInitializerExpr expression) {
        var list = new ArrayList<PrototypeEnricher>();
        expression.getValues().stream()
                .filter(Expression::isClassExpr)
                .map(e -> loadClass(getExternalClassName(expression.findCompilationUnit().get(), e.asClassExpr().getType().asString())))
                .filter(Objects::nonNull)
                .map(CodeFactory::create)
                .filter(Objects::nonNull)
                .filter(i -> PrototypeEnricher.class.isAssignableFrom(i.getClass()))
                .forEach(e ->
                        with(((PrototypeEnricher) e), enricher -> {
                            enricher.init(lookup);
                            list.add(enricher);
                        }));
        consumer.accept(list);
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
    private static void checkEnrichers(List<PrototypeEnricher> list, Class enricher) {
        if (list.stream().noneMatch(e -> enricher.isAssignableFrom(e.getClass()))) {
            var e = CodeFactory.create(enricher);
            if (e instanceof PrototypeEnricher) {
                with(((PrototypeEnricher) e), r -> {
                    r.init(lookup);
                    list.add(r);
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void ensureParsedParents(ClassOrInterfaceDeclaration declaration, PrototypeData properties) {
        for (var extended : declaration.getExtendedTypes()) {
            var parsed = getParsed(extended);
            if (nonNull(parsed)) {
                if (isNull(parsed.getFiles())) {
                    generateCodeForClass(parsed.getDeclaration().findCompilationUnit().get(), parsed);
                } else {
                    if (!parsed.isProcessed()) {
                        notProcessed.add(Pair.of(properties, parsed));
                    }
                }
            } else {
                handleCompiledPrototype(getExternalClassName(declaration.findCompilationUnit().get(), extended.getNameAsString()));
            }
        }

        notNull(properties.getMixInClass(), c ->
                notNull(getExternalClassName(declaration.findCompilationUnit().get(), c),
                        name -> notNull(lookup.findParsed(name), parse ->
                                ifNull(parse.getFiles(), () -> generateCodeForClass(parse.getDeclaration().findCompilationUnit().get(), parse)))));

        declaration.getChildNodes().stream().filter(ClassOrInterfaceDeclaration.class::isInstance).map(ClassOrInterfaceDeclaration.class::cast).forEach(cls ->
                Generator.getCodeAnnotation(cls).ifPresent(ann -> {
                    var clsName = getClassName(cls);
                    lookup.registerParsed(clsName,
                            Structures.Parsed.builder().declaration(cls.asTypeDeclaration()).parser(lookup.getParser()).nested(true).parentClassName(getClassName(declaration)).build());

                    notNull(lookup.findParsed(clsName), parse ->
                            ifNull(parse.getFiles(), () -> generateCodeForPrototype(declaration.findCompilationUnit().get(), parse, cls, ann)));
                }));
    }

    private static void ensureParsedParents(EnumDeclaration declaration, PrototypeDescription<?> parse) {
        if (nonNull(parse) && isNull(parse.getCompiled()) &&  isNull(parse.getFiles())) {
            if (parse.getDeclaration().isEnumDeclaration()) {
                generateCodeForEnum(parse.getDeclaration().findCompilationUnit().get(), parse, parse.getDeclaration(), parse.getDeclaration().getAnnotationByClass(EnumPrototype.class).orElse(null));
            } else {
                throw new GenericCodeGenException("Class '" + parse.getDeclaration().getFullyQualifiedName().get() + "' is not enum!");
            }
        }
    }

    private static void implementPrototype(Structures.Parsed<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, PrototypeDescription<ClassOrInterfaceDeclaration> declaration, Map<String, Type> generic, boolean external) {
        var properties = parse.getProperties();
        for (var method : declaration.getSpec().getMethods()) {
            if (declaration.isValid() && declaration.getDeclaration().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).anyMatch(m -> m.getNameAsString().equals(method.getNameAsString()))) {
                spec.addMember(method.clone());
            } else {
                if (method.getNameAsString().startsWith("get") || method.getNameAsString().startsWith("is")) {
                    if (!external || parse.getDeclaration().stream().filter(MethodDeclaration.class::isInstance).map(MethodDeclaration.class::cast).noneMatch(m ->
                            m.isDefault() && m.getNameAsString().equals(method.getNameAsString()) && m.getTypeAsString().equals(method.getTypeAsString()))) {
                        var field = addFieldFromGetter(parse, spec, method, generic, external);
                        if (nonNull(field) && properties.isClassGetters()) {
                            addGetterFromGetter(spec, method, true, generic, field);
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
        handleImports(declaration.getSpec(), spec);
    }

    private static boolean handleExternalInterface(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceType type) {
        var className = getExternalClassName(declaration.findCompilationUnit().get(), type.getNameAsString());
        if (nonNull(className)) {
            var cls = loadClass(className);
            if (nonNull(cls)) {
                if (cls.isInterface()) {
                    var generics = cls.getGenericInterfaces();
                    var interfaces = cls.getInterfaces();
                    Map<String, Type> typeArguments = null;
                    if (type.getTypeArguments().isPresent()) {
                        typeArguments = processGenerics(cls, type.getTypeArguments().get());
                    }
                    for (var i = 0; i < interfaces.length; i++) {
                        java.lang.reflect.Type[] types = null;
                        if (generics[i] instanceof ParameterizedType) {
                            types = ((ParameterizedType) generics[i]).getActualTypeArguments();
                        }
                        handleExternalInterface(parsed, declaration, spec, interfaces[i], typeArguments, types);
                    }
                    handleExternalInterface(parsed, declaration, spec, cls, typeArguments, null);
                    if (nonNull(intf)) {
                        intf.addExtendedType(handleType(declaration.findCompilationUnit().get(), intf.findCompilationUnit().get(), type));
                    } else {
                        if (spec.getImplementedTypes().stream().noneMatch(type::equals)) {
                            spec.addImplementedType(handleType(declaration.findCompilationUnit().get(), spec.findCompilationUnit().get(), type));
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

                            var org = external.getSpec();
                            ((Structures.Parsed) external).setSpec(org.findCompilationUnit().get().clone().getType(0).asClassOrInterfaceDeclaration());
                            implementPrototype(parsed, spec, external, generics, true);
                            ((Structures.Parsed) external).setSpec(org);
                            if (nonNull(intf)) {
                                intf.addExtendedType(external.getDeclaration().getNameAsString());
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
            if (genericIntf[i] instanceof ParameterizedType) {
                types = ((ParameterizedType) genericIntf[i]).getActualTypeArguments();
            }
            handleExternalInterface(parsed, declaration, spec, interfaces[i], generic, types);
        }

        var properties = parsed.getProperties();

        for (var method : cls.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !method.isDefault() && !defaultMethodExists(declaration, method)) {
                if (method.getParameterCount() == 0 && method.getName().startsWith("get") || method.getName().startsWith("is") && method.getReturnType().getCanonicalName().equals("boolean")) {
                    var field = addFieldFromGetter(parsed, spec, method, generic);
                    if (nonNull(field) && properties.isClassGetters()) {
                        addGetterFromGetter(spec, method, true, generic, field);
                    }
                } else if (method.getParameterCount() == 1 && method.getName().startsWith("set") && method.getReturnType().getCanonicalName().equals("void")) {
                    var field = addFieldFromSetter(parsed, spec, method, generic);
                    if (nonNull(field) && properties.isClassSetters()) {
                        addSetterFromSetter(spec, method, true, generic, field);
                    }
                } else {
                    log.error("Method {} of {} is nor getter or setter. Not implemented!", method.getName(), cls.getCanonicalName());
                }
            }
        }
    }

    private static void handleMixin(PrototypeDescription<ClassOrInterfaceDeclaration> parse) {
        if (nonNull(parse.getProperties().getMixInClass())) {
            var parent = lookup.findParsed(getExternalClassName(parse.getDeclaration().findCompilationUnit().get(), parse.getProperties().getMixInClass()));
            if (parent != null) {
                var spec = parse.getSpec();
                var intf = parse.getIntf();
                var parentSpec = parent.getSpec();
                var parentIntf = parent.getIntf();
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

    private static void setScope(Type type, String scope) {
        if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getScope().isPresent()) {
            type.asClassOrInterfaceType().getScope().get().setName(scope);
        }
    }

    private static String getScope(Type type) {
        if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getScope().isPresent()) {
            return type.asClassOrInterfaceType().getScope().get().getNameAsString();
        }

        return null;
    }

    public static String handleType(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration destination, Type type) {
        return handleType(source, destination, type, null);
    }


    public static String handleType(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration destination, Type type, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap) {
        return handleType(source.findCompilationUnit().get(), destination.findCompilationUnit().get(), type, prototypeMap);
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, Type type) {
        return handleType(source, destination, type, null);
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, Type type, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap) {
        var result = type.toString();
        if (type.isClassOrInterfaceType()) {
            var generic = handleGenericTypes(source, destination, type.asClassOrInterfaceType(), prototypeMap);
            if (!isEmpty(generic)) {
                result = type.asClassOrInterfaceType().getNameAsString() + "<" + String.join(",", generic) + ">";
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
            return arguments.get().stream().map(n -> handleType(source, destination, n.toString(), true, prototypeMap)).collect(Collectors.toList());
        }
    }

    public static List<Pair<String, Boolean>> getGenericsList(CompilationUnit source, CompilationUnit destination, ClassOrInterfaceType type, boolean isCollection) {
        var arguments = type.getTypeArguments();
        if (arguments.isEmpty() || arguments.get().isEmpty()) {
            return Collections.singletonList(Pair.of("Object", false));
        } else {
            return arguments.get().stream().map(n -> handleType(source, destination, n.toString(), true)).map(t ->
                    Pair.of(t, lookup.parsed().stream().anyMatch(p -> getExternalClassName(destination, t).equals(p.getInterfaceFullName())))).collect(Collectors.toList());
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
                lookup.addPrototypeMap(parse, prototypeMap);
            }

            if (isNull(processing)) {
                if (isNull(parse.getFiles())) {
                    generateCodeForClass(parse.getDeclaration().findCompilationUnit().get(), parse);
                }

                destination.addImport(parse.getIntf().getFullyQualifiedName().get());

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
                    if (!full.contains(".prototype.")) { //TODO: Better way to hanle prototype self references
                        destination.findCompilationUnit().ifPresent(u -> u.addImport(full));
                    }
                }
            }
            return type;
        }
    }

    private static void handleFieldAnnotations(CompilationUnit unit, FieldDeclaration field, MethodDeclaration method, boolean compiledAnnotations, PrototypeField proto) {
        var next = Holder.of(false);
        method.getAnnotations().forEach(a ->
                notNull(getExternalClassName(unit, a.getNameAsString()), name -> {
                    var ann = loadClass(name);
                    if (nonNull(ann)) {
                        if (ForInterface.class.equals(ann)) {
                            next.set(true);
                            return;
                        } else if (isNull(ann.getAnnotation(CodeAnnotation.class))) {
                            var target = ann.getAnnotation(Target.class);
                            if (next.get()) {
                                if (target == null || target.toString().contains("METHOD")) {
                                    handleAnnotation(unit, proto.generateInterfaceGetter(), a);
                                }
                            } else {
                                if (target == null || target.toString().contains("FIELD")) {
                                    handleAnnotation(unit, field, a);
                                }
                            }
                        } else {
                            if (CodeFieldAnnotations.class.isAssignableFrom(ann)) {
                                a.getChildNodes().stream().filter(ArrayInitializerExpr.class::isInstance).findFirst().ifPresent(e ->
                                        e.getChildNodes().forEach(n ->
                                                field.addAnnotation(((StringLiteralExpr) n).asStringLiteralExpr().asString())));
                            } else if (Default.class.isAssignableFrom(ann)) {
                                if (a.isSingleMemberAnnotationExpr()) {
                                    field.getVariables().iterator().next().setInitializer(a.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString());
                                } else if (a.isNormalAnnotationExpr()) {
                                    a.asNormalAnnotationExpr().getPairs().forEach(p -> {
                                        if ("value".equals(p.getName().asString())) {
                                            field.getVariables().iterator().next().setInitializer(p.getValue().asStringLiteralExpr().asString());
                                        }
                                    });
                                }
                            } else if (DefaultString.class.isAssignableFrom(ann)) {
                                if (a.isSingleMemberAnnotationExpr()) {
                                    field.getVariables().iterator().next().setInitializer("\"" + a.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString() + "\"");
                                } else if (a.isNormalAnnotationExpr()) {
                                    a.asNormalAnnotationExpr().getPairs().forEach(p -> {
                                        if ("value".equals(p.getName().asString())) {
                                            field.getVariables().iterator().next().setInitializer("\"" + p.getValue().asStringLiteralExpr().asString() + "\"");
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        var parsed = lookup.findExternal(name);
                        if (nonNull(parsed) && parsed.getDeclaration().isAnnotationDeclaration()) {
                            if (parsed.getDeclaration().getAnnotationByClass(CodeAnnotation.class).isEmpty()) {
                                if (next.get()) {
                                    if (Helpers.annotationHasTarget(parsed, "ElementType.METHOD")) {
                                        handleAnnotation(unit, proto.generateInterfaceGetter(), a);
                                    } else {
                                        log.warn("Invalid annotation target {}", name);
                                    }
                                } else {
                                    if (Helpers.annotationHasTarget(parsed, "ElementType.FIELD")) {
                                        handleAnnotation(unit, field, a);
                                    } else {
                                        log.warn("Invalid annotation target {}", name);
                                    }
                                }
                            }
                        } else {
                            if (compiledAnnotations) {
                                if (next.get()) {
                                    handleMissingAnnotation(unit, proto.generateInterfaceGetter(), a);
                                } else {
                                    handleMissingAnnotation(unit, field, a);
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

    private static void handleAnnotation(CompilationUnit unit, BodyDeclaration<?> body, AnnotationExpr ann) {
        body.getAnnotations().stream().filter(a -> a.getNameAsString().equals(ann.getNameAsString())).findFirst().ifPresent(a ->
                body.getAnnotations().remove(a));
        body.addAnnotation(ann);

        notNull(getExternalClassNameIfExists(unit, ann.getNameAsString()), i ->
                body.findCompilationUnit().ifPresent(u -> u.addImport(sanitizeImport(i))));

    }

    private static void handleMissingAnnotation(CompilationUnit unit, BodyDeclaration<?> body, AnnotationExpr ann) {
        var existing = body.getAnnotations().stream().filter(a -> a.getNameAsString().equals(ann.getNameAsString())).findFirst();
        if (existing.isEmpty()) {
            body.addAnnotation(ann);

            notNull(getExternalClassNameIfExists(unit, ann.getNameAsString()), i ->
                    body.findCompilationUnit().ifPresent(u -> u.addImport(sanitizeImport(i))));
        }
    }

    private static void handleAnnotation(CompilationUnit unit, MethodDeclaration method, AnnotationExpr ann, CompilationUnit destinationUnit) {
        method.getAnnotations().stream().filter(a -> a.getNameAsString().equals(ann.getNameAsString())).findFirst().ifPresent(a ->
                method.getAnnotations().remove(a));
        method.addAnnotation(ann);

        notNull(getExternalClassNameIfExists(unit, ann.getNameAsString()), destinationUnit::addImport);
    }


    private static void handleMethodAnnotations(MethodDeclaration method, MethodDeclaration declaration) {
        declaration.getAnnotations().forEach(a ->
                notNull(getExternalClassName(declaration.findCompilationUnit().get(), a.getNameAsString()), name -> {
                    var ann = loadClass(name);
                    if (nonNull(ann) && !ann.isAnnotationPresent(CodeAnnotation.class)) {
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
                        notNull(getExternalClassName(unit, a.getNameAsString()), name -> {
                            var ann = loadClass(name);
                            if (nonNull(ann)) {
                                if (ForInterface.class.equals(ann)) {
                                    next.set(true);
                                    return;
                                }
                                if (isNull(ann.getAnnotation(CodeAnnotation.class))) {
                                    var target = ann.getAnnotation(Target.class);
                                    if (target != null && !target.toString().equals("TYPE")) {
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
                                        spec.addAnnotation(a);
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

    public static <T> T findInheritanceProperty(ClassOrInterfaceDeclaration spec, PrototypeData properties, BiFunction<ClassOrInterfaceDeclaration, PrototypeData, T> func) {
        var data = func.apply(spec, properties);
        if (isNull(data)) {
            for (var type : spec.getExtendedTypes()) {
                var parse = lookup.findGenerated(getClassName(type));
                if (nonNull(parse)) {
                    data = findInheritanceProperty(parse.getDeclaration().asClassOrInterfaceDeclaration(), parse.getProperties(), func);
                    if (nonNull(data)) {
                        break;
                    }
                }
            }
        }
        return data;
    }

    private static void processInnerClass(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration cls) {
        cls.getImplementedTypes().forEach(t -> {
            if (handleExternalInterface(parsed, declaration, spec, null, t)) {
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

    private static PrototypeField addField(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration method, Type generic) {
        var compiledAnnotations = false;
        PrototypeField result = null;
        var fieldName = method.getNameAsString();
        var fieldProto = findField(parsed, fieldName);
        var field = nonNull(fieldProto) ? fieldProto.getDeclaration() : null;
        var unit = type.findCompilationUnit().get();
        if (isNull(field)) {
            var genericMethod = !method.getTypeParameters().isEmpty() && method.getTypeAsString().equals(method.getTypeParameter(0).getNameAsString());
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
            var collection = CollectionsHandler.isCollection(field.getVariable(0).getType());
            result = Structures.FieldData.builder()
                    .parsed(parsed)
                    .name(fieldName)
                    .description(method)
                    .declaration(field)
                    .collection(collection)
                    .ignores(getIgnores(method))
                    .genericMethod(genericMethod)
                    .genericField(isGenericType(method.getType(), parsed.getDeclaration()))
                    .generics(nonNull(generic) ? Map.of(generic.asString(), generic) : null)
                    .prototype(collection ? prototypeMap.get(CollectionsHandler.getCollectionType(method.getType())) :
                            (isNull(generic) ? lookup.findParsed(getExternalClassName(unit, method.getType().asString())) : null))
                    .typePrototypes(!prototypeMap.isEmpty() ? prototypeMap : null)
                    .type(field.getElementType().asString())
                    .fullType(getExternalClassNameIfExists(spec.findCompilationUnit().get(), field.getElementType().asString()))
                    .parent(nonNull(parsed.getBase()) ? findField(parsed.getBase(), fieldName) : null)
                    .build();
            parsed.getFields().add(result);
        } else {
            var proto = parsed.getFields().stream().filter(d -> d.getName().equals(fieldName)).findFirst();
            if (proto.isPresent()) {
                result = proto.get();
                handleType(type, spec, method.getType());
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
        return result;
    }

    private static boolean isGenericType(Type type, TypeDeclaration<ClassOrInterfaceDeclaration> declaration) {
        return declaration.asClassOrInterfaceDeclaration().getTypeParameters().stream().anyMatch(p -> p.getNameAsString().equals(type.asString()));
    }

    private static PrototypeField addFieldFromGetter(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, MethodDeclaration method, Map<String, Type> generic, boolean external) {
        PrototypeField result = null;
        var genericMethod = !method.getTypeParameters().isEmpty() && method.getTypeAsString().equals(method.getTypeParameter(0).getNameAsString());
        var fieldName = getFieldName(method.getNameAsString());
        if (!fieldExists(parsed, fieldName)) {
            FieldDeclaration field;
            if (nonNull(generic) && !generic.isEmpty()) {
                field = spec.addField(generic.get(method.getTypeAsString()), fieldName, PROTECTED);
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
                    .fullType(genericMethod ? null : getExternalClassNameIfExists(spec.findCompilationUnit().get(), field.getElementType().asString()))
                    .type(genericMethod ? method.getTypeParameter(0).getNameAsString() : field.getElementType().asString())
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
                    .type(field.getElementType().asString())
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
        if (!fieldExists(parsed, fieldName)) {
            FieldDeclaration field;
            MethodDeclaration description;
            PrototypeDescription<ClassOrInterfaceDeclaration> prototype = null;
            if (nonNull(generic)) {
                var sig = parseMethodSignature(method);
                var type = generic.get(sig);
                if (isNull(type)) {
                    if (Helpers.isPrimitiveType(sig)) {
                        type = new PrimitiveType().setType(PrimitiveType.Primitive.valueOf(sig.toUpperCase()));
                    } else {
                        type = new ClassOrInterfaceType().setName(sig);
                    }
                }

                handleType(parsed.getDeclaration().asClassOrInterfaceDeclaration(), spec, type);
                prototype = lookup.findParsed(getExternalClassName(parsed.getDeclaration().findCompilationUnit().get(), type.asString()));

                if (nonNull(prototype)) {
                    type = new ClassOrInterfaceType().setName(prototype.getInterfaceName());
                }

                field = spec.addField(type, fieldName, PROTECTED);
                description = new MethodDeclaration().setName(fieldName).setType(type);
            } else {
                genericMethod = !method.getReturnType().getCanonicalName().equals(parseMethodSignature(method));
                field = spec.addField(method.getReturnType(), fieldName, PROTECTED);
                description = new MethodDeclaration().setName(fieldName).setType(method.getReturnType());
                if (!method.getReturnType().isPrimitive() && !method.getReturnType().getCanonicalName().startsWith("java.lang.")) {
                    spec.findCompilationUnit().get().addImport(method.getReturnType().getCanonicalName());
                }
            }

            var dummy = envelopWithDummyClass(description);

            if (method.getDeclaredAnnotations().length > 0) {
                for (var ann : method.getDeclaredAnnotations()) {
                    description.addAnnotation(ann.annotationType());
                    dummy.addImport(ann.annotationType().getPackageName());
                    field.addAnnotation(ann.annotationType());
                    //TODO: Check if the annotation can be applied to field.
                    //TODO: Handle annotation params
                }
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
                    .type(genericMethod ? parseMethodSignature(method) : field.getElementType().asString())
                    //TODO: enable ignores
                    .prototype(prototype)
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

    private static PrototypeField addFieldFromSetter(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, ClassOrInterfaceDeclaration spec, Method method, Map<String, Type> generic) {
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

            var dummy = envelopWithDummyClass(description);

            if (method.getDeclaredAnnotations().length > 0) {
                for (var ann : method.getDeclaredAnnotations()) {
                    description.addAnnotation(ann.annotationType());
                    dummy.addImport(ann.annotationType().getPackageName());
                    field.addAnnotation(ann.annotationType());
                    //TODO: Check if the annotation can be applied to field.
                    //TODO: Handle annotation params
                }
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
                    .type(genericMethod ? parseMethodSignature(method) : field.getElementType().asString())
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

    private static CompilationUnit envelopWithDummyClass(MethodDeclaration description) {
        var dummy = new CompilationUnit();
        dummy.setPackageDeclaration("dummy");
        dummy.addClass("Dummy").addMember(description);
        return dummy;
    }

    public static void addGetter(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field) {
        var name = getGetterName(declaration.getNameAsString(), declaration.getType().asString());
        if (!methodExists(spec, declaration, name, isClass)) {
            String rType;
            if (declaration.getTypeParameters().isEmpty()) {
                rType = handleType(type, spec, declaration.getType());
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
                handleMethodAnnotations(method, declaration);
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
        }
    }

    private static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field) {
        addGetterFromGetter(spec, declaration, isClass, null, field);
    }

    private static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, Map<String, Type> generic, PrototypeField field) {
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec
                    .addMethod(declaration.getNameAsString());
            if (nonNull(generic)) {
                method.setType(generic.get(declaration.getType().asString()));
            } else {
                method.setType(declaration.getType());
            }
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(getFieldName(declaration.getNameAsString())))));
                ((Structures.FieldData) field).setImplementationGetter(method);
            } else {
                method.setBody(null);
                ((Structures.FieldData) field).setInterfaceGetter(method);
            }
        }
    }

    private static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, Method declaration, boolean isClass, Map<String, Type> generic, PrototypeField field) {
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

    public static void addSetter(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field) {
        var fieldName = nonNull(field.getName()) ? field.getName() : declaration.getNameAsString();
        var name = getSetterName(fieldName);
        String returnType = null;
        if (nonNull(field.getType())) {
            returnType = field.getType();
        } else if (nonNull(field.getGenerics())) {
            returnType = field.getGenerics().get(declaration.getType().asString()).asString();
        }
        if (isNull(returnType)) {
            handleType(type, spec, declaration.getType());
        }
        var method = new MethodDeclaration()
                .setName(name)
                .setType("void")
                .addParameter(new Parameter().setName(fieldName).setType(returnType));
        if (!methodExists(spec, method, name, isClass)) {
            spec.addMember(method);
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + fieldName)).setValue(new NameExpr().setName(fieldName))));
                ((Structures.FieldData) field).setImplementationSetter(method);

                if (declaration.getTypeParameters().isNonEmpty()) {
                    method.getParameter(0).setType("Object");
                }
            } else {
                method.setBody(null);
                ((Structures.FieldData) field).setInterfaceSetter(method);

                if (declaration.getTypeParameters().isNonEmpty()) {
                    declaration.getTypeParameters().forEach(method::addTypeParameter);
                }
            }
        }
    }

    private static void addSetterFromSetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, PrototypeField field) {
        addSetterFromSetter(spec, declaration, isClass, null, field);
    }

    private static void addSetterFromSetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, Map<String, Type> generic, PrototypeField field) {
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec
                    .addMethod(declaration.getNameAsString());
            if (nonNull(generic)) {
                method.addParameter(new Parameter().setName(field.getName()).setType(generic.get(declaration.getParameter(0).getType().asString())));
            } else {
                method.addParameter(new Parameter().setName(field.getName()).setType(declaration.getParameter(0).getType()));
            }


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
            var names = new LocalVariableTableParameterNameDiscoverer().getParameterNames(declaration);
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
                notNull(getExternalClassNameIfExists(destinationUnit, ann.getNameAsString()), unit::addImport);
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

    public static void generateCodeForEnum(CompilationUnit declarationUnit, PrototypeDescription<?> prsd, TypeDeclaration<?> type, AnnotationExpr prototype) {
        if (type.isEnumDeclaration()) {
            var typeDeclaration = type.asEnumDeclaration();

            log.info("Processing - {}", typeDeclaration.getNameAsString());

            var properties = getEnumProperties(prototype);
            properties.setPrototypeName(typeDeclaration.getNameAsString());
            properties.setPrototypeFullName(typeDeclaration.getFullyQualifiedName().orElseThrow());

            var mixIn = withRes(properties.getMixInClass(), c ->
                    withRes(getExternalClassName(declarationUnit.findCompilationUnit().get(), c), Generator::findEnum));

            ensureParsedParents(typeDeclaration, mixIn);

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

            parse.setParsedName(spec.getNameAsString());
            parse.setParsedFullName(spec.getFullyQualifiedName().get());
            parse.setInterfaceName(intf.getNameAsString());
            parse.setInterfaceFullName(intf.getFullyQualifiedName().get());
            parse.setProperties(properties);
            parse.setFiles(List.of(unit, iUnit));
            parse.setSpec(spec);
            parse.setIntf(intf);
            parse.setCodeEnum(true);

            if (isNull(prsd) || !prsd.isNested() || isNull(prsd.getParentClassName())) {
                spec.addAnnotation(parse.getParser().parseAnnotation("@Generated(value=\"" + properties.getPrototypeName() + "\", comments=\"" + properties.getInterfaceName() + "\")").getResult().get());
                intf.addAnnotation(parse.getParser().parseAnnotation("@Generated(value=\"" + properties.getPrototypeName() + "\", comments=\"" + (nonNull(mixIn) ? mixIn.getProperties().getClassName() : properties.getClassName()) + "\")").getResult().get());
            }

            unit.setComment(new BlockComment("Generated code by Binis' code generator."));
            iUnit.setComment(new BlockComment("Generated code by Binis' code generator."));

            processEntries(typeDeclaration, intf, mixIn, properties.getOrdinalOffset());
            processEnumImplementation(typeDeclaration, spec);
            handleImports(typeDeclaration, spec);

            lookup.registerGenerated(getClassName(spec), parse);

            if (isNull(mixIn)) {
                addDefaultCreation(parse, mixIn);
            } else {
                iUnit.addImport(mixIn.getInterfaceFullName());
            }

            handleImports(typeDeclaration, intf);

            processingTypes.remove(typeDeclaration.getNameAsString());

            parse.setProcessed(true);
        }
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

    private static void processEnumImplementation(EnumDeclaration declaration, ClassOrInterfaceDeclaration spec) {
        var constructor = spec.addConstructor(PUBLIC)
                .addParameter(int.class, "ordinal")
                .addParameter(String.class, "name")
                .setBody(new BlockStmt().addStatement("super(ordinal, name);"));

        var constructors = declaration.getConstructors();
        if (!constructors.isEmpty()) {
            if (constructors.size() > 1) {
                throw new GenericCodeGenException("Enums with more than one constructors are unsupported!");
            }
            var con = constructors.get(0);
            con.getParameters().forEach(constructor::addParameter);
            con.getBody().getStatements().forEach(s -> constructor.getBody().addStatement(s));
        }

        declaration.getMethods().forEach(spec::addMember);
        declaration.getFields().forEach(spec::addMember);
    }

    private static void processEntries(EnumDeclaration declaration, ClassOrInterfaceDeclaration intf, PrototypeDescription<?> mixIn, long offset) {
        var name = nonNull(mixIn) ? mixIn.getInterfaceName() : intf.getNameAsString();

        if (nonNull(mixIn) && offset == 0L) {
            offset = mixIn.getProperties().getOrdinalOffset() + mixIn.getDeclaration().asEnumDeclaration().getEntries().size();
        }

        for (var i = 0; i < declaration.getEntries().size(); i++) {
            var entry = declaration.getEntries().get(i);
            var expression = new StringBuilder("CodeFactory.initializeEnumValue(").append(name).append(".class, \"").append(entry.getNameAsString()).append("\", ").append(offset + i);
            for (var arg : entry.getArguments()) {
                expression.append(", ").append(arg.toString());
            }
            expression.append(")");
            intf.addFieldWithInitializer(name, entry.getNameAsString(), lookup.getParser().parseExpression(expression.toString()).getResult().get(), STATIC, FINAL);
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
                    intf.addFieldWithInitializer(name, entry.getNameAsString(), lookup.getParser().parseExpression(name + "." + entry.getNameAsString()).getResult().get(), STATIC, FINAL));
        }
    }

    private static Structures.PrototypeDataHandler getEnumProperties(AnnotationExpr prototype) {
        var type = (EnumDeclaration) prototype.getParentNode().get();
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);

        var builder = Structures.builder(prototype.getNameAsString())
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type));
        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                var name = pair.getNameAsString();
                switch (name) {
                    case "name":
                        builder.name(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    case "mixIn":
                        builder.mixInClass(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case "ordinalOffset":
                        builder.ordinalOffset(pair.getValue().asIntegerLiteralExpr().asNumber().intValue());
                        break;
                    default:
                }
            }
        });

        if (cName.equals(iName.get())) {
            cName = iName.get() + "Impl";
        }

        builder.className(cName).interfaceName(iName.get());

        return builder.build();
    }

    private static Structures.PrototypeDataHandler getConstantProperties(AnnotationExpr prototype) {
        var type = (ClassOrInterfaceDeclaration) prototype.getParentNode().get();
        var builder = Structures.PrototypeDataHandler.builder()
                .className(defaultClassName(type))
                .classPackage(defaultPackage(type, null));
        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                var name = pair.getNameAsString();
                switch (name) {
                    case "mixIn":
                        builder.mixInClass(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    default:
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

                        log.info("Processing - {}", prototype.toString());

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
