package net.binis.codegen;

import net.binis.codegen.base.BaseTest;
import net.binis.codegen.codegen.Helpers;
import org.junit.Before;
import org.junit.Test;

public class EnrichTest extends BaseTest {

    @Before
    public void cleanUp() {
        Helpers.cleanUp();
    }

    @Test
    public void enrichAs() {
        testSingle("enrich/enrichAs.java", "enrich/enrichAs-0.java", "enrich/enrichAs-1.java");
    }

    @Test
    public void enrichCreator() {
        testSingle("enrich/enrichCreator.java", "enrich/enrichCreator-0.java", "enrich/enrichCreator-1.java");
    }

    @Test
    public void enrichCreatorModifier() {
        testSingle("enrich/enrichCreatorModifier.java", "enrich/enrichCreatorModifier-0.java", "enrich/enrichCreatorModifier-1.java");
    }

    @Test
    public void enrichCreatorModifierWithoutModifier() {
        testSingle("enrich/enrichCreatorModifier2.java", "enrich/enrichCreatorModifier2-0.java", "enrich/enrichCreatorModifier2-1.java");
    }

    @Test
    public void enrichWithBaseWithModifier() {
        testSingleWithBase("enrich/enrichBase1.java", "net.binis.codegen.BaseImpl",
                "enrich/enrichBaseTest1.java", "net.binis.codegen.TestImpl",
                "enrich/enrichBase1-0.java", "enrich/enrichBase1-1.java",
                "enrich/enrichBaseTest1-0.java", "enrich/enrichBaseTest1-1.java");
    }


}
