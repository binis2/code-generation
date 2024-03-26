/*Generated code by Binis' code generator.*/
package net.binis.codegen.redis;

import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.collection.CodeList;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.redis.RedisTestPrototype", comments = "RedisTestImpl")
@Default("net.binis.codegen.redis.RedisTestImpl")
public interface RedisTest {

    static RedisTest.Modify create(String key) {
        return CodeFactory.create(RedisTest.class, key).with();
    }

    int getData();
    List<String> getList();
    String getTitle();

    String key();

    static RedisTest load() {
        return Redis.load(RedisTest.class);
    }

    static RedisTest load(String key) {
        return Redis.load(key, RedisTest.class);
    }

    RedisTest.Modify with();

    interface Fields<T> {
        T data(int data);
        T title(String title);
    }

    interface Modify extends RedisTest.Fields<RedisTest.Modify>, BaseModifier<RedisTest.Modify, RedisTest> {
        CodeList<String, RedisTest.Modify> list();
        Modify list(List<String> list);
    }
}
