package net.binis.codegen.generation.core.interfaces;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import net.binis.codegen.generation.core.Structures;

import java.util.Map;

public interface PrototypeField {

    String getName();
    MethodDeclaration getDescription();
    FieldDeclaration getDeclaration();
    boolean isCollection();
    Structures.Ignores getIgnores();
    PrototypeDescription<ClassOrInterfaceDeclaration> getPrototype();
    Map<String, Type> getGenerics();

}
