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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.type.EmbeddedModifierType;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeConstant;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.options.CodeOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.enrich.Enrichers.*;
import static net.binis.codegen.options.Options.*;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.with;

@Slf4j
public class Structures {

    public static final Map<String, Supplier<PrototypeDataHandler.PrototypeDataHandlerBuilder>> defaultProperties = new HashMap<>();

    @Getter
    @Setter
    @Builder
    public static class PrototypeDataHandler implements PrototypeData {

        private String prototypeName;
        private String prototypeFullName;
        private String name;
        private String className;
        private String classPackage;
        private boolean classGetters;
        private boolean classSetters;
        private String interfaceName;
        private String interfacePackage;
        private boolean interfaceSetters;
        private String modifierName;
        private String longModifierName;
        private String modifierPackage;

        private String baseClassName;

        private boolean generateConstructor;
        private boolean generateInterface;
        private boolean generateImplementation;
        private boolean base;

        private String baseModifierClass;
        private String mixInClass;
        private String basePath;
        private String interfacePath;
        private String implementationPath;

        private int ordinalOffset;

        private List<PrototypeEnricher> enrichers;
        private List<PrototypeEnricher> inheritedEnrichers;
        private Set<Class<? extends CodeOption>> options;
        private Map<String, Object> custom;

        private List<Class<? extends Enricher>> predefinedEnrichers;
        private List<Class<? extends Enricher>> predefinedInheritedEnrichers;

        //Custom builder to satisfy java-doc
        public static class PrototypeDataHandlerBuilder {
            public PrototypeDataHandlerBuilder custom(String name, Object value) {
                if (isNull(custom)) {
                    custom = new HashMap<>();
                }
                custom.put(name, value);
                return this;
            }
        }

    }

    @ToString
    @Data
    @Builder
    public static class FieldData implements PrototypeField {
        Parsed<ClassOrInterfaceDeclaration> parsed;
        private String name;
        private FieldDeclaration declaration;
        private MethodDeclaration description;
        private String fullType;
        private String type;
        private boolean collection;
        private boolean external;
        private boolean genericMethod;
        private boolean genericField;
        private Structures.Ignores ignores;
        @ToString.Exclude
        private PrototypeDescription<ClassOrInterfaceDeclaration> prototype;
        @ToString.Exclude
        private Map<String, Type> generics;
        @ToString.Exclude
        private Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> typePrototypes;
        @ToString.Exclude
        private List<MethodDeclaration> modifiers;

        @ToString.Exclude
        private PrototypeField parent;

        MethodDeclaration interfaceGetter;
        MethodDeclaration interfaceSetter;
        MethodDeclaration implementationGetter;
        MethodDeclaration implementationSetter;

        public List<MethodDeclaration> getModifiers() {
            if (isNull(modifiers)) {
                modifiers = new ArrayList<>();
            }
            return modifiers;
        }

        @Override
        public void addModifier(MethodDeclaration modifier) {
            getModifiers().add(modifier);
        }

        @Override
        public MethodDeclaration generateGetter() {
            var cls = (ClassOrInterfaceDeclaration) declaration.getParentNode().get();
            Generator.addGetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), cls, description, true, this);
            return implementationGetter;
        }

        @Override
        public MethodDeclaration generateSetter() {
            var cls = (ClassOrInterfaceDeclaration) declaration.getParentNode().get();
            Generator.addSetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), cls, description, true, this);
            return implementationSetter;
        }

        @Override
        public MethodDeclaration generateInterfaceGetter() {
            Generator.addGetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getIntf(), description, false, this);
            return interfaceGetter;
        }

        @Override
        public MethodDeclaration generateInterfaceSetter() {
            Generator.addSetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getIntf(), description, false, this);
            return interfaceSetter;
        }

    }

    @Data
    @Builder
    public static class Parsed<T extends TypeDeclaration<T>> implements PrototypeDescription<T> {

        {
            Helpers.registerKnownEnrichers();
        }

        private boolean processed;
        private boolean invalid;

        private JavaParser parser;

        private Class<?> compiled;
        private String prototypeFileName;
        private String prototypeClassName;

        private PrototypeDataHandler properties;

        private String parsedName;
        private String parsedFullName;

        private String interfaceName;
        private String interfaceFullName;

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        private Map<String, PrototypeConstant> constants = new HashMap<>();

        public String getImplementorFullName() {
            if (nonNull(mixIn)) {
                return mixIn.parsedFullName;
            }
            return parsedFullName;
        }

        private TypeDeclaration<T> declaration;
        private List<CompilationUnit> files;

        private Parsed<T> base;
        private Parsed<T> mixIn;

        private boolean nested;

        private boolean codeEnum;

        private String parentClassName;

        @Builder.Default
        private EmbeddedModifierType embeddedModifierType = EmbeddedModifierType.NONE;

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        private Map<String, ClassOrInterfaceDeclaration> classes = new HashMap<>();

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        private List<PrototypeField> fields = new ArrayList<>();

        private ClassOrInterfaceDeclaration spec;
        private ClassOrInterfaceDeclaration intf;

        @Builder.Default
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private List<Triple<ClassOrInterfaceDeclaration, Node, PrototypeDescription<ClassOrInterfaceDeclaration>>> initializers = new ArrayList<>();

        @Builder.Default
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private List<Consumer<BlockStmt>> customInitializers = new ArrayList<>();

        @Builder.Default
        @ToString.Exclude
        private List<Runnable> postProcessActions = new ArrayList<>();

        public void registerClass(String key, ClassOrInterfaceDeclaration declaration) {
            classes.put(key, declaration);
        }

        public ClassOrInterfaceDeclaration getRegisteredClass(String key) {
            return classes.get(key);
        }

        public void registerPostProcessAction(Runnable task) {
            postProcessActions.add(task);
        }

        public void processActions() {
            postProcessActions.forEach(Runnable::run);
        }

        @Override
        public boolean isValid() {
            return nonNull(properties);
        }

        @Override
        public Optional<PrototypeField> findField(String name) {
            var result = fields.stream().filter(f -> f.getName().equals(name)).findFirst();
            if (result.isEmpty() && nonNull(getBase())) {
                result = getBase().getFields().stream().filter(f -> f.getName().equals(name)).findFirst();
            }
            return result;
        }

        @Override
        public void addEmbeddedModifier(EmbeddedModifierType type) {
            if (!type.equals(embeddedModifierType) && !EmbeddedModifierType.BOTH.equals(embeddedModifierType)) {
                switch (type) {
                    case SINGLE, COLLECTION -> {
                        if (EmbeddedModifierType.NONE.equals(embeddedModifierType)) {
                            embeddedModifierType = type;
                        } else {
                            embeddedModifierType = EmbeddedModifierType.BOTH;
                        }
                    }
                    case BOTH -> embeddedModifierType = type;
                    default -> {/*Do nothing*/}
                }
            }
        }

        @Override
        public void setEmbeddedModifier(EmbeddedModifierType type) {
            embeddedModifierType = type;
        }

        @Override
        public boolean hasOption(Class<? extends CodeOption> option) {
            return nonNull(getProperties().getOptions()) && getProperties().getOptions().contains(option);
        }

        @Override
        public boolean hasEnricher(Class<? extends Enricher> enricher) {
            return getProperties().enrichers.stream().anyMatch(e -> enricher.isAssignableFrom(e.getClass())) ||
                    getProperties().inheritedEnrichers.stream().anyMatch(e -> enricher.isAssignableFrom(e.getClass()));
        }

    }

    @ToString
    @Data
    @Builder
    public static class ConstantData implements PrototypeConstant {
        protected ClassOrInterfaceDeclaration destination;
        protected FieldDeclaration field;
        protected String name;
    }

    @Data
    @Builder
    public static class Ignores {
        private boolean forField;
        private boolean forClass;
        private boolean forInterface;
        private boolean forModifier;

        private boolean forQuery;
    }

    @Data
    @Builder
    public static class Constants {
        private boolean forPublic;
        private boolean forClass;
        private boolean forInterface;
    }

    @Data
    @Builder
    public static class ProcessingType {
        private String interfacePackage;
        private String interfaceName;
        private String classPackage;
        private String className;
    }

    public static PrototypeDataHandler.PrototypeDataHandlerBuilder builder(String type) {
        var result = defaultProperties.get(type);
        if (isNull(result)) {
            return defaultBuilder();
        }
        return result.get();
    }

    public static PrototypeDataHandler.PrototypeDataHandlerBuilder defaultBuilder() {
        return Structures.PrototypeDataHandler.builder()
                .generateConstructor(true)
                .generateInterface(true)
                .generateImplementation(true)
                .classGetters(true)
                .classSetters(true)
                .interfaceSetters(true)
                .modifierName(net.binis.codegen.generation.core.Constants.MODIFIER_INTERFACE_NAME);
    }

    @SuppressWarnings("unchecked")
    public static void registerTemplate(Class<?> ann) {
        if (Annotation.class.isAssignableFrom(ann)) {
            defaultProperties.put(ann.getSimpleName(), () -> {
                var builder = defaultBuilder();

                for (var method : ann.getDeclaredMethods()) {
                    switch (method.getName()) {
                        case "base" -> builder.base((boolean) method.getDefaultValue());
                        case "name" -> builder.name(handleString(method.getDefaultValue()));
                        case "generateConstructor" -> builder.generateConstructor((boolean) method.getDefaultValue());
                        case "options" ->
                                builder.options((Set) Arrays.stream((Class[]) method.getDefaultValue()).collect(Collectors.toSet()));
                        case "interfaceName" -> builder.interfaceName(handleString(method.getDefaultValue()));
                        case "implementationPath" -> builder.implementationPath(handleString(method.getDefaultValue()));
                        case "enrichers" ->
                                builder.predefinedEnrichers((List) Arrays.stream((Class[]) method.getDefaultValue()).toList());
                        case "inheritedEnrichers" ->
                                builder.predefinedInheritedEnrichers((List) Arrays.stream((Class[]) method.getDefaultValue()).toList());
                        case "interfaceSetters" -> builder.interfaceSetters((boolean) method.getDefaultValue());
                        case "classGetters" -> builder.classGetters((boolean) method.getDefaultValue());
                        case "classSetters" -> builder.classSetters((boolean) method.getDefaultValue());
                        case "baseModifierClass" -> builder.baseModifierClass(handleClass(method.getDefaultValue()));
                        case "mixInClass" -> builder.mixInClass(handleClass(method.getDefaultValue()));
                        case "interfacePath" -> builder.interfacePath(handleString(method.getDefaultValue()));
                        case "generateInterface" -> builder.generateInterface((boolean) method.getDefaultValue());
                        case "basePath" -> builder.basePath(handleString(method.getDefaultValue()));
                        case "generateImplementation" ->
                                builder.generateImplementation((boolean) method.getDefaultValue());
                        case "implementationPackage" -> builder.classPackage(handleString(method.getDefaultValue()));
                        default -> builder.custom(method.getName(), method.getDefaultValue());
                    }
                }

                return builder;
            });
        } else {
            log.warn("Can't register template '{}' because it isn't annotation!", ann.getCanonicalName());
        }
    }

    public static void registerTemplate(AnnotationDeclaration template) {
        defaultProperties.put(template.getNameAsString(), () -> {
            var builder = defaultBuilder();

            template.getMembers().stream()
                    .filter(BodyDeclaration::isAnnotationMemberDeclaration)
                    .map(BodyDeclaration::asAnnotationMemberDeclaration)
                    .filter(m -> m.getDefaultValue().isPresent())
                    .forEach(member -> {
                        switch (member.getNameAsString()) {
                            case "base" -> builder.base(handleBooleanExpression(member.getDefaultValue().get()));
                            case "name" -> builder.name(handleStringExpression(member.getDefaultValue().get()));
                            case "generateConstructor" ->
                                    builder.generateConstructor(handleBooleanExpression(member.getDefaultValue().get()));
                            case "options" ->
                                    builder.options(handleClassExpression(member.getDefaultValue().get(), Set.class));
                            case "interfaceName" ->
                                    builder.interfaceName(handleStringExpression(member.getDefaultValue().get()));
                            case "implementationPath" ->
                                    builder.implementationPath(handleStringExpression(member.getDefaultValue().get()));
                            case "enrichers" ->
                                    builder.predefinedEnrichers(handleClassExpression(member.getDefaultValue().get(), List.class));
                            case "inheritedEnrichers" ->
                                    builder.predefinedInheritedEnrichers(handleClassExpression(member.getDefaultValue().get(), List.class));
                            case "interfaceSetters" ->
                                    builder.interfaceSetters(handleBooleanExpression(member.getDefaultValue().get()));
                            case "classGetters" ->
                                    builder.classGetters(handleBooleanExpression(member.getDefaultValue().get()));
                            case "classSetters" ->
                                    builder.classSetters(handleBooleanExpression(member.getDefaultValue().get()));
                            case "baseModifierClass" ->
                                    builder.baseModifierClass(handleClassExpression(member.getDefaultValue().get()));
                            case "mixInClass" ->
                                    builder.mixInClass(handleClassExpression(member.getDefaultValue().get()));
                            case "interfacePath" ->
                                    builder.interfacePath(handleStringExpression(member.getDefaultValue().get()));
                            case "generateInterface" ->
                                    builder.generateInterface(handleBooleanExpression(member.getDefaultValue().get()));
                            case "basePath" -> builder.basePath(handleStringExpression(member.getDefaultValue().get()));
                            case "generateImplementation" ->
                                    builder.generateImplementation(handleBooleanExpression(member.getDefaultValue().get()));
                            case "implementationPackage" ->
                                    builder.classPackage(handleStringExpression(member.getDefaultValue().get()));
                            default -> builder.custom(member.getNameAsString(), member.getDefaultValue().get());
                        }
                    });

            return builder;
        });
    }

    private static <T extends Collection> T handleClassExpression(Expression value, Class<T> cls) {
        var result = Set.class.equals(cls) ? new HashSet<>() : new ArrayList<>();
        if (value.isArrayInitializerExpr()) {
            value.asArrayInitializerExpr().getValues().forEach(v ->
                    with(handleClassExpression(v), r ->
                            with(loadClass(r), c -> result.add(c))));
        } else if (value.isClassExpr()) {
            with(handleClassExpression(value), r ->
                    with(loadClass(r), c -> result.add(c)));
        } else {
            log.warn("Class expression not implemented: {}", value.getClass().getCanonicalName());
        }
        return (T) result;
    }

    private static String handleClassExpression(Expression value) {
        if (value.isClassExpr()) {
            return Helpers.getExternalClassNameIfExists(value.findCompilationUnit().get(), value.asClassExpr().getType().toString());
        }
        log.warn("Class expression not implemented: {}", value.getClass().getCanonicalName());
        return null;
    }

    private static String handleStringExpression(Expression expression) {
        if (expression.isStringLiteralExpr()) {
            return expression.asStringLiteralExpr().getValue();
        }
        log.warn("String expression not implemented: {}", expression.getClass().getCanonicalName());
        return null;
    }

    private static boolean handleBooleanExpression(Expression expression) {
        if (expression.isBooleanLiteralExpr()) {
            return expression.asBooleanLiteralExpr().getValue();
        }

        log.warn("Boolean expression not implemented: {}", expression.getClass().getCanonicalName());
        return false;
    }

    private static String handleString(Object value) {
        var val = (String) value;
        return StringUtils.isBlank(val) ? null : val;
    }

    private static String handleClass(Object value) {
        var val = (Class) value;
        return void.class.equals(val) ? null : val.getCanonicalName();
    }

}
