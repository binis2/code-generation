package net.binis.codegen;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.test.BaseTest;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class BaseModifierTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void test() {
        testSingle("base/modifier/baseTest1.java", "base/modifier/baseTest1-0.java", "base/modifier/baseTest1-1.java");
    }

}
