package net.binis.codegen;

import net.binis.codegen.test.BaseCodeGenTest;
import org.junit.jupiter.api.Test;

class ElementInsertionTest extends BaseCodeGenTest {

    @Test
    void test() {
        testSingle("element/default1.java", "element/default1-0.java", "element/default1-1.java");
    }

}
