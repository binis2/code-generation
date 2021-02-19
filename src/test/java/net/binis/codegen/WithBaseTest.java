package net.binis.codegen;

import net.binis.codegen.test.BaseTest;
import net.binis.codegen.codegen.Helpers;
import org.junit.Before;
import org.junit.Test;

public class WithBaseTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void base() {
        testSingleWithBase("base/base1.java", "net.binis.codegen.BaseImpl",
                "base/baseTest1.java", "net.binis.codegen.TestImpl",
                "base/base1-0.java", "base/base1-1.java",
                "base/baseTest1-0.java", "base/baseTest1-1.java");
    }

    @Test
    public void baseWithModifier() {
        testSingleWithBase("base/base1.java", "net.binis.codegen.BaseImpl",
                "base/baseTest2.java", "net.binis.codegen.TestImpl",
                "base/base1-0.java", "base/base1-1.java",
                "base/baseTest2-0.java", "base/baseTest2-1.java");
    }


}
