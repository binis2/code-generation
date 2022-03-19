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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import lombok.*;
import net.binis.codegen.enrich.*;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Structures {

    public static final Map<String, Supplier<PrototypeDataHandler.PrototypeDataHandlerBuilder>> defaultProperties = initDefaultProperties();

    @Getter
    @Setter
    @Builder
    public static class PrototypeDataHandler implements PrototypeData {

        private String prototypeName;
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

        private List<PrototypeEnricher> enrichers;
        private List<PrototypeEnricher> inheritedEnrichers;

        private List<Class<? extends Enricher>> predefinedEnrichers;
        private List<Class<? extends Enricher>> predefinedInheritedEnrichers;

        //Custom builder to satisfy java-doc
        public static class PrototypeDataHandlerBuilder {

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
        private Structures.Ignores ignores;
        @ToString.Exclude
        private PrototypeDescription<ClassOrInterfaceDeclaration> prototype;
        @ToString.Exclude
        private Map<String, Type> generics;
        @ToString.Exclude
        private Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> typePrototypes;
        @ToString.Exclude
        private List<MethodDeclaration> modifiers;

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
        private List<Triple<ClassOrInterfaceDeclaration, Node, ClassOrInterfaceDeclaration>> initializers = new ArrayList<>();

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

    }

    @Data
    @Builder
    public static class Ignores {
        private boolean forField;
        private boolean forClass;
        private boolean forInterface;
        private boolean forModifier;
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

    private static PrototypeDataHandler.PrototypeDataHandlerBuilder defaultBuilder() {
        return Structures.PrototypeDataHandler.builder()
                .generateConstructor(true)
                .generateInterface(true)
                .generateImplementation(true)
                .classGetters(true)
                .classSetters(true)
                .interfaceSetters(true)
                .modifierName(net.binis.codegen.generation.core.Constants.MODIFIER_INTERFACE_NAME);
    }

    private static Map<String, Supplier<PrototypeDataHandler.PrototypeDataHandlerBuilder>> initDefaultProperties() {
        return Map.of(
                "CodePrototype", Structures::defaultBuilder,
                "CodeBuilder", () -> defaultBuilder()
                        .predefinedEnrichers(List.of(CreatorModifierEnricher.class, ModifierEnricher.class))
                        .classSetters(false)
                        .interfaceSetters(false),
                "CodeValidationBuilder", () -> defaultBuilder()
                        .predefinedEnrichers(List.of(ValidationEnricher.class, CreatorModifierEnricher.class, ModifierEnricher.class))
                        .classSetters(false)
                        .interfaceSetters(false),
                "CodeQueryBuilder", () -> defaultBuilder()
                        .predefinedEnrichers(List.of(QueryEnricher.class, ValidationEnricher.class, CreatorModifierEnricher.class, ModifierEnricher.class))
                        .classSetters(false)
                        .interfaceSetters(false)
                        .baseModifierClass("net.binis.codegen.spring.BaseEntityModifier"));
    }

}
