package net.binis.codegen.generation.core;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2024 Binis Belev
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
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import net.binis.codegen.compiler.CGClassSymbol;
import net.binis.codegen.compiler.CGMethodSymbol;
import net.binis.codegen.compiler.base.JavaCompilerObject;
import net.binis.codegen.compiler.utils.ElementUtils;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.CollectionsHandler.isCollection;
import static net.binis.codegen.generation.core.Helpers.*;

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

    public static MethodDeclaration method(String code) {
        return lookup.getParser().parseMethodDeclaration(code).getResult().get();
    }

    public static PrototypeField addField(PrototypeDescription<ClassOrInterfaceDeclaration> description, String name, String type) {
        return addField(description, name, new ClassOrInterfaceType(null, type));
    }

    public static String calcBlock(String value) {
        if (nonNull(value)) {
            value = value.trim();
            var result = new StringBuilder().append('{').append(value);
            var end = result.charAt(result.length() - 1);
            if (!value.isEmpty() && !(end == ';' || end == '}')) {
                result.append(';');
            }
            result.append('}');
            return result.toString();
        } else {
            return "{}";
        }
    }

    public static PrototypeField addField(PrototypeDescription<ClassOrInterfaceDeclaration> description, String name, Type type) {
        var field = description.getImplementation().addField(type, name, Modifier.Keyword.PROTECTED);
        var desc = new MethodDeclaration().setName(name).setType(type).setBody(null);
        Helpers.envelopWithDummyClass(desc, field);
        var result = Structures.FieldData.builder()
                .parsed((Structures.Parsed<ClassOrInterfaceDeclaration>) description)
                .declaration(field)
                .description(desc)
                .name(name)
                .fullType(Helpers.getExternalClassNameIfExists(description.getDeclarationUnit(), type.asString()))
                .type(type)
                .collection(isCollection(type))
                .ignores(Structures.Ignores.builder().build())
                .custom(true)
                .build();
        description.getFields().add(result);
        return result;
    }

    public static PrototypeField addField(PrototypeDescription<ClassOrInterfaceDeclaration> description, String name, Class<?> type) {
        var field = addField(description, name, type.getSimpleName());
        field.getDescription().findCompilationUnit().ifPresent(unit ->
                unit.addImport(type));
        if (!type.isPrimitive() && !type.getPackageName().equals("java.lang")) {
            description.getDeclarationUnit().addImport(type);
        }
        return field;
    }

    @SuppressWarnings("unchecked")
    public static List<JavaCompilerObject> deepFindElementList(Node node, PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        if (node.getParentNode().isEmpty() || node.getParentNode().get() instanceof CompilationUnit) {
            var name = node instanceof ClassOrInterfaceDeclaration decl ? decl.getFullyQualifiedName().orElse(decl.getNameAsString()) :
                    node instanceof EnumDeclaration enm ? enm.getFullyQualifiedName().orElse(enm.getNameAsString()) : getNodeName(node);
            return (List) lookup.getRoundEnvironment().getRootElements().stream()
                    .filter(TypeElement.class::isInstance)
                    .map(TypeElement.class::cast)
                    .filter(e -> e.getQualifiedName().isEmpty() ? e.getSimpleName().toString().equals(name) : e.getQualifiedName().toString().equals(name))
                    .map(CGClassSymbol::new)
                    .toList();
        }
        var result = deepFindElementList(node.getParentNode().get(), parsed);
        if (nonNull(result)) {
            if (node instanceof MethodDeclaration method) {
                var res = new ArrayList<JavaCompilerObject>();
                var name = method.getNameAsString();
                result.stream()
                        .filter(CGClassSymbol.class::isInstance)
                        .map(CGClassSymbol.class::cast)
                        .forEach(t -> {
                            var it = t.members().getSymbolsByName(name);
                            while (it.hasNext()) {
                                res.add(it.next());
                            }
                        });
                result = res;
            } else if (node instanceof Parameter param) {
                var res = new ArrayList<JavaCompilerObject>();
                var name = param.getNameAsString();
                var type = getExternalClassName(param, param.getTypeAsString());
                result.stream()
                        .filter(CGMethodSymbol.class::isInstance)
                        .map(CGMethodSymbol.class::cast)
                        .forEach(m -> {
                            m.params().forEach(p -> {
                                if ("ErrorType".equals(p.getType().getInstance().getClass().getSimpleName())) {
                                    if (p.getName().equals(name) && p.getVariableType().toString().equals(param.getTypeAsString())) {
                                        res.add(p);
                                    }
                                } else {
                                    if (p.getName().equals(name) && p.getVariableType().toString().equals(type)) {
                                        res.add(p);
                                    }
                                }
                            });
                        });
                result = res;
            }
        }
        return result;
    }

    public static Element deepFindElement(Node node, PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        if (nonNull(lookup.getRoundEnvironment())) {
            ElementUtils.init();
            var result = deepFindElementList(node, parsed);
            if (nonNull(result) && !result.isEmpty()) {
                return (Element) result.get(0).getInstance();
            }
        }
        return null;
    }

    private EnrichHelpers() {
        //Do nothing
    }

}
