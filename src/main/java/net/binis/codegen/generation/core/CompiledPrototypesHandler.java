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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.CodeAnnotation;
import net.binis.codegen.annotation.CodeImplementation;
import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.EnumPrototype;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.tools.Holder;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.EnrichHelpers.block;
import static net.binis.codegen.generation.core.Generator.generateCodeForClass;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.notNull;
import static net.binis.codegen.tools.Tools.with;

@Slf4j
public abstract class CompiledPrototypesHandler {

    public static boolean handleCompiledPrototype(String compiledPrototype) {
        var result = Holder.of(false);
        notNull(loadClass(compiledPrototype), c ->
                notNull(c.getAnnotation(CodePrototype.class), ann -> {
                    var declaration = new CompilationUnit().setPackageDeclaration(c.getPackageName()).addClass(c.getSimpleName()).setInterface(true);

                    for (var p : c.getTypeParameters()) {
                        declaration.addTypeParameter(p.getName());
                    }

                    handleAnnotations(c, declaration);
                    handleFields(c, declaration);
                    handleDefaultMethods(c, declaration);
                    var props = handleProperties(declaration, c, ann);
                    var unit = declaration.findCompilationUnit().orElse(null);

                    var parsed = Structures.Parsed.<ClassOrInterfaceDeclaration>builder()
                            .compiled(c)
                            .properties(props)
                            .parser(lookup.getParser())
                            .declaration(declaration)
                            .declarationUnit(unit);

                    var prsd = parsed.build();
                    lookup.registerParsed(compiledPrototype, prsd);
                    generateCodeForClass(unit, prsd);

                    //TODO: Implement class annotations

                    result.set(true);
                }));
        return result.get();
    }

    public static boolean handleCompiledEnumPrototype(String compiledPrototype) {
        var result = Holder.of(false);
        notNull(loadClass(compiledPrototype), c ->
                notNull(c.getAnnotation(EnumPrototype.class), ann -> {
                    if (c.isEnum()) {
                        var declaration = new CompilationUnit().setPackageDeclaration(c.getPackageName()).addEnum(c.getSimpleName());

                    handleEntries(c, declaration);
                        var props = handleProperties(declaration, c, ann);

                        var parsed = Structures.Parsed.<EnumDeclaration>builder()
                                .compiled(c)
                                .codeEnum(true)
                                .properties(props)
                                .parser(lookup.getParser())
                                .interfaceName(props.getInterfaceName())
                                .interfaceFullName(props.getInterfacePackage() + "." + props.getInterfaceName())
                                .declaration(declaration)
                                .declarationUnit(declaration.findCompilationUnit().orElse(null));

                        var prsd = parsed.build();
                        lookup.registerParsed(compiledPrototype, prsd);
                        //generateCodeForEnum(declaration.findCompilationUnit().get(), prsd);

                        //TODO: Implement class annotations

                        result.set(true);
                    } else {
                        log.warn("'{}' isn't enum class!", compiledPrototype);
                    }
                }));
        return result.get();
    }

    private static void handleEntries(Class<?> c, EnumDeclaration declaration) {
        for (var cnst : c.getEnumConstants()) {
            declaration.addEnumConstant(cnst.toString());
        }
    }

    private static Structures.PrototypeDataHandler handleProperties(ClassOrInterfaceDeclaration type, Class<?> cls, CodePrototype ann) {
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);

        var builder = Structures.builder(cls.getSimpleName())
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type));

        if (StringUtils.isNotBlank((ann.name()))) {
            var intf = ann.name().replace("Entity", "");
            builder.name(ann.name())
                    .className(ann.name())
                    .interfaceName(intf)
                    .longModifierName(intf + "." + Constants.MODIFIER_INTERFACE_NAME);
        }

        if (StringUtils.isNotBlank(ann.interfaceName())) {
            iName.set(ann.interfaceName());
        }

        if (StringUtils.isNotBlank(ann.implementationPackage())) {
            builder.classPackage(ann.implementationPackage());
        }

        if (nonNull(ann.strategy())) {
            builder.strategy(ann.strategy());
        }

        if (StringUtils.isNotBlank(ann.basePath())) {
            builder.basePath(ann.basePath());
        }

        builder.base(ann.base())
                .classGetters(ann.classGetters())
                .classSetters(ann.classSetters())
                .interfaceSetters(ann.interfaceSetters())
                .generateConstructor(ann.generateConstructor())
                .generateInterface(ann.generateInterface())
                .generateImplementation(ann.generateImplementation())
                .mixInClass(nonNull(ann.mixInClass()) && !void.class.equals(ann.mixInClass()) ? ann.mixInClass().getCanonicalName() : null)
                .baseModifierClass(nonNull(ann.baseModifierClass()) && !void.class.equals(ann.baseModifierClass()) ? ann.baseModifierClass().getCanonicalName() : null)
                .enrichers(checkEnrichers(ann.enrichers()))
                .inheritedEnrichers(checkEnrichers(ann.inheritedEnrichers()));

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

        checkBaseClassForEnrichers(cls, result.getEnrichers());

        //TODO: Handle predefined enrichers

        return result;

    }

    private static Structures.PrototypeDataHandler handleProperties(EnumDeclaration type, Class<?> cls, EnumPrototype ann) {
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);

        var builder = Structures.builder(cls.getSimpleName())
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type));

        if (StringUtils.isNotBlank((ann.name()))) {
            var intf = ann.name().replace("Entity", "");
            builder.name(ann.name())
                    .className(ann.name())
                    .interfaceName(intf)
                    .longModifierName(intf + "." + Constants.MODIFIER_INTERFACE_NAME);
        }

        builder.mixInClass(nonNull(ann.mixIn()) && !void.class.equals(ann.mixIn()) ? ann.mixIn().getCanonicalName() : null);

        if (cName.equals(iName.get())) {
            cName = iName.get() + "Impl";
        }

        builder.className(cName).interfaceName(iName.get());

        return builder.build();
    }


    private static void checkBaseClassForEnrichers(Class<?> cls, List<PrototypeEnricher> list) {
        for (var intf : cls.getInterfaces()) {
            //TODO: Handle predefined enrichers
            notNull(intf.getAnnotation(CodePrototype.class), ann ->
                    checkEnrichers(list, ann.inheritedEnrichers()));
            checkBaseClassForEnrichers(intf, list);
        }
    }

    private static List<PrototypeEnricher> checkEnrichers(Class<? extends Enricher>[] enrichers) {
        var list = new ArrayList<PrototypeEnricher>();
        checkEnrichers(list, enrichers);
        return list;
    }

    private static List<PrototypeEnricher> checkEnrichers(List<PrototypeEnricher> list, Class<? extends Enricher>[] enrichers) {
        Arrays.stream(enrichers)
                .map(CodeFactory::create)
                .filter(Objects::nonNull)
                .filter(i -> PrototypeEnricher.class.isAssignableFrom(i.getClass()))
                .forEach(e ->
                        with(((PrototypeEnricher) e), enricher -> {
                            enricher.init(lookup);
                            list.add(enricher);
                        }));
        return list;
    }

    private static void handleFields(Class<?> c, ClassOrInterfaceDeclaration declaration) {
        var unit = declaration.findCompilationUnit().get();
        for (var method : c.getDeclaredMethods()) {
            if (!method.isDefault() && method.getParameterCount() == 0 && !Void.class.equals(method.getReturnType())) {
                var mtd = declaration.addMethod(method.getName()).setType(buildType(unit, method.getGenericReturnType(), method.getReturnType())).setBody(null);
                if (!method.getReturnType().isPrimitive()) {
                    unit.addImport(method.getReturnType());
                }

                for (var ann : method.getAnnotations()) {
                    var methods = ann.annotationType().getDeclaredMethods();
                    lookup.getParser().parseAnnotation(ann.toString()).getResult().ifPresent(annotation -> {
                        for (var m : methods) {
                            if (nonNull(m.getDefaultValue())) {
                                annotation.getChildNodes().stream().filter(MemberValuePair.class::isInstance).map(MemberValuePair.class::cast).filter(v -> v.getName().asString().equals(m.getName())).findFirst().ifPresent(pair -> {
                                    if (m.getDefaultValue() instanceof Class && pair.getValue().toString().equals(((Class) m.getDefaultValue()).getName() + ".class")) {
                                        annotation.remove(pair);
                                    } else if (m.getDefaultValue().getClass().equals(String.class) && m.getDefaultValue().toString().equals(pair.getValue().asStringLiteralExpr().asString())) {
                                        annotation.remove(pair);
                                    } else if (m.getDefaultValue().toString().equals(pair.getValue().toString())) {
                                        annotation.remove(pair);
                                    }
                                });
                            }
                        }
                        unit.addImport(ann.annotationType().getCanonicalName());
                        annotation.setName(ann.annotationType().getSimpleName());
                        addAnnotationTypeImports(ann, unit);
                        mtd.addAnnotation(annotation);
                    });
                }
            }
        }
    }

    private static String buildType(CompilationUnit unit, Type type, Class<?> returnType) {
        if (type instanceof ParameterizedType) {
            var t = (ParameterizedType) type;
            var result = new StringBuilder(returnType.getSimpleName());
            var generics = t.getActualTypeArguments();

            if (generics.length > 0) {
                result.append('<');
                for (var generic : generics) {
                    if (generic instanceof Class) {
                        var cls = (Class) generic;
                        result.append(cls.getSimpleName()).append(", ");
                        if (!cls.isPrimitive()) {
                            unit.addImport(cls);
                        }
                    } else {
                        result.append(generic.getTypeName()).append(", ");
                    }
                }
                result.setLength(result.length() - 2);
                result.append('>');
            }

            return result.toString();
        } else if (type instanceof TypeVariable) {
            return ((TypeVariable) type).getName();
        } else {
            return returnType.getSimpleName();
        }
    }

    private static void handleAnnotations(Class<?> cls, ClassOrInterfaceDeclaration declaration) {
        var unit = declaration.findCompilationUnit().get();
        for (var ann : cls.getAnnotations()) {
            lookup.getParser().parseAnnotation(ann.toString()).getResult().ifPresent(annotation -> {
                unit.addImport(ann.annotationType().getCanonicalName());
                annotation.setName(ann.annotationType().getSimpleName());
                addAnnotationTypeImports(ann, unit);
                declaration.addAnnotation(annotation);
            });
        }
    }

    private static void handleMethodAnnotations(Method method, MethodDeclaration mtd) {
        var unit = mtd.findCompilationUnit().get();
        for (var ann : method.getAnnotations()) {
            if (!ann.annotationType().equals(CodeAnnotation.class)) {
                lookup.getParser().parseAnnotation(ann.toString()).getResult().ifPresent(annotation -> {
                    unit.addImport(ann.annotationType().getCanonicalName());
                    annotation.setName(ann.annotationType().getSimpleName());
                    addAnnotationTypeImports(ann, unit);
                    mtd.addAnnotation(annotation);
                });
            }
        }
    }


    private static void addAnnotationTypeImports(Annotation ann, CompilationUnit unit) {
        for (var method : ann.annotationType().getDeclaredMethods()) {
            if (!"java.lang".equals(method.getReturnType().getPackageName())) {
                try {
                    var result = method.invoke(ann);
                    handleAnnotationValue(result, unit);
                } catch (Exception e) {
                    log.warn("Unable to access value for {}::{}", ann.annotationType().getSimpleName(), method.getName());
                }
            }
        }
    }

    private static void handleAnnotationValue(Object value, CompilationUnit unit) {
        if (nonNull(value)) {
            var name = value.getClass().getCanonicalName();
            if (value instanceof Annotation) {
                addAnnotationTypeImports((Annotation) value, unit);
            } else if (value.getClass().isEnum()) {
                unit.addImport(name + "." + ((Enum) value).name(), true, false);
            } else if (value.getClass().isArray()) {
                handleAnnotationArray(value, unit);
                if (name.endsWith("[]")) {
                    name = name.substring(0, name.length() - 2);
                }
            }
            unit.addImport(name);
        }
    }

    private static void handleAnnotationArray(Object object, CompilationUnit unit) {
        for (var i = 0; i < Array.getLength(object); i++) {
            handleAnnotationValue(Array.get(object, i), unit);
        }
    }

    private static void handleDefaultMethods(Class<?> cls, ClassOrInterfaceDeclaration declaration) {
        var unit = declaration.findCompilationUnit().get();

        for (var method : cls.getDeclaredMethods()) {
            if (method.isDefault()) {
                var ann = method.getAnnotation(CodeImplementation.class);
                if (nonNull(ann)) {
                    var mtd = declaration.addMethod(method.getName(), Modifier.Keyword.DEFAULT).setType(method.getReturnType().getSimpleName());
                    Helpers.importClass(unit, method.getReturnType());
                    for (var par : method.getParameters()) {
                        mtd.addParameter(par.getType().getSimpleName(), par.getName());
                        Helpers.importClass(unit, par.getType());
                    }
                    handleMethodAnnotations(method, mtd);

                    for (var imprt : ann.imports()) {
                        unit.addImport(imprt);
                    }

                    mtd.setBody(block(calcBlock(ann.value())));
                } else {
                    if (Arrays.stream(method.getAnnotations()).noneMatch(a -> nonNull(a.annotationType().getAnnotation(CodeAnnotation.class)))) {
                        log.warn("Compiled default method {}.{} can't be handled!", cls.getSimpleName(), method.getName());
                    }
                }
            }
        }

        for (var intf : cls.getInterfaces()) {
            handleDefaultMethods(intf, declaration);
        }
    }

    private static String calcBlock(String value) {
        var result = new StringBuilder().append('{').append(value);
        if (value.length() > 0 && result.charAt(result.length() - 1) != ';') {
            result.append(';');
        }
        result.append('}');
        return result.toString();
    }

}
