package net.binis.codegen.generation.core.interfaces;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

public interface PrototypeConstant {

    ClassOrInterfaceDeclaration getDestination();
    FieldDeclaration getField();
    String getName();

}
