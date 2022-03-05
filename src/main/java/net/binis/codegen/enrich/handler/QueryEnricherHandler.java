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

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.QueryEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Constants;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.spring.annotation.Joinable;
import net.binis.codegen.spring.annotation.QueryPreset;
import net.binis.codegen.spring.query.Preset;

import java.util.Set;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Tools.with;

@Slf4j
public class QueryEnricherHandler extends BaseEnricher implements QueryEnricher {

    public static final String OPERATION = "Operation";
    private static final String QUERY_START = "QueryStarter";
    private static final String QUERY_SELECT = "QuerySelect";
    private static final String QUERY_SELECT_OPERATION = QUERY_SELECT + OPERATION;
    private static final String QUERY_ORDER = "QueryOrder";
    private static final String QUERY_ORDER_OPERATION = QUERY_ORDER + OPERATION;
    private static final String QUERY_ORDER_START = QUERY_ORDER + "Start";
    private static final String QUERY_AGGREGATE = "QueryAggregate";
    private static final String QUERY_FIELDS_START = "QueryFieldsStart";
    private static final String QUERY_AGGREGATE_OPERATION = QUERY_AGGREGATE + OPERATION;
    private static final String QUERY_JOIN_AGGREGATE_OPERATION = "QueryJoinAggregate" + OPERATION;
    private static final String QUERY_AGGREGATOR = "QueryAggregator";
    private static final String QUERY_PARAM = "QueryParam";
    private static final String QUERY_EXECUTE = "QueryExecute";
    private static final String QUERY_WHERE = "QueryWhere";
    private static final String QUERY_EXECUTOR = "QueryExecutor";
    private static final String QUERY_GENERIC = "QR";
    private static final String QUERY_SELECT_GENERIC = "QS";
    private static final String QUERY_ORDER_GENERIC = "QO";
    private static final String QUERY_AGGREGATE_GENERIC = "QA";
    private static final String QUERY_FETCH_GENERIC = "QF";
    private static final String QUERY_FUNCTIONS = "QueryFunctions";
    private static final String QUERY_COLLECTION_FUNCTIONS = "QueryCollectionFunctions";
    private static final String QUERY_JOIN_COLLECTION_FUNCTIONS = "QueryJoinCollectionFunctions";
    private static final String QUERY_FIELDS = "QueryFields";
    private static final String QUERY_OP_FIELDS = "QueryOperationFields";
    private static final String QUERY_FUNCS = "QueryFuncs";
    private static final String QUERY_FETCH = "QueryFetch";
    private static final String QUERY_NAME = "QueryName";
    private static final String QUERY_SCRIPT = "QueryScript";
    private static final String QUERY_BRACKET = "QueryBracket";
    private static final String QUERY_IMPL = "Impl";
    private static final String PRESET_PREFIX = "__";

    private static final Set<String> reserved = Set.of(
            "ensure",
            "reference",
            "get",
            "list",
            "references",
            "count",
            "top",

            "page",

            "tuple",
            "tuples",
            "prepare",

            "projection",
            "flush",
            "lock",
            "hint",
            "filter",

            "exists",
            "delete",
            "remove");

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var spec = description.getSpec();
        var intf = description.getIntf();
        var entity = description.getProperties().getInterfaceName();

        with(intf.findCompilationUnit().get(), unit -> unit
                .addImport("java.util.List")
                .addImport("net.binis.codegen.creator.EntityCreator")
                .addImport("net.binis.codegen.spring.query.*")
                .addImport("java.util.Optional")
        );
        with(spec.findCompilationUnit().get(), unit -> unit
                .addImport("java.util.List")
                .addImport("net.binis.codegen.factory.CodeFactory")
                .addImport("net.binis.codegen.creator.EntityCreator")
                .addImport("net.binis.codegen.spring.query.*")
                .addImport("net.binis.codegen.spring.query.executor." + QUERY_EXECUTOR)
                .addImport("net.binis.codegen.spring.query.executor.QueryOrderer")
                .addImport("net.binis.codegen.spring.query.base.BaseQueryNameImpl")
                .addImport("java.util.Optional")
                .addImport("java.util.function.Function")
        );

        addFindMethod(description, intf);
        addQuerySelectOrderName(description, intf, spec);

        addPresets(description, intf, spec);

        Helpers.addInitializer(description, intf, isNull(description.getMixIn()) ? spec : description.getMixIn().getSpec(), null);
    }

    @Override
    public int order() {
        return 500;
    }

    private void addQuerySelectOrderName(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var entity = description.getProperties().getInterfaceName();
        var select = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_SELECT)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addExtendedType("QueryModifiers<" + entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ", " + entity + ">>")
                .addExtendedType(entity + "." + QUERY_FIELDS + "<" + QUERY_SELECT_OPERATION + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>")
                .addExtendedType(entity + "." + QUERY_FUNCS + "<" + QUERY_SELECT_OPERATION + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>")
                .addExtendedType(QUERY_ORDER_START + "<" + QUERY_OP_FIELDS + "<" + QUERY_ORDER_OPERATION + "<" + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>>")
                .addExtendedType(QUERY_BRACKET + "<" + QUERY_SELECT + "<" + QUERY_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(select);

        var impl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_EXECUTOR + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addExtendedType(QUERY_EXECUTOR)
                .addImplementedType(entity + "." + QUERY_SELECT)
                .addImplementedType(entity + "." + QUERY_FIELDS_START);
        impl.addConstructor(PROTECTED)
                .setBody(new BlockStmt().addStatement("super(" + entity + ".class, () -> new " + entity + QUERY_NAME + QUERY_IMPL + "());"));
        impl.addMethod("order").setType(entity + "." + QUERY_ORDER)
                .addModifier(PUBLIC)
                .setBody(new BlockStmt()
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_ORDER + ") orderStart(new " + entity + QUERY_ORDER + QUERY_IMPL + "(this, " + entity + QUERY_EXECUTOR + QUERY_IMPL + ".this::orderIdentifier))")));
        impl.addMethod("aggregate").setType(QUERY_AGGREGATE_OPERATION)
                .addModifier(PUBLIC)
                .setBody(new BlockStmt()
                        .addStatement(new ReturnStmt("(" + QUERY_AGGREGATE_OPERATION + ") aggregateStart(new " + entity + QUERY_ORDER + QUERY_IMPL + "(this, " + entity + QUERY_EXECUTOR + QUERY_IMPL + ".this::aggregateIdentifier))")));
        spec.addMember(impl);

        Helpers.addInitializer(description, select, impl, null);

        var orderImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_ORDER + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addExtendedType(QUERY_ORDER + "er")
                .addImplementedType(entity + "." + QUERY_ORDER)
                .addImplementedType(entity + "." + QUERY_AGGREGATE);

        orderImpl.addConstructor(PROTECTED)
                .addParameter(entity + QUERY_EXECUTOR + QUERY_IMPL, "executor")
                .addParameter("Function<String, Object>", "func")
                .setBody(new BlockStmt().addStatement("super(executor, func);"));

        impl.addMember(orderImpl);

        var qName = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_NAME)
                .addTypeParameter(QUERY_SELECT_GENERIC)
                .addTypeParameter(QUERY_ORDER_GENERIC)
                .addTypeParameter(QUERY_GENERIC)
                .addTypeParameter(QUERY_FETCH_GENERIC)
                .addExtendedType(entity + "." + QUERY_FIELDS + "<" + QUERY_SELECT_OPERATION + "<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">>")
                .addExtendedType(entity + "." + QUERY_FUNCS + "<" + QUERY_SELECT_OPERATION + "<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">>")
                .addExtendedType(QUERY_FETCH + "<" + QUERY_SELECT_OPERATION + "<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">, " + QUERY_FETCH_GENERIC + ">");
        intf.addMember(qName);

        var qNameImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_NAME + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addExtendedType("BaseQueryNameImpl")
                .addImplementedType(entity + "." + QUERY_NAME)
                .addImplementedType("QueryEmbed");
        spec.addMember(qNameImpl);

        var qFields = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_FIELDS)
                .addExtendedType(QUERY_SCRIPT + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(qFields);

        var mFields = description.getRegisteredClass(Constants.MODIFIER_FIELDS_KEY);
        if (nonNull(mFields)) {
            qFields.addExtendedType(intf.getNameAsString() + "." + mFields.getNameAsString() + "<" + QUERY_GENERIC + ">");
        }

        var qOpFields = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_OP_FIELDS)
                .addExtendedType(QUERY_SCRIPT + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(qOpFields);

        var qFuncs = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_FUNCS)
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(qFuncs);

        var order = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_ORDER)
                .addExtendedType(QUERY_OP_FIELDS + "<" + QUERY_ORDER_OPERATION + "<" + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>")
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addExtendedType(QUERY_SCRIPT + "<" + QUERY_ORDER_OPERATION + "<" + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(order);

        var aggr = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_AGGREGATE)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addExtendedType(QUERY_AGGREGATOR + "<" + QUERY_AGGREGATE_GENERIC + ", " + QUERY_AGGREGATE_OPERATION + "<" + QUERY_OP_FIELDS + "<" + entity + "." + QUERY_AGGREGATE + "<" + entity + ", " + entity + "." + QUERY_SELECT + "<Number>>>>>")
                .addTypeParameter(QUERY_GENERIC)
                .addTypeParameter(QUERY_AGGREGATE_GENERIC);
        intf.addMember(aggr);

        var fields = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_FIELDS_START)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addExtendedType(QUERY_WHERE + "<" + QUERY_SELECT_GENERIC + ">")
                .addExtendedType(QUERY_OP_FIELDS + "<" + QUERY_FIELDS_START + "<" + QUERY_GENERIC + ", " + QUERY_SELECT_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC)
                .addTypeParameter(QUERY_SELECT_GENERIC);
        intf.addMember(fields);

        with(description.getBase(), base ->
                base.getFields().forEach(desc ->
                        declareField(description, entity, desc, select, impl, orderImpl, order, qName, qNameImpl, qFields, qOpFields, qFuncs)));

        description.getFields().forEach(desc ->
                declareField(description, entity, desc, select, impl, orderImpl, order, qName, qNameImpl, qFields, qOpFields, qFuncs));

        if (nonNull(description.getMixIn())) {
            with(description.getMixIn().getBase(), base ->
                    base.getFields().forEach(desc ->
                            declareField(description, entity, desc, select, impl, orderImpl, order, qName, qNameImpl, qFields, qOpFields, qFuncs)));

            description.getMixIn().getFields().forEach(desc ->
                    declareField(description, entity, desc, select, impl, orderImpl, order, qName, qNameImpl, qFields, qOpFields, qFuncs));
        }

        description.registerClass(Constants.QUERY_EXECUTOR_KEY, impl);
        description.registerClass(Constants.QUERY_SELECT_INTF_KEY, select);
        description.registerClass(Constants.QUERY_ORDER_KEY, orderImpl);
        description.registerClass(Constants.QUERY_ORDER_INTF_KEY, order);
        description.registerClass(Constants.QUERY_NAME_KEY, qNameImpl);
        description.registerClass(Constants.QUERY_NAME_INTF_KEY, qName);

        if (Helpers.hasAnnotation(description, Joinable.class)) {
            Helpers.addInitializer(description, description.getRegisteredClass(Constants.QUERY_ORDER_INTF_KEY), (LambdaExpr) description.getParser().parseExpression("() -> " + description.getIntf().getNameAsString() + ".find().aggregate()").getResult().get(), null);
        }
    }

    private void addFindMethod(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf) {
        Helpers.addDefaultCreation(description);
        var entity = intf.getNameAsString();
        intf.addMethod("find", STATIC)
                .setType(QUERY_START + "<" + entity + ", " + entity + "." + QUERY_SELECT + "<" + entity + ">, " + QUERY_AGGREGATE_OPERATION + "<" + QUERY_OP_FIELDS + "<" + entity + "." + QUERY_AGGREGATE + "<Number, " + entity + "." + QUERY_SELECT + "<Number>>>>, " + QUERY_FIELDS_START + "<" + entity + ", " + entity + "." + QUERY_SELECT + "<" + entity + ">>>")
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + QUERY_START + ") EntityCreator.create(" + entity + "." + QUERY_SELECT + ".class)")));
    }

    private void declareField(PrototypeDescription<ClassOrInterfaceDeclaration> description, String entity, PrototypeField desc, ClassOrInterfaceDeclaration select, ClassOrInterfaceDeclaration impl, ClassOrInterfaceDeclaration orderImpl, ClassOrInterfaceDeclaration order, ClassOrInterfaceDeclaration qName, ClassOrInterfaceDeclaration qNameImpl, ClassOrInterfaceDeclaration fields, ClassOrInterfaceDeclaration opFields, ClassOrInterfaceDeclaration funcs) {
        var field = desc.getDeclaration();
        var fName = field.getVariable(0).getNameAsString();
        var name = checkReserved(fName);

        var trans = isTransient(desc);

        if (desc.isCollection()) {

            if (!trans) {
                var subType = CollectionsHandler.getCollectionType(field.getCommonType());

                if (nonNull(desc.getTypePrototypes())) {
                    var returnType = QUERY_JOIN_COLLECTION_FUNCTIONS + "<" + subType + ", " + QUERY_SELECT_OPERATION + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + QUERY_OP_FIELDS + "<" + QUERY_ORDER_OPERATION + "<" + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>, " + QUERY_GENERIC + ">, " + QUERY_JOIN_AGGREGATE_OPERATION + "<" + subType + "." + QUERY_OP_FIELDS + "<" + subType + "." + QUERY_AGGREGATE + "<Number, " + subType + "." + QUERY_SELECT + "<Number>>>, " + subType + "." + QUERY_SELECT + "<Number>>>";

                    var prototype = desc.getTypePrototypes().get(subType);

                    select.addMethod(name)
                            .setType(returnType)
                            .setBody(null);

                    impl.addMethod(name)
                            .setType(QUERY_JOIN_COLLECTION_FUNCTIONS)
                            .addModifier(PUBLIC)
                            .setBody(new BlockStmt()
                                    .addStatement(new ReturnStmt("(" + QUERY_JOIN_COLLECTION_FUNCTIONS + ") joinStart(\"" + fName + "\", " + subType + "." + QUERY_ORDER + ".class)")));

                    if (nonNull(prototype)) {
                        prototype.registerPostProcessAction(() ->
                                Helpers.addInitializer(prototype, prototype.getRegisteredClass(Constants.QUERY_ORDER_INTF_KEY), (LambdaExpr) prototype.getParser().parseExpression("() -> " + subType + ".find().aggregate()").getResult().get(), null));
                    }
                } else {
                    var returnType = QUERY_COLLECTION_FUNCTIONS + "<" + subType + ", " + QUERY_SELECT_OPERATION + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + QUERY_OP_FIELDS + "<" + QUERY_ORDER_OPERATION + "<" + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>, " + QUERY_GENERIC + ">>";
                    select.addMethod(name)
                            .setType(returnType)
                            .setBody(null);

                    impl.addMethod(name)
                            .setType(QUERY_COLLECTION_FUNCTIONS)
                            .addModifier(PUBLIC)
                            .setBody(new BlockStmt()
                                    .addStatement(new ReturnStmt("identifier(\"" + fName + "\")")));
                }
            }
        } else {
            Helpers.importType(field.getCommonType(), fields.findCompilationUnit().get());

            impl.addMethod(name)
                    .setType(QUERY_SELECT_OPERATION)
                    .addModifier(PUBLIC)
                    .addParameter(field.getCommonType(), fName)
                    .setBody(new BlockStmt()
                            .addStatement(new ReturnStmt("identifier(\"" + fName + "\", " + fName + ")")));

            if (!trans) {
                orderImpl.addMethod(name)
                        .setType(QUERY_ORDER_OPERATION)
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt()
                                .addStatement(new ReturnStmt("(" + QUERY_ORDER_OPERATION + ") func.apply(\"" + fName + "\")")));

                opFields.addMethod(name)
                        .setType(QUERY_GENERIC)
                        .setBody(null);
            }

            if (checkQueryName(entity, desc)) {
                select.addMethod(name).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ", " + desc.getPrototype().getInterfaceName() + ">").setBody(null);
                impl.addMethod(name, PUBLIC).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME)
                        .setBody(new BlockStmt()
                                .addStatement("var result = EntityCreator.create(" + desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + ".class, \"" + desc.getPrototype().getImplementorFullName() + "\");")
                                .addStatement("((QueryEmbed) result).setParent(\"" + fName + "\", this);")
                                .addStatement(new ReturnStmt("result")));

                qName.addMethod(name).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ", " + desc.getPrototype().getInterfaceName() + ">").setBody(null);
                qNameImpl.addMethod(name, PUBLIC).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME)
                        .setBody(new BlockStmt()
                                .addStatement("var result = EntityCreator.create(" + desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + ".class, \"" + desc.getPrototype().getImplementorFullName() + "\");")
                                .addStatement("((QueryEmbed) result).setParent(\"" + fName + "\", executor);")
                                .addStatement(new ReturnStmt("result")));

            } else {
                if (!trans) {
                    funcs.addMethod(name).setType(QUERY_FUNCTIONS + "<" + Helpers.handleGenericPrimitiveType(field.getCommonType()) + "," + QUERY_GENERIC + ">").setBody(null);
                    impl.addMethod(name, PUBLIC).setType(QUERY_FUNCTIONS)
                            .setBody(new BlockStmt()
                                    .addStatement(new ReturnStmt("identifier(\"" + fName + "\")")));


                    qNameImpl.addMethod(name)
                            .setType(QUERY_FUNCTIONS)
                            .addModifier(PUBLIC)
                            .setBody(new BlockStmt()
                                    .addStatement(new ReturnStmt("executor.identifier(\"" + fName + "\")")));
                }
            }

            if (!Helpers.methodExists(fields, desc, false)) {
                fields.addMethod(name)
                        .setType(QUERY_GENERIC)
                        .setBody(null)
                        .addParameter(field.getCommonType(), fName);
            }

            qNameImpl.addMethod(name)
                    .setType(QUERY_SELECT_OPERATION)
                    .addModifier(PUBLIC)
                    .addParameter(field.getCommonType(), fName)
                    .setBody(new BlockStmt()
                            .addStatement(new ReturnStmt("executor.identifier(\"" + fName + "\", " + fName + ")")));

            desc.getDescription().getAnnotations().forEach(a -> {
                var cls = Helpers.getExternalClassNameIfExists(desc.getDescription().findCompilationUnit().get(), a.getNameAsString());
                if ("javax.persistence.Id".equals(cls)) {
                    description.getCustomInitializers().add(b ->
                            b.addStatement("CodeFactory.registerId(" + entity + ".class, \"" + fName + "\", " + field.getVariable(0).getType().asString() + ".class);"));
                }
            });
        }
    }

    private String checkReserved(String name) {
        if (reserved.contains(name)) {
            return "_" + name;
        }
        return name;
    }

    private boolean isTransient(PrototypeField field) {
        for (var ann : field.getDescription().getAnnotations()) {
            var name = ann.getNameAsString();
            if ("Transient".equals(name)) {
                name = Helpers.getExternalClassName(field.getDescription().findCompilationUnit().get(), name);
            }
            if ("javax.persistence.Transient".equals(name) || "org.springframework.data.annotation.Transient".equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (nonNull(description.getMixIn())) {
            description.getMixIn().getSpec().addMember(description.getRegisteredClass(Constants.QUERY_EXECUTOR_KEY));
            combineQueryNames(description);
        } else {
            Helpers.addInitializer(description, description.getRegisteredClass(Constants.QUERY_NAME_INTF_KEY), description.getRegisteredClass(Constants.QUERY_NAME_KEY), null);
        }
    }

    private void addPresets(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var entity = description.getProperties().getInterfaceName();
        var returnType = QUERY_SELECT_OPERATION + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">";
        var implReturnType = description.getInterfaceName() + QUERY_EXECUTOR + QUERY_IMPL;
        var select = description.getRegisteredClass(Constants.QUERY_SELECT_INTF_KEY);
        var exec = description.getRegisteredClass(Constants.QUERY_EXECUTOR_KEY);

        description.getDeclaration().getMembers().stream()
                .filter(BodyDeclaration::isMethodDeclaration)
                .map(BodyDeclaration::asMethodDeclaration)
                .filter(MethodDeclaration::isDefault)
                .filter(m -> m.isAnnotationPresent(QueryPreset.class)).forEach(method ->
                        method.getBody().ifPresent(body -> {
                            if (body.getStatements().size() == 1) {
                                var expression = body.getStatement(0).toString();
                                if (expression.startsWith("Preset.declare().")) {
                                    var mtd = select.addMethod(PRESET_PREFIX + method.getNameAsString())
                                            .setType(returnType)
                                            .setBody(null);

                                    var impl = exec.addMethod(PRESET_PREFIX + method.getNameAsString())
                                            .addModifier(PUBLIC)
                                            .setType(implReturnType);
                                    expression = handlePresetParameters(description, expression, mtd, impl);

                                    impl.setBody(description.getParser().parseBlock("{((" + description.getInterfaceName() + "." + QUERY_SELECT + "<Object>) this)" + expression + "return this;}").getResult().get());
                                } else {
                                    log.error("Expression '{}' is not valid Preset expression! Preset expressions must start with: Preset.declare()", body);
                                }
                            } else {
                                log.error("Invalid Preset expression! Preset expression must be sole expression in method's body!");
                            }
                        }));

    }

    private String handlePresetParameters(PrototypeDescription<ClassOrInterfaceDeclaration> description, String expression, MethodDeclaration mtd, MethodDeclaration impl) {
        var params = expression.split("param\\(\\)");

        if (params.length > 1) {
            var parIdx = 0;

            var builder = new StringBuilder();
            for (var j = 0; j < params.length - 1; j++) {
                var i = params[j].lastIndexOf(".field(");
                String name;
                var type = "Object";
                if (i > -1) {
                    i += 7;
                    name = params[j].substring(i, params[j].indexOf('(', i));
                    var field = description.findField(name);
                    if (field.isPresent()) {
                        type = field.get().getType();
                    } else {
                        log.error("Field not found '{}'!", name);
                    }
                } else {
                    name = "param" + parIdx;
                    parIdx++;
                }
                addPresetParam(mtd, name, type);
                addPresetParam(impl, name, type);
                builder.append(params[j]).append(name);
            }

            expression = builder.append(params[params.length - 1]).toString();
        }

        params = expression.split(".field\\(");

        if (params.length > 1) {
            var builder = new StringBuilder();
            for (var j = 1; j < params.length; j++) {
                var i = params[j].indexOf("(");
                var name = params[j].substring(0, i);
                var field = description.findField(name);
                if (field.isPresent()) {
                    if (params[j].charAt(i + 2) == ')') {
                        builder.append('.').append(name).append('(').append(params[j].substring(i + 2));
                    } else if (params[j].charAt(i + 2) == ',') {
                        builder.append('.').append(name).append('(').append(params[j].substring(i + 4));
                    } else {
                        log.error("Invalid preset format!");
                    }
                } else {
                    log.error("Field not found '{}'!", name);
                }
            }

            expression = builder.toString();
        }

        return expression;
    }

    private void addPresetParam(MethodDeclaration mtd, String name, String type) {
        if (mtd.getParameterByName(name).isEmpty()) {
            mtd.addParameter(type, name);
        }
    }

    private void combineQueryNames(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var mix = description.getRegisteredClass(Constants.QUERY_NAME_KEY);
        var base = description.getMixIn().getRegisteredClass(Constants.QUERY_NAME_KEY);

        base.addImplementedType(mix.getImplementedTypes(0));
        mix.getMembers().forEach(m -> {
            if (!Helpers.methodExists(base, m.asMethodDeclaration(), true)) {
                base.addMember(m);
            }
        });

        description.getRegisteredClass(Constants.QUERY_EXECUTOR_KEY).findAll(ObjectCreationExpr.class, n -> n.getType().getNameAsString().equals(mix.getNameAsString())).forEach(n ->
                n.setType(base.getNameAsString()));

        Helpers.addInitializer(description, description.getRegisteredClass(Constants.QUERY_NAME_INTF_KEY), description.getMixIn().getRegisteredClass(Constants.QUERY_NAME_KEY), null);
    }

    private boolean checkQueryName(String entity, PrototypeField desc) {
        return nonNull(desc.getPrototype()) && (desc.getPrototype().getProperties().getEnrichers().stream().anyMatch(e -> QueryEnricherHandler.class.equals(e.getClass())) ||
                nonNull(desc.getPrototype().getBase()) &&
                        desc.getPrototype().getBase().getProperties().getInheritedEnrichers().stream().anyMatch(e -> QueryEnricherHandler.class.equals(e.getClass())));
    }

}
