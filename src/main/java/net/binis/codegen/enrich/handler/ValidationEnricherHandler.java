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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.StringEscapeUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.validation.*;
import net.binis.codegen.enrich.Enrichers;
import net.binis.codegen.enrich.ValidationEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.generation.core.types.ModifierType;
import net.binis.codegen.options.Options;
import net.binis.codegen.tools.CollectionUtils;
import net.binis.codegen.tools.ContextInterpolator;
import net.binis.codegen.tools.Holder;
import net.binis.codegen.tools.Tools;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ElementKind;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.CollectionsHandler.isMap;
import static net.binis.codegen.generation.core.EnrichHelpers.*;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Reflection.invoke;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.*;

@Slf4j
public class ValidationEnricherHandler extends BaseEnricher implements ValidationEnricher {

    protected static final String VALUE = "value";
    protected static final String PARAMS = "params";
    protected static final String MESSAGE = "message";
    protected static final String MESSAGES = "messages";
    protected static final String AS_CODE = "asCode";
    protected static final String TARGETS = "targets";

    protected static final Class<?> TARGETS_AWARE = loadClass("net.binis.codegen.validation.consts.ValidationTargets$TargetsAware");

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var form = new StringBuilder();
        description.getFields().forEach(f -> handleField(description, f, form, false));

        if (nonNull(description.getMixIn())) {
            description.getMixIn().getFields().forEach(f -> handleField(description, f, form, true));
        }

        if (description.hasOption(Options.VALIDATION_FORM)) {
            buildValidationForm(description, form);
        }
    }

    @Override
    public int order() {
        return 0;
    }

    protected void handleField(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, StringBuilder code, boolean mixIn) {
        var form = description.hasOption(Options.VALIDATION_FORM) && !field.getIgnores().isForValidation() ? formMethod(field) : null;
        field.getDescription().getAnnotations().stream().filter(this::isValidationAnnotation).forEach(a -> {
            if (a.getNameAsString().contains("AllBut")) {
                description.getFields().stream().filter(f -> !f.equals(field)).forEach(f ->
                        processAnnotation(description, f, a, null, form, false, mixIn));
            } else {
                processAnnotation(description, field, a, null, form, false, mixIn);
            }
        });

        if (field.isCollection()) {
            field.getDescription().getType().asClassOrInterfaceType().getTypeArguments().ifPresent(args ->
                    args.forEach(type -> type.getAnnotations().stream().filter(this::isValidationAnnotation).forEach(a -> processAnnotation(description, field, a, type, form, true, mixIn))));
        }

        if (nonNull(form)) {
            var isChild = hasChildren(field);
            var exp = form.getBody().get().getStatement(0).toString();
            if (exp.length() > field.getName().length() + 5) {
                code.append("e -> ").append(exp.replace(".start(", ".start(e, "));
                code.setLength(code.length() - 1);
                if (isChild) {
                    code.insert(code.lastIndexOf(".perform("), ".child()");
                }
                code.append(",\n");
            } else if (isChild) {
                code.append("e -> Validation.start(e, this.getClass(), \"").append(field.getName()).append("\", ").append(field.getName()).append(").child(),\n");
            }
        }
    }

    protected boolean hasChildren(PrototypeField field) {
        var result = hasForm(field.getPrototype());
        if (!result && CollectionUtils.isNotEmpty(field.getTypePrototypes())) {
            result = field.getTypePrototypes().values().stream().anyMatch(this::hasForm);
        }
        return result;
    }

    protected boolean hasForm(PrototypeDescription<?> desc) {
        return nonNull(desc) && desc.hasEnricher(Enrichers.VALIDATION) && desc.hasOption(Options.VALIDATION_FORM);
    }

    protected void processAnnotation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, Type type, MethodDeclaration form, boolean collection, boolean mixIn) {
        var name = Helpers.getExternalClassNameIfExists(unit(annotation), annotation.getNameAsString());

        var cls = loadClass(name);
        if (nonNull(cls)) {
            if (Validate.class.equals(cls) || cls.isAnnotationPresent(Validate.class)) {
                generateValidation(description, field, type, annotation, cls, form, collection, mixIn);
            } else if (Sanitize.class.equals(cls) || cls.isAnnotationPresent(Sanitize.class)) {
                generateSanitization(description, field, type, annotation, cls, form, collection, mixIn);
            } else if (Execute.class.equals(cls) || cls.isAnnotationPresent(Execute.class)) {
                generateExecution(description, field, type, annotation, cls, collection, mixIn);
            }
        } else {
            Tools.with(lookup.findExternal(name), d ->
                    handleAnnotationFromSource(description, d.getDeclaration().asAnnotationDeclaration(), field, type, annotation, form, collection, mixIn));
        }

        var mod = description.getRegisteredClass("EmbeddedModifier");
        if (isNull(mod)) {
            mod = description.getRegisteredClass("Modifier");
        }
        if (nonNull(mod)) {
            Helpers.addSuppressWarningsUnchecked(mod);
        }
        if (nonNull(field.getImplementationSetter())) {
            Helpers.addSuppressWarningsUnchecked(description.getImplementation());
        }
    }

    protected void handleAnnotationFromSource(PrototypeDescription<ClassOrInterfaceDeclaration> description, AnnotationDeclaration decl, PrototypeField field, Type type, AnnotationExpr annotation, MethodDeclaration form, boolean collection, boolean mixIn) {
        var ann = decl.getAnnotationByClass(Validate.class);
        if (ann.isPresent()) {
            generateValidation(description, field, type, annotation, ann.get(), decl, form, collection, mixIn);
        } else {
            ann = decl.getAnnotationByClass(Sanitize.class);
            if (ann.isPresent()) {
                generateSanitization(description, field, type, annotation, ann.get(), decl, form, collection, mixIn);
            } else {
                decl.getAnnotationByClass(Execute.class).ifPresent(annotationExpr ->
                        generateExecution(description, field, type, annotation, annotationExpr, decl, collection, mixIn));
            }
        }
    }

    protected void generateSanitization(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass, MethodDeclaration form, boolean collection, boolean mixIn) {
        var params = getSanitizationParams(field, type, annotation, ann, annotationClass);
        field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(getExternalClassName(annotationClass.findCompilationUnit().get(), params.getCls())));
        generateSanitization(description, field, type, params, form, collection, mixIn);
    }

    protected void generateSanitization(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, AnnotationExpr annotation, Class<?> annotationClass, MethodDeclaration form, boolean collection, boolean mixIn) {
        generateSanitization(description, field, type, getSanitizationParams(field, type, annotation, annotationClass), form, collection, mixIn);
    }

    protected void generateSanitization(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, Params params, MethodDeclaration form, boolean collection, boolean mixIn) {
        if (!mixIn && nonNull(field.getImplementationSetter())) {
            addSanitization(field, field.getImplementationSetter(), params, ModifierType.MAIN, collection);
        }

        field.getModifiers().stream()
                .filter(m -> !mixIn || m.getOrigin().equals(description))
                .filter(modifier -> !ModifierType.COLLECTION.equals(modifier.getType()) || collection)
                .forEach(modifier ->
                        addSanitization(field, modifier.getModifier(), params, modifier.getType(), collection && !modifier.getType().equals(ModifierType.COLLECTION)));

        if (!mixIn && description.hasOption(Options.VALIDATION_FORM)) {
            addSanitization(field, form, params, ModifierType.FORM, collection);
        }
    }

    protected Params getSanitizationParams(PrototypeField field, Type type, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass) {
        var params = Params.builder();

        handleSanitizationAnnotation(ann, params);
        //TODO: Handle aliases

        return checkTargets(params.build(), field, type);
    }

    protected Params getSanitizationParams(PrototypeField field, Type type, AnnotationExpr annotation, Class<?> annotationClass) {
        String cls = null;
        var params = Params.builder();

        if (!Sanitize.class.equals(annotationClass)) {
            var ann = annotationClass.getDeclaredAnnotation(Sanitize.class);
            params.cls(ann.value().getSimpleName()).params(Arrays.asList(ann.params())).targets(processTargetsClass(ann.targets()));
            cls = ann.value().getCanonicalName();
            field.getDeclaration().findCompilationUnit().get().addImport(cls);

            handleAliases(field, annotation, annotationClass, params);
        } else {
            handleSanitizationAnnotation(annotation, params);
        }

        var result = checkTargets(params.build(), field, type);

        if (isNull(result.getAsCode())) {
            Tools.with(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    Tools.with(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    protected void handleSanitizationAnnotation(AnnotationExpr annotation, Params.ParamsBuilder params) {
        for (var node : annotation.getChildNodes()) {
            if (node instanceof ClassExpr exp) {
                params.cls(exp.getTypeAsString());
            } else if (node instanceof MemberValuePair pair) {
                switch (pair.getNameAsString()) {
                    case VALUE -> {
                        var cls = pair.getValue().asClassExpr().getTypeAsString();
                        params.cls(cls).full(getExternalClassName(pair, cls));
                    }
                    case TARGETS -> params.targets(processTargets(pair.getValue()));
                    case PARAMS ->
                            params.params(pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).map(StringLiteralExpr::asString).collect(Collectors.toList()));
                    case AS_CODE -> params.asCode(pair.getValue().asStringLiteralExpr().asString());
                    default -> {
                        //Do nothing
                    }
                }
            }
        }
    }

    protected Params getValidationParams(PrototypeField field, Type type, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass) {
        var params = Params.builder();

        handleValidationAnnotation(ann, params);
        //TODO: Handle aliases

        return checkTargets(params.build(), field, type);
    }

    protected Params getValidationParams(PrototypeField field, Type type, AnnotationExpr annotation, Class<?> annotationClass) {
        var params = Params.builder();
        String cls = null;

        if (!Validate.class.equals(annotationClass)) {
            var ann = annotationClass.getDeclaredAnnotation(Validate.class);
            cls = ann.value().getCanonicalName();
            params.cls(ann.value().getSimpleName()).full(cls).params(Arrays.asList(ann.params())).message(ann.message()).targets(processTargetsClass(ann.targets()));
            handleAliases(field, annotation, annotationClass, params);
        } else {
            handleValidationAnnotation(annotation, params);
        }

        var result = checkTargets(params.build(), field, type);

        if (isNull(result.getAsCode())) {
            Tools.with(loadClass(isNull(cls) ? getExternalClassName(unit(field.getParsed().getDeclaration()), result.getCls()) : cls), c ->
                    Tools.with(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    protected void handleValidationAnnotation(AnnotationExpr annotation, ValidationEnricherHandler.Params.ParamsBuilder params) {
        for (var node : annotation.getChildNodes()) {
            if (node instanceof ClassExpr exp) {
                params.cls(exp.getTypeAsString()).full(getExternalClassName(exp, exp.getTypeAsString()));
            } else if (node instanceof MemberValuePair pair) {
                switch (pair.getNameAsString()) {
                    case VALUE -> {
                        var cls = pair.getValue().asClassExpr().getTypeAsString();
                        params.cls(cls).full(getExternalClassName(pair, cls));
                    }
                    case MESSAGE -> params.message(pair.getValue().asStringLiteralExpr().asString());
                    case MESSAGES ->
                            params.messages(pair.getValue().asArrayInitializerExpr().getValues().stream().map(e -> e.asStringLiteralExpr().asString()).collect(Collectors.toList()));
                    case PARAMS -> {
                        if (pair.getValue().isArrayInitializerExpr()) {
                            params.params(pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).map(StringLiteralExpr::asString).collect(Collectors.toList()));
                        } else {
                            params.params(List.of(pair.getValue())).annotation(unit(annotation));
                        }
                    }
                    case AS_CODE -> params.asCode(pair.getValue().asStringLiteralExpr().asString());
                    case TARGETS -> params.targets(processTargets(pair.getValue()));
                    default -> {
                        //Do nothing
                    }
                }
            }
        }
    }

    protected void handleAliases(PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass, Params.ParamsBuilder params) {
        var list = new ArrayList<>();
        var parOrder = Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> Arrays.stream(m.getDeclaredAnnotations())
                        .filter(a -> a.annotationType().isAssignableFrom(AliasFor.class))
                        .map(AliasFor.class::cast)
                        .anyMatch(a -> PARAMS.equals(a.value())))
                .map(m -> ParamHolder.builder()
                        .name(m.getName())
                        .value(m.getDefaultValue())
                        .annotation(m.getDeclaredAnnotation(AsCode.class))
                        .order(m.getDeclaredAnnotation(AliasFor.class).order())
                        .alt(m.getDeclaredAnnotation(AliasFor.class).alternative())
                        .build())
                .sorted(Comparator.comparing(this::paramHolderOrder))
                .toList();

        Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> MESSAGE.equals(m.getName()))
                .filter(m -> m.getReturnType().equals(String.class))
                .filter(m -> isNull(m.getDeclaredAnnotation(AliasFor.class)))
                .findFirst().ifPresent(m ->
                        params.message((String) m.getDefaultValue()));

        var messages = Holder.<List<Object>>blank();
        var messageAliases = new ArrayList<String>();
        Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> MESSAGES.equals(m.getName()))
                .filter(m -> m.getReturnType().equals(String[].class))
                .filter(m -> isNull(m.getDeclaredAnnotation(AliasFor.class)))
                .findFirst().ifPresent(m ->
                        messages.set(List.of((String[]) m.getDefaultValue())));

        if (messages.isEmpty()) {
            Arrays.stream(annotationClass.getDeclaredMethods())
                    .filter(m -> m.getReturnType().equals(String.class))
                    .filter(m -> nullCheck(m.getDeclaredAnnotation(AliasFor.class), a -> MESSAGES.equals(a.value()), false))
                    .forEach(m -> {
                        var order = m.getDeclaredAnnotation(AliasFor.class).order();
                        if (messages.isEmpty()) {
                            messages.set(new ArrayList<>());
                        }
                        var value = nonNull(m.getDefaultValue()) ? m.getDefaultValue().toString() : "(%s) Invalid value!";

                        var msgs = messages.get();
                        for (var i = msgs.size(); i <= order; i++) {
                            msgs.add(null);
                        }
                        for (var i = messageAliases.size(); i <= order; i++) {
                            messageAliases.add(null);
                        }

                        msgs.set(order, value);
                        messageAliases.set(order, m.getName());
                    });
        }

        Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> TARGETS.equals(m.getName()))
                .filter(m -> m.getReturnType().equals(Class[].class))
                .filter(m -> isNull(m.getDeclaredAnnotation(AliasFor.class)))
                .findFirst().ifPresent(m ->
                        params.targets(processTargetsClass((Class[]) m.getDefaultValue())));


        parOrder.stream().filter(p -> !p.alt).forEach(p ->
                list.add(checkAsCode(p.getValue(), p.getAnnotation())));
        if (!annotation.isMarkerAnnotationExpr()) {
            if (annotation.isSingleMemberAnnotationExpr() && annotation.getChildNodes().size() == 1) {
                handleExpression(annotation, annotationClass, params, list, parOrder, getParamValue(annotation.asSingleMemberAnnotationExpr().getMemberValue()));
            } else {
                for (var node : annotation.getChildNodes()) {
                    if (node instanceof Name) {
                        //skip
                    } else if (node instanceof MemberValuePair pair) {
                        switch (Arrays.stream(annotationClass.getDeclaredMethods())
                                .filter(m -> m.getName().equals(pair.getNameAsString()))
                                .map(m -> m.getDeclaredAnnotation(AliasFor.class))
                                .filter(Objects::nonNull)
                                .map(AliasFor::value)
                                .findFirst()
                                .orElseGet(pair::getNameAsString)) {
                            case VALUE -> params.cls(pair.getValue().asClassExpr().getTypeAsString());
                            case MESSAGE -> params.message(pair.getValue().asStringLiteralExpr().asString());
                            case MESSAGES -> {
                                if (pair.getValue().isArrayInitializerExpr()) {
                                    params.messages(pair.getValue().asArrayInitializerExpr().getValues().stream().map(e -> e.asStringLiteralExpr().asString()).collect(Collectors.toList()));
                                } else if (pair.getValue().isStringLiteralExpr()) {
                                    var msg = pair.getValue().asStringLiteralExpr().asString();
                                    var idx = messageAliases.indexOf(pair.getNameAsString());
                                    if (messages.isEmpty()) {
                                        messages.set(new ArrayList<>());
                                    }
                                    if (idx < messages.get().size()) {
                                        messages.get().set(idx, msg);
                                    } else {
                                        messages.get().add(msg);
                                    }
                                } else if (pair.getValue().isBinaryExpr()) {
                                    var idx = messageAliases.indexOf(pair.getNameAsString());
                                    if (messages.isEmpty()) {
                                        messages.set(new ArrayList<>());
                                    }
                                    if (idx < messages.get().size()) {
                                        messages.get().set(idx, pair.getValue());
                                    } else {
                                        messages.get().add(pair.getValue());
                                    }
                                } else {
                                    log.warn("Unhandled expression ({}) in {}", pair.getValue(), field.getParsed().getPrototypeClassName());
                                }
                            }
                            case AS_CODE -> params.asCode(pair.getValue().asStringLiteralExpr().asString());
                            case TARGETS -> params.targets(processTargets(pair.getValue()));
                            case PARAMS -> {
                                if (pair.getValue().isArrayInitializerExpr()) {
                                    list.addAll(pair.getValue().asArrayInitializerExpr().getValues().stream()
                                            .map(Expression::asStringLiteralExpr)
                                            .map(StringLiteralExpr::asString)
                                            .toList());
                                } else {
                                    var idx = getParamIndex(parOrder, pair.getNameAsString());
                                    if (idx != -1) {
                                        var triple = parOrder.get(idx);
                                        if (triple.isAlt()) {
                                            list.set(triple.getOrder(), checkAsCode(getParamValue(pair.getValue()), triple.getAnnotation()));
                                        } else {
                                            list.set(idx, checkAsCode(getParamValue(pair.getValue()), triple.getAnnotation()));
                                        }
                                    } else {
                                        throw new GenericCodeGenException("Invalid annotation params! " + annotation);
                                    }
                                }
                            }
                            default -> {
                                //Do nothing
                            }
                        }
                    } else if (node instanceof LiteralExpr exp) {
                        handleExpression(annotation, annotationClass, params, list, parOrder, getParamValue(exp));
                    } else if (node instanceof NameExpr exp) {
                        handleExpression(annotation, annotationClass, params, list, parOrder, node);
                        annotation.findCompilationUnit().flatMap(unit -> Helpers.getStaticImportIfExists(unit, exp.getNameAsString()))
                                .ifPresent(i -> field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(i, true, false)));
                    } else if (node instanceof FieldAccessExpr exp) {
                        annotation.findCompilationUnit().flatMap(unit -> Optional.ofNullable(getExternalClassNameIfExists(unit, exp.getScope().toString()))).ifPresent(cls ->
                                with(Helpers.lookup.findParsed(cls), p -> {
                                    var c = p.getConstants().get(exp.getNameAsString());
                                    if (nonNull(c)) {
                                        field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(c.getDestination().getFullyQualifiedName().get()));
                                        var e = expression(c.getDestination().getNameAsString() + "." + c.getName());
                                        handleExpression(annotation, annotationClass, params, list, parOrder, e);
                                    } else {
                                        log.warn("Unknown constant {} on class {}", exp.getNameAsString(), cls);
                                    }
                                }, () -> {
                                    field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(cls));
                                    handleExpression(annotation, annotationClass, params, list, parOrder, exp);
                                }));
                    } else if (node instanceof BinaryExpr) {
                        handleExpression(annotation, annotationClass, params, list, parOrder, node);
                    } else {
                        log.warn("Unhandled expression ({}) in {}", node.toString(), field.getParsed().getPrototypeClassName());
                    }
                }
            }
        }
        if (!list.isEmpty()) {
            params.params(list);
        }

        params.messages = convert(messages.get(), parOrder);
        params.message = (String) convert(params.message, parOrder);
    }

    private List<Object> convert(List<Object> messages, List<ParamHolder> params) {
        if (nonNull(messages)) {
            messages.replaceAll(message -> convert(message, params));
        }
        return messages;
    }

    private Object convert(Object message, List<ParamHolder> params) {
        if (message instanceof String s) {
            return ContextInterpolator.of(param -> params.stream()
                            .filter(p -> !VALUE.equals(p.getName()))
                            .filter(p -> p.getName().equals(param))
                            .findFirst()
                            .map(paramHolder -> "param[" + paramHolder.getOrder() + "]")
                            .orElse(param))
                    .interpolate(s);
        }
        return message;
    }


    protected int paramHolderOrder(ParamHolder obj) {
        return obj.getOrder() * 10 + (obj.alt ? 1 : 0);
    }

    protected void handleExpression(AnnotationExpr annotation, Class<?> annotationClass, Params.ParamsBuilder params, ArrayList<Object> list, List<ParamHolder> parOrder, Object exp) {
        switch (Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> m.getName().equals(VALUE))
                .map(m -> m.getDeclaredAnnotation(AliasFor.class))
                .filter(Objects::nonNull)
                .map(AliasFor::value)
                .findFirst()
                .orElse(VALUE)) {
            case VALUE -> params.cls(exp.toString());
            case MESSAGE -> params.message(exp.toString());
            case AS_CODE -> params.asCode(exp.toString());
            case PARAMS -> {
                var idx = getParamIndex(parOrder, VALUE);
                if (idx != -1) {
                    var triple = parOrder.get(idx);
                    if (nonNull(triple.getAnnotation())) {
                        if (triple.isAlt()) {
                            list.set(triple.getOrder(), checkAsCode(exp, triple.getAnnotation()));
                        } else {
                            list.set(idx, checkAsCode(exp, triple.getAnnotation()));
                        }
                    } else {
                        if (triple.isAlt()) {
                            list.set(triple.getOrder(), exp);
                        } else {
                            list.set(idx, exp);
                        }
                    }
                } else {
                    throw new GenericCodeGenException("Invalid annotation params! " + annotation);
                }
            }
            default -> {
                //Do nothing
            }
        }
    }

    protected Object checkAsCode(Object value, AsCode code) {
        if (nonNull(code)) {
            return AsCodeHolder.builder().value((String) value).format(code.value()).build();
        }
        return value;
    }

    protected Object getParamValue(Expression value) {
        if (value.isStringLiteralExpr()) {
            return value.asStringLiteralExpr().asString();
        } else if (value.isIntegerLiteralExpr()) {
            return value.asIntegerLiteralExpr().asNumber();
        } else if (value.isDoubleLiteralExpr()) {
            return value.asDoubleLiteralExpr().asDouble();
        } else if (value.isBooleanLiteralExpr()) {
            return value.asBooleanLiteralExpr().getValue();
        } else {
            return value;
        }
    }

    protected int getParamIndex(List<ParamHolder> list, String name) {
        for (var i = 0; i < list.size(); i++) {
            if (name.equals(list.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    protected void generateValidation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass, MethodDeclaration form, boolean collection, boolean mixIn) {
        var params = getValidationParams(field, type, annotation, ann, annotationClass);
        field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(getExternalClassName(annotationClass.findCompilationUnit().get(), params.getCls())));
        generateValidation(description, field, type, params, form, collection, mixIn);
    }

    protected void generateValidation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, AnnotationExpr annotation, Class<?> annotationClass, MethodDeclaration form, boolean collection, boolean mixIn) {
        generateValidation(description, field, type, getValidationParams(field, type, annotation, annotationClass), form, collection, mixIn);
    }

    protected void generateValidation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, Params params, MethodDeclaration form, boolean collection, boolean mixIn) {
        if (!mixIn && nonNull(field.getImplementationSetter())) {
            addValidation(field, field.getImplementationSetter(), params, ModifierType.MAIN, collection);
        }

        field.getModifiers().stream()
                .filter(m -> collection || !ModifierType.COLLECTION.equals(m.getType()))
                .filter(m -> !mixIn || m.getOrigin().equals(description))
                .forEach(modifier ->
                        addValidation(field, modifier.getModifier(), params, modifier.getType(), collection && !modifier.getType().equals(ModifierType.COLLECTION)));

        if (!mixIn && description.hasOption(Options.VALIDATION_FORM) && !field.getIgnores().isForValidation()) {
            addValidation(field, form, params, ModifierType.FORM, collection);
        }
    }


    protected void addValidation(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier, boolean collection) {
        handleImport(field, params);
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        var start = findStart(block);

        if (isNull(start)) {
            var exp = new StringBuilder("Validation.start(this.getClass(), \"")
                    .append(field.getName())
                    .append((ModifierType.COLLECTION.equals(modifier) ? "[value]" : ""))
                    .append("\", ")
                    .append(ModifierType.COLLECTION.equals(modifier) ? VALUE : field.getName())
                    .append(").validate")
                    .append(nonNull(params.getMessages()) ? "WithMessages" : "")
                    .append(collection ? "Collection(" : "(")
                    .append(params.getCls())
                    .append(".class, ")
                    .append(calcMessage(params))
                    .append(buildParamsStr(params, field, modifier, collection))
                    .append(")");
            handleStartingExpression(field, modifier, block, exp);
        } else {
            handleChainExpression(field, params, modifier, collection, start.asExpressionStmt(), "validate");
        }

        if (nonNull(params.annotation)) {
            handleImports(params.annotation, field.getParsed().getImplementation());
        }
    }

    protected void handleImport(PrototypeField field, Params params) {
        PrototypeDescription<?> parsed = field.getParsed();
        var u = parsed.getImplementationUnit();
        if (nonNull(parsed.getMixIn())) {
            u = parsed.getMixIn().getImplementationUnit();
        } else if (parsed.isNested()) {
            while (nonNull(parsed.getParentClassName())) {
                var parent = lookup.findParsed(parsed.getParentClassName());
                if (nonNull(parent)) {
                    u = parent.getImplementationUnit();
                    parsed = parent;
                } else {
                    break;
                }
            }
        }
        u.addImport("net.binis.codegen.validation.flow.Validation").addImport(params.getFull());
    }

    protected void addSanitization(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier, boolean collection) {
        handleImport(field, params);
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();
        var start = findStart(block);

        if (isNull(start)) {
            var exp = new StringBuilder("Validation.start(this.getClass(), \"")
                    .append(field.getName())
                    .append((ModifierType.COLLECTION.equals(modifier) ? "[value]" : ""))
                    .append("\", ")
                    .append(ModifierType.COLLECTION.equals(modifier) ? VALUE : field.getName())
                    .append(").sanitize")
                    .append(collection ? "Collection(" : "(")
                    .append(params.getCls())
                    .append(".class")
                    .append(buildParamsStr(params, field, modifier, collection))
                    .append(")");
            handleStartingExpression(field, modifier, block, exp);
        } else {
            handleChainExpression(field, params, modifier, collection, start.asExpressionStmt(), "sanitize");
        }

        if (nonNull(params.annotation)) {
            handleImports(params.annotation, field.getParsed().getImplementation());
        }
    }

    protected void generateExecution(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass, boolean collection, boolean mixIn) {
        var params = getExecutionParams(field, type, annotation, ann, annotationClass);
        field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(getExternalClassName(annotationClass.findCompilationUnit().get(), params.getCls())));
        generateExecution(description, field, type, params, collection, mixIn);
    }

    protected void generateExecution(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, AnnotationExpr annotation, Class<?> annotationClass, boolean collection, boolean mixIn) {
        generateExecution(description, field, type, getExecutionParams(field, type, annotation, annotationClass), collection, mixIn);
    }

    protected void generateExecution(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Type type, Params params, boolean collection, boolean mixIn) {
        if (!mixIn && nonNull(field.getImplementationSetter())) {
            addExecution(field, field.getImplementationSetter(), params, ModifierType.MAIN, collection);
        }

        field.getModifiers().stream()
                .filter(m -> !mixIn || m.getOrigin().equals(description))
                .filter(modifier -> !ModifierType.COLLECTION.equals(modifier.getType()) || collection)
                .forEach(modifier -> addExecution(field, modifier.getModifier(), params, modifier.getType(), collection && !modifier.getType().equals(ModifierType.COLLECTION)));
    }

    protected void addExecution(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier, boolean collection) {
        handleImport(field, params);
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        var start = findStart(block);

        if (isNull(start)) {
            var exp = new StringBuilder("Validation.start(this.getClass(), \"")
                    .append(field.getName())
                    .append((ModifierType.COLLECTION.equals(modifier) ? "[value]" : ""))
                    .append("\", ")
                    .append(ModifierType.COLLECTION.equals(modifier) ? VALUE : field.getName())
                    .append(").execute")
                    .append(collection ? "Collection(" : "(")
                    .append(params.getCls())
                    .append(".class, ")
                    .append(calcMessage(params))
                    .append(buildParamsStr(params, field, modifier, collection))
                    .append(")");
            handleStartingExpression(field, modifier, block, exp);
        } else {
            handleChainExpression(field, params, modifier, collection, start.asExpressionStmt(), "execute");
        }

        if (nonNull(params.annotation)) {
            handleImports(params.annotation, field.getParsed().getImplementation());
        }
    }

    protected Params getExecutionParams(PrototypeField field, Type type, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass) {
        var params = Params.builder();

        handleExecutionAnnotation(ann, params);
        //TODO: Handle aliases

        return checkTargets(params.build(), field, type);
    }

    protected Params getExecutionParams(PrototypeField field, Type type, AnnotationExpr annotation, Class<?> annotationClass) {
        var params = Params.builder();
        String cls = null;

        if (!Execute.class.equals(annotationClass)) {
            var ann = annotationClass.getDeclaredAnnotation(Execute.class);
            params.cls(ann.value().getSimpleName()).params(Arrays.asList(ann.params())).targets(processTargetsClass(ann.targets()));
            cls = ann.value().getCanonicalName();
            unit(field.getDeclaration()).addImport(cls);

            handleAliases(field, annotation, annotationClass, params);
        } else {
            handleExecutionAnnotation(annotation, params);
        }

        var result = checkTargets(params.build(), field, type);

        if (isNull(result.getAsCode())) {
            Tools.with(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    Tools.with(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    protected void handleExecutionAnnotation(AnnotationExpr annotation, Params.ParamsBuilder params) {
        for (var node : annotation.getChildNodes()) {
            if (node instanceof ClassExpr exp) {
                params.cls(exp.getTypeAsString());
            } else if (node instanceof MemberValuePair pair) {
                switch (pair.getNameAsString()) {
                    case VALUE -> {
                        var cls = pair.getValue().asClassExpr().getTypeAsString();
                        params.cls(cls).full(getExternalClassName(pair, cls));
                    }
                    case MESSAGE -> params.message(pair.getValue().asStringLiteralExpr().asString());
                    case TARGETS -> params.targets(processTargets(pair.getValue()));
                    case PARAMS ->
                            params.params(pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).map(StringLiteralExpr::asString).collect(Collectors.toList()));
                    case AS_CODE -> params.asCode(pair.getValue().asStringLiteralExpr().asString());
                    default -> {
                        //Do nothing
                    }
                }
            }
        }
    }

    protected void handleChainExpression(PrototypeField field, Params params, ModifierType modifier, boolean collection, ExpressionStmt start, String method) {
        var mCall = start.getExpression().asMethodCallExpr();
        Expression chain;
        if (!ModifierType.COLLECTION.equals(modifier)) {
            chain = mCall.getScope().get();
            mCall.removeScope();
        } else {
            chain = null;
        }
        var m = new MethodCallExpr(chain, method + (nonNull(params.getMessages()) && "validate".equals(method) ? "WithMessages" : "") + (collection ? "Collection" : "")).addArgument(params.getCls() + ".class");
        if (!"sanitize".equals(method)) {
            m.addArgument(calcMessage(params));
        }
        Tools.with(params.getParams(), p -> p.forEach(param ->
                m.addArgument(buildParamsStr(param, params, field, modifier, collection))));
        if (!ModifierType.COLLECTION.equals(modifier)) {
            mCall.setScope(m);
        } else {
            m.setScope(mCall);
            start.setExpression(m);
        }
    }

    protected static void handleStartingExpression(PrototypeField field, ModifierType modifier, BlockStmt block, StringBuilder exp) {
        if (ModifierType.COLLECTION.equals(modifier)) {
            var ret = block.findFirst(ReturnStmt.class).get();
            var s = ret.findFirst(NameExpr.class).get().toString();
            exp.insert(0, ", value -> ");
            if (isMap(field.getFullType())) {
                exp.insert(0, ", null");
            }
            exp.insert(0, s.substring(0, s.length() - 1))
            .append(");");
            var expr = statement(exp.toString());
            ret.setExpression(((ExpressionStmt) expr).getExpression());
        } else {
            exp.append(".perform(v -> this.map = v);");
            var expr = statement(exp.toString());
            var original = block.getStatements().remove(0);
            ((ExpressionStmt) original).getExpression().asAssignExpr().setValue(new NameExpr("v"));
            var mCall = expr.asExpressionStmt().getExpression().asMethodCallExpr();
            ((LambdaExpr) mCall.getChildNodes().get(mCall.getChildNodes().size() - 1)).setBody(original);
            block.getStatements().add(0, expr);
        }
    }

    protected Statement findStart(Node node) {
        var lambda = node.findFirst(LambdaExpr.class);
        if (lambda.isPresent() && lambda.get().getExpressionBody().isPresent() && nonNull(findStartMethod(lambda.get().getExpressionBody().get()))) {
            return lambda.get().getBody();
        }

        return node.findAll(ExpressionStmt.class).stream().filter(s -> nonNull(findStartMethod(s))).findFirst().orElse(null);
    }

    protected MethodCallExpr findStartMethod(Node node) {
        MethodCallExpr result = null;
        var list = node.findAll(MethodCallExpr.class);

        for (var m : list) {
            var scope = m.getScope();
            if (scope.isPresent()) {
                if (m.getNameAsString().equals("start") &&
                        scope.get().isNameExpr() &&
                        scope.get().asNameExpr().getNameAsString().equals("Validation")) {
                    return m;
                } else {
                    result = findStartMethod(scope.get());
                    if (nonNull(result)) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    protected String calcMessage(Params params) {
        if (nonNull(params.getMessages())) {
            return "new String[] {" + params.messages.stream().map(s -> s instanceof String str ? "\"" + StringEscapeUtils.escapeJava(str) + "\"" : s.toString()).collect(Collectors.joining(", ")) + "}";
        } else {
            return isNull(params.getMessage()) ? "null" : "\"" + StringEscapeUtils.escapeJava(params.getMessage()) + "\"";
        }
    }

    protected String buildParamsStr(Params params, PrototypeField field, ModifierType modifier, boolean collection) {
        var list = params.getParams();
        if (isNull(list) || list.isEmpty()) {
            return "";
        }

        var result = new StringBuilder();
        if (nonNull(params.getAsCode()) && list.size() == 1 && list.get(0) instanceof String) {
            formatCode(field, modifier, result, (String) list.get(0), params.getAsCode(), collection);
        } else {
            for (var param : list) {
                if (param instanceof String p) {
                    result.append(", \"")
                            .append(StringEscapeUtils.escapeJava(p))
                            .append("\"");
                } else if (param instanceof Class cls) {
                    result.append(", ")
                            .append(cls)
                            .append(".class");
                } else if (param instanceof AsCodeHolder holder) {
                    var format = "%s".equals(holder.getFormat()) && !StringUtils.isBlank(params.getAsCode()) ? params.getAsCode() : holder.getFormat();
                    formatCode(field, modifier, result, holder.getValue(), format, collection);
                } else {
                    result.append(", ")
                            .append(nonNull(param) ? param.toString() : "null");
                }
            }
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    protected void formatCode(PrototypeField field, ModifierType modifier, StringBuilder result, String value, String format, boolean collection) {
        var type = calcType(field, modifier, collection);

        result.append(", ")
                .append(String.format(format.replaceAll("\\{type}", type.toString()),
                        value
                                .replaceAll("\\{type}", type.toString())
                                .replaceAll("\\{entity}", "parent".equals(modifier.getValue()) ? field.getDeclaration().findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString() + ".this" :  modifier.getValue())));
    }

    protected Type calcType(PrototypeField field, ModifierType modifier, boolean collection) {
        var type = field.getDeclaration().getVariable(0).getType();

        if (type.isPrimitiveType()) {
            type = type.asPrimitiveType().toBoxedType();
        }

        if ((collection || ModifierType.COLLECTION.equals(modifier)) && type.isClassOrInterfaceType()) {
            var args = type.asClassOrInterfaceType().getTypeArguments();
            if (args.isPresent() && !args.get().isEmpty()) {
                type = args.get().get(0);
            }
        }
        return type;
    }

    protected String buildParamsStr(Object param, Params params, PrototypeField field, ModifierType modifier, boolean collection) {
        if (param instanceof String p) {
            return "\"" + StringEscapeUtils.escapeJava(p) + "\"";
        } else if (param instanceof AsCodeHolder holder) {
            var format = "%s".equals(holder.getFormat()) && !StringUtils.isBlank(params.getAsCode()) ? params.getAsCode() : holder.getFormat();
            var type = calcType(field, modifier, collection);
            return String.format(format.replaceAll("\\{type}", type.toString()), holder.getValue());
        } else {
            return nonNull(param) ? param.toString() : "null";
        }
    }

    protected boolean isValidationAnnotation(AnnotationExpr annotation) {
        var unit = annotation.findCompilationUnit();
        if (unit.isPresent()) {
            var name = Helpers.getExternalClassNameIfExists(unit.get(), annotation.getNameAsString());
            var external = lookup.findExternal(name);
            if (nonNull(external)) {
                return withRes(external.getDeclaration(), decl ->
                        decl.isAnnotationPresent(Validate.class) || decl.isAnnotationPresent(Sanitize.class) || decl.isAnnotationPresent(Execute.class));
            }
            return withRes(loadClass(name), cls ->
                    Validate.class.equals(cls) || cls.isAnnotationPresent(Validate.class) || Sanitize.class.equals(cls) || cls.isAnnotationPresent(Sanitize.class) || Execute.class.equals(cls) || cls.isAnnotationPresent(Execute.class), false);
        } else {
            return false;
        }
    }

    protected void buildValidationForm(PrototypeDescription<ClassOrInterfaceDeclaration> description, StringBuilder form) {
        if (!form.isEmpty()) {
            form.setLength(form.lastIndexOf(","));
            form.append("); }");
            if (description.hasOption(Options.EXPOSE_VALIDATE_METHOD)) {
                description.getInterface().addExtendedType("Validatable");
                description.getInterface().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.Validatable"));
            } else {
                description.getImplementation().addImplementedType("Validatable");
                description.getImplementation().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.Validatable"));
            }
            description.getImplementation().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.flow.Validation"));
            description.getImplementation().addMethod("validate", PUBLIC)
                    .setBody(block("{ Validation.form(this.getClass(), " + form));
            Helpers.addSuppressWarningsUnchecked(description.getImplementation());
        }
    }

    protected MethodDeclaration formMethod(PrototypeField field) {
        var result = new MethodDeclaration();
        result.setBody(block("{ " + field.getName() + " = v; }"));
        return result;
    }

    protected List<String> processTargets(Expression value) {
        if (value instanceof ClassExpr expr) {
            return List.of(getExternalClassName(expr, expr.getType().asString()));
        }
        if (value instanceof ArrayInitializerExpr expr) {
            return expr.getValues().stream()
                    .filter(ClassExpr.class::isInstance)
                    .map(ClassExpr.class::cast)
                    .map(e -> getExternalClassName(e, e.getType().asString()))
                    .toList();
        }

        return List.of();
    }

    @SuppressWarnings("unchecked")
    protected List<String> processTargetsClass(Class[] value) {
        var result = new ArrayList<String>();

        for (var cls : value) {
            if (nonNull(TARGETS_AWARE) && TARGETS_AWARE.isAssignableFrom(cls)) {
                with(CodeFactory.create(cls), inst ->
                        Arrays.stream((Class[]) invoke("targets", inst))
                                .map(Class::getCanonicalName)
                                .forEach(result::add));
            } else {
                result.add(cls.getCanonicalName());
            }
        }

        return result;
    }

    protected Params checkTargets(Params params, PrototypeField field, Type type) {
        if (nonNull(params.getTargets()) && !params.getTargets().isEmpty()) {
            var cls = Holder.of(nonNull(type) ? getExternalClassName(type, type.asClassOrInterfaceType().getNameAsString()) : field.getFullType());

            var loaded = loadClass(cls.get());
            if (params.getTargets().stream().noneMatch(c -> c.equals(cls.get()) ||
                    (nonNull(loaded) && withRes(loadClass(c),
                            loadedTarget -> loadedTarget.isAssignableFrom(loaded) || (loaded.isArray() && Array.class.equals(loadedTarget)), false)))) {
                var element = field.getParsed().findElement(field.getParsed().getPrototypeElement(), field.getName(), ElementKind.METHOD);
                error("Target '" + cls + "' is not in the list of allowed targets for '" + params.getCls() + "': " + params.getTargets(), element);
            }
        }
        return params;
    }

    @Data
    @Builder
    protected static class Params {
        protected String full;
        protected String cls;
        protected String message;
        protected List<Object> messages;
        protected List<Object> params;
        protected List<String> targets;
        protected String asCode;

        protected CompilationUnit annotation;

        //Custom builder to satisfy java-doc
        public static class ParamsBuilder {

        }
    }

    @Data
    @Builder
    protected static class AsCodeHolder {
        protected String value;
        protected String format;
    }

    @Data
    @Builder
    protected static class ParamHolder {
        protected String name;
        protected Object value;
        protected AsCode annotation;
        protected int order;
        protected boolean alt;
    }


}
