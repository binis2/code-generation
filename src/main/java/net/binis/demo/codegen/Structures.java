package net.binis.demo.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
        private String interfaceName;
        private String interfacePackage;
        private String modifierName;
        private String longModifierName;
        private String modifierPackage;

        private String baseClassName;

        private boolean generateInterface;
        private boolean generateModifier;
        private boolean base;

        private String baseModifierClass;
        private String mixInClass;
    }

    @Data
    @Builder
    public static class Parsed {

        private PrototypeData properties;

        private String parsedName;
        private String parsedFullName;

        private String interfaceName;
        private String interfaceFullName;

        private String modifierName;
        private String modifierClassName;

        private ClassOrInterfaceDeclaration declaration;
        private List<CompilationUnit> files;

    }

}
