package net.binis.codegen.enrich;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

public interface PrototypeEnricher {

    void init(PrototypeLookup lookup);
    void setup(PrototypeData properies);
    void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description);
    void finalize(PrototypeDescription<ClassOrInterfaceDeclaration> description);
    int order();

}
