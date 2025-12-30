package net.binis.codegen.enrich.handler;

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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.enrich.AsEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.objects.Projectable;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static net.binis.codegen.generation.core.Constants.MIXIN_MODIFYING_METHOD_PREFIX;

public class AsEnricherHandler extends BaseEnricher implements AsEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        description.getImplementation().findCompilationUnit().ifPresent(u -> u.addImport(CodeFactory.class));
        description.getInterface().addExtendedType(Projectable.class);
        addAsMethod(description.getImplementation());
        addCastMethod(description.getImplementation());
    }

    @Override
    public int order() {
        return 0;
    }

    private static void addAsMethod(ClassOrInterfaceDeclaration spec) {
        spec.addMethod(MIXIN_MODIFYING_METHOD_PREFIX)
                .setType("T")
                .addParameter("Class<T>", "cls")
                .addTypeParameter("T")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("CodeFactory.projection(this, cls)"))));
    }

    private static void addCastMethod(ClassOrInterfaceDeclaration spec) {
        spec.addMethod("cast")
                .setType("T")
                .addParameter("Class<T>", "cls")
                .addTypeParameter("T")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("CodeFactory.cast(this, cls)"))));
    }


}
