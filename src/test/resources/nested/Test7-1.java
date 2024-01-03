/*Generated code by Binis' code generator.*/
package net.binis.codegen.jackson;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.jackson.BaseClass.Item", comments = "ItemImpl")
@Default("net.binis.codegen.jackson.ItemImpl")
public interface Item extends CodeEnum {

    static final Item TEST = CodeFactory.initializeEnumValue(Item.class, "TEST", 0);

    static final Item TEST2 = CodeFactory.initializeEnumValue(Item.class, "TEST2", 1);

    static Item valueOf(String name) {
        return CodeFactory.enumValueOf(Item.class, name);
    }

    static Item valueOf(int ordinal) {
        return CodeFactory.enumValueOf(Item.class, ordinal);
    }

    static Item[] values() {
        return CodeFactory.enumValues(Item.class);
    }
}
