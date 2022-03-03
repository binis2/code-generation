package net.binis.codegen;

import net.binis.codegen.test.BaseTest;
import org.junit.Test;

public class PresetsTest extends BaseTest {

    @Test
    public void test() {
        testSingle("preset/PresetTest1.java", "preset/PresetTest1-0.java", "preset/PresetTest1-1.java");
    }

}
