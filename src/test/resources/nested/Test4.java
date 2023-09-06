package net.binis.codegen.jackson;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.CreatorEnricher;
import net.binis.codegen.options.HiddenCreateMethodOption;

import java.util.List;
import java.util.Map;
import java.util.Set;

class JacksonTest {

    @CodePrototype(interfaceSetters = false, enrichers = CreatorEnricher.class, options = HiddenCreateMethodOption.class)
    interface CodeJacksonTestCollectionPrototype {
        String name();
        List<Item> list();
        Set<Item> set();
        Map<String, Item> map();
        Item item();

        @CodePrototype(interfaceSetters = false, enrichers = CreatorEnricher.class, options = HiddenCreateMethodOption.class)
        interface Item {
            String value();
        }
    }

}
