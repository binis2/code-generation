package net.binis.codegen.enrich.base;

import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.PrototypeLookup;

public class BaseEnricher implements PrototypeEnricher {

    protected PrototypeLookup lookup;

    @Override
    public void init(PrototypeLookup lookup) {
        this.lookup = lookup;
    }

}
