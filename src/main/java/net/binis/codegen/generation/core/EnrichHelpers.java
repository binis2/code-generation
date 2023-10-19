package net.binis.codegen.generation.core;

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
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import static net.binis.codegen.generation.core.Helpers.lookup;

public class EnrichHelpers {

    public static CompilationUnit unit(Node node) {
        return node.findCompilationUnit().get();
    }

    public static BlockStmt block(String code) {
        return lookup.getParser().parseBlock(code).getResult().get();
    }

    public static BlockStmt returnBlock(String variable) {
        return block("{return " + variable + ";}");
    }

    public static Statement statement(String code) {
        return lookup.getParser().parseStatement(code).getResult().get();
    }

    public static Expression expression(String code) {
        return lookup.getParser().parseExpression(code).getResult().get();
    }

    public static AnnotationExpr annotation(String code) {
        return lookup.getParser().parseAnnotation(code).getResult().get();
    }

    public static PrototypeField addField(PrototypeDescription<ClassOrInterfaceDeclaration> description, String name, String type) {
        return addField(description, name, new ClassOrInterfaceType(type));
    }

    public static PrototypeField addField(PrototypeDescription<ClassOrInterfaceDeclaration> description, String name, Type type) {
        var field = description.getImplementation().addField(String.class, name, Modifier.Keyword.PROTECTED);

        var result = Structures.FieldData.builder()
                .parsed((Structures.Parsed<ClassOrInterfaceDeclaration>) description)
                .declaration(field)
                .description(new MethodDeclaration().setName(name).setType(type))
                .name(name)
                .fullType(Helpers.getExternalClassNameIfExists(description.getDeclarationUnit(), type.asString()))
                .type(type)
                .ignores(Structures.Ignores.builder().build())
                .build();
        description.getFields().add(result);
        return result;
    }


    private EnrichHelpers() {
        //Do nothing
    }

}
