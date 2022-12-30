package net.binis.codegen;

import net.binis.codegen.test.BaseTest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;

class CustomPrototypeTest extends BaseTest {

    @Test
    void test() {
        testMulti(List.of(
                Triple.of("custom/custom.java", null, null),
                Triple.of("custom/custom1.java", "custom/custom1-0.java", "custom/custom1-1.java")));
    }

}
