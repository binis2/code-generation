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
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.CodePrototypeTemplate;
import net.binis.codegen.annotation.type.EmbeddedModifierType;
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.enrich.CustomDescription;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.GeneratedFile;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.*;
import net.binis.codegen.generation.core.types.ModifierType;
import net.binis.codegen.options.CodeOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.compiler.utils.ElementUtils.getSymbolFullName;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.*;

@Slf4j
public class Structures {

    public static final String VALUE = "value";

    public static final Map<String, Supplier<PrototypeDataHandler.PrototypeDataHandlerBuilder>> defaultProperties = new HashMap<>();

    private static final Set<Class<?>> checkedAnnotations = initCheckedAnnotations();

    private static Set<Class<?>> initCheckedAnnotations() {
        var result = new HashSet<Class<?>>();
        result.add(Documented.class);
        result.add(Target.class);
        result.add(Retention.class);
        result.add(Repeatable.class);
        result.add(Inherited.class);
        result.add(CodePrototypeTemplate.class);
        return result;
    }

    private Structures() {
        //Do nothing
    }

    @Getter
    @Setter
    @Builder
    public static class PrototypeDataHandler implements PrototypeData {
        private Class<? extends Annotation> prototypeAnnotation;
        private String prototypeName;
        private String prototypeFullName;
        private String name;
        private String className;
        private String classPackage;
        private boolean classPackageSet;
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

        private GenerationStrategy strategy;

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

            public PrototypeDataHandlerBuilder enrichers(List<PrototypeEnricher> enrichers) {
                if (nonNull(enrichers)) {
                    if (isNull(this.enrichers)) {
                        this.enrichers = new ArrayList<>();
                        this.enrichers.addAll(enrichers);
                    } else {
                        enrichers.stream().filter(e -> !this.enrichers.contains(e)).forEach(this.enrichers::add);
                    }
                }
                return this;
            }

            public PrototypeDataHandlerBuilder inheritedEnrichers(List<PrototypeEnricher> inheritedEnrichers) {
                if (nonNull(inheritedEnrichers)) {
                    if (isNull(this.inheritedEnrichers)) {
                        this.inheritedEnrichers = new ArrayList<>();
                        this.inheritedEnrichers.addAll(inheritedEnrichers);
                    } else {
                        inheritedEnrichers.stream().filter(e -> !this.inheritedEnrichers.contains(e)).forEach(this.inheritedEnrichers::add);
                    }
                }
                return this;
            }

            public PrototypeDataHandlerBuilder predefinedEnrichers(List<Class<? extends Enricher>> predefinedEnrichers) {
                if (nonNull(predefinedEnrichers)) {
                    if (isNull(this.predefinedEnrichers)) {
                        this.predefinedEnrichers = new ArrayList<>();
                        this.predefinedEnrichers.addAll(predefinedEnrichers);
                    } else {
                        predefinedEnrichers.stream().filter(e -> !this.predefinedEnrichers.contains(e)).forEach(this.predefinedEnrichers::add);
                    }
                }
                return this;
            }

            public PrototypeDataHandlerBuilder predefinedInheritedEnrichers(List<Class<? extends Enricher>> predefinedInheritedEnrichers) {
                if (nonNull(predefinedInheritedEnrichers)) {
                    if (isNull(this.predefinedInheritedEnrichers)) {
                        this.predefinedInheritedEnrichers = new ArrayList<>();
                        this.predefinedInheritedEnrichers.addAll(predefinedInheritedEnrichers);
                    } else {
                        predefinedInheritedEnrichers.stream().filter(e -> !this.predefinedInheritedEnrichers.contains(e)).forEach(this.predefinedInheritedEnrichers::add);
                    }
                }
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
        private Type type;
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
        private List<ModifierDescription> modifiers;

        @ToString.Exclude
        private PrototypeField parent;

        MethodDeclaration interfaceGetter;
        MethodDeclaration interfaceSetter;
        MethodDeclaration implementationGetter;
        MethodDeclaration implementationSetter;
        boolean custom;

        public List<ModifierDescription> getModifiers() {
            if (isNull(modifiers)) {
                modifiers = new ArrayList<>();
            }
            return modifiers;
        }

        @Override
        public void addModifier(ModifierType type, MethodDeclaration modifier, PrototypeDescription<ClassOrInterfaceDeclaration> origin) {
            getModifiers().add(ModifierDescriptionData.builder()
                    .type(type)
                    .modifier(modifier)
                    .origin(origin)
                    .build());
        }

        @Override
        public MethodDeclaration forceGenerateGetter() {
            if (isNull(implementationGetter)) {
                var cls = (ClassOrInterfaceDeclaration) declaration.getParentNode().get();
                implementationGetter = Generator.addGetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), cls, description, true, this, true);
            }
            return implementationGetter;
        }

        @Override
        public MethodDeclaration forceGenerateSetter() {
            if (isNull(implementationSetter)) {
                var cls = (ClassOrInterfaceDeclaration) declaration.getParentNode().get();
                implementationSetter = Generator.addSetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), cls, description, true, this, true);
            }
            return implementationSetter;
        }

        @Override
        public MethodDeclaration forceGenerateInterfaceGetter() {
            if (isNull(interfaceGetter)) {
                interfaceGetter = Generator.addGetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getInterface(), description, false, this, true);
            }
            return interfaceGetter;
        }

        @Override
        public MethodDeclaration forceGenerateInterfaceSetter() {
            if (isNull(interfaceSetter)) {
                interfaceSetter = Generator.addSetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getInterface(), description, false, this, true);
            }
            return interfaceSetter;
        }


        @Override
        public MethodDeclaration generateGetter() {
            if (isNull(implementationGetter)) {
                var cls = (ClassOrInterfaceDeclaration) declaration.getParentNode().get();
                implementationGetter = Generator.addGetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), cls, description, true, this, false);
            }
            return implementationGetter;
        }

        @Override
        public MethodDeclaration generateSetter() {
            if (isNull(implementationSetter)) {
                var cls = (ClassOrInterfaceDeclaration) declaration.getParentNode().get();
                implementationSetter = Generator.addSetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), cls, description, true, this, false);
            }
            return implementationSetter;
        }

        @Override
        public MethodDeclaration generateInterfaceGetter() {
            if (isNull(interfaceGetter)) {
                interfaceGetter = Generator.addGetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getInterface(), description, false, this, false);
            }
            return interfaceGetter;
        }

        @Override
        public MethodDeclaration generateInterfaceSetter() {
            if (isNull(interfaceSetter)) {
                interfaceSetter = Generator.addSetter(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getInterface(), description, false, this, false);
            }
            return interfaceSetter;
        }

    }

    @Data
    @Builder
    protected static class ModifierDescriptionData implements PrototypeField.ModifierDescription {
        protected ModifierType type;
        protected MethodDeclaration modifier;
        protected PrototypeDescription<ClassOrInterfaceDeclaration> origin;
    }

    @Data
    @Builder
    public static class Parsed<T extends TypeDeclaration<T>> implements PrototypeDescription<T> {

        static {
            Helpers.registerKnownEnrichers();
        }

        protected boolean processed;
        protected boolean invalid;

        protected JavaParser parser;

        protected Class<?> compiled;
        protected String prototypeFileName;
        protected String prototypeClassName;

        protected PrototypeDataHandler properties;

        @Getter(AccessLevel.NONE)
        @Builder.Default
        protected List<PrototypeDataHandler> additionalProperties = new ArrayList<>();

        protected String parsedName;
        protected String parsedFullName;

        protected String interfaceName;
        protected String interfaceFullName;

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        private Map<String, PrototypeConstant> constants = new HashMap<>();

        @SuppressWarnings("unchecked")
        @Override
        public List<PrototypeData> getAdditionalProperties() {
            return (List) additionalProperties;
        }

        public String getImplementorFullName() {
            if (nonNull(mixIn)) {
                return mixIn.parsedFullName;
            }
            return parsedFullName;
        }

        @Override
        public ClassOrInterfaceDeclaration getImplementation() {
            return spec;
        }

        @Override
        public ClassOrInterfaceDeclaration getInterface() {
            return intf;
        }

        protected TypeDeclaration<T> declaration;
        protected CompilationUnit declarationUnit;

        @Builder.Default
        protected List<CompilationUnit> files = initFiles();

        @Builder.Default
        protected Map<String, GeneratedFileHandler> custom = new HashMap<>();

        protected Parsed<T> base;
        protected Parsed<T> mixIn;

        protected boolean nested;
        protected boolean external;
        protected boolean codeEnum;

        protected String parentClassName;

        protected TypeDeclaration parent;

        protected String parentPackage;

        @Builder.Default
        protected EmbeddedModifierType embeddedModifierType = EmbeddedModifierType.NONE;

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        protected Map<String, ClassOrInterfaceDeclaration> classes = new HashMap<>();

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        protected List<PrototypeField> fields = new ArrayList<>();

        protected ClassOrInterfaceDeclaration spec;
        protected ClassOrInterfaceDeclaration intf;
        protected CompilationUnit interfaceUnit;
        protected CompilationUnit implementationUnit;

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        protected Map<String, List<ElementDescription>> elements = new HashMap<>();

        @EqualsAndHashCode.Exclude
        @Builder.Default
        @ToString.Exclude
        protected List<Parsables.Entry.Bag> rawElements = new ArrayList<>();

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        protected Element element;

        public Element getElement() {
            if (isNull(element) && nonNull(elements) && !elements.isEmpty()) {
                element = elements.values().stream()
                        .map(list -> list.get(0).getElement())
                        .filter(e -> nonNull(e) && in(e.getKind(), ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM) && prototypeClassName.equals(getSymbolFullName(e)))
                        .findFirst()
                        .orElse(null);
            }
            return element;
        }

        public Element getPrototypeElement() {
            if (nonNull(rawElements)) {
                return rawElements.stream()
                        .map(Parsables.Entry.Bag::getElement)
                        .filter(e -> ElementKind.INTERFACE.equals(e.getKind()) && e.getSimpleName().toString().equals(declaration.getNameAsString()))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        }

        public Element findElement(String name, ElementKind... kind) {
            if (nonNull(rawElements)) {
                return rawElements.stream()
                        .map(Parsables.Entry.Bag::getElement)
                        .filter(e -> in(e.getKind(), kind) && e.getSimpleName().toString().equals(name))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        }

        public Element findElement(Element parent, String name, ElementKind kind) {
            if (nonNull(parent)) {
                return parent.getEnclosedElements().stream()
                        .filter(e -> e.getKind().equals(kind))
                        .filter(e -> e.getSimpleName().toString().equals(name))
                        .findFirst()
                        .orElse(null);
            }
            return null;
        }

        @Builder.Default
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        protected List<Triple<ClassOrInterfaceDeclaration, Node, PrototypeDescription<ClassOrInterfaceDeclaration>>> initializers = new ArrayList<>();

        @Builder.Default
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        protected List<Consumer<BlockStmt>> customInitializers = new ArrayList<>();

        @Builder.Default
        @ToString.Exclude
        protected List<Runnable> postProcessActions = new ArrayList<>();

        public void setInterface(ClassOrInterfaceDeclaration intf) {
            this.intf = intf;
            interfaceName = intf.getNameAsString();
            interfaceFullName = intf.getFullyQualifiedName().orElse(null);
            interfaceUnit = intf.findCompilationUnit().orElse(null);
            files.set(1, interfaceUnit);
            lookup.registerGeneratedClass(interfaceFullName, intf);
        }

        public void setImplementation(ClassOrInterfaceDeclaration spec) {
            this.spec = spec;
            parsedName = spec.getNameAsString();
            parsedFullName = spec.getFullyQualifiedName().orElse(null);
            implementationUnit = spec.findCompilationUnit().orElse(null);
            files.set(0, implementationUnit);
            lookup.registerGeneratedClass(parsedFullName, spec);
        }

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

        public GeneratedFileHandler addCustomFile(String id) {
            return custom.computeIfAbsent(id, k -> GeneratedFileHandler.builder().id(id).build());
        }

        public GeneratedFileHandler getCustomFile(String id) {
            return custom.get(id);
        }

        @Override
        public Map<String, GeneratedFileHandler> getCustomFiles() {
            return custom;
        }

        public void addProperties(PrototypeDataHandler properties) {
            if (isNull(this.properties)) {
                this.properties = properties;
            } else {
                this.additionalProperties.add(properties);
            }
        }

        protected static List<CompilationUnit> initFiles() {
            var list = new ArrayList<CompilationUnit>(2);
            list.add(null);
            list.add(null);
            return list;
        }

        public boolean isMixIn() {
            return nonNull(mixIn);
        }
    }

    @Data
    @Builder
    public static class ParsedElementDescription implements ElementDescription {
        protected boolean processed;
        protected Element element;
        protected Node node;
        protected AnnotationExpr prototype;
        protected PrototypeData properties;

        protected PrototypeDescription<ClassOrInterfaceDeclaration> description;
    }

    public static class CustomParsed extends Parsed<ClassOrInterfaceDeclaration> implements CustomDescription {
        @Getter
        protected String id;

        @Builder(builderMethodName = "bldr")
        public CustomParsed(String id, boolean processed, boolean invalid, JavaParser parser, Class<?> compiled, String prototypeFileName, String prototypeClassName, PrototypeDataHandler properties, List<PrototypeDataHandler> additionalProperties, String parsedName, String parsedFullName, String interfaceName, String interfaceFullName, Map<String, PrototypeConstant> constants, TypeDeclaration<ClassOrInterfaceDeclaration> declaration, CompilationUnit declarationUnit, List<CompilationUnit> files, Map<String, GeneratedFileHandler> custom, Parsed<ClassOrInterfaceDeclaration> base, Parsed<ClassOrInterfaceDeclaration> mixIn, boolean nested, boolean external, boolean codeEnum, String parentClassName, ClassOrInterfaceDeclaration parent, String parentPackage, EmbeddedModifierType embeddedModifierType, Map<String, ClassOrInterfaceDeclaration> classes, List<PrototypeField> fields, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, CompilationUnit interfaceUnit, CompilationUnit implementationUnit, Map<String, List<ElementDescription>> elements, Element element, List<Parsables.Entry.Bag> rawElements, List<Triple<ClassOrInterfaceDeclaration, Node, PrototypeDescription<ClassOrInterfaceDeclaration>>> initializers, List<Consumer<BlockStmt>> customInitializers, List<Runnable> postProcessActions) {
            super(processed, invalid, parser, compiled, prototypeFileName, prototypeClassName, properties, additionalProperties, parsedName, parsedFullName, interfaceName, interfaceFullName, constants, declaration, declarationUnit, files, custom, base, mixIn, nested, external, codeEnum, parentClassName, parent, parentPackage, embeddedModifierType, classes, fields, spec, intf, interfaceUnit, implementationUnit, elements, rawElements, element, initializers, customInitializers, postProcessActions);
            this.id = id;
            this.files = initFiles();
        }

        @Override
        public void setProperties(PrototypeData properties) {
            this.properties = (PrototypeDataHandler) properties;
        }
    }

    @Data
    @Builder
    public static class GeneratedFileHandler implements GeneratedFile {
        protected String id;
        protected String path;
        protected String content;
        protected TypeDeclaration javaClass;

        public void setJavaClass(TypeDeclaration javaClass) {
            this.javaClass = javaClass;
            lookup.registerGeneratedClass(getClassName(javaClass), javaClass);
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
        return PrototypeDataHandler.builder()
                .strategy(GenerationStrategy.PROTOTYPE)
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
            defaultProperties.put(ann.getCanonicalName(), () -> {
                var builder = defaultBuilder();

                for (var a : ann.getAnnotations()) {
                    checkAnnotation(a, a.annotationType(), builder, Structures::readAnnotationValue);
                }

                readAnnotation(null, ann, builder, Structures::annotationDefaultValue);


                return builder;
            });
        } else {
            log.warn("Can't register template '{}' because it isn't annotation!", ann.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    public static void registerTemplate(AnnotationDeclaration template) {
        defaultProperties.put(template.getFullyQualifiedName().get(), () -> {
            var parent = template.getAnnotations().stream()
                    .filter(a -> defaultProperties.containsKey(getExternalClassName(template, a.getNameAsString())))
                    .findFirst();

            var builder = parent.isPresent() ? defaultProperties.get(getExternalClassName(template, parent.get().getNameAsString())).get() : defaultBuilder();

            parent.ifPresent(annotationExpr ->
                    readAnnotation(annotationExpr, builder));

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
                                    builder.options(handleClassExpression(member.getDefaultValue().get(), Set.class, CodeOption.class));
                            case "interfaceName" ->
                                    builder.interfaceName(handleStringExpression(member.getDefaultValue().get()));
                            case "implementationPath" ->
                                    builder.implementationPath(handleStringExpression(member.getDefaultValue().get()));
                            case "enrichers" ->
                                    builder.predefinedEnrichers(handleClassExpression(member.getDefaultValue().get(), List.class, Enricher.class));
                            case "inheritedEnrichers" ->
                                    builder.predefinedInheritedEnrichers(handleClassExpression(member.getDefaultValue().get(), List.class, Enricher.class));
                            case "interfaceSetters" ->
                                    builder.interfaceSetters(handleBooleanExpression(member.getDefaultValue().get()));
                            case "classGetters" ->
                                    builder.classGetters(handleBooleanExpression(member.getDefaultValue().get()));
                            case "classSetters" ->
                                    builder.classSetters(handleBooleanExpression(member.getDefaultValue().get()));
                            case "baseModifierClass" ->
                                    builder.baseModifierClass(handleClassExpression(member.getDefaultValue().get()));
                            case "mixInClass" -> {
                                var cls = handleClassExpression(member.getDefaultValue().get());
                                if (!"void".equals(cls)) {
                                    builder.mixInClass(cls);
                                }
                            }
                            case "interfacePath" ->
                                    builder.interfacePath(handleStringExpression(member.getDefaultValue().get()));
                            case "generateInterface" ->
                                    builder.generateInterface(handleBooleanExpression(member.getDefaultValue().get()));
                            case "basePath" -> builder.basePath(handleStringExpression(member.getDefaultValue().get()));
                            case "generateImplementation" ->
                                    builder.generateImplementation(handleBooleanExpression(member.getDefaultValue().get()));
                            case "implementationPackage" ->
                                    builder.classPackage(handleStringExpression(member.getDefaultValue().get()));
                            case "strategy" ->
                                    builder.strategy(handleEnumExpression(member.getDefaultValue().get(), GenerationStrategy.class));
                            default -> builder.custom(member.getNameAsString(), member.getDefaultValue().get());
                        }
                    });

            return builder;
        });
    }

    @SuppressWarnings("unchecked")
    private static void readAnnotation(AnnotationExpr ann, PrototypeDataHandler.PrototypeDataHandlerBuilder builder) {
        ann.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair pair) {
                var name = pair.getNameAsString();
                switch (name) {
                    case "name" -> {
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            var intf = value.replace("Entity", "");
                            builder.name(value)
                                    .className(value)
                                    .interfaceName(intf)
                                    .longModifierName(intf + "." + net.binis.codegen.generation.core.Constants.MODIFIER_INTERFACE_NAME);
                        }
                    }
                    case "generateConstructor" ->
                        builder.generateConstructor(pair.getValue().asBooleanLiteralExpr().getValue());
                    case "generateImplementation" ->
                        builder.generateImplementation(pair.getValue().asBooleanLiteralExpr().getValue());
                    case "generateInterface" ->
                        builder.generateInterface(pair.getValue().asBooleanLiteralExpr().getValue());
                    case "interfaceName" -> {
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.interfaceName(value);
                        }
                    }
                    case "classGetters" ->
                        builder.classGetters(pair.getValue().asBooleanLiteralExpr().getValue());
                    case "classSetters" ->
                        builder.classSetters(pair.getValue().asBooleanLiteralExpr().getValue());
                    case "interfaceSetters" ->
                        builder.interfaceSetters(pair.getValue().asBooleanLiteralExpr().getValue());
                    case "base" ->
                        builder.base(pair.getValue().asBooleanLiteralExpr().getValue());
                    case "baseModifierClass" -> {
                        var value = pair.getValue().asClassExpr().getTypeAsString();
                        if (StringUtils.isNotBlank(value) && !"void".equals(value)) {
                            var full = Helpers.getExternalClassNameIfExists(pair, value);
                            builder.baseModifierClass(nonNull(full) ? full : value);
                        }
                    }
                    case "mixInClass" -> {
                        var value = pair.getValue().asClassExpr().getTypeAsString();
                        if (StringUtils.isNotBlank(value) && !"void".equals(value)) {
                            builder.mixInClass(value);
                        }
                    }
                    case "implementationPackage" -> {
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.classPackage(value);
                        }
                    }
                    case "strategy" -> {
                        var value = pair.getValue().asFieldAccessExpr().getNameAsString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.strategy(GenerationStrategy.valueOf(value));
                        }
                    }
                    case "basePath" -> {
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.basePath(value);
                        }
                    }
                    case "interfacePath" -> {
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.interfacePath(value);
                        }
                    }
                    case "implementationPath" -> {
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        if (StringUtils.isNotBlank(value)) {
                            builder.implementationPath(value);
                        }
                    }
                    case "enrichers" ->
                        builder.predefinedEnrichers(handleClassExpression(pair.getValue(), List.class, Enricher.class));
                    case "inheritedEnrichers" ->
                        builder.predefinedInheritedEnrichers(handleClassExpression(pair.getValue(), List.class, Enricher.class));
                    case "options" ->
                        builder.options(handleClassExpression(pair.getValue(), Set.class, CodeOption.class));
                    default -> {}
                }
            } else if (node instanceof Name) {
                //Continue
            } else {
                builder.custom(VALUE, node);
            }
        });
    }

    protected static boolean checkAnnotation(Annotation ann, Class<?> cls, PrototypeDataHandler.PrototypeDataHandlerBuilder builder, BiFunction<Method, Annotation, Object> func) {
        var result = false;
        var proto = defaultProperties.get(cls.getCanonicalName());
        if (nonNull(proto)) {
            copy(builder, proto.get().build());
            readAnnotation(ann, cls, builder, Structures::readAnnotationValue);
            return true;
        } else {
            if (!checkedAnnotations.contains(cls)) {
                for (var a : cls.getAnnotations()) {
                    var isProto = checkAnnotation(a, a.annotationType(), builder, func);
                    if (isProto && isNull(cls.getAnnotation(CodePrototypeTemplate.class))) {
                        readAnnotation(ann, cls, builder, Structures::readAnnotationValue);
                    }
                    result |= isProto;
                }
                if (!result) {
                    checkedAnnotations.add(cls);
                }
            }
        }
        return result;
    }

    protected static void copy(PrototypeDataHandler.PrototypeDataHandlerBuilder builder, PrototypeData proto) {
        var data = (PrototypeDataHandler) proto;

        builder
                .base(data.isBase())
                .name(data.getName())
                .generateConstructor(data.isGenerateConstructor())
                .interfaceName(data.getInterfaceName())
                .interfaceSetters(data.isInterfaceSetters())
                .classGetters(data.isClassGetters())
                .classSetters(data.isClassSetters())
                .generateInterface(data.isGenerateInterface())
                .generateImplementation(data.isGenerateImplementation())
                .classPackage(data.getClassPackage())
                .strategy(data.getStrategy())
                .basePath(data.getBasePath())
                .interfacePath(data.getInterfacePath())
                .implementationPath(data.getImplementationPath())
                .enrichers(withRes(data.getEnrichers(), ArrayList::new))
                .inheritedEnrichers(withRes(data.getInheritedEnrichers(), ArrayList::new))
                .predefinedEnrichers(withRes(data.getPredefinedEnrichers(), ArrayList::new))
                .predefinedInheritedEnrichers(withRes(data.getPredefinedEnrichers(), ArrayList::new));
        with(data.getCustom(), custom -> custom.forEach(builder::custom));
    }


    public static PrototypeData readAnnotation(Annotation ann) {
        var builder = defaultBuilder();
        readAnnotation(null, ann.annotationType(), builder, Structures::annotationDefaultValue);
        checkAnnotation(ann, ann.annotationType(), builder, Structures::readAnnotationValue);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static void readAnnotation(Annotation ann, Class<?> cls, PrototypeDataHandler.PrototypeDataHandlerBuilder builder, BiFunction<Method, Annotation, Object> func) {
        for (var method : cls.getDeclaredMethods()) {
            switch (method.getName()) {
                case "base" -> builder.base((boolean) func.apply(method, ann));
                case "name" -> builder.name(handleString(func.apply(method, ann)));
                case "generateConstructor" -> builder.generateConstructor((boolean) func.apply(method, ann));
                case "options" ->
                        builder.options((Set) Arrays.stream((Class[]) func.apply(method, ann)).collect(Collectors.toSet()));
                case "interfaceName" -> builder.interfaceName(handleString(func.apply(method, ann)));
                case "implementationPath" -> builder.implementationPath(handleString(func.apply(method, ann)));
                case "enrichers" ->
                        builder.predefinedEnrichers((List) Arrays.stream((Class[]) func.apply(method, ann)).toList());
                case "inheritedEnrichers" ->
                        builder.predefinedInheritedEnrichers((List) Arrays.stream((Class[]) func.apply(method, ann)).toList());
                case "interfaceSetters" -> builder.interfaceSetters((boolean) func.apply(method, ann));
                case "classGetters" -> builder.classGetters((boolean) func.apply(method, ann));
                case "classSetters" -> builder.classSetters((boolean) func.apply(method, ann));
                case "baseModifierClass" -> builder.baseModifierClass(handleClass(func.apply(method, ann)));
                case "mixInClass" -> builder.mixInClass(handleClass(func.apply(method, ann)));
                case "interfacePath" -> builder.interfacePath(handleString(func.apply(method, ann)));
                case "generateInterface" -> builder.generateInterface((boolean) func.apply(method, ann));
                case "basePath" -> builder.basePath(handleString(func.apply(method, ann)));
                case "generateImplementation" -> builder.generateImplementation((boolean) func.apply(method, ann));
                case "implementationPackage" -> builder.classPackage(handleString(func.apply(method, ann)));
                case "strategy" -> builder.strategy((GenerationStrategy) func.apply(method, ann));
                default -> builder.custom(method.getName(), func.apply(method, ann));
            }
        }
    }

    private static Object readAnnotationValue(Method method, Annotation ann) {
        try {
            return method.invoke(ann);
        } catch (Exception e) {
            log.warn("Unable to read value for {}() of annotation {}", method.getName(), ann.annotationType().getCanonicalName());
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private static <T extends Collection> T handleClassExpression(Expression value, Class<T> container, Class cls) {
        var result = Set.class.equals(container) ? new HashSet<>() : new ArrayList<>();
        if (value.isArrayInitializerExpr()) {
            value.asArrayInitializerExpr().getValues().forEach(v ->
                    with(handleClassExpression(v), r ->
                            with(checkLoadClass(r, cls), result::add)));
        } else if (value.isClassExpr()) {
            with(handleClassExpression(value), r ->
                    with(checkLoadClass(r, cls), result::add));
        } else {
            log.warn("Class expression not implemented: {}", value.getClass().getCanonicalName());
        }
        return (T) result;
    }

    private static Class checkLoadClass(String r, Class cls) {
        var result = loadClass(r);

        if (isNull(result)) {
            if (lookup.isExternal(r)) {
                if (Enricher.class.equals(cls)) {
                    result = ErrorOnInvokeEnricher.class;
                }
            } else {
                lookup.error("Option or Enricher '" + r + "' is not found in the classpath!", null);
            }
        }

        return result;
    }

    private static String handleClassExpression(Expression value) {
        if (value.isClassExpr()) {
            var cls = Helpers.getExternalClassNameIfExists(value.findCompilationUnit().get(), value.asClassExpr().getType().toString());
            return "void".equals(cls) ? null : cls;
        }
        log.warn("Class expression not implemented: {}", value.getClass().getCanonicalName());
        return null;
    }

    private static String handleStringExpression(Expression expression) {
        if (expression.isStringLiteralExpr()) {
            var value = expression.asStringLiteralExpr().getValue();
            return StringUtils.isBlank(value) ? null : value;
        }
        log.warn("String expression not implemented: {}", expression.getClass().getCanonicalName());
        return null;
    }

    private static <T extends Enum> T handleEnumExpression(Expression expression, Class<T> cls) {
        if (expression.isFieldAccessExpr()) {
            var result = Arrays.stream(cls.getEnumConstants()).filter(c -> c.name().equals(expression.asFieldAccessExpr().getNameAsString())).findFirst();
            if (result.isPresent()) {
                return result.get();
            } else {
                log.error("Unknown enum constant - {}", expression);
            }
        }
        throw new UnsupportedOperationException("Not implemented");

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
        if (nonNull(value)) {
            var val = (Class) value;
            return void.class.equals(val) ? null : val.getCanonicalName();
        }
        return null;
    }

    private static Object annotationDefaultValue(Method method, Annotation annotation) {
        var value = method.getDefaultValue();
        if ((value instanceof String s && StringUtils.isEmpty(s)) || void.class.equals(value)) {
            value = null;
        }
        return value;
    }

    protected static class ErrorOnInvokeEnricher extends BaseEnricher {

        @Override
        public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
            lookup.warn("Usage of enrichers that are not compiled yet!", description.getElement());
        }

        @Override
        public int order() {
            return 0;
        }

    }


}
