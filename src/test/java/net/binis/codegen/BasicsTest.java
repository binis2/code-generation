package net.binis.codegen;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.base.BaseTest;
import net.binis.codegen.codegen.Helpers;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class BasicsTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void test() {
        testSingle("basic/Test1.java", "basic/Test1-0.java", "basic/Test1-1.java");
    }

}
