package net.binis.codegen;

import net.binis.codegen.test.TestExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unchecked")
public class Execute extends TestExecutor {

    @Override
    public boolean execute() {

        var s = new TestImpl();
        assertEquals("Test(id = 0)", s.toString());

        return true;
    }
}
