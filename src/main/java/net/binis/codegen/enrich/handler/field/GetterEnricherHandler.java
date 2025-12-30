package net.binis.codegen.enrich.handler.field;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2026 Binis Belev
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

import net.binis.codegen.compiler.*;
import net.binis.codegen.compiler.utils.ElementMethodUtils;
import net.binis.codegen.compiler.utils.ElementUtils;
import net.binis.codegen.enrich.field.GetterEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.ElementDescription;

import javax.lang.model.element.Element;
import java.util.List;

import static net.binis.codegen.compiler.CGFlags.PUBLIC;
import static net.binis.codegen.compiler.utils.ElementMethodUtils.*;
import static net.binis.codegen.compiler.utils.ElementUtils.cloneType;
import static net.binis.codegen.compiler.utils.ElementUtils.getDeclaration;

public class GetterEnricherHandler extends BaseEnricher implements GetterEnricher {

    @Override
    public void safeEnrichElement(ElementDescription description) {
        var declaration = getDeclaration(description.getElement());
        if (declaration instanceof CGClassDeclaration cls && !cls.isInterface()) {
            if (!cls.isAnnotation()) {
                cls.getDefs().stream()
                        .filter(CGVariableDecl.class::isInstance)
                        .map(CGVariableDecl.class::cast)
                        .filter(v -> !v.isStatic())
                        .toList().forEach(field -> {
                            var name = Helpers.getGetterName(field.getName().toString(), field.getVariableType());
                            if (cls.getDefs().stream()
                                    .filter(CGMethodDeclaration.class::isInstance)
                                    .map(CGMethodDeclaration.class::cast)
                                    .filter(m -> !m.isStatic())
                                    .filter(m -> m.getParameters().size() == 0)
                                    .noneMatch(m -> m.getName().toString().equals(name))) {
                                createGetter(description.getElement(), name, field);
                            }
                        });
            }
        } else if (declaration instanceof CGVariableDecl field) {
            //TODO: Implement for field.
        } else {
            note("Getter is applicable only for classes and fields.", description.getElement());
        }
    }

    protected void createGetter(Element element, String name, CGVariableDecl field) {
        ElementMethodUtils.addMethod(element, name, field.getVarType(), PUBLIC, List.of(), createBlock(createReturnStatement(TreeMaker.create().Ident(field.getName()))));
    }

    @Override
    public int order() {
        return 0;
    }
}
