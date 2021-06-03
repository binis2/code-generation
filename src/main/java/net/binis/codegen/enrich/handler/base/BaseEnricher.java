package net.binis.codegen.enrich.handler.base;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.PrototypeLookup;

public abstract class BaseEnricher implements PrototypeEnricher {

    protected PrototypeLookup lookup;

    @Override
    public void init(PrototypeLookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void setup(PrototypeData properties) {
        //Do nothing
    }

    @Override
    public void finalize(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

}
