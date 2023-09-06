/*Generated code by Binis' code generator.*/
package net.binis.codegen.jackson;

import net.binis.codegen.jackson.CodeJacksonTestCollection.Item;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;
import java.util.Set;
import java.util.Map;
import java.util.List;

@Generated(value = "net.binis.codegen.jackson.JacksonTest.CodeJacksonTestCollectionPrototype", comments = "CodeJacksonTestCollectionImpl")
@Default("net.binis.codegen.jackson.CodeJacksonTestCollectionImpl")
public interface CodeJacksonTestCollection {
    CodeJacksonTestCollection.Item getItem();
    List<CodeJacksonTestCollection.Item> getList();
    Map<String, CodeJacksonTestCollection.Item> getMap();
    String getName();
    Set<CodeJacksonTestCollection.Item> getSet();

    @Default("net.binis.codegen.jackson.CodeJacksonTestCollectionImpl$ItemImpl")
    public interface Item {
        String getValue();
    }
}
