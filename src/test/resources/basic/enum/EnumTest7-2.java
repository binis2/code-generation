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

        var test = Test.create().list().add(Test.TestEnum.ONE).done().done();

        assertEquals(Test.TestEnum.ONE, test.getList().get(0));

        return true;
    }
}
