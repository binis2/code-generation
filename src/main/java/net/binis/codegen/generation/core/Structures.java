package net.binis.codegen.generation.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import lombok.*;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Structures {

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

        private List<PrototypeEnricher> enrichers;
        private List<PrototypeEnricher> inheritedEnrichers;
    }

    @ToString
    @Data
    @Builder
    public static class FieldData implements PrototypeField {
        private String name;
        private FieldDeclaration declaration;
        private MethodDeclaration description;
        private boolean collection;
        private Structures.Ignores ignores;
        @ToString.Exclude
        private PrototypeDescription<ClassOrInterfaceDeclaration> prototype;
        @ToString.Exclude
        private Map<String, Type> generics;
    }

    @Data
    @Builder
    public static class Parsed<T extends TypeDeclaration<T>> implements PrototypeDescription<T> {

        private Class<?> compiled;
        private String prototypeFileName;

        private PrototypeDataHandler properties;

        private String parsedName;
        private String parsedFullName;

        private String interfaceName;
        private String interfaceFullName;

        private TypeDeclaration<T> declaration;
        private List<CompilationUnit> files;

        private Parsed<T> base;
        private Parsed<T> mixIn;

        @EqualsAndHashCode.Exclude
        @Builder.Default
        private Map<String, ClassOrInterfaceDeclaration> classes = new HashMap<>();

        @EqualsAndHashCode.Exclude
        @Builder.Default
        private List<PrototypeField> fields = new ArrayList<>();

        private ClassOrInterfaceDeclaration spec;
        private ClassOrInterfaceDeclaration intf;

        @Builder.Default
        @EqualsAndHashCode.Exclude
        private List<Triple<ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration>> initializers = new ArrayList<>();

        public void registerClass(String key, ClassOrInterfaceDeclaration declaration) {
            classes.put(key, declaration);
        }

        public ClassOrInterfaceDeclaration getRegisteredClass(String key) {
            return classes.get(key);
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

}
