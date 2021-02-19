package net.binis.codegen.codegen;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.codegen.interfaces.PrototypeData;
import net.binis.codegen.codegen.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.PrototypeLookup;

import java.util.*;

public class PrototypeLookupHandler implements PrototypeLookup {

    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> parsed = new HashMap<>();
    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> generated = new HashMap<>();
    private final Set<String> requestedEmbeddedModifiers = new HashSet<>();


    @Override
    public void registerParsed(String prototype, PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        this.parsed.put(prototype, parsed);
    }

    @Override
    public void registerGenerated(String prototype, PrototypeDescription<ClassOrInterfaceDeclaration> generated) {
        this.generated.put(prototype, generated);
    }

    @Override
    public PrototypeDescription<ClassOrInterfaceDeclaration> findParsed(String prototype) {
        return parsed.get(prototype);
    }

    @Override
    public PrototypeDescription<ClassOrInterfaceDeclaration> findGenerated(String prototype) {
        return generated.get(prototype);
    }

    @Override
    public boolean isParsed(String prototype) {
        return parsed.containsKey(prototype);
    }

    @Override
    public boolean isGenerated(String prototype) {
        return generated.containsKey(prototype);
    }

    @Override
    public Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> parsed() {
        return parsed.values();
    }

    @Override
    public Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> generated() {
        return generated.values();
    }

    @Override
    public void generateEmbeddedModifier(PrototypeData properties) {
        requestedEmbeddedModifiers.add(properties.getPrototypeName());
    }

    @Override
    public boolean embeddedModifierRequested(String prototype) {
        return requestedEmbeddedModifiers.contains(prototype);
    }

    public void clean() {
        parsed.clear();
        generated.clear();
        requestedEmbeddedModifiers.clear();
    }

}
