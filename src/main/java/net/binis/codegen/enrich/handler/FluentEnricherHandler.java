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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.enrich.FluentEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Generator.handleType;
import static net.binis.codegen.generation.core.Helpers.methodExists;

public class FluentEnricherHandler extends BaseEnricher implements FluentEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        for (var field : description.getFields()) {
            declareField(field, description);
        }

        if (nonNull(description.getBase())) {
            for (var field : description.getBase().getFields()) {
                declareField(field, description.getBase());
            }
        }

        //TODO: Handle mixins.

    }

    @Override
    public int order() {
        return 0;
    }

    private void declareField(PrototypeField field, PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var properties = description.getProperties();
        if (properties.isGenerateInterface()) {
            addMethod(description.getDeclaration().asClassOrInterfaceDeclaration(), description.getIntf(), false, field);
        }
        if (properties.isGenerateImplementation()) {
            addMethod(description.getDeclaration().asClassOrInterfaceDeclaration(), description.getSpec(), true, field);
        }
    }

    public static void addMethod(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, boolean isClass, PrototypeField field) {
        var name = field.getName();
        var method = new MethodDeclaration()
                .setName(name)
                .setType(spec.getNameAsString())
                .addParameter(new Parameter().setName(name).setType(handleType(type, spec, field.getDeclaration().getVariables().get(0).getType(), false)));
        if (!methodExists(spec, method, name, isClass)) {
            spec.addMember(method);
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + name)).setValue(new NameExpr().setName(name)))
                                .addStatement(new ReturnStmt().setExpression( new ThisExpr())));
                field.addModifier(method);
            } else {
                method.setBody(null);
            }
        }
    }


}
