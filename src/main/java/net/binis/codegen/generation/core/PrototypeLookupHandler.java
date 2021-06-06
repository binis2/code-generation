package net.binis.codegen.generation.core;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.PrototypeLookup;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Tools.*;

@Slf4j
public class PrototypeLookupHandler implements PrototypeLookup {

    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> parsed = new HashMap<>();
    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> generated = new HashMap<>();
    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> requestedEmbeddedModifiers = new HashMap<>();


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
    public PrototypeDescription<ClassOrInterfaceDeclaration> findByInterfaceName(String name) {
        return parsed.values().stream().filter(p -> p.getInterfaceName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public PrototypeField findField(String prototype, String name) {
        return nullCheck(findParsed(prototype), parsed ->
                parsed.getFields().stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null));
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
    public void generateEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        requestedEmbeddedModifiers.putIfAbsent(parsed.getDeclaration().getFullyQualifiedName().get(), parsed);
    }

    @Override
    public boolean embeddedModifierRequested(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        return requestedEmbeddedModifiers.containsKey(parsed.getDeclaration().getFullyQualifiedName().get());
    }

    @Override
    public List<PrototypeDescription<ClassOrInterfaceDeclaration>> findGeneratedByFileName(String fileName) {
        return generated.values().stream().filter(g -> fileName.equals(g.getPrototypeFileName())).collect(Collectors.toList());
    }

    @Override
    public void generateEmbeddedModifier(String type, PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        if (!Helpers.isJavaType(type)) {
            var full = Helpers.getExternalClassName(parsed.getDeclaration().findCompilationUnit().get(), type);
            var p = findParsed(full);
            if (nonNull(p)) {
                generateEmbeddedModifier(p);
            } else {
                parsed.getSpec().findCompilationUnit().ifPresent(u -> u.addImport(full));
                parsed.getIntf().findCompilationUnit().ifPresent(u -> u.addImport(full));
            }
        }
    }

    public void clean() {
        parsed.clear();
        generated.clear();
        requestedEmbeddedModifiers.clear();
    }

}
