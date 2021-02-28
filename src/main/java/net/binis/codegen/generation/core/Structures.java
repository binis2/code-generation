package net.binis.codegen.generation.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.ToString;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import java.util.ArrayList;
import java.util.List;

public class Structures {

    @Data
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
        private boolean generateModifier;
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
        private boolean collection;
        @ToString.Exclude
        private PrototypeDescription<ClassOrInterfaceDeclaration> prototype;
    }

    @Data
    @Builder
    public static class Parsed<T extends TypeDeclaration<T>> implements PrototypeDescription<T> {

        private PrototypeDataHandler properties;

        private String parsedName;
        private String parsedFullName;

        private String interfaceName;
        private String interfaceFullName;

        private String modifierName;
        private String modifierClassName;

        private TypeDeclaration<T> declaration;
        private List<CompilationUnit> files;

        private Parsed<T> base;
        private Parsed<T> mixIn;

        @Builder.Default
        private List<PrototypeField> fields = new ArrayList<>();

        private ClassOrInterfaceDeclaration spec;
        private ClassOrInterfaceDeclaration intf;
        private ClassOrInterfaceDeclaration modifier;
        private ClassOrInterfaceDeclaration embeddedModifier;
        private ClassOrInterfaceDeclaration modifierIntf;
        private ClassOrInterfaceDeclaration embeddedModifierIntf;
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