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
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.utils.StringEscapeUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.validation.*;
import net.binis.codegen.enrich.ValidationEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Constants.EMBEDDED_MODIFIER_KEY;
import static net.binis.codegen.generation.core.Helpers.getExternalClassName;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.notNull;
import static net.binis.codegen.tools.Tools.withRes;

@Slf4j
public class ValidationEnricherHandler extends BaseEnricher implements ValidationEnricher {

    private static final String VALUE = "value";
    private static final String PARAMS = "params";
    private static final String MESSAGE = "message";
    private static final String AS_CODE = "asCode";

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public void finalize(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        description.getFields().forEach(f -> handleField(description, f));
    }

    @Override
    public int order() {
        return 0;
    }

    private void handleField(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field) {
        field.getDescription().getAnnotations().stream().filter(this::isValidationAnnotation).forEach(a -> processAnnotation(description, field, a));
    }

    private void processAnnotation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation) {
        var name = Helpers.getExternalClassNameIfExists(annotation.findCompilationUnit().get(), annotation.getNameAsString());
        notNull(loadClass(name), cls -> {
            if (Validate.class.equals(cls) || cls.isAnnotationPresent(Validate.class)) {
                generateValidation(description, field, annotation, cls);
            } else if (Sanitize.class.equals(cls) || cls.isAnnotationPresent(Sanitize.class)) {
                generateSanitization(description, field, annotation, cls);
            } else if (Execute.class.equals(cls) || cls.isAnnotationPresent(Execute.class)) {
                generateExecution(description, field, annotation, cls);
            }
        });
    }

    private void generateSanitization(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        Params ann = getSanitizationParams(field, annotation, annotationClass);

        if (isNull(field.getImplementationSetter())) {
            field.generateSetter();
        }

        addSanitization(field, field.getImplementationSetter(), ann, ModifierType.MAIN);
        field.getModifiers().forEach(modifier -> addSanitization(field, modifier, ann, ModifierType.MODIFIER));
        handleSanitizationModifier(description, field, EMBEDDED_MODIFIER_KEY, ann);
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

        var result = params.build();

        if (isNull(result.getAsCode())) {
            notNull(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    notNull(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    private Params getValidationParams(PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        var params = Params.builder();
        String cls = null;

        if (!Validate.class.equals(annotationClass)) {
            var ann = annotationClass.getDeclaredAnnotation(Validate.class);
            params.cls(ann.value().getSimpleName()).params(Arrays.asList(ann.params()));
            cls = ann.value().getCanonicalName();
            field.getDeclaration().findCompilationUnit().get().addImport(cls);

            handleAliases(annotation, annotationClass, params);
        } else {
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

        var result = params.build();

        if (isNull(result.getAsCode())) {
            notNull(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    notNull(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
    }

    private void handleAliases(AnnotationExpr annotation, Class<?> annotationClass, Params.ParamsBuilder params) {
        var list = new ArrayList<Object>();
        var parOrder = Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> Arrays.stream(m.getDeclaredAnnotations()).filter(a -> a.annotationType().isAssignableFrom(AliasFor.class)).map(AliasFor.class::cast).anyMatch(a -> PARAMS.equals(a.value())))
                .map(m -> Triple.of(m.getName(), m.getDefaultValue(), m.getDeclaredAnnotation(AsCode.class)))
                .collect(Collectors.toList());

        Arrays.stream(annotationClass.getDeclaredMethods())
                .filter(m -> MESSAGE.equals(m.getName()))
                .filter(m -> m.getReturnType().equals(String.class))
                .filter(m -> isNull(m.getDeclaredAnnotation(AliasFor.class)))
                .findFirst().ifPresent(m ->
                        params.message((String) m.getDefaultValue()));

        parOrder.forEach(p -> list.add(checkAsCode(p.getMiddle(), p.getRight())));

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
                                list.set(idx, getParamValue(pair.getValue()));
                            } else {
                                throw new GenericCodeGenException("Invalid annotation params! " + annotation);
                            }
                        }
                        break;
                    default:
                }
            } else if (node instanceof StringLiteralExpr) {
                var exp = ((StringLiteralExpr) node).asString();
                switch (Arrays.stream(annotationClass.getDeclaredMethods())
                        .filter(m -> m.getName().equals(VALUE))
                        .map(m -> m.getDeclaredAnnotation(AliasFor.class))
                        .filter(Objects::nonNull)
                        .map(AliasFor::value)
                        .findFirst()
                        .orElse(VALUE)) {
                    case VALUE:
                        params.cls(exp);
                        break;
                    case MESSAGE:
                        params.message(exp);
                        break;
                    case AS_CODE:
                        params.asCode(exp);
                        break;
                    case PARAMS:
                        var idx = getParamIndex(parOrder, VALUE);
                        if (idx != -1) {
                            var triple = parOrder.get(idx);
                            if (nonNull(triple.getRight())) {
                                list.set(idx, checkAsCode(exp, triple.getRight()));
                            } else {
                                list.set(idx, exp);
                            }
                        } else {
                            throw new GenericCodeGenException("Invalid annotation params! " + annotation);
                        }
                        break;
                    default:

                }
            }
        }
        if (!list.isEmpty()) {
            params.params(list);
        }
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

    private int getParamIndex(List<Triple<String, Object, AsCode>> list, String name) {
        for (var i = 0; i < list.size(); i++) {
            if (name.equals(list.get(i).getLeft())) {
                return i;
            }
        }
        return -1;
    }

    private void generateValidation(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        var ann = getValidationParams(field, annotation, annotationClass);

        if (isNull(field.getImplementationSetter())) {
            field.generateSetter();
        }

        addValidation(field, field.getImplementationSetter(), ann, ModifierType.MAIN);
        field.getModifiers().forEach(modifier -> addValidation(field, modifier, ann, ModifierType.MODIFIER));
        handleValidationModifier(description, field, EMBEDDED_MODIFIER_KEY, ann);
    }

    private void handleValidationModifier(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, String key, Params params) {
        var modifier = description.getRegisteredClass(key);
        if (nonNull(modifier)) {
            modifier.getChildNodes().stream()
                    .filter(MethodDeclaration.class::isInstance)
                    .map(MethodDeclaration.class::cast)
                    .filter(m -> m.getNameAsString().equals(field.getName()))
                    .findFirst().ifPresent(m ->
                            addValidation(field, m, params, ModifierType.EMBEDDED));
        }
    }

    private void addValidation(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier) {
        method.findCompilationUnit().get().addImport("net.binis.codegen.validation.flow.Validation");
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        if (block.getStatements().get(0).asExpressionStmt().getExpression() instanceof AssignExpr) {
            var exp = new StringBuilder("Validation.start(\"")
                    .append(field.getName())
                    .append("\", ")
                    .append(field.getName())
                    .append(").validate(")
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
            var m = new MethodCallExpr(chain, "validate").addArgument(params.getCls() + ".class").addArgument(calcMessage(params));
            notNull(params.getParams(), p -> p.forEach(param ->
                    m.addArgument(buildParamsStr(param, params, field, modifier))));
            mCall.setScope(m);
        }
    }

    private String calcMessage(Params params) {
        return isNull(params.getMessage()) ? "null" : "\"" + StringEscapeUtils.escapeJava(params.getMessage()) + "\"";
    }

    private void handleSanitizationModifier(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, String key, Params params) {
        var modifier = description.getRegisteredClass(key);
        if (nonNull(modifier)) {
            modifier.getChildNodes().stream()
                    .filter(MethodDeclaration.class::isInstance)
                    .map(MethodDeclaration.class::cast)
                    .filter(m -> m.getNameAsString().equals(field.getName()))
                    .findFirst().ifPresent(m ->
                            addSanitization(field, m, params, ModifierType.EMBEDDED));
        }
    }

    private void addSanitization(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier) {
        method.findCompilationUnit().get().addImport("net.binis.codegen.validation.flow.Validation");
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        if (block.getStatements().get(0).asExpressionStmt().getExpression() instanceof AssignExpr) {
            var exp = new StringBuilder("Validation.start(\"")
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
            var m = new MethodCallExpr(chain, "sanitize").addArgument(params.getCls() + ".class").addArgument(calcMessage(params));
            notNull(params.getParams(), p -> p.forEach(param ->
                    m.addArgument(buildParamsStr(param, params, field, modifier))));
            mCall.setScope(m);
        }
    }

    private void generateExecution(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, AnnotationExpr annotation, Class<?> annotationClass) {
        var ann = getExecutionParams(field, annotation, annotationClass);

        if (isNull(field.getImplementationSetter())) {
            field.generateSetter();
        }

        addExecution(field, field.getImplementationSetter(), ann, ModifierType.MAIN);
        field.getModifiers().forEach(modifier -> addExecution(field, modifier, ann, ModifierType.MODIFIER));
        handleExecutionModifier(description, field, EMBEDDED_MODIFIER_KEY, ann);
    }

    private void handleExecutionModifier(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field, String key, Params params) {
        var modifier = description.getRegisteredClass(key);
        if (nonNull(modifier)) {
            modifier.getChildNodes().stream()
                    .filter(MethodDeclaration.class::isInstance)
                    .map(MethodDeclaration.class::cast)
                    .filter(m -> m.getNameAsString().equals(field.getName()))
                    .findFirst().ifPresent(m ->
                            addExecution(field, m, params, ModifierType.EMBEDDED));
        }
    }

    private void addExecution(PrototypeField field, MethodDeclaration method, Params params, ModifierType modifier) {
        method.findCompilationUnit().get().addImport("net.binis.codegen.validation.flow.Validation");
        var block = method.getChildNodes().stream().filter(BlockStmt.class::isInstance).map(BlockStmt.class::cast).findFirst().get();

        if (block.getStatements().get(0).asExpressionStmt().getExpression() instanceof AssignExpr) {
            var exp = new StringBuilder("Validation.start(\"")
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
                    m.addArgument(buildParamsStr(param, params, field, modifier))));
            mCall.setScope(m);
        }
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

        var result = params.build();

        if (isNull(result.getAsCode())) {
            notNull(loadClass(isNull(cls) ? getExternalClassName(field.getParsed().getDeclaration().findCompilationUnit().get(), result.getCls()) : cls), c ->
                    notNull(c.getDeclaredAnnotation(AsCode.class), a -> result.setAsCode(a.value())));
        }

        return result;
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
                                holder.getValue().replaceAll("\\{entity}", modifier.getValue())));
            } else {
                result.append(", ")
                        .append(nonNull(param) ? param.toString() : "null");
            }
        }
        return result.toString();
    }

    private String buildParamsStr(Object param, Params params, PrototypeField field, ModifierType modifier) {
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
        return withRes(loadClass(Helpers.getExternalClassNameIfExists(annotation.findCompilationUnit().get(), annotation.getNameAsString())), cls ->
                Validate.class.equals(cls) || cls.isAnnotationPresent(Validate.class) || Sanitize.class.equals(cls) || cls.isAnnotationPresent(Sanitize.class) || Execute.class.equals(cls) || cls.isAnnotationPresent(Execute.class), false);
    }

    private static enum ModifierType {
        MAIN("this"),
        MODIFIER("parent"),
        EMBEDDED("entity");

        private String value;

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
        private List<Object> params;
        private String asCode;
    }

    @Data
    @Builder
    private static class AsCodeHolder {
        private String value;
        private String format;
    }

}
