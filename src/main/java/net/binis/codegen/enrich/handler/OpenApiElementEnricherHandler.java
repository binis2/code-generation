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
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.compiler.CGAnnotation;
import net.binis.codegen.compiler.CGClassDeclaration;
import net.binis.codegen.compiler.CGMethodDeclaration;
import net.binis.codegen.compiler.CGValueExpression;
import net.binis.codegen.enrich.OpenApiEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.options.Options;
import net.binis.codegen.tools.Reflection;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.compiler.utils.ElementAnnotationUtils.*;
import static net.binis.codegen.compiler.utils.ElementUtils.getDeclaration;
import static net.binis.codegen.tools.Reflection.instantiate;
import static net.binis.codegen.tools.Reflection.loadClass;

public class OpenApiElementEnricherHandler extends BaseEnricher implements OpenApiEnricher {

    protected static final Class<? extends Annotation> OPEN_API_CLASS = loadClass("io.swagger.v3.oas.annotations.media.Schema");
    protected static final boolean IS_OPENAPI_AVAILABLE = nonNull(OPEN_API_CLASS);

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
        if (nonNull(description.getElement())) {
            var decl = getDeclaration(description.getElement());
            if (decl instanceof CGClassDeclaration cls) {
                cls.getMethods().forEach(this::enrichMethod);
            }
        }
//        if (generate(description)) {
//            var intf = description.getInterface();
//            intf.findCompilationUnit().ifPresent(unit -> {
//                unit.addImport("io.swagger.v3.oas.annotations.media", false, true);
//                description.getFields().forEach(this::enrichField);
//            });
//        }
    }

    @SuppressWarnings("unchecked")
    protected void enrichMethod(CGMethodDeclaration method) {
        CGAnnotation ann = null;
        if (method.getParameters().size() == 0) { //TODO: Check if is getter
            ann = removeAnnotation(method, OPEN_API_CLASS);
        }
        if (isNull(ann)) {
            ann = addAnnotation(method, OPEN_API_CLASS);
        }

        addIfMissingAnnotationAttribute(ann, "name", Helpers.getFieldName(method.getName().toString()));

        checkForRequired(method, ann);
        checkForDefaultValue(method, ann);
        checkForLength(method, ann);
        checkForRange(method, ann);

        var cls = method.getReturnType().toClass();

        if (nonNull(cls)) {
            if (cls.isEnum()) {
                addIfMissingAnnotationAttribute(ann, "type", "string");
                addIfMissingAnnotationAttribute(ann, "allowableValues", Arrays.stream(cls.getEnumConstants()).map(Object::toString).toArray(String[]::new));
            } else if (CodeEnum.class.isAssignableFrom(cls)) {
                addIfMissingAnnotationAttribute(ann, "type", "string");
                addIfMissingAnnotationAttribute(ann, "allowableValues", Arrays.stream(CodeFactory.enumValues(cls)).map(e -> ((CodeEnum) e).name()).toArray(String[]::new));
            }
        } else {
            var proto = lookup.findGenerated(method.getReturnType().toSymbolString());
            if (nonNull(proto) && proto.isCodeEnum()) {
                addIfMissingAnnotationAttribute(ann, "type", "string");
                addIfMissingAnnotationAttribute(ann, "allowableValues",proto.getDeclaration().asEnumDeclaration().getEntries().stream().map(NodeWithSimpleName::getNameAsString).toArray(String[]::new));
            }
        }
    }

    protected void checkForRequired(CGMethodDeclaration method, CGAnnotation ann) {
        Class<? extends Annotation> cls = loadClass("net.binis.codegen.validation.annotation.ValidateNull");
        if (nonNull(cls) && nonNull(getAnnotation(method, cls))) {
            addIfMissingAnnotationAttribute(ann, "required", true);
        }
    }

    protected void checkForDefaultValue(CGMethodDeclaration method, CGAnnotation ann) {
        var def = getAnnotation(method, Default.class);
        if (nonNull(def)) {
            strip(def.getArgument("value")).ifPresent(value -> {
                addIfMissingAnnotationAttribute(ann, "defaultValue", value);
            });
        }
    }

    protected void checkForRange(CGMethodDeclaration method, CGAnnotation ann) {
        Class<? extends Annotation> cls = loadClass("net.binis.codegen.validation.annotation.ValidateRange");
        if (nonNull(cls)) {
            var a = getAnnotation(method, cls);
            if (nonNull(a)) {
                for (var pair : a.getArguments()) {
                    switch (pair.getKeyAsString()) {
                        case "max" -> addIfMissingAnnotationAttribute(ann, "maximum", strip(pair.getValueAsString()));
                        case "min" -> addIfMissingAnnotationAttribute(ann, "minimum", strip(pair.getValueAsString()));
                    }
                }
            }
        }
    }

    protected void checkForLength(CGMethodDeclaration method, CGAnnotation ann) {
        Class<? extends Annotation> cls = loadClass("net.binis.codegen.validation.annotation.ValidateLength");
        if (nonNull(cls)) {
            var a = getAnnotation(method, cls);
            if (nonNull(a)) {
                for (var pair : a.getArguments()) {
                    switch (pair.getKeyAsString()) {
                        case "value", "max" -> addIfMissingAnnotationAttribute(ann, "maxLength", stripInt(pair.getValue()));
                        case "min" -> addIfMissingAnnotationAttribute(ann, "minLength", stripInt(pair.getValue()));
                    }
                }
            }
        }
    }

    protected boolean generate(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        return description.hasOption(Options.GENERATE_OPENAPI_ALWAYS) || (IS_OPENAPI_AVAILABLE && description.hasOption(Options.GENERATE_OPENAPI_IF_AVAILABLE));
    }

    protected Optional<String> strip(CGValueExpression arg) {
        if (nonNull(arg)) {
            var expr = arg.getValue();
            if (nonNull(expr)) {
                return Optional.of(strip(arg.getValue().toString()));
            }
        }
        return Optional.empty();
    }

    protected int stripInt(Object value) {
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    protected String strip(String str) {
        return StringUtils.strip(str, "\\\"");
    }

}
