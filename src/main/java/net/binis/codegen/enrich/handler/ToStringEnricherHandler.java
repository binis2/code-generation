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
import net.binis.codegen.enrich.ToStringEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.options.Options;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.EnrichHelpers.block;
import static net.binis.codegen.generation.core.Helpers.addImport;

public class ToStringEnricherHandler extends BaseEnricher implements ToStringEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 50000;
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var intf = description.getInterface();
        var impl = description.getImplementation();

        if (nonNull(impl)) {
            var name = nonNull(intf) ? intf.getNameAsString() : impl.getNameAsString();

            var method = impl.addMethod("toString", PUBLIC).setType("String").addAnnotation(Override.class);
            var body = new StringBuilder();
            body.append("{ return \"").append(name).append("(");
            var cnt = buildBody(description, impl, body);
            if (nonNull(description.getBase())) {
                if (cnt > 0) {
                    body.append("\", ");
                } else {
                    body.append("\"");
                }
                cnt = buildBody(description.getBase(), impl, body);
                if (cnt == 0) {
                    body.setLength(body.length() - 7);
                }
            }
            body.append("\")\"; }");

            method.setBody(block(body.toString()));
        }
    }

    protected int buildBody(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration impl, StringBuilder body) {
        var fields = description.getFields();
        var cnt = 0;
        for (var field : fields) {
            if (shouldInclude(field)) {
                cnt++;
                body.append(field.getName()).append(" = \" + ");
                if (field.isCollection()) {
                    addImport(impl, "net.binis.codegen.tools.CollectionUtils");
                    body.append("CollectionUtils.printInfo(").append(field.getName()).append(", ").append(description.hasOption(Options.TO_STRING_FULL_COLLECTION_INFO)).append(") + \", ");
                } else {
                    body.append(field.getName()).append(" + \", ");
                }
            }
        }
        if (cnt > 0) {
            body.delete(body.length() - 3, body.length());
        } else {
            body.append("\" + ");
        }
        return cnt;
    }

    protected boolean shouldInclude(PrototypeField field) {
        if (field.getIgnores().isForToString()) {
            return false;
        }
        if (field.getIgnores().isIncludedForToString()) {
            return true;
        }
        return !field.getParsed().hasOption(Options.TO_STRING_ONLY_EXPLICITLY_INCLUDED);
    }


}
