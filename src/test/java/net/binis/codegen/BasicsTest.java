package net.binis.codegen;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.base.BaseTest;
import org.junit.Test;

import static net.binis.codegen.codegen.Helpers.lookup;
import static org.junit.Assert.assertEquals;
import static net.binis.codegen.tools.Tools.with;

@Slf4j
public class BasicsTest extends BaseTest {

    @Test
    public void test() {
        testSingle("basic/Test1.java", "basic/Test1-0.java", "basic/Test1-1.java");
    }

}
