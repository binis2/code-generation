package net.binis.codegen.enrich;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

@Slf4j
public class ElementInsertionTestEnricher extends BaseEnricher {
    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        log.info("yay");
    }

    @Override
    public int order() {
        return 0;
    }
}
