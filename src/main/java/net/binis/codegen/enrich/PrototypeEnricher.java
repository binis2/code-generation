package net.binis.codegen.enrich;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.codegen.interfaces.PrototypeData;
import net.binis.codegen.codegen.interfaces.PrototypeDescription;

public interface PrototypeEnricher {

    void init(PrototypeLookup lookup);
    void setup(PrototypeData properies);
    void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description);

}
