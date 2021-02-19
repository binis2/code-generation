package net.binis.codegen.enrich;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.codegen.interfaces.PrototypeData;
import net.binis.codegen.codegen.interfaces.PrototypeDescription;

import java.util.Collection;

public interface PrototypeLookup {

    void registerParsed(String prototype, PrototypeDescription<ClassOrInterfaceDeclaration> parsed);
    void registerGenerated(String prototype, PrototypeDescription<ClassOrInterfaceDeclaration> generated);
    PrototypeDescription<ClassOrInterfaceDeclaration> findParsed(String prototype);
    PrototypeDescription<ClassOrInterfaceDeclaration> findGenerated(String prototype);
    boolean isParsed(String prototype);
    boolean isGenerated(String prototype);
    Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> parsed();
    Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> generated();

    void generateEmbeddedModifier(PrototypeData properties);
    boolean embeddedModifierRequested(String prototype);

}
