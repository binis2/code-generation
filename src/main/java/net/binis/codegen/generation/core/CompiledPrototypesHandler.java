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
import com.github.javaparser.ast.body.*;
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
import net.binis.codegen.tools.Tools;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.EnrichHelpers.annotation;
import static net.binis.codegen.generation.core.EnrichHelpers.block;
import static net.binis.codegen.generation.core.Generator.generateCodeForClass;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.with;

@Slf4j
public abstract class CompiledPrototypesHandler {

    @SuppressWarnings("unchecked")
    public static boolean handleCompiledPrototype(String compiledPrototype) {
        var result = Holder.of(false);
        Tools.with(loadClass(compiledPrototype), c ->
                Generator.getCodeAnnotations(c).ifPresent(ann -> {

                    var declaration = c.isEnum() ? new CompilationUnit().setPackageDeclaration(c.getPackageName()).addEnum(c.getSimpleName()) : new CompilationUnit().setPackageDeclaration(c.getPackageName()).addClass(c.getSimpleName()).setInterface(true);

                    handleAnnotations(c, declaration);

                    Structures.PrototypeDataHandler props;
                    if (declaration instanceof ClassOrInterfaceDeclaration decl) {
                        for (var p : c.getTypeParameters()) {
                            decl.addTypeParameter(p.getName());
                        }
                        handleFields(c, decl);
                        handleDefaultMethods(c, decl);
                        props = handleProperties(decl, c, ann);
                    } else {
                        var decl = (EnumDeclaration) declaration;
                        props = handleProperties(decl, c, ann);
                        Arrays.stream(c.getEnumConstants()).forEach(cnst ->
                                decl.addEntry(new EnumConstantDeclaration(cnst.toString())));
                        //decl.addEntry();
                    }

                    var unit = declaration.findCompilationUnit().orElse(null);

                    var parsed = Structures.Parsed.<ClassOrInterfaceDeclaration>builder()
                            .compiled(c)
                            .properties(props)
                            .parser(lookup.getParser())
                            .declaration((TypeDeclaration) declaration)
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
        Tools.with(loadClass(compiledPrototype), c ->
                Tools.with(c.getAnnotation(EnumPrototype.class), ann -> {
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

    @SuppressWarnings("unchecked")
    private static Structures.PrototypeDataHandler handleProperties(ClassOrInterfaceDeclaration type, Class<?> cls, Annotation ann) {
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);
        var props = Structures.readAnnotation(ann);

        var builder = Structures.builder(cls.getSimpleName())
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type));

        if (StringUtils.isNotBlank((props.getName()))) {
            var intf = props.getName().replace("Entity", "");
            builder.name(props.getName())
                    .className(props.getName())
                    .interfaceName(intf)
                    .longModifierName(intf + "." + Constants.MODIFIER_INTERFACE_NAME);
        }

        if (StringUtils.isNotBlank(props.getInterfaceName())) {
            iName.set(props.getInterfaceName());
        }

        if (StringUtils.isNotBlank(props.getClassPackage())) {
            builder.classPackage(props.getClassPackage()).classPackageSet(true);
        }

        if (nonNull(props.getStrategy())) {
            builder.strategy(props.getStrategy());
        }

        if (StringUtils.isNotBlank(props.getBasePath())) {
            builder.basePath(props.getBasePath());
        }

        builder.base(props.isBase())
                .classGetters(props.isClassGetters())
                .classSetters(props.isClassSetters())
                .interfaceSetters(props.isInterfaceSetters())
                .generateConstructor(props.isGenerateConstructor())
                .generateInterface(props.isGenerateInterface())
                .generateImplementation(props.isGenerateImplementation())
                .mixInClass(props.getMixInClass())
                .baseModifierClass(props.getBaseModifierClass())
                .predefinedEnrichers(((Structures.PrototypeDataHandler) props).getPredefinedEnrichers())
                .predefinedInheritedEnrichers(((Structures.PrototypeDataHandler) props).getPredefinedInheritedEnrichers());

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

        Tools.with(result.getPredefinedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getEnrichers(), e)));

        Tools.with(result.getPredefinedInheritedEnrichers(), list ->
                list.forEach(e -> checkEnrichers(result.getInheritedEnrichers(), e)));

        return result;

    }

    private static Structures.PrototypeDataHandler handleProperties(EnumDeclaration type, Class<?> cls, Annotation ann) {
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);
        var props = Structures.readAnnotation(ann);

        var builder = Structures.builder(cls.getSimpleName())
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type));

        if (StringUtils.isNotBlank((props.getName()))) {
            var intf = props.getName().replace("Entity", "");
            builder.name(props.getName())
                    .className(props.getName())
                    .interfaceName(intf)
                    .longModifierName(intf + "." + Constants.MODIFIER_INTERFACE_NAME);
        }

        builder.mixInClass(props.getMixInClass());

        if (cName.equals(iName.get())) {
            cName = iName.get() + "Impl";
        }

        builder.className(cName).interfaceName(iName.get());

        return builder.build();
    }


    private static void checkBaseClassForEnrichers(Class<?> cls, List<PrototypeEnricher> list) {
        for (var intf : cls.getInterfaces()) {
            //TODO: Handle predefined enrichers
            Tools.with(intf.getAnnotation(CodePrototype.class), ann ->
                    checkEnrichers(list, ann.inheritedEnrichers()));
            checkBaseClassForEnrichers(intf, list);
        }
    }

    private static List<PrototypeEnricher> checkEnrichers(Class<? extends Enricher>[] enrichers) {
        var list = new ArrayList<PrototypeEnricher>();
        checkEnrichers(list, enrichers);
        return list;
    }

    private static List<PrototypeEnricher> checkEnrichers(List<PrototypeEnricher> list, Class<? extends Enricher>... enrichers) {
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
                    var annotation = annotation(ann.toString());
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
                }
            }
        }

    }

    private static String buildType(CompilationUnit unit, Type type, Class<?> returnType) {
        if (type instanceof ParameterizedType t) {
            var result = new StringBuilder(returnType.getSimpleName());
            var generics = t.getActualTypeArguments();

            if (generics.length > 0) {
                result.append('<');
                for (var generic : generics) {
                    if (generic instanceof Class cls) {
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
        } else if (type instanceof TypeVariable t) {
            return t.getName();
        } else {
            return returnType.getSimpleName();
        }
    }

    private static void handleAnnotations(Class<?> cls, TypeDeclaration declaration) {
        var unit = declaration.findCompilationUnit().get();
        for (var ann : cls.getAnnotations()) {
            var annotation = annotation(ann.toString());
            unit.addImport(ann.annotationType().getCanonicalName());
            annotation.setName(ann.annotationType().getSimpleName());
            addAnnotationTypeImports(ann, unit);
            declaration.addAnnotation(annotation);
        }
    }

    private static void handleMethodAnnotations(Method method, MethodDeclaration mtd) {
        var unit = mtd.findCompilationUnit().get();
        for (var ann : method.getAnnotations()) {
            if (!ann.annotationType().equals(CodeAnnotation.class)) {
                var annotation = annotation(ann.toString());
                unit.addImport(ann.annotationType().getCanonicalName());
                annotation.setName(ann.annotationType().getSimpleName());
                addAnnotationTypeImports(ann, unit);
                mtd.addAnnotation(annotation);
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
            if (value instanceof Annotation a) {
                addAnnotationTypeImports(a, unit);
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
