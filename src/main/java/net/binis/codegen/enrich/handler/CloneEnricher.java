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
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import java.util.Map;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Constants.CLONE_METHOD;

public class CloneEnricher extends BaseEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        addCloneMethod(description, true);
        addCloneMethod(description, false);
    }

    @Override
    public int order() {
        return 0;
    }

    private static void addCloneMethod(PrototypeDescription<ClassOrInterfaceDeclaration> description, boolean isClass) {
        var spec = isClass ? description.getSpec() : description.getIntf();
        var method = spec
                .addMethod(CLONE_METHOD)
                .setType(description.getIntf().getNameAsString());
        if (isClass) {
            var body = new BlockStmt()
                    .addStatement(new VariableDeclarationExpr().addVariable(new VariableDeclarator().setType("var").setName("result").setInitializer("new " + spec.getNameAsString() + "()")));

            if (nonNull(description.getBase())) {
                description.getBase().getFields().forEach(field -> declareField(description, spec, field, body));
            }
            description.getFields().forEach(field -> declareField(description, spec, field, body));

            body.addStatement(new ReturnStmt().setExpression(new NameExpr().setName("result")));
            method
                    .addModifier(PUBLIC)
                    .setBody(body);
        } else {
            method.setBody(null);
        }
    }

    private static void declareField(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration spec, PrototypeField field, BlockStmt body) {
        if (field.isCollection()) {
            spec.findCompilationUnit().get().addImport("java.util.stream.Collectors");

            var collection = CollectionsHandler.getCollectionType(description.getDeclaration().findCompilationUnit().get(), spec.findCompilationUnit().get(), field.getDeclaration().getVariable(0).getType().asClassOrInterfaceType());

            var initExpr = "result." + field.getName() + " = " + field.getName();
            switch (collection.getImplementorInterface()) {
                case "java.util.List":
                    initExpr += ".stream().collect(Collectors.toList());";
                    break;
                case "java.util.Set":
                    initExpr += ".stream().collect(Collectors.toSet());";
                    break;
                case "java.util.Map":
                    initExpr += ".entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));";
                    break;
            }

            body.addStatement(new IfStmt().setCondition(new BinaryExpr().setLeft(new NameExpr().setName(field.getName())).setRight(new NullLiteralExpr()).setOperator(BinaryExpr.Operator.NOT_EQUALS)).setThenStmt(
                    new BlockStmt().addStatement(description.getParser().parseStatement(initExpr).getResult().get())));
        } else {
            body.addStatement(new AssignExpr().setTarget(new NameExpr().setName("result." + field.getName())).setValue(new NameExpr().setName(field.getName())));
        }
    }

}