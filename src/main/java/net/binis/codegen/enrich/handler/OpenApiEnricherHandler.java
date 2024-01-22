package net.binis.codegen.enrich.handler;

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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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

import static java.util.Objects.isNull;
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
            var ann = getter.addAndGetAnnotation("Schema");
            ann.addPair("name", "\"" + field.getName() + "\"");

            field.getDescription().getAnnotationByName("ValidateNull").ifPresent(a ->
                    ann.addPair("required", "true"));
            checkForDefaultValue(field, ann);
            checkForLength(field, ann);
            checkForRange(field, ann);


            if (nonNull(field.getPrototype()) && field.getPrototype().isCodeEnum()) {
                var exp = new ArrayInitializerExpr();
                field.getPrototype().getDeclaration().asEnumDeclaration().getEntries().forEach(e -> exp.getValues().add(new StringLiteralExpr(e.getNameAsString())));
                ann.addPair("allowableValues", exp);
                ann.addPair("type", new StringLiteralExpr("string"));
            } else {
                with(loadClass(field.getFullType()), cls -> {
                    if (cls.isEnum()) {
                        var exp = new ArrayInitializerExpr();
                        Arrays.stream(cls.getEnumConstants()).forEach(e -> exp.getValues().add(new StringLiteralExpr(e.toString())));
                        ann.addPair("allowableValues", exp);
                    } else if (CodeEnum.class.isAssignableFrom(cls)) {
                        var exp = new ArrayInitializerExpr();
                        Arrays.stream(CodeFactory.enumValues((Class) cls)).forEach(e -> exp.getValues().add(new StringLiteralExpr(((CodeEnum) e).name())));
                        ann.addPair("allowableValues", exp);
                    }
                });
            }
        }
    }

    protected void checkForDefaultValue(PrototypeField field, NormalAnnotationExpr ann) {
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

    protected void checkForRange(PrototypeField field, NormalAnnotationExpr ann) {
        field.getDescription().getAnnotationByName("ValidateRange").ifPresent(a -> {
            if (a instanceof NormalAnnotationExpr expr) {
                for (var pair : expr.getPairs()) {
                    switch (pair.getNameAsString()) {
                        case "max" -> ann.addPair("maximum", strip(pair.getValue()));
                        case "min" -> ann.addPair("minimum", strip(pair.getValue()));
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
                        case "value", "max" -> ann.addPair("maxLength", pair.getValue());
                        case "min" -> ann.addPair("minLength", pair.getValue());
                    }
                };
            } else if (a instanceof SingleMemberAnnotationExpr expr) {
                ann.addPair("maxLength", expr.getMemberValue());
            }
        });
    }

    protected boolean generate(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        return description.hasOption(Options.GENERATE_OPENAPI_ALWAYS) || (IS_OPENAPI_AVAILABLE && description.hasOption(Options.GENERATE_OPENAPI_IF_AVAILABLE));
    }

    protected StringLiteralExpr strip(Expression value) {
        return new StringLiteralExpr(StringUtils.strip(StringUtils.strip(value.toString(), "\""), "\\\""));
    }

}
