package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.handler.AsEnricher;
import org.junit.Test;

public class EnrichTest extends BasicsTest{

    @Test
    public void enrichAs() {
        testSingle("basic/enrichAs.java", "basic/enrichAs-0.java", "basic/enrichAs-1.java");
    }

}
