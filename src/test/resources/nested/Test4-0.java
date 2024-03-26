/*Generated code by Binis' code generator.*/
package net.binis.codegen.jackson;

import net.binis.codegen.jackson.CodeJacksonTestCollection.Item;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;
import java.util.Set;
import java.util.Map;
import java.util.List;

@Generated(value = "net.binis.codegen.jackson.JacksonTest.CodeJacksonTestCollectionPrototype", comments = "CodeJacksonTestCollection")
public class CodeJacksonTestCollectionImpl implements CodeJacksonTestCollection {

    protected CodeJacksonTestCollection.Item item;

    protected List<CodeJacksonTestCollection.Item> list;

    protected Map<String, CodeJacksonTestCollection.Item> map;

    protected String name;

    protected Set<CodeJacksonTestCollection.Item> set;

    static {
        CodeFactory.registerType(CodeJacksonTestCollection.class, CodeJacksonTestCollectionImpl::new, null);
    }

    public CodeJacksonTestCollectionImpl() {
    }

    public CodeJacksonTestCollection.Item getItem() {
        return item;
    }

    public List<CodeJacksonTestCollection.Item> getList() {
        return list;
    }

    public Map<String, CodeJacksonTestCollection.Item> getMap() {
        return map;
    }

    public String getName() {
        return name;
    }

    public Set<CodeJacksonTestCollection.Item> getSet() {
        return set;
    }

    public void setItem(CodeJacksonTestCollection.Item item) {
        this.item = item;
    }

    public void setList(List<CodeJacksonTestCollection.Item> list) {
        this.list = list;
    }

    public void setMap(Map<String, CodeJacksonTestCollection.Item> map) {
        this.map = map;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSet(Set<CodeJacksonTestCollection.Item> set) {
        this.set = set;
    }

    public static class ItemImpl implements Item {

        protected String value;

        static {
            CodeFactory.registerType(CodeJacksonTestCollection.Item.class, ItemImpl::new, null);
        }

        public ItemImpl() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
