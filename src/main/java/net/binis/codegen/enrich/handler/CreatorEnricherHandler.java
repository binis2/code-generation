package net.binis.codegen.enrich.handler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.enrich.CreatorEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

import static com.github.javaparser.ast.Modifier.Keyword.STATIC;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Constants.EMBEDDED_MODIFIER_KEY;

public class CreatorEnricherHandler extends BaseEnricher implements CreatorEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var properties = description.getProperties();
        var spec = description.getSpec();
        var intf = description.getIntf();
        var creatorClass = "EntityCreator";

        intf.findCompilationUnit().get().addImport("net.binis.codegen.creator." + creatorClass);

        Helpers.addDefaultCreation(description);

        intf.addMethod("create", STATIC)
                .setType(intf.getNameAsString())
                .setBody(new BlockStmt().addStatement(new ReturnStmt(creatorClass + ".create(" + intf.getNameAsString() + ".class)")));

        if (!properties.isBase()) {
            var type = spec;
            if (nonNull(description.getMixIn())) {
                type = description.getMixIn().getSpec();
            }

            Helpers.addInitializer(description, intf, type);
        }
    }

    @Override
    public int order() {
        return 0;
    }

}
