package net.binis.codegen.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class Structures {

    @Data
    @Builder
    public static class PrototypeData {
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
        private String creatorClass;
        private boolean creatorModifier;
        private String mixInClass;
    }

    @Data
    @Builder
    public static class Parsed<T extends TypeDeclaration<T>> {

        private PrototypeData properties;

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
