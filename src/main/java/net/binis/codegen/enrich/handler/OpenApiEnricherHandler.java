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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.expr.*;

import net.binis.codegen.annotation.Default;
import net.binis.codegen.enrich.OpenApiEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.options.Options;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.javaparser.StaticJavaParser.parseName;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.with;

public class OpenApiEnricherHandler extends BaseEnricher implements OpenApiEnricher {

    protected static final boolean IS_OPENAPI_AVAILABLE = nonNull(loadClass("io.swagger.v3.oas.annotations.media.Schema"));

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 5000;
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (generate(description)) {
            var intf = description.getInterface();
            intf.findCompilationUnit().ifPresent(unit -> {
                unit.addImport("io.swagger.v3.oas.annotations.media", false, true);
                description.getFields().forEach(this::enrichField);
            });
        }
    }

    @SuppressWarnings("unchecked")
    protected void enrichField(PrototypeField field) {
        var getter = field.forceGenerateInterfaceGetter();
        if (nonNull(getter)) {
            var ann = field.getDescription().getAnnotationByName("Schema")
                    .map(AnnotationExpr::clone)
                    .map(NormalAnnotationExpr.class::cast)
                    .orElseGet(() -> new NormalAnnotationExpr(parseName("Schema"), new NodeList<>()));
            getter.addAnnotation(ann);

            field.getDeclaration().getAnnotationByName("Schema").ifPresent(Node::remove);

            if (notPairExists(ann, "name")) {
                ann.addPair("name", "\"" + field.getName() + "\"");
            }

            if (notPairExists(ann, "required")) {
                field.getDescription().getAnnotationByName("ValidateNull").ifPresent(a ->
                        ann.addPair("required", "true"));
            }

            checkForDefaultValue(field, ann);
            checkForLength(field, ann);
            checkForRange(field, ann);


            if (nonNull(field.getPrototype()) && field.getPrototype().isCodeEnum()) {
                generateEnumSchema(field.getPrototype().getDeclaration().asEnumDeclaration().getEntries().stream(), EnumConstantDeclaration::getNameAsString, ann);
            } else {
                with(loadClass(field.getFullType()), cls -> {
                    if (cls.isEnum()) {
                        generateEnumSchema(Arrays.stream(cls.getEnumConstants()), Object::toString, ann);
                    } else if (CodeEnum.class.isAssignableFrom(cls)) {
                        generateEnumSchema(Arrays.stream(CodeFactory.enumValues((Class) cls)), CodeEnum::name, ann);
                    }
                });
            }
            if (field.isCollection()) {
                var arrayAnn = field.getDescription().getAnnotationByName("ArraySchema")
                        .map(NormalAnnotationExpr.class::cast)
                        .orElseGet(() -> new NormalAnnotationExpr(parseName("ArraySchema"), new NodeList<>()));
                getter.remove(ann);
                getter.addAnnotation(arrayAnn);
                if (notPairExists(arrayAnn, "schema")) {
                    arrayAnn.addPair("schema", ann);
                }
            }
        }
    }

    protected <T> void generateEnumSchema(Stream<T> enumEntries, Function<T, String> getEnumName, NormalAnnotationExpr schemaAnnotation) {
        var exp = new ArrayInitializerExpr();
        enumEntries.forEach(e -> exp.getValues().add(new StringLiteralExpr(getEnumName.apply(e))));
        if (notPairExists(schemaAnnotation, "allowableValues")) {
            schemaAnnotation.addPair("allowableValues", exp);
        }
        if (notPairExists(schemaAnnotation, "type")) {
            schemaAnnotation.addPair("type", new StringLiteralExpr("string"));
        }
    }

    protected void checkForDefaultValue(PrototypeField field, NormalAnnotationExpr ann) {
        if (notPairExists(ann, "defaultValue")) {
            field.getDescription().getAnnotationByClass(Default.class).ifPresent(a -> {
                if (a instanceof NormalAnnotationExpr expr) {
                    expr.getPairs().stream().filter(p -> p.getNameAsString().equals("value")).findFirst().ifPresent(p -> {
                        ann.addPair("defaultValue", strip(p.getValue()));
                    });
                } else if (a instanceof SingleMemberAnnotationExpr expr) {
                    ann.addPair("defaultValue", strip(expr.getMemberValue()));
                }
            });
        }
    }

    protected void checkForRange(PrototypeField field, NormalAnnotationExpr ann) {
        field.getDescription().getAnnotationByName("ValidateRange").ifPresent(a -> {
            if (a instanceof NormalAnnotationExpr expr) {
                for (var pair : expr.getPairs()) {
                    switch (pair.getNameAsString()) {
                        case "max" -> {
                            if (notPairExists(ann, "maximum")) {
                                ann.addPair("maximum", strip(pair.getValue()));
                            }
                        }
                        case "min" -> {
                            if (notPairExists(ann, "minimum")) {
                                ann.addPair("minimum", strip(pair.getValue()));
                            }
                        }
                    }
                }
            }
        });
    }

    protected void checkForLength(PrototypeField field, NormalAnnotationExpr ann) {
        field.getDescription().getAnnotationByName("ValidateLength").ifPresent(a -> {
            if (a instanceof NormalAnnotationExpr expr) {
                for (var pair : expr.getPairs()) {
                    switch (pair.getNameAsString()) {
                        case "value", "max" -> {
                            if (notPairExists(ann, "maxLength")) {
                                ann.addPair("maxLength", pair.getValue());
                            }
                        }
                        case "min" -> {
                            if (notPairExists(ann, "minLength")) {
                                ann.addPair("minLength", pair.getValue());
                            }
                        }
                    }
                }
            } else if (a instanceof SingleMemberAnnotationExpr expr) {
                if (notPairExists(ann, "maxLength")) {
                    ann.addPair("maxLength", expr.getMemberValue());
                }
            }
        });
    }

    protected boolean generate(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        return description.hasOption(Options.GENERATE_OPENAPI_ALWAYS) || (IS_OPENAPI_AVAILABLE && description.hasOption(Options.GENERATE_OPENAPI_IF_AVAILABLE));
    }

    protected StringLiteralExpr strip(Expression value) {
        return new StringLiteralExpr(StringUtils.strip(StringUtils.strip(value.toString(), "\""), "\\\""));
    }

    protected boolean notPairExists(NormalAnnotationExpr ann, String name) {
        return ann.getPairs().stream().noneMatch(p -> p.getNameAsString().equals(name));
    }

}
