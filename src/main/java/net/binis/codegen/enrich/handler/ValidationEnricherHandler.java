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

import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.validation.*;
import net.binis.codegen.enrich.Enrichers;
import net.binis.codegen.enrich.ValidationEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.options.Options;
import net.binis.codegen.tools.Holder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Helpers.getExternalClassName;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.*;

@Slf4j
public class ValidationEnricherHandler extends BaseEnricher implements ValidationEnricher {

    private static final String VALUE = "value";
    private static final String PARAMS = "params";
    private static final String MESSAGE = "message";
    private static final String MESSAGES = "messages";
    private static final String AS_CODE = "asCode";

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var form = new StringBuilder();
        description.getFields().forEach(f -> handleField(description, f, form));

        if (description.hasOption(Options.VALIDATION_FORM)) {
            buildValidationForm(description, form);
        }
    }

    @Override
    public int order() {
        return 0;
    }

    private void handleField(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, StringBuilder code) {
        var form = description.hasOption(Options.VALIDATION_FORM) ? formMethod(field) : null;
        field.getDescription().getAnnotations().stream().filter(this::isValidationAnnotation).forEach(a -> processAnnotation(description, field, a, form));
        if (nonNull(form)) {
            var isChild = nonNull(field.getPrototype()) && field.getPrototype().hasEnricher(Enrichers.VALIDATION) && field.getPrototype().hasOption(Options.VALIDATION_FORM);
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

    private void processAnnotation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, MethodDeclaration form) {
        var name = Helpers.getExternalClassNameIfExists(annotation.findCompilationUnit().get(), annotation.getNameAsString());

        var cls = loadClass(name);
        if (nonNull(cls)) {
            if (Validate.class.equals(cls) || cls.isAnnotationPresent(Validate.class)) {
                generateValidation(description, field, annotation, cls, form);
            } else if (Sanitize.class.equals(cls) || cls.isAnnotationPresent(Sanitize.class)) {
                generateSanitization(description, field, annotation, cls, form);
            } else if (Execute.class.equals(cls) || cls.isAnnotationPresent(Execute.class)) {
                generateExecution(description, field, annotation, cls);
            }
        } else {
            notNull(lookup.findExternal(name), d ->
                    handleAnnotationFromSource(description, d.getDeclaration().asAnnotationDeclaration(), field, annotation, form));
        }
    }

    private void handleAnnotationFromSource(PrototypeDescription<ClassOrInterfaceDeclaration> description, AnnotationDeclaration decl, PrototypeField field, AnnotationExpr annotation, MethodDeclaration form) {
        var ann = decl.getAnnotationByClass(Validate.class);
        if (ann.isPresent()) {
            generateValidation(description, field, annotation, ann.get(), decl, form);
        } else {
            ann = decl.getAnnotationByClass(Sanitize.class);
            if (ann.isPresent()) {
                generateSanitization(description, field, annotation, ann.get(), decl, form);
            } else {
                ann = decl.getAnnotationByClass(Execute.class);
                if (ann.isPresent()) {
                    generateExecution(description, field, annotation, ann.get(), decl);
                }
            }
        }
    }

    private void generateSanitization(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass, MethodDeclaration form) {
        var params = getSanitizationParams(field, annotation, ann, annotationClass);
        field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(getExternalClassName(annotationClass.findCompilationUnit().get(), params.getCls())));
        generateSanitization(description, field, params, form);
    }

    private void generateSanitization(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass, MethodDeclaration form) {
        generateSanitization(description, field, getSanitizationParams(field, annotation, annotationClass), form);
    }

    private void generateSanitization(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Params params, MethodDeclaration form) {
        if (nonNull(field.getImplementationSetter())) {
            addSanitization(field, field.getImplementationSetter(), params, ModifierType.MAIN);
        }

        field.getModifiers().forEach(modifier -> addSanitization(field, modifier, params, ModifierType.MODIFIER));

        if (description.hasOption(Options.VALIDATION_FORM)) {
            addSanitization(field, form, params, ModifierType.FORM);
        }
    }

    private Params getSanitizationParams(PrototypeField field, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass) {
        var params = Params.builder();

        handleSanitizationAnnotation(ann, params);
        //TODO: Handle aliases

        return params.build();
    }

    private Params getSanitizationParams(PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        String cls = null;
        var params = Params.builder();

        if (!Sanitize.class.equals(annotationClass)) {
            var ann = annotationClass.getDeclaredAnnotation(Sanitize.class);
            params.cls(ann.value().getSimpleName()).params(Arrays.asList(ann.params()));
            cls = ann.value().getCanonicalName();
            field.getDeclaration().findCompilationUnit().get().addImport(cls);

            handleAliases(annotation, annotationClass, params);
        } else {
            handleSanitizationAnnotation(annotation, params);
        }

        var result = params.build();

        if (isNull(result.getAsCode())) {
            notNull(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    notNull(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    private void handleSanitizationAnnotation(AnnotationExpr annotation, Params.ParamsBuilder params) {
        for (var node : annotation.getChildNodes()) {
            if (node instanceof ClassExpr) {
                params.cls(((ClassExpr) node).getTypeAsString());
            } else if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                switch (pair.getNameAsString()) {
                    case VALUE:
                        params.cls(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case PARAMS:
                        params.params(pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).map(StringLiteralExpr::asString).collect(Collectors.toList()));
                        break;
                    case AS_CODE:
                        params.asCode(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    default:
                }
            }
        }
    }

    private Params getValidationParams(PrototypeField field, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass) {
        var params = Params.builder();

        handleValidationAnnotation(ann, params);
        //TODO: Handle aliases

        return params.build();
    }

    private Params getValidationParams(PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        var params = Params.builder();
        String cls = null;

        if (!Validate.class.equals(annotationClass)) {
            var ann = annotationClass.getDeclaredAnnotation(Validate.class);
            params.cls(ann.value().getSimpleName()).params(Arrays.asList(ann.params())).message(ann.message());
            cls = ann.value().getCanonicalName();
            field.getDeclaration().findCompilationUnit().get().addImport(cls);

            handleAliases(annotation, annotationClass, params);
        } else {
            handleValidationAnnotation(annotation, params);
        }

        var result = params.build();

        if (isNull(result.getAsCode())) {
            notNull(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    notNull(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    private void handleValidationAnnotation(AnnotationExpr annotation, ValidationEnricherHandler.Params.ParamsBuilder params) {
        for (var node : annotation.getChildNodes()) {
            if (node instanceof ClassExpr) {
                params.cls(((ClassExpr) node).getTypeAsString());
            } else if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                switch (pair.getNameAsString()) {
                    case VALUE:
                        params.cls(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case MESSAGE:
                        params.message(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    case MESSAGES:
                        params.messages(pair.getValue().asArrayInitializerExpr().getValues().stream().map(e -> e.asStringLiteralExpr().asString()).collect(Collectors.toList()));
                        break;
                    case PARAMS:
                        if (pair.getValue().isArrayInitializerExpr()) {
                            params.params(pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).map(StringLiteralExpr::asString).collect(Collectors.toList()));
                        } else {
                            params.params(List.of(pair.getValue()));
                        }
                        break;
                    case AS_CODE:
                        params.asCode(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    default:
                }
            }
        }
    }

    private void handleAliases(AnnotationExpr annotation, Class<?> annotationClass, Params.ParamsBuilder params) {
        var list = new ArrayList<Object>();
        var parOrder = Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> Arrays.stream(m.getDeclaredAnnotations())
                        .filter(a -> a.annotationType().isAssignableFrom(AliasFor.class))
                        .map(AliasFor.class::cast).anyMatch(a -> PARAMS.equals(a.value())))
                .map(m -> ParamHolder.builder()
                        .name(m.getName())
                        .value(m.getDefaultValue())
                        .annotation(m.getDeclaredAnnotation(AsCode.class))
                        .order(m.getDeclaredAnnotation(AliasFor.class).order())
                        .build())
                .collect(Collectors.toList());

        parOrder.sort(Comparator.comparing(ParamHolder::getOrder));

        Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> MESSAGE.equals(m.getName()))
                .filter(m -> m.getReturnType().equals(String.class))
                .filter(m -> isNull(m.getDeclaredAnnotation(AliasFor.class)))
                .findFirst().ifPresent(m ->
                        params.message((String) m.getDefaultValue()));

        var messages = Holder.<List<String>>blank();
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

                        msgs.set(order, value);
                    });
        }

        parOrder.forEach(p -> list.add(checkAsCode(p.getValue(), p.getAnnotation())));
        var msgs = 0;

        for (var node : annotation.getChildNodes()) {
            if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                switch (Arrays.stream(annotationClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals(pair.getNameAsString()))
                        .map(m -> m.getDeclaredAnnotation(AliasFor.class))
                        .filter(Objects::nonNull)
                        .map(AliasFor::value)
                        .findFirst()
                        .orElseGet(pair::getNameAsString)) {
                    case VALUE:
                        params.cls(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case MESSAGE:
                        params.message(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    case MESSAGES:
                        if (pair.getValue().isArrayInitializerExpr()) {
                            params.messages(pair.getValue().asArrayInitializerExpr().getValues().stream().map(e -> e.asStringLiteralExpr().asString()).collect(Collectors.toList()));
                        } else if (pair.getValue().isStringLiteralExpr()) {
                            var msg = pair.getValue().asStringLiteralExpr().asString();
                            if (messages.isEmpty()) {
                                messages.set(new ArrayList<>());
                            }
                            if (msgs < messages.get().size()) {
                                messages.get().set(msgs, msg);
                            } else {
                                messages.get().add(msg);
                            }
                            msgs++;
                        }
                        break;
                    case AS_CODE:
                        params.asCode(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    case PARAMS:
                        if (pair.getValue().isArrayInitializerExpr()) {
                            list.addAll(pair.getValue().asArrayInitializerExpr().getValues().stream()
                                    .map(Expression::asStringLiteralExpr)
                                    .map(StringLiteralExpr::asString)
                                    .collect(Collectors.toList()));
                        } else {
                            var idx = getParamIndex(parOrder, pair.getNameAsString());
                            if (idx != -1) {
                                var triple = parOrder.get(idx);
                                list.set(idx, checkAsCode(getParamValue(pair.getValue()), triple.getAnnotation()));
                            } else {
                                throw new GenericCodeGenException("Invalid annotation params! " + annotation);
                            }
                        }
                        break;
                    default:
                }
            } else if (node instanceof LiteralExpr) {
                var exp = getParamValue((LiteralExpr) node);
                switch (Arrays.stream(annotationClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals(VALUE))
                        .map(m -> m.getDeclaredAnnotation(AliasFor.class))
                        .filter(Objects::nonNull)
                        .map(AliasFor::value)
                        .findFirst()
                        .orElse(VALUE)) {
                    case VALUE:
                        params.cls(exp.toString());
                        break;
                    case MESSAGE:
                        params.message(exp.toString());
                        break;
                    case AS_CODE:
                        params.asCode(exp.toString());
                        break;
                    case PARAMS:
                        var idx = getParamIndex(parOrder, VALUE);
                        if (idx != -1) {
                            var triple = parOrder.get(idx);
                            if (nonNull(triple.getAnnotation())) {
                                list.set(idx, checkAsCode(exp, triple.getAnnotation()));
                            } else {
                                list.set(idx, exp);
                            }
                        } else {
                            throw new GenericCodeGenException("Invalid annotation params! " + annotation);
                        }
                        break;
                    default:
                        //Do nothing
                }
            }
        }
        if (!list.isEmpty()) {
            params.params(list);
        }
        params.messages = messages.get();
    }

    private Object checkAsCode(Object value, AsCode code) {
        if (nonNull(code)) {
            return AsCodeHolder.builder().value((String) value).format(code.value()).build();
        }
        return value;
    }

    private Object getParamValue(Expression value) {
        if (value.isStringLiteralExpr()) {
            return value.asStringLiteralExpr().asString();
        } else if (value.isIntegerLiteralExpr()) {
            return value.asIntegerLiteralExpr().asNumber();
        } else if (value.isDoubleLiteralExpr()) {
            return value.asDoubleLiteralExpr().asDouble();
        } else if (value.isBooleanLiteralExpr()) {
            return value.asBooleanLiteralExpr().getValue();
        }
        //TODO: Handle external constants
        return null;
    }

    private int getParamIndex(List<ParamHolder> list, String name) {
        for (var i = 0; i < list.size(); i++) {
            if (name.equals(list.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    private void generateValidation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass, MethodDeclaration form) {
        var params = getValidationParams(field, annotation, ann, annotationClass);
        field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(getExternalClassName(annotationClass.findCompilationUnit().get(), params.getCls())));
        generateValidation(description, field, params, form);
    }

    private void generateValidation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass, MethodDeclaration form) {
        generateValidation(description, field, getValidationParams(field, annotation, annotationClass), form);
    }

    private void generateValidation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Params params, MethodDeclaration form) {
        if (nonNull(field.getImplementationSetter())) {
            addValidation(field, field.getImplementationSetter(), params, ModifierType.MAIN);
        }

        field.getModifiers().forEach(modifier -> addValidation(field, modifier, params, ModifierType.MODIFIER));

        if (description.hasOption(Options.VALIDATION_FORM)) {
            addValidation(field, form, params, ModifierType.FORM);
        }
    }


    private void addValidation(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier) {
        method.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.flow.Validation"));
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        if (block.getStatements().get(0).asExpressionStmt().getExpression() instanceof AssignExpr) {
            var exp = new StringBuilder("Validation.start(this.getClass(), \"")
                    .append(field.getName())
                    .append("\", ")
                    .append(field.getName())
                    .append(").validate")
                    .append(nonNull(params.getMessages()) ? "WithMessages(" : "(")
                    .append(params.getCls())
                    .append(".class, ")
                    .append(calcMessage(params))
                    .append(buildParamsStr(params, field, modifier))
                    .append(").perform(v -> this.map = v);");
            var expr = lookup.getParser().parseStatement(exp.toString()).getResult().get();
            var original = block.getStatements().remove(0);
            ((ExpressionStmt) original).getExpression().asAssignExpr().setValue(new NameExpr("v"));
            var mCall = expr.asExpressionStmt().getExpression().asMethodCallExpr();
            ((LambdaExpr) mCall.getChildNodes().get(mCall.getChildNodes().size() - 1)).setBody(original);
            block.getStatements().add(0, expr);
        } else {
            var mCall = block.getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr();
            var chain = mCall.getScope().get();
            mCall.removeScope();
            var m = new MethodCallExpr(chain, "validate" + (nonNull(params.getMessages()) ? "WithMessages" : "")).addArgument(params.getCls() + ".class").addArgument(calcMessage(params));
            notNull(params.getParams(), p -> p.forEach(param ->
                    m.addArgument(buildParamsStr(param, params, field))));
            mCall.setScope(m);
        }
    }

    private String calcMessage(Params params) {
        if (nonNull(params.getMessages())) {
            return "new String[] {" + params.messages.stream().map(s -> "\"" + StringEscapeUtils.escapeJava(s) + "\"").collect(Collectors.joining(", ")) + "}";
        } else {
            return isNull(params.getMessage()) ? "null" : "\"" + StringEscapeUtils.escapeJava(params.getMessage()) + "\"";
        }
    }

    private void addSanitization(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier) {
        method.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.flow.Validation"));
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        if (block.getStatements().get(0).asExpressionStmt().getExpression() instanceof AssignExpr) {
            var exp = new StringBuilder("Validation.start(this.getClass(), \"")
                    .append(field.getName())
                    .append("\", ")
                    .append(field.getName())
                    .append(").sanitize(")
                    .append(params.getCls())
                    .append(".class")
                    .append(buildParamsStr(params, field, modifier))
                    .append(").perform(v -> this.map = v);");
            var expr = lookup.getParser().parseStatement(exp.toString()).getResult().get();
            var original = block.getStatements().remove(0);
            ((ExpressionStmt) original).getExpression().asAssignExpr().setValue(new NameExpr("v"));
            var mCall = expr.asExpressionStmt().getExpression().asMethodCallExpr();
            ((LambdaExpr) mCall.getChildNodes().get(mCall.getChildNodes().size() - 1)).setBody(original);
            block.getStatements().add(0, expr);
        } else {
            var mCall = block.getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr();
            var chain = mCall.getScope().get();
            mCall.removeScope();
            var m = new MethodCallExpr(chain, "sanitize").addArgument(params.getCls() + ".class");
            notNull(params.getParams(), p -> p.forEach(param ->
                    m.addArgument(buildParamsStr(param, params, field))));
            mCall.setScope(m);
        }
    }

    private void generateExecution(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass) {
        var params = getExecutionParams(field, annotation, ann, annotationClass);
        field.getDeclaration().findCompilationUnit().ifPresent(u -> u.addImport(getExternalClassName(annotationClass.findCompilationUnit().get(), params.getCls())));
        generateExecution(description, field, params);
    }

    private void generateExecution(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        generateExecution(description, field, getExecutionParams(field, annotation, annotationClass));
    }

    private void generateExecution(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, Params params) {
        if (nonNull(field.getImplementationSetter())) {
            addExecution(field, field.getImplementationSetter(), params, ModifierType.MAIN);
        }

        field.getModifiers().forEach(modifier -> addExecution(field, modifier, params, ModifierType.MODIFIER));
    }

    private void addExecution(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier) {
        method.findCompilationUnit().get().addImport("net.binis.codegen.validation.flow.Validation");
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        if (block.getStatements().get(0).asExpressionStmt().getExpression() instanceof AssignExpr) {
            var exp = new StringBuilder("Validation.start(this.getClass(), \"")
                    .append(field.getName())
                    .append("\", ")
                    .append(field.getName())
                    .append(").execute(")
                    .append(params.getCls())
                    .append(".class, ")
                    .append(calcMessage(params))
                    .append(buildParamsStr(params, field, modifier))
                    .append(").perform(v -> this.map = v);");
            var expr = lookup.getParser().parseStatement(exp.toString()).getResult().get();
            var original = block.getStatements().remove(0);
            ((ExpressionStmt) original).getExpression().asAssignExpr().setValue(new NameExpr("v"));
            var mCall = expr.asExpressionStmt().getExpression().asMethodCallExpr();
            ((LambdaExpr) mCall.getChildNodes().get(mCall.getChildNodes().size() - 1)).setBody(original);
            block.getStatements().add(0, expr);
        } else {
            var mCall = block.getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr();
            var chain = mCall.getScope().get();
            mCall.removeScope();
            var m = new MethodCallExpr(chain, "execute").addArgument(params.getCls() + ".class").addArgument(calcMessage(params));
            notNull(params.getParams(), p -> p.forEach(param ->
                    m.addArgument(buildParamsStr(param, params, field))));
            mCall.setScope(m);
        }
    }

    private Params getExecutionParams(PrototypeField field, AnnotationExpr annotation, AnnotationExpr ann, AnnotationDeclaration annotationClass) {
        var params = Params.builder();

        handleExecutionAnnotation(ann, params);
        //TODO: Handle aliases

        return params.build();
    }

    private Params getExecutionParams(PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        var params = Params.builder();
        String cls = null;

        if (!Execute.class.equals(annotationClass)) {
            var ann = annotationClass.getDeclaredAnnotation(Execute.class);
            params.cls(ann.value().getSimpleName()).params(Arrays.asList(ann.params()));
            cls = ann.value().getCanonicalName();
            field.getDeclaration().findCompilationUnit().get().addImport(cls);

            handleAliases(annotation, annotationClass, params);
        } else {
            handleExecutionAnnotation(annotation, params);
        }

        var result = params.build();

        if (isNull(result.getAsCode())) {
            notNull(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    notNull(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    private void handleExecutionAnnotation(AnnotationExpr annotation, Params.ParamsBuilder params) {
        for (var node : annotation.getChildNodes()) {
            if (node instanceof ClassExpr) {
                params.cls(((ClassExpr) node).getTypeAsString());
            } else if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                switch (pair.getNameAsString()) {
                    case VALUE:
                        params.cls(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case MESSAGE:
                        params.message(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    case PARAMS:
                        params.params(pair.getValue().asArrayInitializerExpr().getValues().stream().map(Expression::asStringLiteralExpr).map(StringLiteralExpr::asString).collect(Collectors.toList()));
                        break;
                    case AS_CODE:
                        params.asCode(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    default:
                }
            }
        }
    }

    private String buildParamsStr(Params params, PrototypeField field, ModifierType modifier) {
        var list = params.getParams();
        if (isNull(list) || list.isEmpty()) {
            return "";
        }

        var result = new StringBuilder();
        for (var param : list) {
            if (param instanceof String) {
                result.append(", \"")
                        .append(StringEscapeUtils.escapeJava((String) param))
                        .append("\"");
            } else if (param instanceof AsCodeHolder) {
                var holder = (AsCodeHolder) param;
                var format = "%s".equals(holder.getFormat()) && !StringUtils.isBlank(params.getAsCode()) ? params.getAsCode() : holder.getFormat();
                result.append(", ")
                        .append(String.format(format.replaceAll("\\{type}", field.getDeclaration().getVariable(0).getTypeAsString()),
                                holder.getValue()
                                        .replaceAll("\\{type}", field.getDeclaration().getVariable(0).getTypeAsString())
                                        .replaceAll("\\{entity}", (ModifierType.MODIFIER.equals(modifier) ? "(" + field.getDeclaration().findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString() + ")" : "") + modifier.getValue())));
            } else {
                result.append(", ")
                        .append(nonNull(param) ? param.toString() : "null");
            }
        }
        return result.toString();
    }

    private String buildParamsStr(Object param, Params params, PrototypeField field) {
        if (param instanceof String) {
            return "\"" + StringEscapeUtils.escapeJava((String) param) + "\"";
        } else if (param instanceof AsCodeHolder) {
            var holder = (AsCodeHolder) param;
            var format = "%s".equals(holder.getFormat()) && !StringUtils.isBlank(params.getAsCode()) ? params.getAsCode() : holder.getFormat();
            return String.format(format.replaceAll("\\{type}", field.getDeclaration().getVariable(0).getTypeAsString()), holder.getValue());
        } else {
            return nonNull(param) ? param.toString() : "null";
        }
    }


    private boolean isValidationAnnotation(AnnotationExpr annotation) {
        var name = Helpers.getExternalClassNameIfExists(annotation.findCompilationUnit().get(), annotation.getNameAsString());
        var external = lookup.findExternal(name);
        if (nonNull(external)) {
            return withRes(external.getDeclaration(), decl ->
                    decl.isAnnotationPresent(Validate.class) || decl.isAnnotationPresent(Sanitize.class) || decl.isAnnotationPresent(Execute.class));
        }
        return withRes(loadClass(name), cls ->
                Validate.class.equals(cls) || cls.isAnnotationPresent(Validate.class) || Sanitize.class.equals(cls) || cls.isAnnotationPresent(Sanitize.class) || Execute.class.equals(cls) || cls.isAnnotationPresent(Execute.class), false);
    }

    private void buildValidationForm(PrototypeDescription<ClassOrInterfaceDeclaration> description, StringBuilder form) {
        if (form.length() > 0) {
            form.setLength(form.lastIndexOf(","));
            form.append("); }");
            if (description.hasOption(Options.EXPOSE_VALIDATE_METHOD)) {
                description.getIntf().addExtendedType("Validatable");
                description.getIntf().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.Validatable"));
            } else {
                description.getSpec().addImplementedType("Validatable");
                description.getSpec().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.Validatable"));
            }
            description.getSpec().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.validation.flow.Validation"));
            description.getSpec().addMethod("validate", PUBLIC).setBody(description.getParser().parseBlock("{ Validation.form(this.getClass(), " + form).getResult().get());
        }
    }

    private MethodDeclaration formMethod(PrototypeField field) {
        var result = new MethodDeclaration();
        result.setBody(lookup.getParser().parseBlock("{ " + field.getName() + " = v; }").getResult().get());
        return result;
    }

    private enum ModifierType {
        MAIN("this"),
        MODIFIER("parent"),
        FORM(null);

        private final String value;

        ModifierType(String s) {
            this.value = s;
        }

        String getValue() {
            return value;
        }
    }

    @Data
    @Builder
    private static class Params {
        private String cls;
        private String message;
        private List<String> messages;

        private List<Object> params;
        private String asCode;

        //Custom builder to satisfy java-doc
        public static class ParamsBuilder {

        }
    }

    @Data
    @Builder
    private static class AsCodeHolder {
        private String value;
        private String format;
    }

    @Data
    @Builder
    private static class ParamHolder {
        private String name;
        private Object value;
        private AsCode annotation;
        private int order;
    }


}
