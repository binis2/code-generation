package net.binis.codegen.redis;

import net.binis.codegen.test.TestExecutor;
import static org.junit.jupiter.api.Assertions.*;


public class Execute extends TestExecutor {

    @Override
    public boolean execute() {

        var obj = RedisTest.create("test");

        assertEquals("test", obj.key());

        return true;
    }
}
