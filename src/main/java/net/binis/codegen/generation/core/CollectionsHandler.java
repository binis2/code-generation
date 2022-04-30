package net.binis.codegen.generation.core;

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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static java.util.Objects.isNull;
import static net.binis.codegen.generation.core.Generator.getGenericsList;
import static net.binis.codegen.generation.core.Helpers.getExternalClassName;
import static net.binis.codegen.generation.core.Helpers.methodExists;

@Slf4j
public class CollectionsHandler {

    public static boolean isCollection(Type type) {
        if (type.isClassOrInterfaceType()) {
            return isCollection(type.asClassOrInterfaceType().getNameAsString());
        }

        return false;
    }

    public static boolean isCollection(Class<?> type) {
        return isCollection(type.getName());
    }

    public static boolean isListOrSet(String type) {
        return ("List".equals(type) || "Set".equals(type) || "CodeList".equals(type) || "CodeSet".equals(type));
    }

    public static boolean isCollection(String type) {
        return isListOrSet(type) || "Map".equals(type) || "CodeMap".equals(type);
    }

    public static void addModifier(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String modifierName, String className, boolean isClass) {
        if (!methodExists(spec, declaration, isClass)) {
            var type = declaration.getDeclaration().getVariables().get(0).getType().asClassOrInterfaceType();
            var collection = isNull(declaration.getDescription()) ?
                    getCollectionType(declaration.getDeclaration().findCompilationUnit().get(), spec.findCompilationUnit().get(), type) :
                    getCollectionType(declaration.getDescription().findCompilationUnit().get(), spec.findCompilationUnit().get(), declaration.getDescription().getType().asClassOrInterfaceType());
            var generic = collection.getGeneric().stream().map(Pair::getLeft).collect(Collectors.joining(", "));
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport(collection.getInterfaceImport());
                u.addImport(collection.getImplementorInterface());
            });

            var method = spec
                    .addMethod(declaration.getName())
                    .setType(collection.getType() + (!isClass ? ("<" + (collection.isPrototypeParam() ? generic + ".EmbeddedCollectionModify<" + modifierName + ".EmbeddedModify<T, R>>, " : "") + generic + ", " + modifierName + ".EmbeddedModify<T, R>>") : ""));
            if (isClass) {
                var parent = className + ".this." + declaration.getName();
                var block = new BlockStmt()
                        .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " == null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))));
                if (collection.isPrototypeParam()) {
                    block.addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ", " + generic + ".class)")));
                } else {
                    block.addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ")")));
                }
                method
                        .addModifier(PUBLIC)
                        .setBody(block);
                spec.findCompilationUnit().get().addImport(collection.getClassImport());
            } else {
                method.setBody(null);
            }
        }
    }

    public static CollectionType getCollectionType(CompilationUnit source, CompilationUnit destination, ClassOrInterfaceType type) {
        var generic = getGenericsList(source, destination, type, true);

        var builder = CollectionType.builder().generic(generic);
        switch (type.getNameAsString()) {
            case "List":
            case "CodeList":
                builder
                        .type("CodeList")
                        .classType("CodeListImpl")
                        .implementor("java.util.ArrayList")
                        .implementorInterface("java.util.List")
                        .prototypeParam(isPrototypeParam(type, generic));
                break;
            case "Set":
            case "CodeSet":
                builder
                        .type("CodeSet")
                        .classType("CodeSetImpl")
                        .implementor("java.util.HashSet")
                        .implementorInterface("java.util.Set")
                        .prototypeParam(isPrototypeParam(type, generic));
                break;
            case "Map":
            case "CodeMap":
                builder
                        .type("CodeMap")
                        .classType("CodeMapImpl")
                        .implementor("java.util.HashMap")
                        .implementorInterface("java.util.Map");
                break;
            default:
                builder
                        .type("Unknown")
                        .classType("UnknownImpl");
        }

        var result = builder.build();

        if (result.isPrototypeParam()) {
            result.setType("EmbeddedCodeCollection");
            result.setClassType("Embedded" + result.getClassType());
        }

        result.setInterfaceImport("net.binis.codegen.collection." + result.getType());
        result.setClassImport("net.binis.codegen.collection." + result.getClassType());

        return result;
    }

    private static boolean isPrototypeParam(ClassOrInterfaceType type, List<Pair<String, Boolean>> generic) {
        var arguments = type.getTypeArguments();
        if (arguments.isPresent() && arguments.get().isNonEmpty()) {
            return !arguments.get().get(0).toString().equals(generic.get(0).getKey());
        }
        return false;
    }

    public static String getCollectionType(Type type) {
        if (type.isClassOrInterfaceType()) {
            var args = type.asClassOrInterfaceType().getTypeArguments();
            if (args.isPresent() && (args.get().size() == 1)) {
                return args.get().get(0).asString();
            }
        }
        return "Object";
    }

    public static String getFullCollectionType(Type type) {
        return getExternalClassName(type.findCompilationUnit().get(), getCollectionType(type));
    }


    @Data
    @Builder
    public static class CollectionType {
        private String type;
        private String classType;
        private String classImport;
        private String interfaceImport;
        private List<Pair<String, Boolean>> generic;
        private String implementor;
        private String implementorInterface;
        private boolean prototypeParam;
    }

}
