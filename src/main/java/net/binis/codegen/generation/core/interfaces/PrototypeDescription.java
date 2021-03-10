package net.binis.codegen.generation.core.interfaces;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.List;

public interface PrototypeDescription<T extends TypeDeclaration<T>> {

    String getPrototypeFileName();

    PrototypeData getProperties();

    String getParsedName();
    String getParsedFullName();

    String getInterfaceName();
    String getInterfaceFullName();

    String getModifierName();
    String getModifierClassName();

    TypeDeclaration<T> getDeclaration();
    List<CompilationUnit> getFiles();

    PrototypeDescription<T> getBase();
    PrototypeDescription<T> getMixIn();

    List<PrototypeField> getFields();

    ClassOrInterfaceDeclaration getSpec();
    ClassOrInterfaceDeclaration getIntf();
    ClassOrInterfaceDeclaration getModifier();
    ClassOrInterfaceDeclaration getEmbeddedModifier();
    ClassOrInterfaceDeclaration getModifierIntf();
    ClassOrInterfaceDeclaration getEmbeddedModifierIntf();

}
