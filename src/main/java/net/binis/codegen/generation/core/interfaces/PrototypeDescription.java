package net.binis.codegen.generation.core.interfaces;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public interface PrototypeDescription<T extends TypeDeclaration<T>> {

    Class<?> getCompiled();

    String getPrototypeFileName();

    PrototypeData getProperties();

    String getParsedName();
    String getParsedFullName();

    String getInterfaceName();
    String getInterfaceFullName();

    TypeDeclaration<T> getDeclaration();
    List<CompilationUnit> getFiles();

    PrototypeDescription<T> getBase();
    PrototypeDescription<T> getMixIn();

    List<PrototypeField> getFields();

    ClassOrInterfaceDeclaration getSpec();
    ClassOrInterfaceDeclaration getIntf();

    List<Triple<ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration, ClassOrInterfaceDeclaration>> getInitializers();

    void registerClass(String key, ClassOrInterfaceDeclaration declaration);
    ClassOrInterfaceDeclaration getRegisteredClass(String key);

}
