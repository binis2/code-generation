package net.binis.codegen.enrich.handler.constructor;

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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.*;
import net.binis.codegen.compiler.utils.ElementMethodUtils;
import net.binis.codegen.compiler.utils.ElementUtils;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.ElementDescription;

import java.util.List;
import java.util.stream.Stream;

import static net.binis.codegen.compiler.CGFlags.PUBLIC;
import static net.binis.codegen.compiler.utils.ElementUtils.cloneType;
import static net.binis.codegen.compiler.utils.ElementUtils.getDeclaration;

@Slf4j
public abstract class BaseArgsConstructorEnricherHandler extends BaseEnricher {

    @Override
    public void enrichElement(ElementDescription description) {
        var declaration = getDeclaration(description.getElement());
        if (declaration instanceof CGClassDeclaration cls && !cls.isInterface() && !cls.isEnum() && !cls.isAnnotation()) {
            var fields = applyFieldsFilter(cls.getDefs().stream()
                    .filter(CGVariableDecl.class::isInstance)
                    .map(CGVariableDecl.class::cast))
                    .toList();
            if (!fields.isEmpty() && cls.getDefs().stream()
                    .filter(CGMethodDeclaration.class::isInstance)
                    .map(CGMethodDeclaration.class::cast)
                    .filter(CGMethodDeclaration::isConstructor)
                    .filter(m -> m.getParameters().size() == fields.size())
                    .noneMatch(m -> matchParamTypes(m, fields))) {
                createConstructor(cls, fields);
            }
        } else {
            note(getName() + "ArgsConstructor is applicable only for classes.", description.getElement());
        }
    }

    protected abstract Stream<CGVariableDecl> applyFieldsFilter(Stream<CGVariableDecl> stream);

    protected abstract String getName();

    protected void createConstructor(CGClassDeclaration cls, List<CGVariableDecl> fields) {
        var maker = TreeMaker.create();
        var body = ElementMethodUtils.addConstructor(cls, PUBLIC, fields.stream()
                .map(f -> maker.VarDef(maker.Modifiers(CGFlags.PARAMETER | CGFlags.FINAL), CGName.create(f.getName().toString()), cloneType(maker, f.getVarType()), null))
                .toList()).getBody();
        fields.forEach(field ->
            body.getStatements().append(ElementMethodUtils.createStatement(maker.Assign(ElementUtils.createFieldAccess(maker, "this." + field.getName()), maker.Ident(field.getName())))));
    }

    protected boolean matchParamTypes(CGMethodDeclaration method, List<CGVariableDecl> fields) {
        var params = method.getParameters();
        for (var i = 0; i < params.size(); i++) {
            if (!params.get(i).getFullVariableType().equals(fields.get(i).getFullVariableType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int order() {
        return 0;
    }

}
