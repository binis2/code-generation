package net.binis.codegen.objects.enrichers;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2023 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class TestNoneStrategyEnricher extends BaseEnricher {
    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        assertEquals("net.binis.codegen.TestNoneStrategy", description.getPrototypeClassName());
        assertTrue(description.isValid());

        var desc = lookup.createCustomDescription("test");
        var unit = new CompilationUnit().setPackageDeclaration("net.binis.codegen");
        var intf = new ClassOrInterfaceDeclaration().setName("Test");
        unit.addType(intf);
        desc.setInterface(intf);
    }

    @Override
    public int order() {
        return 0;
    }
}
