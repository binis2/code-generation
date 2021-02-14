package net.binis.codegen;

import net.binis.codegen.codegen.Helpers;
import org.junit.Before;
import org.junit.Test;

public class EnrichTest extends BasicsTest{

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void enrichAs() {
        testSingle("basic/enrichAs.java", "basic/enrichAs-0.java", "basic/enrichAs-1.java");
    }

    @Test
    public void enrichCreator() {
        testSingle("basic/enrichCreator.java", "basic/enrichCreator-0.java", "basic/enrichCreator-1.java");
    }

    @Test
    public void enrichCreatorModifier() {
        testSingle("basic/enrichCreatorModifier.java", "basic/enrichCreatorModifier-0.java", "basic/enrichCreatorModifier-1.java");
    }

    @Test
    public void enrichCreatorModifierWithoutModifier() {
        testSingle("basic/enrichCreatorModifier2.java", "basic/enrichCreatorModifier2-0.java", "basic/enrichCreatorModifier2-1.java");
    }


}
