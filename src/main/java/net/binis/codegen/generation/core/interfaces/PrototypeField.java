package net.binis.codegen.generation.core.interfaces;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

public interface PrototypeField {

    String getName();
    FieldDeclaration getDeclaration();
    boolean isCollection();
    PrototypeDescription<ClassOrInterfaceDeclaration> getPrototype();

}
