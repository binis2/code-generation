package net.binis.codegen;

import net.binis.codegen.test.TestExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Execute extends TestExecutor {

    @Override
    public boolean execute() {

        assertEquals("Test(title = One)", Test.ONE.toString());

        return true;
    }
}
