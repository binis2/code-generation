package net.binis.codegen;

import net.binis.codegen.test.TestExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Execute extends TestExecutor {

    @Override
    public boolean execute() {

        assertEquals("TestOne", TestEnum.ONE.getTitle());

        return true;
    }
}
