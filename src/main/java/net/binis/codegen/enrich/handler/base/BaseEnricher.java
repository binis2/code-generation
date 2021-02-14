package net.binis.codegen.enrich.handler.base;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.codegen.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.PrototypeLookup;

public abstract class BaseEnricher implements PrototypeEnricher {

    protected PrototypeLookup lookup;

    @Override
    public void init(PrototypeLookup lookup) {
        this.lookup = lookup;
    }

    protected ClassOrInterfaceDeclaration getImplementation(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        return description.getFiles().get(0).findFirst(ClassOrInterfaceDeclaration.class).get();
    }

    protected ClassOrInterfaceDeclaration getInterface(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        return description.getFiles().get(1).findFirst(ClassOrInterfaceDeclaration.class).get();
    }


}
