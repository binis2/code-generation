package net.binis.codegen;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.test.BaseTest;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class AnnotationsTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void test() {
        testSingle("annotation/default1.java", "annotation/default1-0.java", "annotation/default1-1.java");
    }

    @Test
    public void testFor() {
        testSingle("annotation/testFor1.java", "annotation/testFor1-0.java", "annotation/testFor1-1.java");
    }


}
